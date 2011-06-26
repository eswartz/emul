
SUBDIRS = Generate OSLib Modules
bin_PROGRAMS = v9t9
#noinst_PROGRAMS = v9t9.so
AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)
SRC = $(TOP)/source
DIR = source
LIB = .
INCDIRS = @INCDIRS@ $(SRC)/OSLib $(SRC)/Modules $(SRC) $(TOP)/Resources
INCLUDES = $(foreach i, $(strip $(INCDIRS)), -I$(i))  -I$(TOP)
localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\" -DSHAREDIR=\"$(pkgdatadir)\" \
	-DTOPSRCDIR=\"$(shell pwd)/$(top_srcdir)\" 

v9t9_SOURCES = command_parser.y command_lexer.l command.c configfile.c \
	datafiles.c xmalloc.c 9900.c 9901.c cru.c debugger.c dis9900.c \
	dsr.c emulate.c \
	log.c grom.c keyboard.c memory.c mix_server.c fiad.c demo.c \
	sound.c speech.c lpc.c sysdeps.c timer.c v9t9.c v9t9_module.c \
	vdp.c vdpsprites.c command_rl.c help.c moduledb.c \
	compiler.c

#v9t9_so_SOURCES = $(v9t9_SOURCES)

YFLAGS = -d -v

BUILT_SOURCES = vdpdrawrow.c vdpdrawrowtext.c

EXTRA_v9t9_SOURCES = unixmain.c winmain.c gtkcallbacks.c gtkinterface.c \
	gtkloop.c gtksupport.c gtk_v99filesel.c gnomecallbacks.c gnomeinterface.c \
	gnomeloop.c gnomesupport.c qteloop.cc $(BUILT_SOURCES)

#EXTRA_v9t9_so_SOURCES = $(EXTRA_v9t9_SOURCES)

C_SRCS = $(filter %.c,@V9T9SRCS@ $(BUILT_SOURCES))
CXX_SRCS = $(filter %.cc,@V9T9SRCS@ $(BUILT_SOURCES))
EXTRA_v9t9_OBJECTS = $(patsubst %.c,%.$(OBJEXT),$(C_SRCS)) \
	$(patsubst %.cc,%.$(OBJEXT), $(CXX_SRCS))

#EXTRA_v9t9_so_OBJECTS = $(EXTRA_v9t9_OBJECTS)

EXTRA_DIST = gtkloop.c gtkloop.h gtkinterface.c gtkinterface.h \
	gtkcallbacks.c gtksupport.c gtksupport.h \
	gtk_v99filesel.c gtk_v99filesel.h gtkcallbacks.h \
	unixmain.c winmain.c win32prefix.h winheaders.pch \
	tms5220r.c __global_cli_sti.c \
	$(wildcard *.h) \
	winheaders.pch++ winv9t9.h  

LIBDIRS = OSLib Modules 
ALLLIBS = $(foreach i,$(LIBDIRS),$(i)/lib$(i).a) \
		Modules/@HOSTOS@/lib@HOSTOS@.a ../libiberty/libiberty.a

LIBS = $(EXTRA_v9t9_OBJECTS) $(ALLLIBS) @LIBS@ 

vdpdrawrow.c vdpdrawrowtext.c:
	 make -C Generate

FORCE:

gtkcallbacks.$(OBJEXT): gtkcallbacks.c gtkcallbacks.h $(wildcard $(SRC)/gtkcallbacks-*.h)

gtkcallbacks.c: $(TOP)/v9t9.glade
	-@PERL@ -w $(SRC)/fixgtkcallbacks.pl gtkcallbacks.c


gnomecallbacks.$(OBJEXT): gnomecallbacks.c gnomecallbacks.h $(wildcard $(SRC)/gnomecallbacks-*.h)

# can't really regenerate this now
#gnomecallbacks.c: $(TOP)/v9t9.glade-2
#	-@PERL@ -w $(SRC)/fixgnomecallbacks.pl gnomecallbacks.c


qteloop.$(OBJEXT): qteloop.cc qteloop.h Modules/video_qte.h Modules/keyboard_qte.h 

moc_qtehandler.cc: qtehandler.h
	$(QTDIR)/bin/moc qtehandler.h -o moc_qtehandler.cc

##%.$(OBJEXT): %.c
##	$(COMPILE) -o $@ -c $<

##$(ALLLIBS): FORCE
##	$(MAKE) -C $(dir $@)

v9t9: $(v9t9_OBJECTS) $(EXTRA_v9t9_OBJECTS) $(ALLLIBS)
	$(LINK) $(v9t9_OBJECTS) $(LIBS)

v9t9.so : $(v9t9_so_OBJECTS) $(EXTRA_v9t9_so_OBJECTS) $(ALLLIBS)
	$(LINK) -shared $(v9t9_so_OBJECTS) $(LIBS)

v9t9mp: $(v9t9_OBJECTS) $(EXTRA_v9t9_OBJECTS) $(ALLLIBS)
	$(LINK) $(v9t9_OBJECTS) $(LIBS) -lmpatrol -lpthread -lbfd -liberty

V9t9 = $(DESTDIR)$(bindir)/`echo v9t9|sed 's/$(EXEEXT)$$//'|sed '$(transform)'|sed 's/$$/$(EXEEXT)/'`

install-exec-local:
		@echo "***"
		@echo "*** WARNING:  v9t9 is being installed setuid root"
		@echo "***           to support SVGAlib."
		@echo "***"
	$(INSTALL_PROGRAM) -o root -m u=rwx,go=rx,+s v9t9 $(V9t9)

%.$(OBJEXT): %.c
	$(COMPILE) -fPIC -c $< -o $@

%.$(OBJEXT): %.cc
	$(CXXCOMPILE) -fPIC -c $< -o $@

%.$(OBJEXT): %.cpp
	$(CXXCOMPILE) -fPIC -c $< -o $@

mostlyclean-compile:
	-rm -f *.$(OBJEXT) core *.core


