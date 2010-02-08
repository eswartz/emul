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

VERSION=`cat $TOP/.version`
AM_INIT_AUTOMAKE(v9t9, $VERSION)

dnl get compiler flags to pass to child directories
MAPFLAGS="-Wl,-cref -Wl,-Map -Wl,@OUTFILE@.map"
ARFLAGS="rcsu"
DEPFLAGS="-MD"
AC_PROG_MWCC

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

AM_GNU_GETTEXT
AC_SUBST(HAVE_GETTEXT)

ALL_LINGUAS=""
AC_SUBST(ALL_LINGUAS)
CATALOGS=""
AC_SUBST(CATALOGS)

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

dnl Check for host OS -- this is a generic check only
AC_OPERATING_SYSTEM

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

dnl typical modules
MODSRCS="$MODSRCS video_null.c keyboard_null.c sound_null.c"
MODSRCS="$MODSRCS dsr_emudisk.c dsr_realdisk.c dsr_rs232.c dsr_emupio.c"
AC_DEFINE(NULL_VIDEO)
AC_DEFINE(NULL_KEYBOARD)
AC_DEFINE(NULL_SOUND)
AC_DEFINE(EMU_DISK_DSR)
AC_DEFINE(REAL_DISK_DSR)
AC_DEFINE(REAL_RS232_DSR)
AC_DEFINE(EMU_PIO_DSR)

dnl get OS-dependent modules
if test "x$HOSTOS" = "xUnix"; then

V9T9SRCS="$V9T9SRCS unixmain.c"
INCDIRS="$INCDIRS $TOP/source/Modules/Unix"
OSLIBSRCS="$OSLIBSRCS Posix.c"
LIBS="$LIBS -lpthread"
UNIXMODSRCS="$UNIXMODSRCS dsr_rs232_unix.c"

elif test "x$HOSTOS" = "xWin32"; then

AC_DEFINE(USE_MM_TIMER)
AC_DEFINE(WIN32_VIDEO) AC_DEFINE(WIN32_KEYBOARD) 
AC_DEFINE(WIN32_SOUND) AC_DEFINE(USE_MM_TIMER)
if test "x$CC" = "xgcc" ; then
CFLAGS="$CFLAGS -fnative-struct"
CXXFLAGS="$CFLAGS -fnative-struct"
fi
V9T9SRCS="$V9T9SRCS winmain.c fnmatch.c"
INCDIRS="$INCDIRS $TOP/source/Modules/Win32"
OSLIBSRCS="$OSLIBSRCS Win32.c"
WINMODSRCS="$WINMODSRCS dsr_rs232_win32.c video_win32.c video_win32_drawdib.c video_win32_ddraw.cc keyboard_win32.c sound_win32.c"
LIBS="$LIBS -lddraw -ldxguid -lmsvfw32 -lwinmm -luser32 -lgdi32"

elif test "x$HOSTOS" = "xMacOS"; then

V9T9SRCS="$V9T9SRCS macosmain.c"
INCDIRS="$INCDIRS $TOP/source/Modules/MacOS"
OSLIBSRCS="$OSLIBSRCS MacOS.c"

fi

dnl Checks for libraries.
AC_CHECK_LIB(readline, readline,
	[LIBS="$LIBS -lreadline -ltermcap"; AC_DEFINE(HAVE_READLINE)],,-ltermcap)
AC_CHECK_LIB(png, png_create_write_struct,
	[LIBS="$LIBS -lpng -lz"; AC_DEFINE(WITH_LIB_PNG)],,-lz)

dnl check for SVGAlib is more complex due to broken libs
dnl which need to see __global_sti and __global_cli
AC_CHECK_LIB(vga, vga_init, 
	AC_DEFINE(LINUX_SVGA_VIDEO) AC_DEFINE(LINUX_SVGA_KEYBOARD)
	LIBS="$LIBS -lvga $TOP/source/__global_cli_sti.c"
	V9T9SRCS="$V9T9SRCS svgaloop.c"
	UNIXMODSRCS="$UNIXMODSRCS keyboard_svgalib.c video_svgalib.c",
	[], 
	[$TOP/source/__global_cli_sti.c])
AC_CHECK_HEADERS(linux/soundcard.h, 
	AC_DEFINE(OSS_SOUND)
	UNIXMODSRCS="$UNIXMODSRCS sound_oss.c")
AC_CHECK_HEADERS(alsa/asoundlib.h, 
	AC_DEFINE(ALSA_SOUND)
	UNIXMODSRCS="$UNIXMODSRCS sound_alsa.c"
	LIBS="$LIBS -lasound")
AC_CHECK_HEADERS(sys/kd.h, 
	AC_DEFINE(LINUX_SPEAKER_SOUND)
	UNIXMODSRCS="$UNIXMODSRCS sound_speaker.c")
