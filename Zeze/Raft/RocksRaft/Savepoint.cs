﻿using System;
using System.Collections.Generic;
using System.Text;

namespace Zeze.Raft.RocksRaft
{
    sealed class Savepoint
    {
        internal Dictionary<long, Log> Logs { get; } = new Dictionary<long, Log>(); // 保存所有的log
        //private readonly Dictionary<long, Log> Newly = new Dictionary<long, Log>(); // 当前Savepoint新加的，用来实现Rollback，先不实现。

        public void PutLog(Log log)
        {
            Logs[log.LogKey] = log;
            //newly[log.LogKey] = log;
        }

        public Log GetLog(long logKey)
        {
            return Logs.TryGetValue(logKey, out var log) ? log : null;
        }

        public Savepoint Duplicate()
        {
            Savepoint sp = new Savepoint();
            foreach (var e in Logs)
            {
                sp.Logs[e.Key] = e.Value.Duplicate();
            }
            return sp;
        }

        public void Merge(Savepoint other)
        {
            foreach (var e in other.Logs)
            {
                e.Value.MergeTo(this);
            }
        }

        public void Rollback()
        {
            // 现在没有实现 Log.Rollback。不需要再做什么，保留接口，以后实现Rollback时再处理。
            /*
            foreach (var e in newly)
            {
                e.Value.Rollback();
            }
            */
        }
    }
}