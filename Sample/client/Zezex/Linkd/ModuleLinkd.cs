﻿
using System.Threading.Tasks;
using Zeze.Net;

namespace Zezex.Linkd
{
    public sealed partial class ModuleLinkd : AbstractModule
    {
        public void Start(Client.App app)
        {
        }

        public void Stop(Client.App app)
        {
        }

        protected override async Task<long> ProcessKeepAlive(Protocol _p)
        {
            var p = _p as KeepAlive;
            return Zeze.Transaction.Procedure.NotImplement;
        }
    }
}
