# zebra-ssi

指令集合

LED_OFF:      04 E8 04 00 FF 10
SCAN_DISABLE: 04 EA 04 00 FF 0E
SCAN_ENABLE:  04 E9 04 00 FF 0F

START_DECODE: 04 E4 04 00 FF 14
STOP_DECODE:  04 E5 04 00 FF 13

Data As Is Beeps:                      07 C6 04 00 02 EB 00 FE 42
Data As Is:                            07 C6 04 00 FF EB 00 FD 45
<PREFIX> <DATA> <SUFFIX 1>:            07 C6 04 00 FF EB 05 FD 40
<PREFIX> <DATA> <SUFFIX 1> <SUFFIX 2>: 07 C6 04 00 FF EB 07 FD 3E

Prefix 02       09 C6 04 00 FF 63 10 69 02 FD 50
Suffix 1 03  	  09 C6 04 00 FF 62 10 68 03 FD 51
Suffix 1 0D     09 C6 04 00 FF 62 10 68 0D FD 47
Suffix 2 0A 	  09 C6 04 00 FF 64 10 6A 0A FD 46
Suffix 2 03 	  09 C6 04 00 FF 64 10 6A 03 FD 4D

Presentation (Blink) 07 C6 04 00 FF 8A 07 FD 9F

''' Enable Interleaved 2 of 5          07 C6 04 00 FF 06 01 FE 29
Enable Code 93                     07 C6 04 00 FF 09 01 FE 26
Enable Codabar                     07 C6 04 00 FF 07 01 FE 28
Enable MSI						             07 C6 04 00 FF 0B 01 FE 24
Enable PDF417					             07 C6 04 00 FF 0F 01 FE 20
Enable MicroPDF417                 07 C6 04 00 FF E3 01 FD 4C
Enable Data Matrix                 07 C6 04 00 FF F0 01 FD 3F
Inverse Autodetect                 07 C6 04 00 FF F1 02 FD 3D
Enable Maxicode                    08 C6 04 00 FF F0 26 01 FD 18
