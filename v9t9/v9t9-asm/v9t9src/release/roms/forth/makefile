#	Makefile for FORTH.
#
#
#	Add "/Ddebug" if you want a listing from the assembler.

ROMSDIR = ..
MODULESDIR = ..\..\modules

!ifdef	debug
LST=	forth.lst
!else
LST=
!endif

$(ROMSDIR)\forth.rom $(MODULESDIR)\forthc.bin $(LST): \
	forth.tsm dev.inc kbd.inc ram.inc int.inc general.inc term.inc \
	video.inc
	tiasm\tiasm forth.tsm $(ROMSDIR)\forth.rom $(MODULESDIR)\forthc.bin \
	$(LST)
	
$(ROMSDIR)\forth.grm : forth.gsm
	tasm forth.gsm
	tlink /x forth
	exe2bin forth $(ROMSDIR)\forth.grm
	del forth.obj
	del forth.exe


all:	$(ROMSDIR)\forth.rom $(LST) $(ROMSDIR)\forth.grm $(MODULESDIR)\forthc.bin
