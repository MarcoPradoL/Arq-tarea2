Arq-tarea2
==========

Ejecutar Sistema A
Abrir una ventana de comandos
posicionarse sobre la carpeta scr
ejecutar el comando javac *.java para compilar todos los archivos 
Ejecutar archivo EMStart.bat 
opcion 1:
Ejecutar archivo SCStart.bat
opcion 2: 
Ejecutar archivo SCStart2.bat


SCSConsole: responde a eventoscon ID 6,7 y 8
Codigos de Accion:

"xxx" si id=6 entonces la cadena de texto "xxx" contiene el valor, en texto de false o true de  puerta rota

"yyy" si id=7 entonces la cadena de texto "yyy" contiene el valor, en texto de false o true de  ventana rota
"zzz" si id=8 entonces la cadena de texto "zzz" contiene el valor, en texto de false o true de  deteccion de movimiento
"zzz" si id=12 entonces la cadena de texto "www" contiene el valor, en texto de false o true de  deteccion de Fuego

los eventos con ID 100 terminban la ejecucion.

opcion 1:
Door Sensor: 
Envia eventos ID 6
responde a eventos ID -9 0 100
Codigos de accion 
"D1" Confirmacion de encendido de alarma de la puerta
"D0" Confirmacion de apagado de alarma de la puerta

Windows Sensor: 
Envia eventos ID 7
responde a eventos ID -10 0 100
Codigos de accion 
"W1" Confirmacion de encendido de alarma de la ventana
"W0" Confirmacion de apagado de alarma de la ventana

Windows Sensor: 
Envia eventos ID 8
responde a eventos ID -11 0 100
Codigos de accion 
"M1" Confirmacion de encendido de alarma del detector de movimiento
"M0" Confirmacion de apagado de alarma del detector de movimiento

Opcion 2
Security Sensor: 
Envia eventos ID 6,7 y 8 , 12
responde a eventos ID -9, -10, -11 , -13, 100
Codigos de accion 
similar a los de los sensores de la opcion 1

 
Security Controller: 
Envia eventos ID -9, -10, -11
responde a eventos ID 9, 10, 11

Monior de seguridad:
Envia eventos ID 9, 10, 11, 14, 13
responde a eventos ID 6, 7, 8

Fire Controller: 
Envia eventos ID  -13
responde a eventos ID 13, 100

Codigos de accion para manipular  alarma de incendios 
"F1" Confirmacion de encendido de alarma del detector de fuego
"F0" Confirmacion de apagado de alarma del detector de fuego

Sprinkler Controller: 
Envia eventos ID 
responde a eventos ID 14, 100

Codigos de accion para manipular los rociadores
"S1" Confirmacion de encendido de rociadores
"S0" Confirmacion de apagado de rociadores


MCSConsole: responde a eventos con ID 20,21
Envia eventos ID 22
responde a eventos 20, 21
Codigos de Accion:

"xxx" si id=20 entonces la cadena de texto "xxx" contiene el valor, en texto el numero de participante que envia el latido

"yyy" si id=21 entonces la cadena de texto "yyy" contiene el valor, en texto el numero del participante, el nombre y una breve descripcion



