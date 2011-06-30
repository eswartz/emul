AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

BIN = $(TOP)/../../build/bin

bin_PROGRAMS =  $(BIN)/tiasm

__BIN__tiasm_SOURCES = tiasm.c

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

localedir=@prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\" -I$(top_srcdir) $(shell pkg-config glib-2.0 --cflags)
LIBS = @LIBS@  $(shell pkg-config glib-2.0 --libs)
