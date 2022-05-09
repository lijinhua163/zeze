﻿using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Transactions;
using Zeze.Transaction;
using Zeze.Services.ServiceManager;
using System.Collections.Concurrent;

namespace Zeze
{
    public sealed class Application
    {
        internal static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

        public Dictionary<string, Database> Databases { get; private set; } = new Dictionary<string, Database>();
        public Config Config { get; private set; }
        public bool IsStart { get; private set; }
        public Agent ServiceManagerAgent { get; private set; }
        public Zeze.Arch.RedirectBase Redirect { get; set; }
        internal IGlobalAgent GlobalAgent { get; private set; }

        public Component.AutoKey.Module AutoKeys { get; private set; }
        public Collections.Queue.Module Queues { get; private set; }

        internal Locks Locks { get; private set; }

        public Component.AutoKey GetAutoKey(string name)
        {
            return AutoKeys.GetOrAdd(name);
        }

        private Checkpoint _checkpoint;
        public Checkpoint Checkpoint
        {
            get
            {
                return _checkpoint;
            }
            /*
            set
            {
                lock (this)
                {
                    if (null == value)
                        throw new ArgumentNullException();
                    if (IsStart)
                        throw new Exception("Checkpoint only can setup before start.");
                    _checkpoint = value;
                }
            }
            */
        }

        internal class LastFlushWhenReduce
        {
            public TableKey Key { get; set; }
            public Util.AtomicLong LastGlobalSerialId = new();
            public long Ticks { get; set; }
            public bool Removed { get; set; } = false;
            public Nito.AsyncEx.AsyncMonitor Monitor = new();
            public LastFlushWhenReduce(TableKey tkey)
            {
                Key = tkey;
            }
        }

        private ConcurrentDictionary<TableKey, LastFlushWhenReduce> FlushWhenReduce { get; }
            = new ConcurrentDictionary<TableKey, LastFlushWhenReduce>();
        private ConcurrentDictionary<long, Util.IdentityHashSet<LastFlushWhenReduce>> FlushWhenReduceActives { get; }
            = new ConcurrentDictionary<long, Util.IdentityHashSet<LastFlushWhenReduce>>();
        private Util.SchedulerTask FlushWhenReduceTimerTask;

        internal async Task SetLastGlobalSerialId(TableKey tkey, long globalSerialId)
        {
            while (true)
            {
                var last = FlushWhenReduce.GetOrAdd(tkey, (k) => new LastFlushWhenReduce(k));
                using (await last.Monitor.EnterAsync())
                {
                    if (last.Removed)
                        continue;

                    last.LastGlobalSerialId.GetAndSet(globalSerialId);
                    last.Ticks = DateTime.Now.Ticks;
                    last.Monitor.PulseAll();
                    var minutes = last.Ticks / TimeSpan.TicksPerMinute;
                    FlushWhenReduceActives.GetOrAdd(minutes, (key) => new Util.IdentityHashSet<LastFlushWhenReduce>()).Add(last);
                    return;
                }
            }
        }

        internal async Task<bool> TryWaitFlushWhenReduce(TableKey tkey, long hope)
        {
            while (true)
            {
                var last = FlushWhenReduce.GetOrAdd(tkey, (k) => new LastFlushWhenReduce(k));
                using (await last.Monitor.EnterAsync())
                {
                    while (false == last.Removed && last.LastGlobalSerialId.Get() < hope)
                    {
                        // 超时的时候，马上返回。
                        // 这个机制的是为了防止忙等。
                        // 所以不需要严格等待成功。
                        // TODO 加上超时支持。
                        await last.Monitor.WaitAsync();
                    }
                    if (last.Removed)
                        continue;
                    return true;
                }
            }
        }

        public const long FlushWhenReduceIdleMinuts = 30;

