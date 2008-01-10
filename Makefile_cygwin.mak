CC=gcc -mconsole -mwin32 -mno-cygwin
#CFLAGS=-O2 -DWIN32 -DNDEBUG -D_CONSOLE -D_MBCS
CFLAGS=-g -DWIN32 -D_DEBUG -D_CONSOLE -D_MBCS
OFILES=$(addsuffix .o,$(basename $(wildcard *.c)))
HFILES=$(wildcard *.h)

all: agent

agent: $(OFILES)
	$(CC) $(CFLAGS) -o $@ $(OFILES) -lkernel32 -luser32 -lshell32 -lWS2_32 -lIphlpapi

%.o: %.c $(HFILES)
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f *.o agent
