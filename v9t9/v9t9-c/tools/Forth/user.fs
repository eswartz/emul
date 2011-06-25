\	user.fs						-- FORTH user variables
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
\	$Id: user.fs,v 1.7 2008-12-19 06:17:59 ejs Exp $

\	\\\\\\\\\\\\\\

\	If these are suddenly broken, the culprit
\	is >BODY.  cross.fs assumes we have a certain
\	field present for every word, which we only have
\	for CREATEd words.
\
| StartRAM (#rp) +			    constant (rp0)		\ grows down
| (rp0) (#sp) +				    constant (sp0)		\ grows down
| (sp0) 		 				constant (pad0)		\ grows up
| (pad0) (#pad) (##-pad) + + 	constant (#-pad0)		\ grows down
| (#-pad0) 					    constant (slit-pad0)	\ grows up
| (slit-pad0) (#slit-pad) + 	constant (tib0)			\ grows up
| (tib0) (#tib) +				constant (first)	\ grows up
| (first) (#blk) +              constant (limit)
| (limit)                       constant (up0)