        private void FlushWhenReduceTimer(Util.SchedulerTask ThisTask)
        {
            var minuts = DateTime.Now.Ticks / TimeSpan.TicksPerMinute;

            foreach (var active in FlushWhenReduceActives)
            {
                if (active.Key - minuts > FlushWhenReduceIdleMinuts)
                {
                    foreach (var last in active.Value)
                    {
                        lock (last)
                        {
                            if (last.Removed)
                                continue;

                            if (last.Ticks / TimeSpan.TicksPerMinute > FlushWhenReduceIdleMinuts)
                            {
                                if (FlushWhenReduce.TryRemove(last.Key, out _))
                                {
                                    last.Removed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        public Schemas Schemas { get; set; } // no thread protected
        public string SolutionName { get; }

        public Application(string solutionName, Config config = null)
        {
            SolutionName = solutionName;

            Config = config;
            if (null == Config)
                Config = Config.Load();

            //int workerMax, ioMax;
            ThreadPool.GetMinThreads(out var workerMin, out var ioMin);
            //ThreadPool.GetMaxThreads(out workerMax, out ioMax);
            //Console.WriteLine($"worker ({workerMin}, {workerMax}) io({ioMin}, {ioMax})");
            if (Config.WorkerThreads > 0)
            {
                workerMin = Config.WorkerThreads;
                //workerMax = Config.WorkerThreads;
            }
            if (Config.CompletionPortThreads > 0)
            {
                ioMin = Config.CompletionPortThreads;
                //ioMax = Config.CompletionPortThreads;
            }
            ThreadPool.SetMinThreads(workerMin, ioMin);
            //ThreadPool.SetMaxThreads(workerMax, ioMax);

            Config.CreateDatabase(this, Databases);
            _checkpoint = new Checkpoint(Config.CheckpointMode, Databases.Values);
            ServiceManagerAgent = new Agent(this);
        }

        private readonly ConcurrentDictionary<string, Table> Tables = new();

        public void AddTable(string dbName, Table table)
        {
            if (Databases.TryGetValue(dbName, out var db))
            {
                if (false == Tables.TryAdd(table.Name, table))
                    throw new Exception($"duplicate table name={table.Name}");
                db.AddTable(table);
                return;
            }
            throw new Exception($"database not found dbName={dbName}");
        }

        public void RemoveTable(string dbName, Table table)
        {
            Tables.TryRemove(table.Name, out _);
            if (Databases.TryGetValue(dbName, out var db))
            {
                db.RemoveTable(table);
                return;
            }
            throw new Exception($"database not found dbName={dbName}");
        }

        public Table GetTable(string name)
        {
            if (Tables.TryGetValue(name, out var table))
                return table;
            return null;
        }

        public Database GetDatabase(string name)
        {
            if (Databases.TryGetValue(name, out var exist))
            {
                return exist;
            }
            throw new Exception($"database not exist name={name}");
        }

        public Procedure NewProcedure(Func<Task<long>> action, string actionName,
            TransactionLevel level = TransactionLevel.Serializable,
            object userState = null)
        {
            if (IsStart)
            {
                return new Procedure(this, action, actionName, level, userState);
            }
            throw new Exception("App Not Start");
        }

        public async Task StartAsync()
        {
            lock (this)
            {
                if (IsStart)
                    return;
                IsStart = true;
            }

            Config?.ClearInUseAndIAmSureAppStopped(this, Databases); // XXX REMOVE ME!
            foreach (var db in Databases.Values)
            {
                db.DirectOperates.SetInUse(Config.ServerId, Config.GlobalCacheManagerHostNameOrAddress);
            }

            Locks = new Locks();

            var serviceConf = Config.GetServiceConf(Agent.DefaultServiceName);
            if (null != serviceConf) {
                ServiceManagerAgent.Client.Start();
                await ServiceManagerAgent.WaitConnectorReadyAsync();
            }
            AutoKeys = new(this);
            Queues = new(this);

            Database defaultDb = GetDatabase("");
            foreach (var db in Databases.Values)
            {
                db.Open(this);
            }

            var hosts = Config.GlobalCacheManagerHostNameOrAddress.Split(';');
            if (hosts.Length > 0)
            {
                var israft = hosts[0].EndsWith(".xml");
                if (false == israft)
                {
                    var impl = new GlobalAgent(this);
                    impl.Start(hosts, Config.GlobalCacheManagerPort);
                    GlobalAgent = impl;
                }
                else
                {
                    var impl = new Zeze.Services.GlobalCacheManagerWithRaftAgent(this);
                    await impl.Start(hosts);
                    GlobalAgent = impl;
                }
            }

            Checkpoint.Start(Config.CheckpointPeriod); // 定时模式可以和其他模式混用。

            /////////////////////////////////////////////////////
            /// Schemas Check
            Schemas.Compile();
            var keyOfSchemas = Zeze.Serialize.ByteBuffer.Allocate();
            keyOfSchemas.WriteString("zeze.Schemas." + Config.ServerId);
            while (true)
            {
                var (data, version) = defaultDb.DirectOperates.GetDataWithVersion(keyOfSchemas);
                if (null != data)
                {
                    var SchemasPrevious = new Schemas();
                    try
                    {
                        SchemasPrevious.Decode(data);
                        SchemasPrevious.Compile();
                    }
                    catch (Exception ex)
                    {
                        logger.Error(ex);
                        SchemasPrevious = null;
                        logger.Error(ex, "Schemas Implement Changed?");
                    }
                    Schemas.CheckCompatible(SchemasPrevious, this);
                }
                var newdata = Serialize.ByteBuffer.Allocate();
                Schemas.Encode(newdata);
                if (defaultDb.DirectOperates.SaveDataWithSameVersion(keyOfSchemas, newdata, ref version))
                    break;
            }
            FlushWhenReduceTimerTask = Util.Scheduler.Schedule(FlushWhenReduceTimer, 60 * 1000, 60 * 1000);
        }

        public void Stop()
        {
            lock (this)
            {
                var domain = AppDomain.CurrentDomain;
                domain.UnhandledException -= UnhandledExceptionEventHandler;
                domain.ProcessExit -= ProcessExit;

                GlobalAgent?.Dispose(); // 关闭时需要生成新的SessionId，这个现在使用AutoKey，需要事务支持。

                if (false == IsStart)
                    return;

                FlushWhenReduceTimerTask?.Cancel();
                FlushWhenReduceTimerTask = null;

                Config?.ClearInUseAndIAmSureAppStopped(this, Databases);
                IsStart = false;

                _checkpoint?.StopAndJoin();
                _checkpoint = null;
                foreach (var db in Databases.Values)
                {
                    db.Close();
                }
                Databases.Clear();
                ServiceManagerAgent.Stop();
                Locks = null;
                Config = null;
            }
        }
 
        public async Task CheckpointNow()
        {
            await _checkpoint.CheckpointNow();
        }

        public Application()
        {
            var domain = AppDomain.CurrentDomain;
            domain.UnhandledException += UnhandledExceptionEventHandler;
            domain.ProcessExit += ProcessExit;
            // domain.DomainUnload += DomainUnload;
        }

        private void ProcessExit(object sender, EventArgs e)
        {
            Stop();
        }

        private void UnhandledExceptionEventHandler(object sender, UnhandledExceptionEventArgs args)
        {
            Exception e = (Exception)args.ExceptionObject;
            logger.Error(e, "UnhandledExceptionEventArgs");
        }

        public Zeze.Util.TaskOneByOneByKey TaskOneByOneByKey { get; } = new Zeze.Util.TaskOneByOneByKey();
    }
}
