
#undef ENABLE_NLS
#undef HAVE_CATGETS
#undef HAVE_GETTEXT
#undef HAVE_LC_MESSAGES
#undef HAVE_STPCPY
#undef PACKAGE
#undef VERSION

/* Windows */
/* this timer works best -- the midi/multimedia timer */
#undef USE_MM_TIMER
/* this one is horrible but portable */
#undef USE_MESSAGE_TIMER
/* this one looks most promising but requires Win98/WinNT4.0 */
#undef USE_WAITABLE_TIMER

/* Host OS */

/* UNIX-like */
#undef UNDER_UNIX
/* Win32, obviously */
#undef UNDER_WIN32
/* don't get excited -- not yet supported */
#undef UNDER_MACOS

/* use GCC inline assembly for MUL/DIV and status in 9900.c */
#undef GNU_X86_ASM

/* v9t9 system modules */

/* include "null" video module */
#undef NULL_VIDEO
/* include "null" keyboard module */
#undef NULL_KEYBOARD
/* include "null" sound module */
#undef NULL_SOUND

/* emulated disk (files in a directory = FIAD) */
#undef EMU_DISK_DSR
/* "real" disk (disk images) */
#undef REAL_DISK_DSR
/* "real" RS232 (using TI's DSR ROM and CRU mappings) */
#undef REAL_RS232_DSR
/* emulated PIO (parallel port/printer) */
#undef EMU_PIO_DSR

/* Linux modules */
#undef X_WIN_VIDEO
#undef X_WIN_KEYBOARD
#undef LINUX_SVGA_VIDEO
#undef LINUX_SVGA_KEYBOARD
#undef LINUX_SPEAKER_SOUND
#undef OSS_SOUND
#undef ALSA_SOUND
#undef ESD_SOUND

/* Windows modules -- required for any Win32 build! */
#undef WIN32_VIDEO
#undef WIN32_KEYBOARD
#undef WIN32_SOUND

/* GTK+ modules, for Linux or Win32 */
#undef HAVE_GTK
#undef GTK_KEYBOARD
#undef GTK_VIDEO

/* Qt Embedded */
#undef HAVE_QTE
#undef QTE_KEYBOARD
#undef QTE_VIDEO

/* utility libraries */

/* use readline library */
#undef HAVE_READLINE
/* PNG file support for screen shots */
#undef WITH_LIB_PNG

/* using GNU gettext? */
#undef HAVE_GETTEXT

