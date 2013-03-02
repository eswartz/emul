# rebuild.sh
# 
# (c) 1994-2011 Edward Swartz
# 
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; either version 2 of the License, or
#   (at your option) any later version.
#  
#   This program is distributed in the hope that it will be useful, but
#   WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#   General Public License for more details.
#  
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
#   02111-1307, USA.
# 

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

