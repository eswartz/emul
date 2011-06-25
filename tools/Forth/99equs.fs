\	99equs.fs					-- global FORTH equates
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
\	$Id: 99equs.fs,v 1.14 2009-01-03 23:46:45 ejs Exp $

>cross also assembler
order 

>assembler
also assembler
order

s" lib/99equs" included
s" lib/99ram" included

previous

>assembler

also assembler
rTOS R:	TOS		rTOS *R: *TOS	rTOS *R+: *TOS+		rTOS @>(R):	@>(TOS)
rT1 R:	T1		rT1 *R: *T1		rT1 *R+: *T1+		rT1 @>(R):	@>(T1)
rT2 R:	T2		rT2 *R: *T2		rT2 *R+: *T2+		rT2 @>(R):	@>(T2)
rT3 R:	T3		rT3 *R: *T3		rT3 *R+: *T3+		rT3 @>(R):	@>(T3)
rT4 R:	T4		rT4 *R: *T4		rT4 *R+: *T4+		rT4 @>(R):	@>(T4)
rRP	R:	RP		rRP	*R:	*RP		rRP *R+: *RP+		rRP	@>(R):	@>(RP)
rIP	R:	IP		rIP	*R:	*IP		rIP *R+: *IP+		rIP	@>(R):	@>(IP)
rSP	R:	SP		rSP	*R:	*SP		rSP *R+: *SP+		rSP	@>(R):	@>(SP)

rDODOES R: DODOES
rDOCON R: DOCON
rDOCOL R: DOCOL


\	push TOS to stack 
: PUSH
	dect SP
	mov TOS , *SP
; immediate

\	pop TOS from stack
: POP
	mov *SP+ , TOS
; immediate

\	drop and pop TOS from stack
: POP2
	inct SP
	mov *SP+ , TOS
; immediate

\	drop 2 and pop TOS from stack
: POP3
	ai SP , 4 #
	mov *SP+ , TOS
; immediate

\	drop, not changing TOS
: 0POP
	inct SP
; immediate

\	drop 2, not changing TOS
: 0POP2
	ai SP , 4 #
; immediate

T has? inlining-next <A> [if]
	: NEXT
		mov *IP+ , R0
		b	*R0
	;	immediate
[else]
	: NEXT
		b 	rNEXT *R
	; 	immediate
[endif]

previous

>cross

: VECTOR ( ip wp -- )
	T ,  , H
;

: vector! ( ip wp addr -- )
	dup >r T ! H r> T ! H
;

