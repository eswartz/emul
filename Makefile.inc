CONF = Debug

OPSYS = $(shell uname -o 2>/dev/null || uname -s)
MACHINE = $(shell uname -m)

INCDIRS = . system/$(OPSYS) machine/$(MACHINE)
SRCDIRS = $(INCDIRS) system framework services main
BINDIR  = obj/$(OPSYS)/$(MACHINE)/$(CONF)

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
  LIBS = shell32.lib advapi32.lib Iphlpapi.lib WS2_32.lib
  ifeq ($(MACHINE),i686)
    MACHINE = i386
  endif
endif

ifeq ($(OPSYS),Cygwin)
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),Msys)
  CC = gcc
  OPTS += -mwin32
  LIBS = -lws2_32 -liphlpapi
endif

ifeq ($(OPSYS),Darwin)
  LIBS = -lpthread
  RANLIB = ranlib $@
endif

ifneq ($(OPSYS),Windows)
  OPTS += -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_GNU_SOURCE
  OPTS += -Wall
  ifneq ($(CC),icc)
    OPTS += -Wno-parentheses
  endif
  ifneq ($(CC),g++)
    OPTS += -Wmissing-prototypes
  endif
endif

CC ?= gcc
AR ?= ar
EXTOBJ ?= .o
EXTLIB ?= .a
EXTEXE ?=
EXPORT_DYNAMIC ?= -rdynamic
LIBS ?= -lpthread -lssl -lrt

ifdef PATH_Plugins
  OPTS += $(EXPORT_DYNAMIC) -DPATH_Plugins="$(PATH_Plugins)"
  LIBS += -ldl
endif

VERSION = $(shell grep "%define version " $(TCF_AGENT_DIR)/main/tcf-agent.spec | sed -e "s/%define version //")
INSTALLROOT ?= /tmp
SBIN = /usr/sbin
INIT = /etc/init.d

HFILES = $(foreach dir,$(SRCDIRS),$(wildcard $(TCF_AGENT_DIR)/$(dir)/*.h))
CFILES = $(foreach fnm,$(foreach dir,$(SRCDIRS),$(wildcard $(TCF_AGENT_DIR)/$(dir)/*.c)),$(subst ^$(TCF_AGENT_DIR)/,,^$(fnm)))
OFILES = $(addprefix $(BINDIR)/,$(addsuffix $(EXTOBJ),$(basename $(filter-out main/main%,$(CFILES)))))
EXECS  = $(addprefix $(BINDIR)/,agent$(EXTEXE) client$(EXTEXE) tcfreg$(EXTEXE) valueadd$(EXTEXE) tcflog$(EXTEXE))

ifdef LUADIR
  EXECS += $(BINDIR)/tcflua
  OPTS += -DPATH_LUA="$(LUADIR)" "-I$(LUADIR)/include"
endif

ifdef SERVICES
  OPTS += $(shell $(TCF_AGENT_DIR)/bin/services-to-cflags $(SERVICES))
endif

OPTS += $(foreach dir,$(INCDIRS),-I$(TCF_AGENT_DIR)/$(dir))

