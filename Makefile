CONF = Debug

OPSYS = $(shell uname -o 2>/dev/null || uname -s)
MACHINE = $(shell uname -m)

ifeq ($(CONF),Debug)
  CFLAGS += -g
else
  CFLAGS += -O -DNDEBUG
endif

ifeq ($(OPSYS),Windows)
  CC = @./mcc -p $(OPSYS)/$(MACHINE)/$(CONF)/agent.pdb
  EXTOBJ = .obj
  EXTLIB = .lib
  EXTEXE = .exe
  LIBS = shell32.lib advapi32.lib Iphlpapi.lib WS2_32.lib
  ifeq ($(MACHINE),i686)
    MACHINE = i386
  endif
endif

ifeq ($(OPSYS),Cygwin)
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),Msys)
  CC = gcc
  CFLAGS := -mwin32 $(CFLAGS)
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),Darwin)
  LIBS = -lpthread
  RANLIB = ranlib $@
endif

ifneq ($(OPSYS),Windows)
  CFLAGS += -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE
  CFLAGS += -Wall -Wmissing-prototypes -Wno-parentheses
endif

CC ?= gcc
AR ?= ar
EXTOBJ ?= .o
EXTLIB ?= .a
EXTEXE ?=
EXPORT_DYNAMIC ?= -rdynamic
LIBS ?= -lpthread -lssl -lrt

ifdef PATH_Plugins
  CFLAGS += $(EXPORT_DYNAMIC) -DPATH_Plugins="$(PATH_Plugins)"
  LIBS += -ldl
endif

VERSION = $(shell grep "%define version " tcf-agent.spec | sed -e "s/%define version //")
BINDIR = $(OPSYS)/$(MACHINE)/$(CONF)
INSTALLROOT ?= /tmp
SBIN = /usr/sbin
INIT = /etc/init.d

OFILES = $(addprefix $(BINDIR)/,$(filter-out main%$(EXTOBJ),$(addsuffix $(EXTOBJ),$(basename $(wildcard *.c)))))
HFILES = $(wildcard *.h)
CFILES = $(wildcard *.c)
EXECS = $(BINDIR)/agent$(EXTEXE) $(BINDIR)/client$(EXTEXE) $(BINDIR)/tcfreg$(EXTEXE) $(BINDIR)/valueadd$(EXTEXE) $(BINDIR)/tcflog$(EXTEXE)
ifdef LUADIR
  EXECS += $(BINDIR)/tcflua
endif

ifdef SERVICES
  CFLAGS += $(shell ./services-to-cflags $(SERVICES))
endif

all:	$(EXECS)

$(BINDIR)/libtcf$(EXTLIB) : $(OFILES)
	$(AR) -rc $@ $^
	$(RANLIB)

$(BINDIR)/agent$(EXTEXE): $(BINDIR)/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/client$(EXTEXE): $(BINDIR)/main_client$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_client$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/tcflua$(EXTEXE): $(BINDIR)/main_lua$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) $(EXPORT_DYNAMIC) -o $@ $(BINDIR)/main_lua$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS) $(LUADIR)/lib/liblua$(EXTLIB) -lm -ldl

$(BINDIR)/tcfreg$(EXTEXE): $(BINDIR)/main_reg$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_reg$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/valueadd$(EXTEXE): $(BINDIR)/main_va$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_va$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/tcflog$(EXTEXE): $(BINDIR)/main_log$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_log$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/main_lua$(EXTOBJ): main_lua.c $(HFILES) Makefile
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) -I$(LUADIR)/include -c -o $@ $<

$(BINDIR)/%$(EXTOBJ): %.c $(HFILES) Makefile
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -rf $(BINDIR) RPM *.tar *.tar.bz2 *.rpm

install: all
	install -d -m 755 $(INSTALLROOT)$(SBIN)
	install -d -m 755 $(INSTALLROOT)$(INIT)
	install -c $(BINDIR)/agent -m 755 $(INSTALLROOT)$(SBIN)/tcf-agent
	install -c tcf-agent.init -m 755 $(INSTALLROOT)$(INIT)/tcf-agent

tcf-agent-$(VERSION).tar.bz2: $(HFILES) $(CFILES) Makefile tcf-agent.spec tcf-agent.init
	rm -rf tcf-agent-$(VERSION) tcf-agent-$(VERSION).tar.bz2
	mkdir tcf-agent-$(VERSION)
	cp *.spec *.c *.h *.init *.html *.sln *.vcproj Makefile tcf-agent-$(VERSION)
	tar cjf tcf-agent-$(VERSION).tar.bz2 tcf-agent-$(VERSION)
	rm -rf tcf-agent-$(VERSION)

tar: tcf-agent-$(VERSION).tar.bz2

ifeq ($(OPSYS),GNU/Linux)
rpm: all tar
	rm -rf RPM
	mkdir RPM RPM/BUILD RPM/RPMS RPM/RPMS/`uname -i` RPM/RPMS/noarch RPM/SOURCES RPM/SPECS RPM/SRPMS RPM/tmp
	echo "%_topdir $(PWD)/RPM" >~/.rpmmacros
	echo "%_tmppath $(PWD)/RPM/tmp" >>~/.rpmmacros
	rpmbuild -ta tcf-agent-$(VERSION).tar.bz2
	mv RPM/RPMS/`uname -i`/*.rpm .
	mv RPM/SRPMS/*.rpm .
	rm -rf RPM ~/.rpmmacros
endif

