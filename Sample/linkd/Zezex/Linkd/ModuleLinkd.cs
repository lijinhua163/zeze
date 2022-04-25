﻿
using System.Threading.Tasks;
using Zeze.Arch;
using Zeze.Net;

namespace Zezex.Linkd
{
    public sealed partial class ModuleLinkd : AbstractModule
    {
        public void Start(Zezex.App app)
        {
        }

        public void Stop(Zezex.App app)
        {
        }

        protected override async Task<long> ProcessAuthRequest(Protocol p)
        {
            var rpc = p as Auth;
            /*
            BAccount account = _taccount.Get(protocol.Argument.Account);
            if (null == account || false == account.Token.Equals(protocol.Argument.Token))
            {
                result.Send(protocol.Sender);
                return Zeze.Transaction.Procedure.LogicError;
            }

            Game.App.Instance.LinkdService.GetSocket(account.SocketSessionId)?.Dispose(); // kick, 最好发个协议再踢。如果允许多个连接，去掉这行。
            account.SocketSessionId = protocol.Sender.SessionId;
            */
            var linkSession = rpc.Sender.UserState as LinkdUserSession;
            linkSession.Account = rpc.Argument.Account;
            rpc.SendResultCode(Auth.Success);

            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessKeepAlive(Protocol p)
        {
            var protocol = p as KeepAlive;
            var linkSession = protocol.Sender.UserState as LinkdUserSession;
            if (null == linkSession)
            {
                // handshake 完成之前不可能回收得到 keepalive，先这样处理吧。
                protocol.Sender.Close(null);
                return Zeze.Transaction.Procedure.LogicError;
            }
            linkSession.KeepAlive(App.Instance.LinkdService);
            protocol.Sender.Send(protocol); // send back;
            return Zeze.Transaction.Procedure.Success;
        }
    }
}