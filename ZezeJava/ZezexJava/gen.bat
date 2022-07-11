@echo off
setlocal
pushd %~dp0

 ..\..\publish\Gen.exe solution.xml
..\..\publish\Gen.exe solution.linkd.xml

..\..\Gen\bin\Debug\net6.0\Gen.exe solution.xml
..\..\Gen\bin\Debug\net6.0\Gen.exe solution.linkd.xml

pause

