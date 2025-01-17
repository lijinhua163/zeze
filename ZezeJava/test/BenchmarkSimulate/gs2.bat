@echo off
setlocal
pushd %~dp0

set SERVER_ID=2
set SERVER_NAME=gs%SERVER_ID%

title %SERVER_NAME%

java -Dlogname=%SERVER_NAME% -Xlog:gc=info,gc+heap=info:%SERVER_NAME%_gc.log:time -cp .;lib\* Benchmark.Simulate ^
serverId=%SERVER_ID% ^
taskThreadCount=50 ^
schdThreadCount=10 ^
totalKeyRange=2000000 ^
localKeyRange=100000 ^
localKeyWindow=10000 ^
procsEveryWindowMove=100 ^
concurrentProcs=300 ^
localPercent=0 ^
serviceManagerIp=127.0.0.1 ^
serviceManagerPort=5001 ^
globalServerIp=127.0.0.1 ^
globalServerPort=5555 ^
read1Weight=40 ^
readWrite1Weight=30 ^
read2Write1Weight=20 ^
readWrite2Weight=10 ^
