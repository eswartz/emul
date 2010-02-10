
CFLAGS=-DGETTIMEOFDAY_TWO_ARGS -I/usr/lib/jvm/java-6-openjdk/include/ \
	-I/usr/lib/jvm/java-6-sun/include \
	-I/usr/lib/jvm/java-6-sun/include/linux -g 

all: swig libanalogtv.so libanalogtv.jar

clean:
	rm -f libanalogtv.jar libanalogtv.so analogtv.o yarandom.o analogtv_wrap.o analogtv_wrap.c

analogtv_wrap.c: analogtv.i $(wildcard *.h)
	rm -rf swig; mkdir -p swig/org/xorg/analogtv
	swig -I. -java -outdir swig/org/xorg/analogtv -package org.xorg.analogtv analogtv.i
	
libanalogtv.so: analogtv.o yarandom.o analogtv_wrap.o
	gcc -fPIC -Wl,-zdefs -shared -o libanalogtv.so $^ -lm

%.o: %.c  $(wildcard *.h)
	gcc -fPIC  -c -o $@ $< $(CFLAGS)

swig:
	mkdir -p swig
	
libanalogtv.jar: analogtv.i
	( cd swig && javac -target 1.5 org/xorg/analogtv/*.java && jar cvf ../libanalogtv.jar org/xorg/analogtv/*.class )
	