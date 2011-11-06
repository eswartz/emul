CONF ?= Debug

ifndef OPSYS
  OPSYS   := $(shell uname -o 2>/dev/null || uname -s)
endif
ifndef MACHINE
  MACHINE := $(shell uname -m)
endif

INCDIRS = . system/$(OPSYS) machine/$(MACHINE)
SRCDIRS = $(INCDIRS) system framework services main
BINDIR  = obj/$(OPSYS)/$(MACHINE)/$(CONF)

MKDIR   = mkdir -p $(1)
RMDIR   = rm -rf $(1)

ifeq ($(CONF),Debug)
  OPTS += -g
else
  OPTS += -O -DNDEBUG
endif

ifeq ($(OPSYS),Windows)
  CC = @$(TCF_AGENT_DIR)/bin/mcc -p $(BINDIR)/agent.pdb
  EXTOBJ = .obj
  EXTLIB = .lib
  EXTEXE = .exe
  LIBS = shell32.lib advapi32.lib Iphlpapi.lib WS2_32.lib version.lib
  EXPORT_DYNAMIC =
endif

ifeq ($(OPSYS),Cygwin)
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),Msys)
  OPTS += -mwin32
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),MinGW)
  OPTS += -mwin32
  EXTOBJ = .obj
  EXTEXE = .exe
  LIBS = -lws2_32 -liphlpapi
  MKDIR = if not exist $(subst /,\,$(1)) mkdir $(subst /,\,$(1))
  RMDIR = if exist $(subst /,\,$(1)) rmdir /s /q $(subst /,\,$(1))
endif

ifeq ($(OPSYS),Darwin)
  LIBS = -lpthread
  RANLIB = ranlib $@
endif

ifeq ($(OPSYS),GNU/Linux)
  LIBS = -lpthread -lssl -lcrypto -lrt
  ifeq ($(NO_UUID),)
    LIBS += -luuid
  endif
endif

ifneq ($(OPSYS),Windows)
  OPTS += -D_FILE_OFFSET_BITS=64
  OPTS += -Wall
  ifneq ($(CC),g++)
    OPTS += -Wmissing-prototypes
  endif
endif

# Compiler flags definition

CC		?= gcc
OUT_OBJ_F	?= -o # with a trailing space
NO_LINK_F	?= -c

# Linker definition and flags

LINK		?= $(CC)
LINK_FLAGS	?= $(CFLAGS)
LINK_OUT_F	?= $(OUT_OBJ_F)

# Archiver definition and flags

AR		?= ar
AR_FLAGS	?= -rc

EXTOBJ ?= .o
EXTLIB ?= .a
EXTEXE ?=
EXPORT_DYNAMIC ?= -rdynamic
LIBS ?= -lpthread -lssl -lrt

ifdef PATH_Plugins
  OPTS += $(EXPORT_DYNAMIC) -DPATH_Plugins="$(PATH_Plugins)"
  LIBS += -ldl
endif

ifdef OpenSSL
  OPTS += -DPATH_OpenSSL="$(OpenSSL)"
  ifeq ($(OPSYS),Windows)
    OPTS += "-I$(OpenSSL)/inc32"
    ifeq ($(CONF),Debug)
      LIBSSL = $(OpenSSL)/out32.dbg/ssleay32.lib
      LIBCRYPTO = $(OpenSSL)/out32.dbg/libeay32.lib
    else
      LIBSSL = $(OpenSSL)/out32/ssleay32.lib
      LIBCRYOTO = $(OpenSSL)/out32/libeay32.lib
    endif
    LIBS += User32.lib Gdi32.lib
  else
    OPTS += "-I$(OpenSSL)/include"
    LIBSSL = $(OpenSSL)/libssl.a
    LIBCRYPTO = $(OpenSSL)/libcrypto.a
  endif
  LIBS += $(LIBSSL) $(LIBCRYPTO)
endif

ifeq ($(OPSYS),MinGW)
  VERSION := 0.4.0
else
  VERSION := $(shell grep "%define version " $(TCF_AGENT_DIR)/main/tcf-agent.spec | sed -e "s/%define version //")
endif

INSTALLROOT ?= /tmp
INCLUDE = /usr/include
SBIN = /usr/sbin
INIT = /etc/init.d

HFILES = $(foreach dir,$(SRCDIRS),$(wildcard $(TCF_AGENT_DIR)/$(dir)/*.h))
CFILES = $(foreach fnm,$(foreach dir,$(SRCDIRS),$(wildcard $(TCF_AGENT_DIR)/$(dir)/*.c)),$(subst ^$(TCF_AGENT_DIR)/,,^$(fnm)))
OFILES = $(addprefix $(BINDIR)/,$(addsuffix $(EXTOBJ),$(basename $(filter-out main/main%,$(CFILES)))))
EXECS  = $(addprefix $(BINDIR)/,agent$(EXTEXE) client$(EXTEXE) tcfreg$(EXTEXE) valueadd$(EXTEXE) tcflog$(EXTEXE))

ifeq ($(OPSYS),Cygwin)
  CFILES += system/Windows/pthreads-win32.c
  CFILES += system/Windows/context-win32.c
endif

ifeq ($(OPSYS),Msys)
  CFILES += system/Windows/pthreads-win32.c
  CFILES += system/Windows/context-win32.c
endif

ifeq ($(OPSYS),MinGW)
  CFILES += system/Windows/pthreads-win32.c
  CFILES += system/Windows/context-win32.c
endif

ifdef SERVICES
  OPTS += $(shell $(TCF_AGENT_DIR)/bin/services-to-cflags $(SERVICES))
endif

OPTS += $(foreach dir,$(INCDIRS),-I$(TCF_AGENT_DIR)/$(dir))
