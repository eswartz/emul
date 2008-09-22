CONF=Debug

CC=gcc
ifeq ($(CONF),Debug)
CFLAGS=-g
else
CFLAGS=-O
endif
CFLAGS:=$(CFLAGS) -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE -Wmissing-prototypes 

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

OFILES=$(addprefix $(BINDIR)/,$(filter-out main%.o,$(addsuffix .o,$(basename $(wildcard *.c)))))
HFILES=$(wildcard *.h) Makefile
EXECS=$(BINDIR)/agent $(BINDIR)/client $(BINDIR)/tcfreg $(BINDIR)/valueadd $(BINDIR)/tcflog

all:	$(EXECS)

$(BINDIR)/libtcf.a : $(OFILES)
	ar -rc $@ $(OFILES)

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

$(BINDIR)/%.o: %.c $(HFILES)
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -rf $(BINDIR)
