#!/bin/sh

if ! autoconf ; then
echo could not run autoconf, or an error occurred.  Please install them and try again.
echo Press enter to continue, or ctrl-C to break...
read 
fi

if ! automake ; then
echo could not run automake, or an error occurred.  This is probably not a problem.
echo Press enter to continue, or ctrl-C to break...
read 
fi

rm -f po/POTFILES.in &&
find source -name \*.c -or -name \*.cpp | grep -v -E '(,,|{arch}|.AppleDouble)' >>po/POTFILES.in &&
find tools -name \*.c -or -name \*.cpp | grep -v -E '(,,|{arch}|.AppleDouble)' >>po/POTFILES.in &&
rm -f config.cache &&
./configure $* &&
make

