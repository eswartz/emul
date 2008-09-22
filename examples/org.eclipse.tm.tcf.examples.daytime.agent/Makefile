CONF=Debug

CC=gcc
ifeq ($(CONF),Debug)
CFLAGS=-g
else
CFLAGS=-O
endif
CFLAGS:=$(CFLAGS) -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE -Wmissing-prototypes  -I. -I../../agent -I-

OPSYS=$(shell uname -o)
MACHINE=$(shell uname -m)
ifeq ($(OPSYS),Cygwin)
LIBS=-lws2_32 -liphlpapi
else
ifeq ($(OPSYS),Msys)
CFLAGS:=-mwin32 $(CFLAGS)
LIBS=-lws2_32 -liphlpapi
else
LIBS=-lpthread -lrt
endif
endif

BINDIR=$(OPSYS)/$(MACHINE)/$(CONF)

CFILES=$(basename $(wildcard *.c)) $(basename $(notdir $(wildcard ../../agent/*.c)))
OFILES=$(addprefix $(BINDIR)/,$(filter-out main%.o, $(addsuffix .o, $(CFILES))))
HFILES=$(wildcard *.h) $(wildcard ../../agent/*.h) Makefile
  
EXECS=$(BINDIR)/agent

all:	$(EXECS)

$(BINDIR)/libtcf.a : $(OFILES)
	ar -rc $@ $(OFILES)

$(BINDIR)/agent: $(BINDIR)/main.o $(BINDIR)/libtcf.a
	$(CC) $(CFLAGS) -o $@ $(BINDIR)/main.o $(BINDIR)/libtcf.a $(LIBS)

$(BINDIR)/%.o: %.c $(HFILES) $(BINDIR)
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) -c -o $@ $<

$(BINDIR)/%.o: ../../agent/%.c $(HFILES)
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -rf $(BINDIR)
