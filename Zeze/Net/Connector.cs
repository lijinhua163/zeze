﻿using System;
using System.Threading.Tasks;
using System.Xml;

namespace Zeze.Net
{
    /// <summary>
    /// 连接器：建立并保持一个连接，可以设置自动重连及相关参数。
    /// 可以继承并重载相关事件函数。重载实现里面需要调用 base.OnXXX。
    /// 继承是为了给链接扩充状态，比如：应用的连接需要login，可以维护额外的状态。
    /// 继承类启用方式：
    /// 1. 在配置中通过 class="FullClassName" 的。
    /// 2. 动态创建并加入Service
    /// </summary>
    public class Connector
    {
        public Service Service { get; private set; }

        public string HostNameOrAddress { get; }
        public int Port { get; } = 0;
        private volatile bool autoReconnect = true;
        public bool AutoReconnect
        {
            get
            {
                return autoReconnect;
            }
            set
            {
                autoReconnect = value;
                if (autoReconnect)
                {
                    TryReconnect();
                }
                else
                {
                    lock (this)
                    {
                        if (ReconnectTask != null)
                        {
                            ReconnectTask.Cancel();
                            ReconnectTask = null;
                        }
                    }
                }
            }
        }
        public bool IsConnected { get; private set; } = false;
        public bool IsHandshakeDone => TryGetReadySocket() != null;
        private volatile TaskCompletionSource<AsyncSocket> FutureSocket = new(TaskCreationOptions.RunContinuationsAsynchronously);
        public string Name => $"{HostNameOrAddress}:{Port}";

        public AsyncSocket Socket { get; private set; }
        public object UserState;

        private int _MaxReconnectDelay = 8000;
        public int MaxReconnectDelay
        {
            get
            {
                return _MaxReconnectDelay;
            }

            set
            {
                _MaxReconnectDelay = value;
                if (_MaxReconnectDelay < 1000)
                    _MaxReconnectDelay = 1000;
            }
        }
        private int ReConnectDelay;
        public Util.SchedulerTask ReconnectTask { get; private set; }

        public Connector(string host, int port = 0, bool autoReconnect = true)
        {
            HostNameOrAddress = host;
            Port = port;
            AutoReconnect = autoReconnect;
        }

        public static Connector Create(XmlElement e)
        {
            var className = e.GetAttribute("Class");
            return string.IsNullOrEmpty(className)
                    ? new Connector(e)
                    : (Connector)Activator.CreateInstance(Type.GetType(className), e);
        }

        public Connector(XmlElement self)
        {
            string attr = self.GetAttribute("Port");
            if (attr.Length > 0)
                Port = int.Parse(attr);
            HostNameOrAddress = self.GetAttribute("HostNameOrAddress");
            attr = self.GetAttribute("IsAutoReconnect");
            if (attr.Length > 0)
                AutoReconnect = bool.Parse(attr);
            attr = self.GetAttribute("MaxReconnectDelay");
            if (attr.Length > 0)
                MaxReconnectDelay = int.Parse(attr) * 1000;
        }

        internal void SetService(Service service)
        {
            lock (this)
            {
                if (Service != null)
                    throw new Exception($"Connector of '{Name}' Service != null");
                Service = service;
            }
        }

        public AsyncSocket GetReadySocket()
        {
            var task = GetReadySocketAsync();
            task.Wait();
            return task.Result;
        }

        public async Task<AsyncSocket> GetReadySocketAsync()
        {
            var volatileTmp = FutureSocket;
            await volatileTmp.Task;
            return volatileTmp.Task.Result;
        }

        public virtual AsyncSocket TryGetReadySocket()
        {
            var volatileTmp = FutureSocket;
            try
            {
                if (volatileTmp.Task.IsCompletedSuccessfully)
                    return volatileTmp.Task.Result;
            }
            catch (Exception)
            {
                // skip
            }
            return null;
        }

        public virtual void OnSocketClose(AsyncSocket closed, Exception e)
        {
            lock (this)
            {
                if (Socket == closed)
                {
                    Stop(e);
                    TryReconnect();
                }
            }
        }

        public virtual void OnSocketConnected(AsyncSocket so)
        {
            lock (this)
            {
                ReConnectDelay = 0;
                IsConnected = true;
            }
        }

        public virtual void OnSocketHandshakeDone(AsyncSocket so)
        {
            lock (this)
            {
                if (Socket == so && FutureSocket.TrySetResult(so))
                    return;
            }
            // not owner. close now.
            so.Close(new Exception("not owner?"));
        }

        public virtual void TryReconnect()
        {
            lock (this)
            {
                if (false == AutoReconnect
                    || null != Socket
                    || null != ReconnectTask)
                {
                    return;
                }

                if (ReConnectDelay <= 0)
                {
                    ReConnectDelay = 1000;
                }
                else
                {
                    ReConnectDelay *= 2;
                    if (ReConnectDelay > MaxReconnectDelay)
                        ReConnectDelay = MaxReconnectDelay;
                }
                ReconnectTask = Util.Scheduler.Schedule((ThisTask) => Start(), ReConnectDelay); ;
            }
        }

        public virtual void Start()
        {
            lock (this)
            {
                ReconnectTask?.Cancel();
                ReconnectTask = null;

                if (null != Socket)
                    return;
                Socket = Service.NewClientSocket(HostNameOrAddress, Port, UserState, this);
            }
        }

        public virtual void Stop(Exception e = null)
        {
            AsyncSocket tmp = null;
            lock (this)
            {
                ReconnectTask?.Cancel();
                ReconnectTask = null;

                if (null == Socket)
                    return; // not start or has stopped.

                IsConnected = false;
                FutureSocket.TrySetException(e ?? new Exception("Connector Stopped: " + Name));
                FutureSocket = new TaskCompletionSource<AsyncSocket>(TaskCreationOptions.RunContinuationsAsynchronously);
                tmp = Socket;
                Socket = null; // 阻止递归。
            }
            tmp?.Dispose();
        }

        public override string ToString()
        {
            return $"{Name}-{Socket}-{Socket?.Socket}";
        }
    }
}