AC_CHECK_HEADERS(esd.h,
	AC_DEFINE(ESD_SOUND)
	UNIXMODSRCS="$UNIXMODSRCS sound_esd.c"
	LIBS="$LIBS -lesd")

AC_PATH_X
if test "$no_x" = ""; then
AC_DEFINE(X_WIN_VIDEO)
AC_DEFINE(X_WIN_KEYBOARD)
V9T9SRCS="$V9T9SRCS xlibloop.c"
UNIXMODSRCS="$UNIXMODSRCS video_X.c keyboard_X.c"
INCS="$INCS $x_includes"
LIBS="$LIBS -L$x_libraries -Wl,--export-dynamic -lX11 -lXext -lXi"
fi

###################

# check user preferences for GTK or GNOME
AC_ARG_WITH(gnome2,
[  --with-gnome2[=ARG]      use the GNOME2 interface (ARG=yes)],
[ 
if test "$withval" = "yes"; then
	want_gnome2=yes
else
	want_gnome2=no
fi],
[
	want_gnome2=no  # not default
]
)

AC_ARG_WITH(gtk,
[  --with-gtk[=ARG]        use the GTK interface (ARG=yes)],
[ 
if test "$withval" = "yes"; then
	want_gtk=yes
	want_gnome2=no	# conflicts
else
	want_gtk=no
fi],
[
	want_gtk=default
]
)

###################

AC_CHECK_PROG(HAVE_PKG_CONFIG, pkg-config, 1)

##################

# GNOME 2.0

if test '( "$HOSTOS" = "Unix" && "$no_x" = "" )' || test "$HOSTOS" = "Win32" ; then

# sources/includes
GNOMESRCS="gnomecallbacks.c gnomeinterface.c gnomeloop.c gnomesupport.c gnome_v99filesel.c"
GNOMEMODSRCS="keyboard_gnome.c video_gnome.c"

if test "$want_gnome2" != "no" ; then
	# check for configuration
	printf "checking for GNOME2... "
	has_gnome2=yes
	if test "x$HAVE_PKG_CONFIG" = "x1" ;  then
		GNOMEFLAGS="`pkg-config --cflags libgnomeui-2.0`"
		GNOMELIBS="`pkg-config --libs libgnomeui-2.0` -lstdc++"
	fi

	if test "x$GNOMEFLAGS" = "x" ; then
		echo "no"
		if test "$want_gnome2" != "default" ;  then
			AC_MSG_ERROR(GNOME2 not found or installed.)
		fi
		has_gnome2=no
	fi

	if test "$has_gnome2" = "yes"; then
		echo "yes"
		AC_DEFINE(GTK_KEYBOARD) AC_DEFINE(GTK_VIDEO)
		V9T9SRCS="$V9T9SRCS $GNOMESRCS" MODSRCS="$MODSRCS $GNOMEMODSRCS" LIBS="$LIBS $GNOMELIBS"
		CFLAGS="$CFLAGS $GNOMEFLAGS"
		CXXFLAGS="$CXXFLAGS $GNOMEFLAGS"
	fi
else
	has_gnome2=no
fi

AM_CONDITIONAL(HAVE_GNOME, test "x$has_gnome2" == "yes")
if test "$has_gnome2" = "yes"; then
AC_DEFINE(HAVE_GNOME)
AC_DEFINE(HAVE_GTK)
fi

else	# hostos==unix, hostos==win32

AM_CONDITIONAL(HAVE_GNOME, false)
AM_CONDITIONAL(HAVE_GTK, false)

fi  # hostos==unix, hostos==win32

###################

# GTK 1.3

if test '( "$HOSTOS" = "Unix" && "$no_x" = "" )' || test "$HOSTOS" = "Win32" ; then

# sources
GTKSRCS="gtkcallbacks.c gtkinterface.c gtkloop.c gtksupport.c gtk_v99filesel.c"
GTKMODSRCS="keyboard_gtk.c video_gtk.c"

if test "$want_gtk" != "no" && test "$has_gnome2" != "yes"; then

