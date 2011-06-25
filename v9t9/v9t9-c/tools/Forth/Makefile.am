AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

@SET_MAKE@

ROMADDR=./romaddr$(EXEEXT)
 
# be able to access local files...-e 
GFORTHPATH:=$(shell pwd)/site-forth:$(GFORTHPATH):$(shell pwd)

#TIASM = ../TIAsm/tiasm
TIASM_RUN = java -jar $(TIASM)
TIASM = $(TOP)/../build/tiasm.jar

OUTS = nforthA.rom nforth.grm nforthB.rom nforthB.sym  \
	nforth.prm nforthc.bin nforthg.bin  nforthc.sym nforthg.sym

FORTHCOMMON=kernel.fs dict.fs compile.fs interp.fs user.fs \
	init.fs site-forth/cross.fs include.fs
FORTH99=99build.fs 99test.fs 99simple.fs 99equs.fs 99asm.fs 99prims.fs \
	99config.fs 99memory.fs 99kernel.fs \
	compile.fs common.fs commonhash.fs files.fs \
	interp.fs benchmark.fs video.fs blocks.fs editor.fs testing.fs \
	lib/99equs lib/99ram locals.fs

ROM99=nforth.tsm nforth0.tsm $(wildcard *.i) nforth.gsm

EXTRA_DIST = $(ROM99) $(FORTH99) $(FORTHCOMMON) \
	romaddr.c doc/placeholder.txt nforth.cnf $(OUTS)

all-local:	$(OUTS)

clean-local:
	rm -f $(OUTS)

install-data-local:
	mkdir -p $(pkgdatadir)/forth/roms $(pkgdatadir)/forth/site-forth
	for i in $(OUTS) ; do \
		$(INSTALL_DATA) $$i $(pkgdatadir)/forth/roms/$$i ; \
	done
	for i in $(FORTHCOMMON) ; do \
		$(INSTALL_DATA) $$i $(pkgdatadir)/forth/$(notdir $$i) ; \
	done
	$(INSTALL_DATA) nforth.cnf $(pkgdatadir)/nforth.cnf

force:

$(ROMADDR): romaddr.c
	@CC@ -o $@ $^

$(TIASM):
	( cd ../TIAsm && ant  )

#$(TIASM):
#	make -C $(dir $(TIASM))


nforth0.rom: $(FORCE) $(ROM99) $(TIASM)
	$(TIASM_RUN) -DENHANCED_MEMORY nforth0.tsm -e nforth0.rom -l nforth0.lst 

nforthA.rom: $(FORCE) $(ROM99) $(TIASM)
	$(TIASM_RUN) -DENHANCED_MEMORY nforth.tsm -e nforthA.rom -l nforthc.lst 

nforth.grm: nforth.gsm $(TIASM)
	$(TIASM_RUN) -DENHANCED_MEMORY $< -g $@ -l nforthg.lst

GFORTH = @GFORTH@

if HAVE_GFORTH

#ROMADDR=100

LIST=nforthB.lst

nforth.prm nforthc.bin nforthg.bin nforthc.sym nforthg.sym: $(FORCE) $(TIASM) $(ROMADDR) nforth0.rom $(FORTH99) $(FORTHCOMMON)
	rm -f $(LIST)
	-@GFORTH@ -e ": get-rom-addr $(shell $(ROMADDR) nforth0.rom) ; create mach-file ,\" `pwd`/99config.fs" 99build.fs -e bye &>out.lst 
	if test -f $(LIST) ; then \
		if ! grep 'Nothing' < $(LIST) &>/dev/null; then tail $(LIST); false; fi ; \
	fi
	python getforthsyms.py <$(LIST) >nforthc.sym
	cp -f nforthc.sym nforthg.sym

nforthB.rom nforthB.sym: $(FORCE) nforth.prm nforth0.rom $(ROMADDR)
	cp -f nforth0.rom nforthB.rom && \
	SIZE=$(shell $(ROMADDR) nforth0.rom) && \
	if [ ! -z "$$SIZE" ] ; then \
		dd if=nforth.prm of=nforthB.rom bs=1 seek=$$SIZE count=$$[16384-$$SIZE]; \
	fi ;		 
	python getforthsyms.py <$(LIST) >nforthB.sym
    
else

nforth.prm nforthc.bin nforthg.bin: $(FORCE) $(TIASM) $(ROMADDR) nforth0.rom $(FORTH99) $(FORTHCOMMON)
	@echo "GNU forth (gforth) is not installed, kernel cannot be rebuilt."

endif


