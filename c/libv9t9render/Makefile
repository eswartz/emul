
OS=$(shell /bin/uname -o)

ifeq "$(OS)" "Cygwin"

# For Windows builds, we expect Cygwin to launch 'make' and MinGW to build the library.

#GCCPATH=c:/cygwin/bin
ifeq "$(PROCESSOR_ARCHITEW6432)" "AMD64"
GCCPATH=c:/mingw_w64/bin
CC=$(GCCPATH)/x86_64-w64-mingw32-gcc
else
GCCPATH=c:/mingw/bin
CC=$(GCCPATH)/mingw32-gcc
endif
CFLAGS=-g  -I. $(JAVAINC) -O6 #-mno-cygwin 
LDFLAGS=-shared #-mno-cygwin -Wl,--no-allow-shlib-undefined -Wl,--kill-at -shared 
SOEXT=.dll
OBJEXT=.obj
LIB=
	
else 	# Linux/OS X/etc

JAVAC=javac
JAR=jar

CC=gcc
CFLAGS=-g -O6 -fPIC  
LDFLAGS=-fPIC -Wl,-zdefs -shared --kill-at		
LIBS=-lm
OBJEXT=.o
SOEXT=.so
LIB=lib

endif

CFLAGS += -DGETTIMEOFDAY_TWO_ARGS

all: $(LIB)v9t9render$(SOEXT) 

SRCS=render.c renderRGBA.c analogtv.c yarandom.c v9t9-analogtv.c
OBJS=$(SRCS:.c=$(OBJEXT))

clean:
	rm -f v9t9render$(SOEXT) $(OBJS) 

$(LIB)v9t9render$(SOEXT): $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $^ $(LIBS) 

%$(OBJEXT): %.c $(wildcard *.h)
	$(CC) -c -o $@ $< $(CFLAGS)

