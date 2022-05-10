@echo off
setlocal
pushd %~dp0

start "ServiceManagerServer"     java -Dlogname=ServiceManagerServer     -cp .;..\..\ZezeJava\lib\*;..\..\ZezeJava\build\libs\ZezeJava-0.9.22-SNAPSHOT.jar Zeze.Services.ServiceManagerServer
start "GlobalCacheManagerServer" java -Dlogname=GlobalCacheManagerServer -cp .;..\..\ZezeJava\lib\*;..\..\ZezeJava\build\libs\ZezeJava-0.9.22-SNAPSHOT.jar Zeze.Services.GlobalCacheManagerServer -raft RunAllNodes