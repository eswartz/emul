AUTOMAKE_OPTIONS = foreign no-dependencies

noinst_LIBRARIES = libWin32.a

libWin32_a_SOURCES = 
libWin32_a_SRCS = @WINMODSRCS@
libWin32_a_C_SRCS = $(filter %.c, $(libWin32_a_SRCS))
libWin32_a_CPP_SRCS = $(filter %.cpp, $(libWin32_a_SRCS))
libWin32_a_LIBADD = $(libWin32_a_C_SRCS:%.c=%.$(OBJEXT)) \
	$(libWin32_a_CPP_SRCS:%.cpp=%.$(OBJEXT))

EXTRA_DIST = dsr_rs232_win32.c keyboard_win32.c sound_win32.c \
	video_win32.c video_win32.h video_win32_ddraw.cpp video_win32_drawdib.c \
	video_win32_ddraw.h video_win32_drawdib.h

TOP = $(top_srcdir)
SRC = $(TOP)/source
INCS = -I$(SRC) -I$(SRC)/OSLib -I$(SRC)/Modules -I$(TOP)/Resources -I$(TOP)
localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"
CFLAGS = $(INCS) @CFLAGS@ $(DEFS)

LIBS = $(libWin32_a_LIBADD) @LIBS@


%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cc
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cpp
	$(COMPILE) -c $< -o $@
