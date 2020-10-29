
#pragma once

#include <string>
#include "ByteBuffer.h"
#include "ToLua.h"
#include <functional>
#include <unordered_map>
#include "dh.h"
#include "codec.h"

namespace Zeze
{
namespace Net
{
	bool Startup();
	void Cleanup();

	class Protocol;
	class BufferedCodec;

	class Socket
	{
		std::mutex mutex;
		int socket = 0;
		int selectorFlags = 0; // used in Selector

		void SetOutputSecurity(bool c2sneedcompress, const int8_t* key, int keylen);
		void SetInputSecurity(bool s2cneedcompress, const int8_t* key, int keylen);

		friend class Service;
		friend class Selector;

		std::shared_ptr<BufferedCodec> OutputBuffer;
		std::shared_ptr<BufferedCodec> InputBuffer;

		std::shared_ptr<limax::Codec> OutputCodec;
		std::shared_ptr<limax::Codec> InputCodec;

		void OnSend();
		void OnRecv();

	public:
		bool IsHandshakeDone;
		long long SessionId;
		Service* service;
		std::shared_ptr<Socket> This;
		std::string LastAddress;

		static long long NextSessionId()
		{
			static long long seed = 0;
			static std::mutex mutex;

			std::lock_guard<std::mutex> g(mutex);
			return ++seed;
		}
		Socket(Service* svr);
		~Socket();
		void Close(std::exception* e);
		void Send(const char* data, int length) { Send(data, 0, length); }
		void Send(const char* data, int offset, int length);
		// �ɹ�ʱ�����سɹ����ӵĵ�ַ������ empty string ��ʾʧ�ܡ�
		bool Connect(const std::string& host, int port, const std::string& lastSuccessAddress, int timeoutSecondsPerConnect);
	};

	class Service
	{
		std::string name;
		std::shared_ptr<Socket> socket;
		std::string lastSuccessAddress;
		ToLua ToLua;
		Helper Helper;
	public:
		Service(const std::string& _name);
		virtual ~Service();
		const std::string & Name() { return name; }
		std::shared_ptr<Socket> GetSocket() { return socket; }
		std::shared_ptr<Socket> GetSocket(long long sessionId)
		{
			if (socket.get() != NULL && socket->SessionId == sessionId)
				return socket;
			return std::shared_ptr<Socket>(NULL);
		}
		void InitializeLua(lua_State* L);
		void Connect(const std::string& host, int port, int timeoutSecondsPerConnect = 5);
		virtual void OnSocketClose(const std::shared_ptr<Socket> & sender, const std::exception* e);
		virtual void OnHandshakeDone(const std::shared_ptr<Socket>& sender);
		virtual void OnSocketConnectError(const std::shared_ptr<Socket>& sender, const std::exception* e);
		virtual void OnSocketConnected(const std::shared_ptr<Socket>& sender);
		virtual void DispatchUnknownProtocol(const std::shared_ptr<Socket>& sender, int typeId, Zeze::Serialize::ByteBuffer& data);
		virtual void DispatchProtocol(Protocol* p);
		virtual void OnSocketProcessInputBuffer(const std::shared_ptr<Socket>& sender, Zeze::Serialize::ByteBuffer& input);

		friend class ToLua;
		friend class Helper;
		friend class Protocol;

		class ProtocolFactoryHandle
		{
		public:
			typedef std::function<Protocol* ()> FuncFactory;
			typedef std::function<int(Protocol*)> FuncHandle;
			FuncFactory Factory;
			FuncHandle Handle;

			ProtocolFactoryHandle(const FuncFactory& factory, const FuncHandle& handle)
				: Factory(factory), Handle(handle)
			{
			}
			ProtocolFactoryHandle()
			{
			}
		};
		void AddProtocolFactory(int typeId, const ProtocolFactoryHandle & func)
		{
			std::pair<ProtocolFactoryMap::iterator, bool> r = ProtocolFactory.insert(std::pair<int, ProtocolFactoryHandle>(typeId, func));
			if (false == r.second)
				throw std::exception("duplicate protocol TypeId");
		}

		void SetDhGroup(char dhGroup)
		{
			this->dhGroup = dhGroup;
		}

	private:
		typedef std::unordered_map<int, ProtocolFactoryHandle> ProtocolFactoryMap;
		ProtocolFactoryMap ProtocolFactory;
		Protocol* CreateProtocol(int typeId, Zeze::Serialize::ByteBuffer& os);
		std::shared_ptr<limax::DHContext> dhContext;
		char dhGroup = 1;
		int ProcessSHandshake(Protocol* p);
	};

} // namespace Net
} // namespace Zeze