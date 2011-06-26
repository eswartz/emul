AC_INIT(Makefile.in)

AC_CANONICAL_HOST
HOST=$host
AC_SUBST(HOST)
AM_CONFIG_HEADER(./[$host]/config.h)
mkdir -p $host
cp config.h.in ./$host/config.h.in

#echo -e "host is $host\n";
#if test "x$host" = "xarm-linux" ; then
#configh=$host/config.h
#AM_CONFIG_HEADER(`echo $configh`)
#if [ -f $host ] ; then
#rm -f $host
#fi
#mkdir -p $host
#cp config.h.in $host/
#OBJEXT=arm.o
#EXEEXT=-arm
#else
#AM_CONFIG_HEADER(config.h)
#fi

dnl get top-level directory
if test "x$TOP" = "x" ; then
TOP=`pwd`
fi
AC_SUBST(TOP)
CPPFLAGS="$CPPFLAGS -I$TOP/$host"

VERSION=`cat $TOP/version`
AM_INIT_AUTOMAKE(v9t9, $VERSION)

AC_OPERATING_SYSTEM

dnl get compiler flags to pass to child directories
MAPFLAGS="-Wl,-cref -Wl,-Map -Wl,@OUTFILE@.map"
ARFLAGS="rcsu"
DEPFLAGS="-MD"

AC_SUBST(DEPFLAGS)
AC_SUBST(MAPFLAGS)
AC_SUBST(ARFLAGS)

dnl some common programs
AC_PROG_INSTALL
AC_PROG_MAKE_SET
AC_PROG_CC
AC_PROG_CXX

# reset extensions
OBJEXT=$host.o
#EXEEXT=-$host
EXEEXT=


AC_PROG_YACC
AM_PROG_LEX
AC_PROG_MAKE_SET
AC_MSG_CHECKING(perl)
if perl -e print; then PERL=perl;
elif /usr/bin/perl -e print &>/dev/null; then PERL=/usr/bin/perl;
elif /usr/local/bin/perl -e print &>/dev/null; then PERL=/usr/local/bin/perl;
else PERL="not found, you will need to hand-edit source/gtkcallbacks.c if you change v9t9.glade"
fi
if test "x$PERL" != "x"; then
AC_MSG_RESULT($PERL)
else
PERL=true
fi
AC_SUBST(PERL)

dnl checks for OS features
#AC_CYGWIN # checked above
AC_OBJEXT
AC_SYS_LONG_FILE_NAMES

dnl Checks for header files.
AC_HEADER_DIRENT
AC_HEADER_STDC
AC_HEADER_SYS_WAIT
AC_CHECK_HEADERS(fcntl.h limits.h malloc.h sys/ioctl.h sys/time.h unistd.h endian.h)
dnl Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_TYPE_PID_T
AC_TYPE_SIZE_T
AC_HEADER_TIME
AC_STRUCT_TM

dnl Checks for library functions.
AC_FUNC_ALLOCA
AC_FUNC_FNMATCH
AC_PROG_GCC_TRADITIONAL
AC_FUNC_MEMCMP
AC_TYPE_SIGNAL
AC_FUNC_UTIME_NULL
AC_FUNC_VPRINTF
AC_CHECK_FUNCS(getcwd gethostname mkdir rmdir select strcspn strdup strerror strstr strtol strcasecmp strncasecmp strupr swab gettext getpwd)


AC_SUBST(V9T9SRCS)
AC_SUBST(INCDIRS)
AC_SUBST(MODSRCS)
AC_SUBST(UNIXMODSRCS)
AC_SUBST(WINMODSRCS)
AC_SUBST(MACMODSRCS)
AC_SUBST(OSLIBSRCS)

dnl find gforth
AM_CONDITIONAL(GFORTH_BUILD_IS_BROKEN, true)

AC_CHECK_PROG(GFORTH, gforth, gforth)
if test -z "$GFORTH" ; then
	AC_MSG_RESULT(gforth version 0.4.0 or later must be installed to build v9t9 forth)
fi
AM_CONDITIONAL(HAVE_GFORTH, test "x$GFORTH" != "x")

dnl check for developer option
AC_ARG_ENABLE(developer,
[  --enable-developer   Turn on developer dump information building v9t9 forth],
[case "${enableval}" in
	yes) develop=true ;;
	no)	develop=false ;;
	*) AC_MSG_ERROR(bad value ${enableval} for --enable-developer) ;;
esac],[develop=true])

if test x$develop != xfalse ; then
	export DUMP="&>/dev/null"
else
	export DUMP=
fi
AC_SUBST(DUMP)

AC_PROG_INSTALL

AC_OUTPUT(Makefile 
	DSRs/Makefile TIAsm/Makefile \
	FileUtils/Makefile \
	Forth/Makefile )	


