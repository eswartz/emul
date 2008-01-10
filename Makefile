CC=gcc
CFLAGS=-g -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE
OFILES=$(addsuffix .o,$(basename $(wildcard *.c)))
HFILES=$(wildcard *.h)

all: agent

agent: $(OFILES)
	$(CC) $(CFLAGS) -o $@ $(OFILES) -lpthread -lrt -lelf

%.o: %.c $(HFILES)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f *.o agent
