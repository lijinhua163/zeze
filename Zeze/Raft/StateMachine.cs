﻿using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zeze.Net;

namespace Zeze.Raft
{
    public abstract class StateMachine
    {
        public Raft Raft { get; internal set; }

        public StateMachine()
        {
            AddFactory(new HeartbeatLog().TypeId, () => new HeartbeatLog());
        }

        private ConcurrentDictionary<int, Func<Log>> LogFactorys
            = new ConcurrentDictionary<int, Func<Log>>();

        
        // 建议在继承类的构造里面注册LogFactory。
        protected void AddFactory(int logTypeId, Func<Log> factory)
        {
            if (!LogFactorys.TryAdd(logTypeId, factory))
                throw new Exception("Duplicate Log Id");
        }

        public virtual Log LogFactory(int logTypeId)
        {
            if (LogFactorys.TryGetValue(logTypeId, out var factory))
            {
                return factory();
            }
            Environment.Exit(7777);
            return null;
        }

        /// <summary>
        /// 把 StateMachine 里面的数据系列化到 path 指定的文件中。
        /// 需要自己访问的并发特性。返回快照建立时的Raft.LogSequence.Index。
        /// 原子性建议伪码如下：
        /// lock (Raft) // 这会阻止对 StateMachine 的写请求。
        /// {
        ///     LastIncludedIndex = Raft.LogSequence.Index;
        ///     LastIncludedTerm = Raft.LogSequence.Term;
        ///     MyData.SerializeToFile(path);
        /// }
        /// 上面的问题是，数据很大时，SerializeToFile时间比较长。
        /// 这时候需要自己优化并发。如下：
        /// lock (Raft)
        /// {
        ///     LastIncludedIndex = Raft.LogSequence.Index;
        ///     LastIncludedTerm = Raft.LogSequence.Term;
        ///     // 设置状态，如果限制只允许一个snapshot进行。怎么处理比较好？
        ///     MyData.StartSerializeToFile();
        /// }
        /// MyData.ConcurrentSerializeToFile(path);
        /// lock (Raft)
        /// {
        ///     // 清理一些状态。
        ///     MyData.EndSerializeToFile();
        /// }
        /// 这样在保存到文件的过程中，服务可以继续进行。
        /// </summary>
        /// <param name="path"></param>
        public abstract void Snapshot(string path, out long LastIncludedIndex, out long LastIncludedTerm);

        /// <summary>
        /// 从上一个快照中重建 StateMachine。
        /// Raft 处理 InstallSnapshot 到达最后一个数据时，调用这个方法。
        /// 然后 Raft 会从 LastIncludedIndex 后面开始复制日志。进入正常的模式。
        /// </summary>
        /// <param name="path"></param>
        public abstract void LoadFromSnapshot(string path);
    }
}
