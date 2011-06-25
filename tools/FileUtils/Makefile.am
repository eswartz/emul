AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

bin_PROGRAMS = tidir ti2txt tidisasm tidecomp tispeak

tidir_SOURCES = tidir.c
ti2txt_SOURCES = ti2txt.c
tidisasm_SOURCES = tidisasm.c 
tidecomp_SOURCES = tidecomp.c dcbitvector.c
tispeak_SOURCES = tispeak.c lpcutils.c

CTOP=$(TOP)/../c/v9t9-c

INCLUDES = -I$(CTOP)/source -I$(CTOP)/source/OSLib -I$(CTOP) $(shell pkg-config glib-2.0 --cflags)

OBJS = $(CTOP)/source/fiad.$(OBJEXT) $(CTOP)/source/xmalloc.$(OBJEXT) \
	$(CTOP)/source/dis9900.$(OBJEXT)

LIBS = $(OBJS) $(CTOP)/source/OSLib/libOSLib.a \
	-lpthread -ldl -lstdc++ $(shell pkg-config glib-2.0 --libs)

lpcutils.$(OBJEXT): $(CTOP)/source/lpc.c

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"

CFLAGS = @CFLAGS@ $(DEFS) $(INCLUDES) $(OPTFLAGS)

tidir$(EXEEXT): $(OBJS) $(tidir_OBJECTS)
	$(LINK) $(tidir_OBJECTS) $(LIBS)
	-ln -f tidir$(EXEEXT) tidir

ti2txt$(EXEEXT): $(OBJS) $(ti2txt_OBJECTS)
	$(LINK) $(ti2txt_OBJECTS) $(LIBS)
	-ln -f ti2txt$(EXEEXT) ti2txt

tidisasm$(EXEEXT): $(OBJS) $(tidisasm_OBJECTS)
	$(LINK) $(tidisasm_OBJECTS) $(LIBS)
	-ln -f tidisasm$(EXEEXT) tidisasm

tidecomp$(EXEEXT): $(OBJS) $(tidecomp_OBJECTS)
	$(LINK) $(tidecomp_OBJECTS) $(LIBS)
	-ln -f tidecomp$(EXEEXT) tidecomp


force:

all-local: force
	make -C $(CTOP)/source
	make -C $(CTOP)/source/OSLib
