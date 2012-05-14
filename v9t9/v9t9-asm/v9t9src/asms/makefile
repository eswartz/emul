#       V9t9: the TI Emulator! v6.0 Makefile
#       ------------------------------------
#
#       This makefile was written for Borland Make v3.1.
#       If you encounter errors with this makefile, see the notes
#       below for hand-fixes.
#
#       This makefile assumes you have all your *.asm, *.inc, and *.h
#       files in the current directory (maybe called ASMS\), and that
#       directories ..\OBJS, ..\2OBJS, ..\SOBJS, and ..\FINIS exist.
#
#       The tree that is in the zipfile is:
#
#       V9t9SRC\
#               ASMS\           # run make from here
#               OBJS\
#               OBJS.286\
#               OBJS.SLO\
#               FINIS\
#
#
#       Run "make [xxxxx] {all|clean}" where xxxxxx might be:
#
#       /Drelease       -- compiles version without debugging symbols
#
#       /Dd286          -- compile 286 version
#               OR
#       /Ddslow         -- compile slow version
#
#
#	/Dtextonly 	-- compile text-only version
#
#       USE THE SAME CAPITALIZATION!
#
#       The default is to create a debugabble 386 version.
#
#       All object files will be put into ..\OBJS , ..\2OBJS , or ..\SOBJS
#       depending on if you define nothing, d286, or dslow.
#
#       The executable will be put in ..\FINIS.
#
#       Note:  you may get stuck in ..\*OBJS or ..\FINIS depending on
#       how the programs compile.  I don't know how to make it come
#       back to ..\ASMS on an error.
#
#       ----------------   NOTE NOTE NOTE NOTE! ------------------
#
#       You should have at least FILES=20 and BUFFERS=20 in your
#       CONFIG.SYS, otherwise the link may fail with the lie "cannot
#       find file xxxxx.OBJ".  (It's really a "out-of-file-handles"
#       error.)
#

tasm    = tasmx          # options are below, change path if necessary
tlink   = tlink         # options are below, change path if necessary
objs    = ..\objs       # default, changed below
finis   = ..\finis      # you can change at will

options_speed   = /dFAST
options_proc    = /dT386
executablename = v9t9.exe

!ifdef  d286                                    # 286 version
options_proc    =
executablename  = v9t9_286.exe
objs            = ..\objs.286

!else           # cannot define both!

!ifdef  dslow                                   # slow version
options_speed   = /dSUPERFAST                   # don't ask why
objs            = ..\objs.slo
executablename  = v9t9_slo.exe
!endif
!endif


!ifdef	textonly
objs		= ..\objs.txt
executablename	= v9t9_txt.exe
options_speed	= $(options_speed) /dTEXTONLY
!endif


!ifdef  release                                 # release vs. debug version
tasm_debug      =
tlink_debug     =
!else
tasm_debug      = /zi
tlink_debug     = /v /m
!endif


!ifdef  german
kbd     =       gkeybd
executablename= v9t9_ger.exe
!else
kbd     =       keyboard
!endif


source_files =  tiemul.asm video.asm special.asm int.asm hardware.asm \
                files.asm sound.asm $(kbd).asm \
                debug.asm speech.asm support.asm record.asm log.asm

obj_files  =    $(objs)\tiemul.obj $(objs)\video.obj $(objs)\special.obj \
                $(objs)\int.obj $(objs)\hardware.obj $(objs)\files.obj \
                $(objs)\sound.obj $(objs)\$(kbd).obj $(objs)\debug.obj \
                $(objs)\speech.obj $(objs)\support.obj $(objs)\record.obj \
                $(objs)\log.obj

obj_files_link =tiemul video special int hardware files sound $(kbd) \
                debug speech support record log
executable      = $(finis)\$(executablename)

tasm_options = $(tasm_debug) /q /m3 $(options_proc) $(options_speed) 
tlink_options= $(tlink_debug)


memory_incs =	memory.inc slowmem.inc
general_incs =  strucs.inc registers.inc standard.h demoequs.inc $(memory_incs)

