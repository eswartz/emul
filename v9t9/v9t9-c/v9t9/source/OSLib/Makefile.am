AUTOMAKE_OPTIONS = foreign no-dependencies

noinst_LIBRARIES = libOSLib.a

TOP = $(top_srcdir)
SRC = $(TOP)/source
INCS = -I$(SRC) -I$(TOP) -I$(SRC)/OSLib
DIR = OSLib
localedir=@prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"

CC = @CC@
LD = @LD@
CFLAGS = $(INCS) @CFLAGS@ $(DEFS)
LDFLAGS = @LDFLAGS@
ARFLAGS = @ARFLAGS@

libOSLib_a_SOURCES = FileHandles.c Generic.c StringExtras.c StringUtils.c
libOSLib_a_SRCS = @OSLIBSRCS@
libOSLib_a_LIBADD = $(libOSLib_a_SRCS:%.c=%.$(OBJEXT))

EXTRA_DIST = Win32.c Win32.h Posix.c Posix.h MacOS.c MacOS.h \
	OSLib.h OSLibGeneric.h OSLibExtras.h StringUtils.h \
	centry.h cexit.h clstandardheaders.h clstandardtypes.h


LIBS = $(libOSLib_a_LIBADD) @LIBS@

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cc
	$(CXXCOMPILE) -c $< -o $@

%.$(OBJEXT): %.cpp
	$(CXXCOMPILE) -c $< -o $@

mostlyclean-compile:
	-rm -f *.$(OBJEXT) core *.core
