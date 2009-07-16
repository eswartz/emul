CONF=Debug

CC ?= gcc
AR ?= ar

ifeq ($(CONF),Debug)
CFLAGS += -g
else
CFLAGS += -O -DNDEBUG
endif
CFLAGS += -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE
CFLAGS += -Wall -Wmissing-prototypes -Wno-parentheses

OPSYS=$(shell uname -o 2>/dev/null || uname -s)
MACHINE=$(shell uname -m)
ifeq ($(OPSYS),Cygwin)
LIBS=-lws2_32 -liphlpapi
else
ifeq ($(OPSYS),Msys)
CFLAGS:=-mwin32 $(CFLAGS)
LIBS=-lws2_32 -liphlpapi
else
ifeq ($(OPSYS),Darwin)
LIBS=-lpthread
RANLIB=ranlib $@
else
LIBS=-lpthread -lssl -lrt
endif
endif
endif

VERSION=$(shell grep "%define version " tcf-agent.spec | sed -e "s/%define version //")
BINDIR=$(OPSYS)/$(MACHINE)/$(CONF)
INSTALLROOT ?= /tmp
SBIN=/usr/sbin
INIT=/etc/init.d

OFILES=$(addprefix $(BINDIR)/,$(filter-out main%.o,$(addsuffix .o,$(basename $(wildcard *.c)))))
HFILES=$(wildcard *.h)
CFILES=$(wildcard *.c)
EXECS=$(BINDIR)/agent $(BINDIR)/client $(BINDIR)/tcfreg $(BINDIR)/valueadd $(BINDIR)/tcflog

ifdef SERVICES
CFLAGS += $(shell ./services-to-cflags $(SERVICES))
endif

all:	$(EXECS)

$(BINDIR)/libtcf.a : $(OFILES)
	$(AR) -rc $@ $^
	$(RANLIB)

$(BINDIR)/agent: $(BINDIR)/main.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/client: $(BINDIR)/main_client.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_client.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/tcfreg: $(BINDIR)/main_reg.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_reg.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/valueadd: $(BINDIR)/main_va.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_va.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/tcflog: $(BINDIR)/main_log.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main_log.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/%.o: %.c $(HFILES) Makefile
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

