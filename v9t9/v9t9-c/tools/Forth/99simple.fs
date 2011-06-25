\	99simple.fs					-- words for minimal FORTH kernel
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
\	$Id: 99simple.fs,v 1.4 2008-12-18 20:46:07 ejs Exp $

code: :dodoes
\	PUSH
	mov TOS , *SP+
	mov R11 , TOS
	mov *TOS , R0
	inct TOS
	jmp 1 $f
end-code

code: :docol 
	dect RP
	mov IP , *RP
	mov TOS , IP
\	inct IP
end-code

code: @Next
	mov *IP+ , R0
	b	*R0
end-code

code: ExEntry
1 $:
	mov *IP+ , R0
	b	*R0
end-code

Code :dovar
	NEXT
end-code

Code :douser
	mov *TOS , TOS
	ai TOS , StartUser #	
	NEXT
End-code

Code :docon
	mov *TOS , TOS
	NEXT
end-code

Code ;S
	mov *RP+ , IP
	mov *IP+ , R0
\	mov *WA+ , R0
	b	*R0
end-code

\ \\\\\\\\\\\\\\\\\\\\

10 constant TEN

code ZAA
	mov r1 , r1
	NEXT
end-code

: ZBB
	ZAA ZCC TWT
;

: ZCC
	ZBB ZAA TEN
;

20 constant TWT

\ \\\\\\\\\\\\\\\\\

Code ((cold))
1 $:
\	dbg

	\ temporary!
	li RP , 2010 #
	li SP , 2020 #

\	li rNEXT , ' @Next #

	li TOS , ' ZBB  >body #	\ !!
	li DODOES , ' :DoDoes #

	\ infinite loop
	limi 1 #

	b	' :docol @>

end-code

' ((cold)) $6002 !




