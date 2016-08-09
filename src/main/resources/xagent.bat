@echo off
if "%OS%" == "Windows_NT" setlocal

if "%BASEDIR%" == "" set BASEDIR=%~sdp0..

set _RUNJAVA="%JAVA_HOME%\bin\java"

for %%i in ("%BASEDIR%\lib\*.jar") do call %BASEDIR%\bin\setenv.bat %%i 

rem for %%i in ("%BASEDIR%\lib\*.properties") do call %BASEDIR%\bin\setenv.bat %%i 

call  %BASEDIR%\bin\setenv.bat "%JAVA_HOME%\lib\tools.jar"

%_RUNJAVA% -classpath %CLASSPATH% com.ebay.platform.xagent.gui.XAgentGuiLauncher

:exit
exit /b %errorlevel%
