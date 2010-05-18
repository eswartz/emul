TCF_AGENT_DIR=.

include $(TCF_AGENT_DIR)/Makefile.inc
override CFLAGS += $(OPTS)

all:	$(EXECS)

$(BINDIR)/libtcf$(EXTLIB) : $(OFILES)
	$(AR) -rc $@ $^
	$(RANLIB)

$(BINDIR)/agent$(EXTEXE): $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/client$(EXTEXE): $(BINDIR)/main/main_client$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main_client$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/tcflua$(EXTEXE): $(BINDIR)/main/main_lua$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) $(EXPORT_DYNAMIC) -o $@ $(BINDIR)/main/main_lua$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS) $(LUADIR)/lib/liblua$(EXTLIB) -lm -ldl

$(BINDIR)/tcfreg$(EXTEXE): $(BINDIR)/main/main_reg$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main_reg$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/valueadd$(EXTEXE): $(BINDIR)/main/main_va$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main_va$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/tcflog$(EXTEXE): $(BINDIR)/main/main_log$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main_log$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/%$(EXTOBJ): %.c $(HFILES) Makefile
	@mkdir -p $(dir $@)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -rf $(BINDIR) RPM *.tar *.tar.bz2 *.rpm

install: all
	install -d -m 755 $(INSTALLROOT)$(SBIN)
	install -d -m 755 $(INSTALLROOT)$(INIT)
	install -c $(BINDIR)/agent -m 755 $(INSTALLROOT)$(SBIN)/tcf-agent
	install -c main/tcf-agent.init -m 755 $(INSTALLROOT)$(INIT)/tcf-agent

ALLFILES = Makefile* *.html *.sln *.vcproj *.h \
  bin framework machine main services system

tcf-agent-$(VERSION).tar.bz2: $(HFILES) $(CFILES) Makefile main/tcf-agent.spec main/tcf-agent.init
	rm -rf tcf-agent-$(VERSION) tcf-agent-$(VERSION).tar.bz2
	mkdir tcf-agent-$(VERSION)
	tar c --exclude "*.svn" $(ALLFILES) | tar x -C tcf-agent-$(VERSION)
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

