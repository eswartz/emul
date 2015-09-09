
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
	CC=gcc -m32
	else
	ARCH=64
	CC=gcc -m64
	endif

	CC=gcc -m$(ARCH)
	CFLAGS=-g -O6 -fPIC  
	LDFLAGS=-fPIC -Wl,-zdefs -shared
	LIBS=-lm
	OBJEXT=-$(ARCH).o
	LIB=lib
	
	SOEXT=$(ARCH).so

endif

TARGET=../../hrtlib$(SOEXT)

all: $(TARGET)
clean: 
	rm $(TARGET)
	
PREFIX=com_vladium_utils_timing
JAVAHOME=c:/program files/java/jdk1.8.0_51

$(TARGET): $(PREFIX)_HRTimer.c $(PREFIX)_HRTimer.h
	$(CC) $(LDFLAGS) $(CFLAGS) -o $@ $(PREFIX)_HRTimer.c -I"$(JAVAHOME)/include" \
	"-I$(JAVAHOME)/include/win32" \
	 -lwinmm -Wl,--kill-at -DNDEBUG
