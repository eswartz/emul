\	99config.fs					-- FORTH kernel configuration
\
\	(c) 1996-2009 Edward Swartz
\
\   This program is free software; you can redistribute it and/or modify
\   it under the terms of the GNU General Public License as published by
\   the Free Software Foundation; either version 2 of the License, or
\   (at your option) any later version.
\ 
\   This program is distributed in the hope that it will be useful, but
\   WITHOUT ANY WARRANTY; without even the implied warranty of
\   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
\   General Public License for more details.
\ 
\   You should have received a copy of the GNU General Public License
\   along with this program; if not, write to the Free Software
\   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
\   02111-1307, USA.  
\
\	$Id: 99config.fs,v 1.15 2009-01-03 23:46:45 ejs Exp $

get-current target-environment set-current

\ set up cross-compiler constants

false constant ec						\ ???
false constant standard-threading		\ direct threading: an XT points directly to code
\ true constant inlining-next
false constant inlining-next
\ true constant profiling				\ BROKEN: some primitives broken (check test)
false constant profiling
true constant forth-83-dictionary		\ dictionary style
true constant rom
true constant prims
true constant OS
8 KB constant kernel-size
1 KB constant stack-size
1 KB constant rstack-size

\ false constant caching-tos				\ TOS just points to *SP
true constant caching-tos				\ TOS is cached *SP

false constant user-vars

." Defining CONFIG:" order cr

true constant enhanced-mode             \ enhanced memory map

set-current

\	9900 definitions

2	constant	cell
1 	constant 	cell<<
4 	constant	cell>bit
8	constant	bits/byte
8	constant	bits/char
8	constant	float

1	constant	bigendian
2	constant	/maxalign


0	 constant	nil


