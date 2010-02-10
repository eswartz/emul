
OS=$(shell /bin/uname -o)

SWIG=swig
PACKAGEDIR_R=v9t9/jni/v9t9render/utils
PACKAGEDIR_G=v9t9/jni/v9t9render

ifeq "$(OS)" "Cygwin"

GCCPATH=c:/cygwin/bin
JAVAPATH=c:/program files/java/jdk1.6.0_11/
JAVAC="$(JAVAPATH)/bin/javac.exe"
JAR="$(JAVAPATH)/bin/jar.exe"
JAVAINC=-I"$(JAVAPATH)/include" -I"$(JAVAPATH)/include/win32"
CFLAGS=-g  -I. $(JAVAINC) -O6 
LIBS=

all: v9t9renderutils.dll libv9t9renderutils.jar

SRCS=render.c v9t9render_utils_wrap.c renderRGBA.c
OBJS=$(SRCS:.c=.obj)
OBJEXT=.obj

clean:
	rm -f v9t9renderutils.dll $(OBJS) v9t9render_utils_wrap.c

v9t9renderutils.dll: $(OBJS)
	$(GCCPATH)/gcc -mno-cygwin -Wl,--no-allow-shlib-undefined -Wl,--kill-at -shared -o $@ $^ $(LIBS) -lm 

%.obj: %.c $(wildcard *.h)
	$(GCCPATH)/gcc -mno-cygwin -c -o $@ $< $(CFLAGS)

	
else 	# Linux/OS X/etc

JAVAC=javac
JAR=jar

CFLAGS_R=-I/usr/lib/jvm/java-6-sun/include/ \
	-I/usr/lib/jvm/java-6-sun/include/linux \
	-g -O6
LIBS_R= 
SRCS_R=render.c v9t9render_utils_wrap.c  renderRGBA.c
OBJS_R=$(SRCS_R:.c=.o)
OBJEXT=.o

all: libv9t9renderutils.so libv9t9renderutils.jar libv9t9render.so libv9t9render.jar

CFLAGS_G=$(shell pkg-config gtk+-2.0 --cflags) \
        	-I../libanalogtv -I/usr/lib/jvm/java-6-openjdk/include/ \
        	$(shell pkg-config gtkglext-1.0 --cflags) \
        	-g -O6
LIBS_G=$(shell pkg-config gtk+-2.0 --libs) \
        	$(shell pkg-config gtkglext-x11-1.0 --libs) \
        	-L../libanalogtv -lanalogtv 
        
SRCS_G=render-gtk.c render-ogl.c v9t9render_wrap.c
OBJS_G=$(SRCS_G:.c=.o)

clean:
	rm -f libv9t9renderutils.so libv9t9render.so libv9t9renderutils.jar libv9t9render.jar \
		$(OBJS_R) $(OBJS_G) v9t9render_utils_wrap.c v9t9render_wrap.c

libv9t9renderutils.so: $(OBJS_R)
	gcc -fPIC -Wl,-zdefs -shared -o $@ $^ $(LIBS_R) -lm 

libv9t9render.so: $(OBJS_G)
	gcc -fPIC -Wl,-zdefs -shared -o $@ $^ $(LIBS_G) -lm 

%.o: %.c $(wildcard *.h)
	gcc -fPIC  -c -o $@ $< $(CFLAGS_R) $(CFLAGS_G)

endif	# linux

v9t9render_utils_wrap.c: v9t9render_utils.i
	rm -rf swig; mkdir -p swig/$(PACKAGEDIR_R)
	$(SWIG) -I. -java -outdir swig/$(PACKAGEDIR_R) -package v9t9.jni.v9t9render.utils v9t9render_utils.i

v9t9render_wrap.c: v9t9render.i
	rm -rf swig; mkdir -p swig/$(PACKAGEDIR_G)
	$(SWIG) -I. -java -outdir swig/$(PACKAGEDIR_G) -package v9t9.jni.v9t9render v9t9render.i

libv9t9renderutils.jar: v9t9render_utils_wrap.c
	( cd swig && $(JAVAC) -target 1.5 $(PACKAGEDIR_R)/*.java && $(JAR) cvf ../$@ $(PACKAGEDIR_R)/*.class )
libv9t9render.jar: v9t9render_wrap.c
	( cd swig && $(JAVAC) -target 1.5 $(PACKAGEDIR_G)/*.java && $(JAR) cvf ../$@ $(PACKAGEDIR_G)/*.class )
	
ifeq "0" "1"


        CFLAGS=$(shell pkg-config gtk+-2.0 --cflags) \
        	-I../libanalogtv -I/usr/lib/jvm/java-6-openjdk/include/ \
        	$(shell pkg-config gtkglext-1.0 --cflags) \
        	-g #-O6
        LIBS=$(shell pkg-config gtk+-2.0 --libs) \
        	$(shell pkg-config gtkglext-x11-1.0 --libs) \
        	-L../libanalogtv -lanalogtv 
        
        all: libv9t9render.so libv9t9render.jar
        
        SRCS=render-gtk.c render-ogl.c v9t9render_wrap.c
        OBJS=$(SRCS:.c=.o)
        
        PACKAGEDIR=v9t9/jni/v9t9render
        clean:
        	rm -f libv9t9render.so $(OBJS)
        
        v9t9render_wrap.c: v9t9render.i
        	rm -rf swig; mkdir -p swig/$(PACKAGEDIR)
        	swig -I. -java -outdir swig/$(PACKAGEDIR) -package v9t9.jni.v9t9render v9t9render.i
        	
        libv9t9render.so: $(OBJS)
        	gcc -fPIC -Wl,-zdefs -shared -o $@ $^ $(LIBS) -lm 
        
        %.o: %.c $(wildcard *.h)
        	gcc -fPIC  -c -o $@ $< $(CFLAGS)
        
        libv9t9render.jar: v9t9render_wrap.o
        	( cd swig && javac -target 1.5 $(PACKAGEDIR)/*.java && jar cvf ../libv9t9render.jar $(PACKAGEDIR)/*.class )
endif
