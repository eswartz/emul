noinst_PROGRAMS = generatevdpdrawrow generatevdpdrawrowtext
generatevdpdrawrow_SOURCES = generatevdpdrawrow.c
generatevdpdrawrowtext_SOURCES = generatevdpdrawrowtext.c

AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)
SRC = $(TOP)/source
localedir = @prefix@/share/locale

# NOTE: I don't know how to force stuff to be built for the host!
# HACK
CC = gcc
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"
LDFLAGS = 
#CFLAGS :=
LIBS =
OBJEXT = o
# HACK

INCS = -I$(SRC) -I$(SRC)/OSLib -I. -I$(TOP)

CFLAGS = @CFLAGS@ $(INCS)

GENSRCS = $(SRC)/vdpdrawrow.c $(SRC)/vdpdrawrowtext.c

all-local: $(GENSRCS)

clean-local: 
	$(RM) $(GENSRCS)

$(SRC)/vdpdrawrow.c: generatevdpdrawrow
	./$< > $@

$(SRC)/vdpdrawrowtext.c: generatevdpdrawrowtext
	./$< > $@

