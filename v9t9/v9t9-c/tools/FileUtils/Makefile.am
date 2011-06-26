AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

BIN = $(TOP)/../../build/bin

bin_PROGRAMS = $(BIN)/tidir $(BIN)/ti2txt $(BIN)/tidisasm $(BIN)/tidecomp $(BIN)/tispeak

__BIN__tidir_SOURCES = tidir.c
__BIN__ti2txt_SOURCES = ti2txt.c
__BIN__tidisasm_SOURCES = tidisasm.c 
__BIN__tidecomp_SOURCES = tidecomp.c dcbitvector.c
__BIN__tispeak_SOURCES = tispeak.c lpcutils.c

CTOP=$(TOP)/../v9t9

INCLUDES = -I$(CTOP)/source -I$(CTOP)/source/OSLib -I$(CTOP) $(shell pkg-config glib-2.0 --cflags)

OBJS_EXTRA = $(CTOP)/source/fiad.$(OBJEXT) $(CTOP)/source/xmalloc.$(OBJEXT) \
	$(CTOP)/source/dis9900.$(OBJEXT)

LIBS = $(OBJS_EXTRA) $(CTOP)/source/OSLib/libOSLib.a \
	-lpthread -ldl -lstdc++ $(shell pkg-config glib-2.0 --libs)

lpcutils.$(OBJEXT): $(CTOP)/source/lpc.c

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"

CFLAGS = @CFLAGS@ $(DEFS) $(INCLUDES) $(OPTFLAGS)

$(BIN)/tidir$(EXEEXT): $(OBJS) $(__BIN__tidir_OBJECTS) $(__BIN__tidir_DEPENDENCIES)
	$(LINK) $(__BIN__tidir_OBJECTS) $(LIBS)
	-ln -f $(BIN)/tidir$(EXEEXT) $(BIN)/tidir

$(BIN)/ti2txt$(EXEEXT): $(OBJS) $(__BIN__ti2txt_OBJECTS) $(__BIN__ti2txt_DEPENDENCIES)
	$(LINK) $(__BIN__ti2txt_OBJECTS) $(LIBS)
	-ln -f $(BIN)/ti2txt$(EXEEXT) $(BIN)/ti2txt

$(BIN)/tidisasm$(EXEEXT): $(OBJS) $(__BIN__tidisasm_OBJECTS) $(__BIN__tidisasm_DEPENDENCIES)
	$(LINK) $(__BIN__tidisasm_OBJECTS) $(LIBS)
	-ln -f $(BIN)/tidisasm$(EXEEXT) $(BIN)/tidisasm

$(BIN)/tidecomp$(EXEEXT): $(OBJS) $(__BIN__tidecomp_OBJECTS) $(__BIN__tidecomp_DEPENDENCIES)
	$(LINK) $(__BIN__tidecomp_OBJECTS) $(LIBS)
	-ln -f $(BIN)/tidecomp$(EXEEXT) $(BIN)/tidecomp

$(BIN)/tispeak$(EXEEXT): $(OBJS) $(__BIN__tispeak_OBJECTS) $(__BIN__tispeak_DEPENDENCIES)
	$(LINK) $(__BIN__tispeak_OBJECTS) $(LIBS)
	-ln -f $(BIN)/tispeak$(EXEEXT) $(BIN)/tispeak
	
force:

all: prereq all-am

prereq:
	make -C $(CTOP)/source
	make -C $(CTOP)/source/OSLib
	
