\	99test.fs					-- minimal FORTH kernel for testing
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
\	$Id: 99test.fs,v 1.4 2008-12-18 20:46:07 ejs Exp $

1 constant init-test-level

\	get-rom-addr defined in command line as a constant
get-rom-addr constant ROM-start-addr

create mach-file ," 99config.fs" 
include cross.fs

>target

only forth also definitions

include 99asm.fs
include 99equs.fs

: PUSH 
	mov TOS , *SP+
; immediate


>minimal

\	Memory layout
\
\	User variables stored relative to >A000.
\	Dictionary starts after that.
\	(We could easily put this stuff at >3000 and have 24k for dict.)

$2800 constant StartRAM
$800 constant	(#rp)
$800 constant	(#sp)
$100 constant	(#pad)
$100 constant	(##-pad)
$100 constant	(#slit-pad)
$100 constant	(#tib)

$3800 constant StartUser

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\

lock

>rom

include 99simple.fs

unlock

.regions

turnkey


.stats
.unresolved

unlock

rom-dictionary extent save-region test.bin








