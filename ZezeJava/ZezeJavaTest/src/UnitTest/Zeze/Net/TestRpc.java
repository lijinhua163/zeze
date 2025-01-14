package UnitTest.Zeze.Net;

import Zeze.Net.AsyncSocket;
import Zeze.Net.Protocol;
import Zeze.Net.Rpc;
import Zeze.Net.Service;
import Zeze.Transaction.Procedure;
import Zeze.Util.Factory;
import junit.framework.TestCase;
import org.junit.Assert;

public class TestRpc extends TestCase {
	final Zeze.Util.TaskCompletionSource<AsyncSocket> connected = new Zeze.Util.TaskCompletionSource<>();

	public final void testRpcSimple() throws Throwable {
		Service server = new Service("TestRpc.Server");
		Zeze.Util.Task.tryInitThreadPool(null, null, null);
		FirstRpc first = new FirstRpc();
		Factory<Protocol<?>> f = FirstRpc::new;
		System.out.println(first.getTypeId());
		server.AddFactoryHandle(first.getTypeId(), new Service.ProtocolFactoryHandle<>(f, this::ProcessFirstRpcRequest));

		server.NewServerSocket("127.0.0.1", 5000, null);
		Client client = new Client(this);
		client.AddFactoryHandle(first.getTypeId(), new Service.ProtocolFactoryHandle<>(FirstRpc::new));

		AsyncSocket clientSocket = client.NewClientSocket("127.0.0.1", 5000, null, null);
		connected.get();

		first = new FirstRpc();
		first.Argument.setInt1(1234);
		//Console.WriteLine("SendFirstRpcRequest");
		first.SendForWait(clientSocket).await();
		//Console.WriteLine("FirstRpc Wait End");
		Assert.assertEquals(first.Argument.getInt1(), first.Result.getInt1());
	}

	public final long ProcessFirstRpcRequest(Protocol<?> p) {
		FirstRpc rpc = (FirstRpc)p;
		rpc.Result.Assign(rpc.Argument);
		rpc.SendResult();
		System.out.println("ProcessFirstRpcRequest result.Int1=" + rpc.Result.getInt1());
		return Procedure.Success;
	}

	public static class FirstRpc extends Rpc<demo.Module1.Value, demo.Module1.Value> {
		public FirstRpc() {
			Argument = new demo.Module1.Value();
			Result = new demo.Module1.Value();
		}

		@Override
		public int getModuleId() {
			return 1;
		}

		@Override
		public int getProtocolId() {
			return -1;
		}
	}

	public static class Client extends Service {
		private final TestRpc test;

		public Client(TestRpc test) {
			super("TestRpc.Client");
			this.test = test;
		}

		@Override
		public void OnSocketConnected(AsyncSocket so) throws Throwable {
			super.OnSocketConnected(so);
			test.connected.SetResult(so);
		}
	}
}
