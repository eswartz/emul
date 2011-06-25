AUTOMAKE_OPTIONS = foreign no-dependencies

bin_PROGRAMS = tiasm

tiasm_SOURCES = tiasm.c

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

localedir=@prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\" -I$(top_srcdir) $(shell pkg-config glib-2.0 --cflags)
LIBS = @LIBS@  $(shell pkg-config glib-2.0 --libs)
