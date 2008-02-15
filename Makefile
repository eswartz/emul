CC=gcc
CFLAGS=-g -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE
OFILES=$(filter-out main%.o,$(addsuffix .o,$(basename $(wildcard *.c))))
HFILES=$(wildcard *.h)
EXES=agent client tcfreg
UNAME=$(shell uname -o)

ifeq ($(UNAME),Cygwin)
LIBS=-lpthread -lws2_32
else
LIBS=-lpthread -lrt -lelf
endif

all:	$(EXES)

agent: main.o $(OFILES)
	$(CC) $(CFLAGS) -o $@ main.o $(OFILES) $(LIBS)

client: main_client.o $(OFILES)
	$(CC) $(CFLAGS) -o $@ main_client.o $(OFILES) $(LIBS)

tcfreg: main_reg.o $(OFILES)
	$(CC) $(CFLAGS) -o $@ main_reg.o $(OFILES) $(LIBS)

%.o: %.c $(HFILES)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f *.o $(EXES)
