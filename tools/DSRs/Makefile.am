TOP = @TOP@

AUTOMAKE_OPTIONS = foreign no-dependencies

EXTRA_DIST = $(DSRS) opcodes.inc aclocal.m4

TIASM = java -jar $(TIASM_JAR)
TIASM_JAR = $(TOP)/../build/tiasm.jar
ASMFLAGS=

DSRS = emudisk.dsr emu2disk.dsr emurs232.dsr emupio.dsr
OUTS = $(DSRS:%.dsr=%.bin)

all:	$(OUTS)

clean:
	rm -f $(OUTS)

install-data-am:
	mkdir -p $(pkgdatadir)/dsrs
	for i in $(OUTS) ; do \
		$(INSTALL_DATA) $$i $(pkgdatadir)/dsrs/$$i ; \
	done

INCS = opcodes.inc

FORCE:

emudisk.bin:	emudisk.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

emu2disk.bin:	emu2disk.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

emurs232.bin:	emurs232.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

emupio.bin:	emupio.dsr $(INCS) $(TIASM_JAR)
	$(TIASM) $< -d $@ $(ASMFLAGS)

