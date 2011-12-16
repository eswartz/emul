TCF_AGENT_DIR=../../../org.eclipse.tcf.agent

include $(TCF_AGENT_DIR)/Makefile.inc

override CFLAGS += $(foreach dir,$(INCDIRS),-I$(dir)) $(OPTS)

HFILES := $(foreach dir,$(SRCDIRS),$(wildcard $(dir)/*.h)) $(HFILES)
CFILES := $(sort $(foreach dir,$(SRCDIRS),$(wildcard $(dir)/*.c)) $(CFILES))

EXECS = $(BINDIR)/agent$(EXTEXE)

all:    $(EXECS)

$(BINDIR)/libtcf$(EXTLIB) : $(OFILES)
	$(AR) -rc $@ $^
	$(RANLIB)

$(BINDIR)/agent$(EXTEXE): $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB)
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main/main$(EXTOBJ) $(BINDIR)/libtcf$(EXTLIB) $(LIBS)

$(BINDIR)/%$(EXTOBJ): %.c $(HFILES) Makefile
	@$(call MKDIR,$(dir $@))
	$(CC) $(CFLAGS) -c -o $@ $<

$(BINDIR)/%$(EXTOBJ): $(TCF_AGENT_DIR)/%.c $(HFILES) Makefile
	@$(call MKDIR,$(dir $@))
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	$(call RMDIR,$(BINDIR))
