.AUTODEPEND

#		*Translator Definitions*
CC = bcc +DSRASM.CFG
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
 dsrasm.obj

#		*Explicit Rules*
dsrasm.exe: dsrasm.cfg $(EXE_dependencies)
  $(TLINK) /v/x/c/P-/L$(LIBPATH) @&&|
c0c.obj+
dsrasm.obj
dsrasm
		# no map file
emu.lib+
mathc.lib+
cc.lib
|


#		*Individual File Dependencies*
dsrasm.obj: dsrasm.cfg dsrasm.c 

#		*Compiler Configuration File*
dsrasm.cfg: dsrasm.mak
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
-H=DSRASM.SYM
-wpro
-weas
-wpre
-I$(INCLUDEPATH)
-L$(LIBPATH)
-P-.C
| dsrasm.cfg


