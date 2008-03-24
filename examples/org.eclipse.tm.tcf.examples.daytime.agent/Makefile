CC=gcc
CFLAGS=-O -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE -I. -I../../agent -I-

CFILES=$(basename $(wildcard *.c)) $(basename $(notdir $(wildcard ../../agent/*.c)))
OFILES=$(filter-out main%.o, $(addsuffix .o, $(CFILES)))
HFILES=$(wildcard *.h) $(wildcard ../../agent/*.h) Makefile
  
EXES=agent

UNAME=$(shell uname -o)

ifeq ($(UNAME),Cygwin)
LIBS=-lws2_32 -liphlpapi
else
LIBS=-lpthread -lrt -lelf
endif

all:	$(EXES)

agent: main.o $(OFILES)
	$(CC) $(CFLAGS) -o $@ main.o $(OFILES) $(LIBS)

%.o: %.c $(HFILES)
	$(CC) $(CFLAGS) -c -o $@ $<

%.o: ../../agent/%.c $(HFILES)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f *.o $(EXES)
