
OS=$(shell uname -o)

ifeq "$(OS)" "Cygwin"

#GCCPATH=c:/cygwin/bin/
#CC=$(GCCPATH)/gcc
GCCPATH=c:/mingw_w64/bin
CC=$(GCCPATH)/x86_64-w64-mingw-gcc
JAVAPATH=c:/program files/java/jdk1.6.0_18/
JAVAC="$(JAVAPATH)/bin/javac.exe"
JAR="$(JAVAPATH)/bin/jar.exe"
CFLAGS=-DGETTIMEOFDAY_TWO_ARGS
JAVAINC=-I"$(JAVAPATH)/include" -I"$(JAVAPATH)/include/win32"
LDFLAGS=
SOEXT=dll
else


JAVAC=javac
JAR=jar
JAVAINC=-I/usr/lib/jvm/java-6-sun/include/ \
	-I/usr/lib/jvm/java-6-sun/include/linux
LDFLAGS=-fPIC  -Wl,-zdefs 
CFLAGS=-fPIC -DGETTIMEOFDAY_TWO_ARGS
SOEXT=so

endif

CFLAGS+=-g  -I. $(JAVAINC) -O6 

all: swig libanalogtv.$(SOEXT) libanalogtv.jar

clean:
	rm -f libanalogtv.jar libanalogtv.so analogtv.o yarandom.o analogtv_wrap.o analogtv_wrap.c

analogtv_wrap.c swig: analogtv.i $(wildcard *.h)
	rm -rf swig; mkdir -p swig/org/xorg/analogtv
	swig -I. -java -outdir swig/org/xorg/analogtv -package org.xorg.analogtv analogtv.i
	
libanalogtv.$(SOEXT): analogtv.o yarandom.o analogtv_wrap.o
	gcc $(LDFLAGS) -shared -o $@ $^ -lm

%.o: %.c  $(wildcard *.h)
	gcc -c -o $@ $< $(CFLAGS)

libanalogtv.jar: analogtv_wrap.c
	( cd swig && \
	$(JAVAC) -target 1.5 org/xorg/analogtv/*.java \
	&& $(JAR) cvf ../libanalogtv.jar org/xorg/analogtv/*.class )
	