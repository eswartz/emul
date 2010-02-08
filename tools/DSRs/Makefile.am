TOP = @TOP@

AUTOMAKE_OPTIONS = foreign no-dependencies
TIASM = ../TIAsm/tiasm
EXTRA_DIST = $(DSRS) opcodes.inc aclocal.m4

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

$(TIASM): FORCE
	make -C $(dir $(TIASM))

emudisk.bin:	emudisk.dsr $(INCS) $(TIASM)
	$(TIASM) $< -d $@

emu2disk.bin:	emu2disk.dsr $(INCS) $(TIASM)
	$(TIASM) $< -d $@

emurs232.bin:	emurs232.dsr $(INCS) $(TIASM)
	$(TIASM) $< -d $@

emupio.bin:	emupio.dsr $(INCS) $(TIASM)
	$(TIASM) $< -d $@

