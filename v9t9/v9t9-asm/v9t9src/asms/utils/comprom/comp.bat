compile ..\roms\%1.bin >%1.asm
tasmx /q %1
tlink %1
exe2bin %1