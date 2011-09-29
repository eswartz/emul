TCF_AGENT_DIR=../../../org.eclipse.tcf.agent

include $(TCF_AGENT_DIR)/Makefile.inc

override CFLAGS += $(foreach dir,$(INCDIRS),-I$(dir)) $(OPTS)

HFILES := $(foreach dir,$(SRCDIRS) backend,$(wildcard $(dir)/*.h)) $(HFILES)
CFILES := $(sort $(foreach dir,$(SRCDIRS) backend,$(wildcard $(dir)/*.c)) $(CFILES))

EXECS = $(BINDIR)/dwarf-test$(EXTEXE)

all:    $(EXECS)

$(BINDIR)/libtcf$(EXTLIB) : $(OFILES)
	$(AR) -rc $@ $^
	$(RANLIB)

$(BINDIR)/dwarf-test$(EXTEXE): $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/%$(EXTOBJ): %.c $(HFILES) Makefile
	@$(call MKDIR,$(dir $@))
	$(CC) $(CFLAGS) -c -o $@ $<

$(BINDIR)/%$(EXTOBJ): $(TCF_AGENT_DIR)/%.c $(HFILES) Makefile
	@$(call MKDIR,$(dir $@))
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	$(call RMDIR,$(BINDIR))
