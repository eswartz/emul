AUTOMAKE_OPTIONS = foreign

noinst_LIBRARIES = libMacOS.a

libMacOS_a_SOURCES = 
libMacOS_a_SRCS = @MACMODSRCS@
libMacOS_a_LIBADD = $(libMacOS_a_SRCS:%.c=%.$(OBJEXT))

TOP = $(top_srcdir)
SRC = $(TOP)/source
INCS = -I$(SRC) -I$(SRC)/OSLib -I$(SRC)/Modules -I$(TOP)
localedir = @prefix@/share/locale
DEFS = @DEFS@ -DLOCALEDIR=\"$(localedir)\"
CFLAGS = $(INCS) @CFLAGS@ $(DEFS)

LIBS = $(libMacOS_a_LIBADD) @LIBS@