incs = $(memory_incs) $(general_incs) adlib.inc comprom.inc demo.inc demoequs.inc \
	emulate.inc fastmem.inc fdc.inc files.inc floppy.inc graphics.inc  \
	grtext.inc joystick.inc lpc.inc memcode.inc memory.inc pcspeak.inc \
	register.inc rs232.inc sblaster.inc serial.inc slowmem.inc sprites.inc \
        state.inc strucs.inc supermem.inc tifont.inc sbdma.inc

#       If anyone can get MAKE to recognize the
#               .asm.{path}obj:
#       form for implicit rules, you can change COMP= below to be
#       be rule, and delete all the $(COMP) commands.
#
#       Please tell me if you can do this.  The make always fails
#       by not generating any single object file, as if everything
#       were hunky-dorey otherwise.

#       This way is repetitive but it works as is.
#

COMP=   $(tasm) $(tasm_options) $& $(objs)\$&

$(objs)\tiemul.obj:     tiemul.asm memcode.inc emulate.inc $(general_incs) \
                        tiemul.h video.h special.h int.h hardware.h files.h \
                        sound.h keyboard.h debug.h speech.h support.h \
                        record.h log.h fastmem.inc
        $(COMP)

$(objs)\video.obj:      video.asm $(general_incs) sprites.inc graphics.inc \
                        video.h tiemul.h debug.h speech.h record.h log.h \
			grtext.inc
        $(COMP)

$(objs)\special.obj:    special.asm $(general_incs) \
                        tiemul.h video.h special.h int.h hardware.h files.h \
                        sound.h keyboard.h debug.h speech.h support.h \
                        record.h log.h
        $(COMP)

$(objs)\int.obj:        int.asm $(general_incs) \
                        tiemul.h video.h hardware.h files.h keyboard.h \
                        debug.h record.h log.h int.h support.h
        $(COMP)

$(objs)\hardware.obj:   hardware.asm $(general_incs) rs232.inc \
                        tiemul.h video.h int.h files.h keyboard.h hardware.h
        $(COMP)

$(objs)\files.obj:      files.asm $(general_incs) files.inc floppy.inc fdc.inc serial.inc \
                        tiemul.h video.h hardware.h keyboard.h support.h files.h
        $(COMP)

$(objs)\sound.obj:      sound.asm $(general_incs) pcspeak.inc adlib.inc sblaster.inc \
                        tiemul.h int.h speech.h support.h record.h log.h sound.h sbdma.inc
        $(COMP)

$(objs)\$(kbd).obj:   $(kbd).asm $(general_incs) joystick.inc \
                        tiemul.h video.h special.h int.h hardware.h \
                        log.h keyboard.h
        $(COMP)
		
$(objs)\debug.obj:      debug.asm $(general_incs) \
                        tiemul.h video.h keyboard.h support.h debug.h
        $(COMP)

$(objs)\speech.obj:     speech.asm $(general_incs) lpc.inc \
                        tiemul.h video.h int.h sound.h support.h record.h \
                        speech.h  pcspeech.inc sbspeech.inc
        $(COMP)

$(objs)\support.obj:    support.asm strucs.inc standard.h \
                        tiemul.h video.h special.h int.h hardware.h files.h \
                        sound.h keyboard.h debug.h speech.h support.h \
                        record.h log.h
        $(COMP)

$(objs)\record.obj:     record.asm strucs.inc demo.inc state.inc demoequs.inc \
                        tiemul.h video.h int.h sound.h speech.h support.h \
                        record.h standard.h
        $(COMP)

$(objs)\log.obj:        strucs.inc standard.h \
                        int.h support.h log.h
        $(COMP)

$(executable): $(obj_files) $(source_files) $(incs)
        cd $(objs)
        $(tlink) $(tlink_options) $(obj_files_link), $(executable)
        cd $(finis)

all:    $(executable)

clean:
        del $(objs)\*.obj
        

