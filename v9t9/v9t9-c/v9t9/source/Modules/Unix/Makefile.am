AUTOMAKE_OPTIONS = foreign no-dependencies

noinst_LIBRARIES = libUnix.a

libUnix_a_SOURCES = sound_pthread_mixer.h
libUnix_a_SRCS = @UNIXMODSRCS@
libUnix_a_LIBADD = $(libUnix_a_SRCS:%.c=%.$(OBJEXT))

#libUnix_a_SOURCES = @UNIXMODSRCS@

EXTRA_DIST = dsr_rs232_unix.c keyboard_X.c keyboard_svgalib.c \
	sound_alsa.c sound_oss.c sound_esd.c sound_speaker.c \
	sound_pthread_mixer.h video_X.c video_X_draw.h video_svgalib.c

PTHREAD_MODULES=sound_alsa.c sound_oss.c sound_esd.c
PTHREAD_OBJS=$(PTHREAD_MODULES:%.c=%.$(OBJEXT))

TOP = $(top_srcdir)
SRC = $(TOP)/source
INCS = -I$(SRC) -I$(SRC)/OSLib -I$(SRC)/Modules -I$(TOP)
localedir=@prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"
CFLAGS = $(INCS) @CFLAGS@ $(DEFS)

LIBS = $(libUnix_a_LIBADD) @LIBS@

#
# automake can't seem to track dependencies on the sources changing
# when libraries are involved... whatever...
all-redirect: all-am
	@if test sound_pthread_mixer.h -nt libUnix.a || test ../sound_thread_mixer.h -nt libUnix.a; then \
		rm -f libUnix.a $(PTHREAD_OBJS); \
		make libUnix.a; \
	fi


%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cc
	$(CXXCOMPILE) -c $< -o $@

%.$(OBJEXT): %.cpp
	$(CXXCOMPILE) -c $< -o $@

mostlyclean-compile:
	-rm -f *.$(OBJEXT) core *.core
