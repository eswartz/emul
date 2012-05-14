.AUTODEPEND

#		*Translator Definitions*
CC = bcc +TIASM.CFG
TASM = TASM
TLIB = tlib
TLINK = tlink
LIBPATH = E:\BORLANDC\LIB
INCLUDEPATH = E:\BORLANDC\INCLUDE


#		*Implicit Rules*
.c.obj:
  $(CC) -c {$< }

.cpp.obj:
  $(CC) -c {$< }

#		*List Macros*


EXE_dependencies =  \
 tiasm.obj

#		*Explicit Rules*
tiasm.exe: tiasm.cfg $(EXE_dependencies)
  $(TLINK) /v/x/c/P-/L$(LIBPATH) @&&|
c0c.obj+
tiasm.obj
tiasm
		# no map file
emu.lib+
mathc.lib+
cc.lib
|


#		*Individual File Dependencies*
tiasm.obj: tiasm.cfg tiasm.c 
	$(CC) -H -c tiasm.c

#		*Compiler Configuration File*
tiasm.cfg: tiasm.mak
  copy &&|
-mc
-3
-v
-G
-O
-Og
-Oe
-Om
-Ov
-Ol
-Ob
-Op
-Oi
-Z
-k-
-d
-vi-
-H=TIASM.SYM
-wpro
-weas
-wpre
-I$(INCLUDEPATH)
-L$(LIBPATH)
-P-.C
| tiasm.cfg


