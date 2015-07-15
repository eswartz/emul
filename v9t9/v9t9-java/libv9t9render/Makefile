
OS=$(shell /bin/uname -o)

ifeq "$(OS)" "Cygwin"
WIN=1
endif
ifeq "$(OS)" "Msys"
WIN=1
endif

ifeq "$(WIN)" "1"
	# For Windows builds, we expect Cygwin to launch 'make' and MinGW to build the library.
	
	ifeq "$(PROCESSOR_ARCHITEW6432)" "AMD64"
	ARCH=64
	else
	ARCH=32
	endif
	
	#GCCPATH=c:/cygwin/bin
	ifeq "$(ARCH)" "64"
		GCCPATH=c:/mingw_w64/bin
		CC=$(GCCPATH)/x86_64-w64-mingw32-gcc
	else
		#GCCPATH=c:/mingw/bin
		#CC=$(GCCPATH)/mingw32-gcc
		CC=gcc -m32
	endif
	
	CFLAGS=-g  -I. $(JAVAINC) -O6 #-mno-cygwin 
	LDFLAGS=-shared #-mno-cygwin -Wl,--no-allow-shlib-undefined -Wl,--kill-at -shared 
	OBJEXT=-$(ARCH).obj
	LIB=
	SOEXT=$(ARCH).dll
	
else 	# Linux/OS X/etc

	M=$(shell /bin/uname -m)
	ifeq "$(M)" "i686"
	ARCH=32
	else
	ARCH=64
	endif

	JAVAC=javac
	JAR=jar
	
	CC=gcc
	CFLAGS=-g -O6 -fPIC  
	LDFLAGS=-fPIC -Wl,-zdefs -shared
	LIBS=-lm
	OBJEXT=-$(ARCH).o
	LIB=lib
	
	ARCH=$(shell /bin/uname -m)
	
	SOEXT=$(ARCH).so

endif

CFLAGS += -DGETTIMEOFDAY_TWO_ARGS

all: $(LIB)v9t9render$(SOEXT) 

SRCS=render.c renderRGBA.c analogtv.c yarandom.c v9t9-analogtv.c noisy.c
OBJS=$(SRCS:.c=$(OBJEXT))

clean:
	rm -f v9t9render$(SOEXT) $(OBJS) 

$(LIB)v9t9render$(SOEXT): $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $^ $(LIBS) 

%$(OBJEXT): %.c $(wildcard *.h)
	$(CC) -c -o $@ $< $(CFLAGS)

