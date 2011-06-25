\	common.fs					-- common host/target source
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
\	$Id: common.fs,v 1.7 2009-01-03 16:52:59 ejs Exp $

\	Common source for cross-compiler and target compiler
\
\	Pass 0 for cross compiler, and 1 for target compiler.

dup
[IF]
	unlock also target definitions
[ELSE]
	
[THEN]

also assembler
order

$690 rDODOES + constant BL-DODOES
$690 rDOCON + constant BL-DOCON
$690 rDOCOL + constant BL-DOCOL

$6A0 constant BL-@

previous

[IF]
	previous lock
[ELSE]

[THEN]
