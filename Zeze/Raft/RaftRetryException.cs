﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Zeze.Raft
{
    public class RaftRetryException : Exception
    {
        public RaftRetryException()
        {

        }

        public RaftRetryException(string msg)
            : base(msg)
        {

        }
    }
}