# check for configuration

	echo "checking for GTK... "
	has_gtk=yes
	if test "x$HAVE_PKG_CONFIG" = "x1" ;  then
		GTKFLAGS="`pkg-config --cflags gtk+`"
		GTKLIBS="`pkg-config --libs gtk+`"
	fi

	if test "x$GTKFLAGS" = "x"; then
		AC_CHECK_PROG(HAVE_GTK_CONFIG, gtk-config, 1)
		if test "x$HAVE_GTK_CONFIG" = "x1" ; then
			GTKFLAGS="`gtk-config --cflags`"
			GTKLIBS="`gtk-config --libs`"
		fi
	fi

	if test "x$GTKFLAGS" = "x" ; then
		echo "no"
		if test "$want_gtk" != "default" ;  then
			AC_MSG_ERROR(GTK not found or not installed.)
		fi
		has_gtk=no
	fi

	if test "$has_gtk" = "yes"; then
		echo "yes"
		AC_DEFINE(GTK_KEYBOARD) AC_DEFINE(GTK_VIDEO)
		GTKFLAGS="$GTKFLAGS -I/usr/include/gdk-pixbuf-1.0"
		GTKLIBS="$GTKLIBS -lgdk_pixbuf"
		V9T9SRCS="$V9T9SRCS $GTKSRCS" MODSRCS="$MODSRCS $GTKMODSRCS" LIBS="$LIBS $GTKLIBS"
		CFLAGS="$CFLAGS $GTKFLAGS"
		CXXFLAGS="$CXXFLAGS $GTKFLAGS"
	fi
fi

AM_CONDITIONAL(HAVE_GTK, test "x$has_gtk" == "yes")
if test "$has_gtk" = "yes"; then
AC_DEFINE(HAVE_GTK)
fi

else	# hostos==unix, hostos==win32

AM_CONDITIONAL(HAVE_GTK, false)

fi  # hostos==unix, hostos==win32

###############################

# Qt Embedded

AC_LANG_PUSH(C++)
oldcxxflags=$CXXFLAGS
CXXFLAGS="$CXXFLAGS -I$QTDIR/include -I$QPEDIR/include"
AC_TRY_COMPILE([#include <qpe/qpeapplication.h>],
[int argc; char **argv; QPEApplication q(argc, argv);],
[
has_qte=yes
QTESRCS="qteloop.cc moc_qtehandler.cc"
QTEMODSRCS="video_qte.cc keyboard_qte.cc"
QTEFLAGS="-DQWS -I$QTDIR/include -I$QPEDIR/include -I$QPEDIR/include/qpe -fno-rtti -fno-exceptions"
QTELDFLAGS="-L$QTDIR/lib -Wl,--noinhibit-exec"
QTELIBS="-lqpe -lqte -ljpeg"
]
)
CXXFLAGS=$oldcxxflags
AC_LANG_POP()

# check for explicit arguments
AC_ARG_WITH(qte,
[  --with-qte[=ARG]        use the Qt/Embedded interface (ARG=yes)],
[if test "$withval" = "yes"; then
if test "$has_qte" = "yes"; then
AC_DEFINE(QTE_VIDEO) AC_DEFINE(QTE_KEYBOARD)
V9T9SRCS="$V9T9SRCS $QTESRCS" MODSRCS="$MODSRCS $QTEMODSRCS" LIBS="$LIBS $QTELIBS"
else
AC_MSG_ERROR(QT/Embedded not found or configured.)
fi
else
QTESRCS=""
QTEMODSRCS=""
QTELIBS=""
HAVE_QTE=0
has_qte=no
fi],
[
if test "$has_qte" = "yes"; then
AC_DEFINE(QTE_KEYBOARD) AC_DEFINE(QTE_VIDEO)
fi
])

AM_CONDITIONAL(HAVE_QTE, test "x$has_qte" == "yes")
if test "$has_qte" = "yes"; then
AC_DEFINE(HAVE_QTE)
AC_MSG_RESULT(QT/Embedded + Qtopia in $QTDIR, $QPEDIR)
CFLAGS="$CFLAGS $QTEFLAGS"
INCDIRS="$INCDIRS $QTEINCDIRS"
CXXFLAGS="$CXXFLAGS $QTEFLAGS"
LDFLAGS="$LDFLAGS $QTELDFLAGS"
V9T9SRCS="$V9T9SRCS $QTESRCS" MODSRCS="$MODSRCS $QTEMODSRCS" LIBS="$LIBS $QTELIBS"
fi

###############################

AC_SUBST(V9T9SRCS)
AC_SUBST(INCDIRS)
AC_SUBST(MODSRCS)
AC_SUBST(UNIXMODSRCS)
AC_SUBST(WINMODSRCS)
AC_SUBST(MACMODSRCS)
AC_SUBST(OSLIBSRCS)

dnl find gforth
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
##AC_CONFIG_SUBDIRS(source source/Generate source/OSLib)
##AC_CONFIG_SUBDIRS(source tools tools/TIAsm tools/DSRs tools/Forth tools/TinyForth tools/FileUtils)

AC_OUTPUT(Makefile intl/Makefile po/Makefile.in libiberty/Makefile \
	 source/Generate/Makefile \
	source/OSLib/Makefile source/Makefile \
	source/Modules/Makefile \
	source/Modules/Unix/Makefile source/Modules/Win32/Makefile \
	source/Modules/MacOS/Makefile 
	)
