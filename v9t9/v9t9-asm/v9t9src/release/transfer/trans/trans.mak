.AUTODEPEND

#		*Translator Definitions*
CC = bcc +TRANS.CFG
TASM = TASM
TLIB = tlib
TLINK = tlink
LIBPATH = C:\BORLANDC\LIB
INCLUDEPATH = C:\BORLANDC\INCLUDE


#		*Implicit Rules*
.c.obj:
  $(CC) -c {$< }

.cpp.obj:
  $(CC) -c {$< }

#		*List Macros*


EXE_dependencies =  \
 trans.tsm \
 main.tsm \
 utils.tsm \
 defs.tsm \
 menu.tsm

#		*Explicit Rules*
trans.exe: trans.cfg $(EXE_dependencies)


#		*Individual File Dependencies*
trans.tsm: trans.cfg trans.ti 
        TXT2ASM TRANSS TRANS.TI

main.tsm: trans.cfg main.ti 
        TXT2ASM MAINS MAIN.TI

utils.tsm: trans.cfg utils.ti 
        TXT2ASM UTILSS UTILS.TI

defs.tsm: trans.cfg defs.ti 
        TXT2ASM DEFSS DEFS.TI

menu.tsm: trans.cfg menu.ti 
        TXT2ASM MENUS MENU.TI


#		*Compiler Configuration File*
trans.cfg: trans.mak
  copy &&|
-v
-vi-
-wpro
-weas
-wpre
-I$(INCLUDEPATH)
-L$(LIBPATH)
-P-.TI
| trans.cfg


