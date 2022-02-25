﻿
using System;
using Zeze.Net;

namespace demo.Module1
{
    public sealed partial class ModuleModule1 : AbstractModule
    {
        public void Start(demo.App app)
        {
        }

        public void Stop(demo.App app)
        {
        }

        protected override long ProcessProtocol1(Protocol p)
        {
            var protocol = p as Protocol1;
            protocol.Send(protocol.Sender);
            Console.WriteLine(protocol.Argument);
            new Rpc2().Send(protocol.Sender, (Protocol) => 0);
            return Zeze.Transaction.Procedure.Success;
        }

        protected override long ProcessProtocol3(Protocol p)
        {
            return Zeze.Transaction.Procedure.NotImplement;
        }

        protected override long ProcessRpc1Request(Protocol p)
        {
            var rpc = p as Rpc1;
            rpc.SendResult();
            return Zeze.Transaction.Procedure.Success;
        }

        protected override long ProcessProtocolNoProcedure(Protocol p)
        {
            throw new NotImplementedException();
        }

        public Table1 Table1 => _Table1;
        public Table2 Table2 => _Table2;
        public TableImportant TableImportant => _TableImportant;
        public tflush Tflush => _tflush;
    }
}
