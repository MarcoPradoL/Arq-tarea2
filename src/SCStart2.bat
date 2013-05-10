%ECHO OFF
%ECHO Starting SCS System
PAUSE
%ECHO SCS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SCSConsole %1
%ECHO Starting Secutiry Controller Console
START "SECURIRY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
%ECHO Starting Security Sensor Console
START "SECURITY SENSOR CONSOLE"  /NORMAL java SecuritySensor %1
