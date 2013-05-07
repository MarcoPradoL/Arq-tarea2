%ECHO OFF
%ECHO Starting SCS System
PAUSE
%ECHO SCS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SCSConsole %1
%ECHO Starting Door Sensor Console
START "DOOR SENSOR CONSOLE" /MIN /NORMAL java DoorSensor %1
%ECHO Starting Window Sensor Console
START "WINDOW SENSOR CONSOLE" /MIN /NORMAL java WindowSensor %1
%ECHO Starting Motion Sensor Console
START "MOTION SENSOR CONSOLE" /MIN /NORMAL java MotionSensor %1
