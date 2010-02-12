
OS=$(shell /bin/uname -o)

SWIG=swig
PACKAGEDIR_R=v9t9/jni/v9t9render/utils
PACKAGEDIR_G=v9t9/jni/v9t9render

ifeq "$(OS)" "Cygwin"

JAVAPATH=c:/program files/java/jdk1.6.0_18/
JAVAC="$(JAVAPATH)/bin/javac.exe"
JAR="$(JAVAPATH)/bin/jar.exe"
JAVAINC=-I"$(JAVAPATH)/include" -I"$(JAVAPATH)/include/win32"
#GCCPATH=c:/cygwin/bin
GCCPATH=c:/mingw_w64/bin
CC=$(GCCPATH)/x86_64-w64-mingw32-gcc
CFLAGS=-g  -I. $(JAVAINC) -O6 #-mno-cygwin 
LDFLAGS= -shared #-mno-cygwin -Wl,--no-allow-shlib-undefined -Wl,--kill-at -shared 
 
all: v9t9renderutils.dll libv9t9renderutils.jar  

SRCS_R=render.c v9t9render_utils_wrap.c renderRGBA.c
OBJS_R=$(SRCS_R:.c=.obj)
OBJEXT=.obj

clean:
	rm -f v9t9renderutils.dll $(OBJS_G) $(OBJS_R) v9t9render_utils_wrap.c

v9t9renderutils.dll: $(OBJS_R)
	$(CC) $(LDFLAGS) -o $@ $^ $(LIBS) 

%.obj: %.c $(wildcard *.h)
	$(CC) -c -o $@ $< $(CFLAGS)

	
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

all: libv9t9renderutils.so libv9t9renderutils.jar libv9t9render.so libv9t9render.jar v9t9render_wrap.c

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
