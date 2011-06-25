SUBDIRS = Win32 Unix MacOS
#SUFFIXES = .cpp
AUTOMAKE_OPTIONS = foreign no-dependencies

noinst_LIBRARIES = libModules.a
# this is only set to force CC, COMPILE, etc to be set
libModules_a_SOURCES = dsr_rs232.h dummy.cc
libModules_a_SRCS = @MODSRCS@

C_SRCS = $(filter %.c,$(libModules_a_SRCS))
CXX_SRCS = $(filter %.cc,$(libModules_a_SRCS))

libModules_a_LIBADD = $(patsubst %.c,%.$(OBJEXT),$(C_SRCS)) \
	$(patsubst %.cc,%.$(OBJEXT), $(CXX_SRCS))

EXTRA_DIST = dsr_emudisk.c dsr_realdisk.c dsr_rs232.c dsr_rs232_int_null.c \
	dsr_rs232.h keyboard_gtk.c video_gtk.c keyboard_null.c video_null.c \
	sound_null.c 

TOP = $(top_srcdir)
SRC = $(TOP)/source
DIR = Modules
INCS = -I$(TOP) -I$(SRC) -I$(SRC)/OSLib -I$(TOP)/Resources -I$(SRC)/Modules/Unix
localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"

CFLAGS = $(INCS) @CFLAGS@ $(DEFS)
CXXFLAGS = $(INCS) @CXXFLAGS@ $(DEFS)
LIBS = $(libModules_a_LIBADD) @LIBS@ 

video_qte.$(OBJEXT): video_qte.cc video_qte.h ../qteloop.h
	$(CXXCOMPILE)  -c $< -o $@ -O3

keyboard_qte.$(OBJEXT): keyboard_qte.cc keyboard_qte.h ../qteloop.h

# automake can't seem to track dependencies on the sources changing...
all-local: libModules.a
	@if test Makefile.am -nt libModules.a ; then \
		rm -f libModules.a; \
	fi

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cc
	$(CXXCOMPILE) -c $< -o $@

%.$(OBJEXT): %.cpp
	$(CXXCOMPILE) -c $< -o $@

mostlyclean-compile:
	-rm -f *.$(OBJEXT) core *.core


