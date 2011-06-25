
AUTOMAKE_OPTIONS = foreign 
TOP = $(top_srcdir)
SRC = $(TOP)/source

noinst_LIBRARIES = libiberty.a

CC = @CC@
LD = @LD@
CFLAGS = $(INCS) @CFLAGS@ $(DEFS)
LDFLAGS = @LDFLAGS@
ARFLAGS = @ARFLAGS@

libiberty_a_SOURCES = fnmatch.c dirent.c getopt.c getopt1.c
EXTRA_DIST = fnmatch.c dirent.c dirent.h getopt.c getopt1.c

%.$(OBJEXT): %.c
	$(COMPILE) -c $< -o $@

%.$(OBJEXT): %.cc
	$(CXXCOMPILE) -c $< -o $@

%.$(OBJEXT): %.cpp
	$(CXXCOMPILE) -c $< -o $@
