#       V9t9: the TI Emulator! v6.0
#       ---------------------------
#
#	This makefile will compile the emulated ROMs,
#	such as disk, RS232, and speech.
#
#       This makefile was written for Borland Make v3.1,
#	which I can't get to recognize implicit rules!!!  Argh!
#
#

ROMSDIR= ..\release\roms

$(ROMSDIR)\emudisk.bin: emudisk.dsr
	tasm $&.dsr
	tlink /x $&,$(ROMSDIR)\$&		
	utils\exe2bin $(ROMSDIR)\$&.exe	
	del $&.obj
	del $(ROMSDIR)\$&.exe		
	utils\swapbyte $(ROMSDIR)\$&.bin

$(ROMSDIR)\emu2disk.bin: emu2disk.dsr
	tasm $&.dsr
	tlink /x $&,$(ROMSDIR)\$&		
	utils\exe2bin $(ROMSDIR)\$&.exe	
	del $&.obj
	del $(ROMSDIR)\$&.exe		
	utils\swapbyte $(ROMSDIR)\$&.bin

$(ROMSDIR)\emurs232.bin: emurs232.dsr
	tasm $&.dsr
	tlink /x $&,$(ROMSDIR)\$&		
	utils\exe2bin $(ROMSDIR)\$&.exe	
	del $&.obj
	del $(ROMSDIR)\$&.exe		
	utils\swapbyte $(ROMSDIR)\$&.bin

$(ROMSDIR)\emuspch.bin: emuspch.byt
	tasm $&.byt
	tlink /x $&,$(ROMSDIR)\$&		
	utils\exe2bin $(ROMSDIR)\$&.exe	
	del $&.obj
	del $(ROMSDIR)\$&.exe		


all:	$(ROMSDIR)\emudisk.bin $(ROMSDIR)\emu2disk.bin \
	$(ROMSDIR)\emurs232.bin $(ROMSDIR)\emuspch.bin
