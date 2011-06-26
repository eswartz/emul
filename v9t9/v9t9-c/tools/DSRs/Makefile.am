TOP = @TOP@

BUILD=$(TOP)/../../build
ROMS=$(BUILD)/dsrs

AUTOMAKE_OPTIONS = foreign

EXTRA_DIST = $(DSRS) opcodes.inc aclocal.m4

TIASM = java -jar $(TIASM_JAR)
TIASM_JAR = $(BUILD)/bin/tiasm.jar
ASMFLAGS=

DSRS = emudisk.dsr emu2disk.dsr emurs232.dsr emupio.dsr
OUTS = $(DSRS:%.dsr=$(ROMS)/%.bin)

all:	$(ROMS) $(OUTS)

$(ROMS):
	mkdir -p $(ROMS)

clean:
	rm -f $(OUTS)

install-data-am:
	mkdir -p $(pkgdatadir)/dsrs
	for i in $(OUTS) ; do \
		$(INSTALL_DATA) $$i $(pkgdatadir)/dsrs/$$i ; \
	done

INCS = opcodes.inc

FORCE:

$(ROMS)/emudisk.bin:	emudisk.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

$(ROMS)/emu2disk.bin:	emu2disk.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

$(ROMS)/emurs232.bin:	emurs232.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

$(ROMS)/emupio.bin:	emupio.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

