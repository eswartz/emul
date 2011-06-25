\	99prims.fs					-- FORTH primitives in 9900 assembler
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
\	$Id: 99prims.fs,v 1.44 2009-07-26 01:09:40 ejs Exp $

\	basic
2 constant #cell
1 constant #char

0   constant 0
1   constant 1
2   constant 2
\ 8   constant 8
-1  constant -1
-2  constant -2

\ \\\\\\\\\\\\\\\\\\\\\\\\\

\ THREADING and DICTIONARY
\
\ in ALL modes, these registers are in play:
\
\ RP = return stack pointer
\ SP = stack pointer
\ IP = instruction pointer (for colon definitions)
\ NEXT = pointer to NEXT routine (for exiting code definitions)
\
\ in INDIRECT THREADING mode, these registers are in play:
\
\ WA = work address
\
\ Indirect threading is a model where an execution token (xt) is
\ the address of a cell in memory that contains the pointer where
\ assembly execution will start.  This model favors memory space
\ over code size in the case where directly embedding assembly into
\ the dictionary may be expensive.  
\
\ WA as an xt thus points to the CFA.
\
\ The entry to any word has WA pointing past the CFA to the PFA.
\ For colon definitions, IP will be assigned from the WA.
\
\ To boot in this mode, WA contains the xt of (boot), and IP points
\ to the first instruction.
\
\ A DEFERed word stores a pointer to a cell in its PFA.
\ Its CFA is :dodefer or :dordefer, depending
\ on the place in the dictionary.  A normal deferred word's
\ cell can live in the definition (like a VARIABLE) but
\ a ROM-deferred word points to statically allocated RAM space.
\
\ IS must deal with this as well as the target and cross-compiling DEFER.
\
\ 


\ \\\\\\\\\\\\\\\\\\\\\\\\\

\	Backend words for implementing the basic FORTH word types

\	no-operation
Code noop
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

\	enable/disable v9t9 debugger
Code (dbg)
	dbg
	NEXT
end-code

\ 	get tick count ( -- dword )
Code ticks
	PUSH
	ticks T1
	mov T2 , TOS
	PUSH
	mov T1 , TOS
	NEXT
end-code

\	push literal from instruction stream
code lit
	PUSH
	mov *IP+ , TOS
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

\	push length-prefixed string from instruction stream
Code (s")
\	DBG
	PUSH
	movb *IP+ , T1
	srl T1 , 8 #
	mov IP , TOS
	PUSH
	a	T1 , IP
	mov T1 , TOS
	inc IP
	andi IP , $fffe #
\	-dbg
	NEXT
end-code
\ ' slit ALIAS (s")


Code >R
    dect RP
    mov TOS , *RP
    POP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code
    
Code R>
    PUSH
    mov *RP+ , TOS
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

Code @
    mov *TOS , TOS
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

Code C@
    movb *TOS , TOS
    srl TOS , 8 #
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

Code R@
    PUSH
    mov *RP , TOS
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

Code RDROP
    inct RP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $f  [ [endif] ]    
end-code

\ direct threading -----------------------

\	Execute does> part of word
code: :dodoes
\	:dodoes is entered through BL *DODOES leaving R11 
\	pointing to the CA of the does> part, followed by the PFA of the word.
	PUSH
	mov *R11+ , R0
	mov R11 , TOS
	b	*R0
end-code

\   unwind colon definition
Code ;S
    mov *RP+ , IP
[ has? inlining-next [if] ]
    NEXT
[ [else] ]
    jmp 9 $f
[ [endif] ]    
end-code

code :docol 
\	:docol is entered through BL *DOCOL leaving R11
\	pointing to the list of CAs
	dect RP
	mov IP , *RP
	mov R11 , IP
end-code

code: @Next
\	drop through
end-code

code: ExEntry
9 $:
[ has? profiling [if] ]
\	dbg
	mov *IP , R0
	dect R0
	mov *R0 , R0
	inc *R0
\	-dbg
[ [then] ]
	mov *IP+ , R0
	b	*R0
end-code

\	Execute deferred word
code: :dodefer
\	dbg
	mov *R11+ , R0
	b	*R0
end-code

\	Execute deferred word stored in ROM, whose PFA points to a RAM address
code: :dordefer
\	dbg
	mov *R11+ , R0
	mov	*R0 , R0
	b	*R0
end-code

\	generic CREATE already pushed PFA
Code :dovar
	NEXT
end-code

\   push constant
Code :docon
    PUSH
    mov *R11 , TOS
[ has? inlining-next [if] ]
    NEXT
[ [else] ]
    jmp 9 $b
[ [endif] ]    
end-code
\ ]test" constant 42 star"
\ test" constant star 42 ="
\ ]test" variable foo"
\ test" variable foo 23 ! foo @ 23 ="

Code  (IS)
	MOV TOS , T1
	inct t1
[ has? enhanced-mode [if] ]	
	ci TOS , $4000 #
	jhe 1 $f
0 $:
    mov *T1 , T1
1 $:
[ [else] ]
    ci TOS , $8000 #
    jhe 1 $f
    ci TOS , $2000 #
    jl 0 $f
    ci TOS , $6000 #
    jl 1 $f
0 $:
    mov *T1 , T1
1 $:
[ [endif] ]
	
	POP
	MOV TOS , *T1
	POP
	NEXT
end-code

Code  (IS?)  ( isaddr -- xt )
    MOV TOS , T1
    inct t1
[ has? enhanced-mode [if] ] 
    ci TOS , $4000 #
    jhe 1 $f
0 $:
    mov *T1 , T1
1 $:
[ [else] ]
    ci TOS , $8000 #
    jhe 1 $f
    ci TOS , $2000 #
    jl 0 $f
    ci TOS , $6000 #
    jl 1 $f
0 $:
    mov *T1 , T1
1 $:
[ [endif] ]
    
    MOV *T1 , TOS
    NEXT
end-code

has? user-vars 0= [if]

\	push addr of user var
Code :douser
	mov *TOS , TOS
	NEXT
End-code

[else]

\	push addr of user var
Code :douser
	mov *TOS , TOS
	ai TOS , (up0) #
	NEXT
End-code

[then]

\ ------------------------------------

Code branch
	A *IP , IP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

Code ?branch
	MOV TOS , TOS
	JNE 2 $f
	POP
	a *IP , IP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
2 $:	
    inct IP     \ skip offset
    POP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\

\ 	========================
\	stack manipulation words
\	========================

Code DUP
	PUSH
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

Code DROP
	POP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

Code SWAP
	mov TOS , T1
	mov *SP , TOS
	mov T1 , *SP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

Code 1+
    inc TOS
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code

Code +!   ( n addr -- )
    a   *SP+ , *TOS
    POP
[ has? inlining-next [if] ]  NEXT  [ [else] ]  jmp 9 $b  [ [endif] ]    
end-code
test" +! base @ 2 base +! base @  $a base !  $c = swap $a = and"


Code 2DUP
	dect SP
	mov TOS , *SP
	dect SP
	mov 4 @>(SP) , *SP
	NEXT
end-code

Code ?DUP
	mov TOS , TOS
	jeq 0 $f
	PUSH
0 $: NEXT
end-code

Code 2DROP
	POP2
	NEXT
end-code

Code 2SWAP
	mov TOS , T1
	mov 2 @>(SP) , TOS
	mov T1 , 2 @>(SP)

	mov *SP , T1
	mov 4 @>(SP) , *SP
	mov T1 , 4 @>(SP)
	NEXT
end-code

Code OVER
	PUSH
	mov 2 @>(SP) , TOS
	NEXT
end-code

Code 2OVER
	PUSH
	mov 6 @>(SP) , TOS
	PUSH
	mov 6 @>(SP) , TOS
	NEXT
end-code

Code ROT	( a b c -- b c a )
	mov 2 @>(SP) , T1
	mov *SP , 2 @>(SP)
	mov TOS , *SP
	mov T1 , TOS
	NEXT
end-code

Code NIP 	( a b -- b )
    inct SP
	NEXT
end-code

Code PICK	( ix* n -- x[n] )
	a TOS , TOS
	a SP , TOS
	mov *TOS , TOS
	NEXT
end-code

Code 2>R
	ai RP , -4 #
	mov TOS , *RP
	mov *SP+ , 2 @>(RP)
	POP
	NEXT
end-code
test" 2>r 45. 2>r 2r> 45. d="	

Code 2R>
	PUSH
	mov 2 @>(RP) , TOS
	PUSH
	mov *RP+ , TOS
	inct RP
	NEXT
end-code

Code 2R@
	PUSH
	mov 2 @>(RP) , TOS
	PUSH
	mov *RP , TOS
	NEXT
end-code

Code 2RDROP
	ai RP , 4 #
	NEXT
end-code


\	========================
\	math words
\	========================

code: shifter
0 $:
	mov	TOS , R0		\ shift count
	POP
	x	T1
	NEXT
end-code

Code RSHIFT
	li	T1 , 0800 rTOS + #		\ SRA
	jmp	0 $b
end-code

Code LSHIFT
	li	T1 , 0a00 rTOS + #		\ SLA
	jmp	0 $b
end-code

Code urshift
	li	T1 , 0900 rTOS + #		\ SRL
	jmp 0 $b
end-code

Code cshift
	li	T1 , 0b00 rTOS + #		\ SRC
	jmp	0 $b
end-code

Code UM*
	mov TOS , T1
	mpy *SP , T1
	mov T2 , *SP
	mov T1 , TOS
	NEXT
end-code
test" um* 50 8 um* 280. d="

Code AND
	inv TOS
	szc TOS , *SP
	POP
	NEXT
end-code

Code OR
	soc *SP+ , TOS
	NEXT
end-code

Code XOR
	xor *SP+ , TOS
	NEXT
end-code
test" xor $55 $aa xor $ff ="

\	========================

Code EXECUTE
\	dbg
	mov TOS , R0
	POP
	b	*R0
end-code

Code (of)		\ ( itm itm? -- <> | itm ) IP:branch
	mov TOS , T1
	POP
	c 	TOS , T1
	jne	1 $f
	POP
	inct IP
	NEXT
1 $:
	a	*IP , IP
	NEXT	
end-code

Code (loop)
	inc	*RP	  				\ increment
	c	*RP , 2 @>(RP)		\ done?
	jne	1 $f				\ nope
\	ai	RP , 4 #			\ yup... lose loop limits
	inct IP					\ & skip jump amount
	NEXT
1 $:
	a	*IP , IP
	NEXT
end-code

Code (+loop)
	mov	2 @>(RP) , T1
	mov	T1 , T2				\ limit
\	dec T1					\ limit-1
	s	*RP , T1
	a	TOS , *RP
	s	*RP , T2
	POP
	xor T2 , T1
	jlt	0 $f
	a	*IP , IP
	NEXT
0 $:
\	ai	RP , 4	#			\ lose limits
	inct IP					\ done, skip jump amount
	NEXT
end-code

Code (do)
	ai	RP , -4 #			\ make room 
	mov	TOS , *RP			\ put start
	mov	*SP+ , 2 @>(RP)		\ put limit
	POP
	NEXT
end-code

Code (?do)
	ai	RP , -4 #			\ make room 
	mov TOS , T1
	mov	T1 , *RP			\ put start
	mov	*SP , 2 @>(RP)		\ put limit
	s	*SP+ , T1
	jne 1 $f				\ continue
	a   *IP , IP
\	ai 	IP , 4 #            \ skip BRANCH n
\	ai	RP , 4 #			\ pop limits
	POP
	NEXT
1 $:
	inct IP					\ skip branch
	POP
	NEXT
end-code

Code UNLOOP
\	dbg
	ai	RP , 4 #
	NEXT
end-code

Code I
	PUSH
	mov	*RP , TOS
	NEXT
end-code

Code J
    PUSH
    mov 4 @>(RP) , TOS
    NEXT
end-code

Code RPICK	( R:ix* -- R:ix*n | n -- x[n] )
	mov TOS , T1
	a T1 , T1
	mov *SP+ , TOS
0 $:
	PUSH
	a RP , T1
	mov *T1 , TOS
	NEXT
end-code


Code i'
	li T1 , 2 #
	jmp 0 $b
end-code

Code j'
	li T1 , 6 #
	jmp 0 $b
end-code

Code k
	li T1 , 8 #
	jmp 0 $b
end-code

Code DIGIT					\ (b c -- d -1 | 0)
\ 	determine if 'c' is a legal digit in base 'b'

	mov *SP+ , T1			\ base
							\ digit in TOS
	ci	TOS , $61 #			\ lowercase a-*
	jhe	3 $f
	ci	TOS , $41 #			\ uppercase A-*
	jhe	4 $f
	ci	TOS , $3a #
	jhe	1 $f
	ci	TOS , $30 #			\ 0-9
	jl	1 $f

	ai	TOS , -$30 #
	jmp 0 $f
3 $: ai	TOS , -$20 #		\ make uppercase
4 $: ai	TOS , -$37 #		\ make binary
0 $: c	TOS , T1			\ compare digit to base...
	jhe	1 $f

	PUSH
	seto TOS				\ good
	NEXT

1 $: clr	TOS  			\ error
	NEXT
end-code

Code m/mod		( ud un -- un.r ud.q )
\	dbg
\	divide high word by dividend
	mov TOS , T3
	mov *SP , T2
	clr T1
	div T3 , T1			\ T1:ud.h*10000/r  T2:ud.h*10000%r
	mov T1 , TOS		\ TOS:ud.h*10000/r (hi word of quotient)
	mov T2 , T1
	mov 2 @>(SP) , T2 	\ T1:ud.h*10000%r  T2:ud.l
\	divide low word + previous remainder*10000
	div T3 , T1			
	mov T2 , 2 @>(SP)	\ T2:ud%r
	mov T1 , *SP		\ lo word of quotient
\	-dbg
	NEXT
end-code

DeferROM (find)      ( c-addr lfa -- c-addr 0 | nfa 1 ) 

Code (lfind)
	\ find word in dictionary	 ( c-addr lfa -- c-addr 0 | nfa 1 )	
	\ lfa is nfa-2

\   	dbg
	mov	TOS , TOS			\ TOS=LFA
	jeq	9 $f				\ fail

\	search list 

	clr T3
	clr T4
	
	mov *SP , R11            \ R11=char ptr
	clr R12
    movb *R11 , R12          \ R12=length
	jmp 2 $f

4 $: 
    mov -2 @>(TOS) , TOS    \ get LFA to new one...
    jeq 9 $f                \ if end... (unlikely)

2 $:
	inct TOS				\ LFA>NFA
	mov TOS , T1			\ new NFA to check

	movb *T1+ , T3
	jgt	4 $b				\ hidden word ($80 not set) (unlikely)

	sb R12 , T3
	andi T3 , $1F00 #		\ compare lengths 
	jne 4 $b				\ nope (likely)

    mov R11 , T2
    inc T2
    mov R12 , R0
    swpb R0
	
3 $: movb *T1+ , T3 
    movb *T2+ , T4
    cb T3 , T4
    jeq 1 $f			\ exact match?

    \ see if they might be letters that differ in case only
    xor T3 , T4
	ci T4 , $2000 #			\ differ in case bit?
	jne 4 $b
	
	\ double-check they're really letters and not punctuation
	szc T4 , T3				\ turn off case in matching char
	ci T3 , $4100 #
	jl 4 $b
	ci T3 , $5A00 #
	jh 4 $b
	
1 $:
	dec R0
	jgt	3 $b

	mov	TOS , *SP		\ overwrite caddr with xt
	seto TOS
\	-dbg
    NEXT
    
9 $:    
    clr TOS                 \ failed
\   -dbg
	NEXT

end-code

Code (nfa=)		( caddr nfa -- 1|0 )
\	dbg
	mov TOS , T1
	POP
	mov TOS , T2
	movb *T1+ , T3		\ length of nfa
	jgt	1 $f			\ whoops, hidden

	andi T3 , $1f00 #
	cb T3 , *T2+		\ lengths equal?
	jne 1 $f
	srl T3 , 8 #
2 $:
	cb *T1+ , *T2+
	jne 1 $f
	dec T3
	jgt 2 $b
	seto TOS
\	-dbg
	NEXT
1 $:
	clr TOS
\	-dbg
	NEXT
end-code

2 [IF]

Code traverse 	( xt|nfa dir -- nfa|xt )
\	traverse from one end of definition to the other
\	dir<0 means xt->nfa, dir>0 means nfa->xt

\	add a field for profiler

\	dbg

	mov TOS , T1
	POP
	mov T1 , T1
	jlt	0 $f

		\ nfa -> xt
	movb *TOS+ , T1		\ length+info byte
	andi T1 , $1f00 #	\ mask length
	srl T1 , 8 #
	inc T1				\ align
	a T1 , TOS
	andi TOS , $fffe #

[ has? profiling [if] ]
	inct TOS
[ [then] ]
	jmp 2 $f

0 $:	\ xt -> nfa
	li T1 , $0100 #
	clr T2

[ has? profiling [if] ]
	dect TOS
[ [then] ]

	dec TOS
	movb *TOS , T2
	ci T2 , $2000 #				\ blank
	jne 1 $f
	dec TOS
1 $: dec TOS
	movb *TOS , T3 
	jgt 3 $f					\ len byte has $80 set
	andi T3 , $1F00 #
	cb T1 , T3
	jeq 2 $f
3 $:
	ai T1 , $0100 #
	jgt 1 $b
2 $: 
 	NEXT
end-code
[THEN]

Code FILL		( start n ch -- )
	sla TOS , 8 #
	mov *SP+ , T2
	mov *SP+ , T3
	mov T2 , T2
	jmp 2 $f	
1 $: movb TOS , *T3+
	dec T2
2 $: jgt 1 $b
	POP
	NEXT
end-code

\	facility
Code (KEY?)
	PUSH
	xop	TOS , SYS^ #
	#key? data			
	NEXT
end-code
DeferROM KEY?
\ ' (KEY?) ROMIS KEY?

Code (KEY)
\	dbg
	PUSH
0 $: xop TOS , SYS^ #
	#key? data					
	movb TOS , TOS
	jeq	0 $b
	xop TOS , SYS^ #
	#rdkey data					
\	-dbg
	NEXT
end-code
DeferROM KEY
\ ' (KEY) ROMIS KEY

Code (EMIT)
\	dbg
\	sla TOS , 8 #
	xop TOS , SYS^ #
	#emit data
	POP
	NEXT
end-code
DeferROM EMIT
\ ' (EMIT) ROMIS EMIT

Code (emit8)
\	sla TOS , 8 #
	xop TOS , SYS^ #
	#emit8 data
	pop
	NEXT
end-code
DeferROM emit8
\ ' (emit8) ROMIS emit8

\	This version dumps to V9t9 console
Code (EMIT99)
	xop TOS , SYS^ #
	#emit data
	swpb TOS
	emitchar TOS
	POP
	NEXT
end-code

Code /STRING	( addr n delta -- addr+delta n-delta )
	s	TOS , *SP
	a	TOS , 2 @>(SP)
	POP
	NEXT
end-code

Code AT-XY		( col row -- )
	mov *SP , T1
	sla T1 , 8 #
	andi TOS , $ff #
	soc TOS , T1
	POP2
	xop T1 , SYS^ #
	#gxy data	
	NEXT
end-code

\ Move memory backward (src -> dst)
Code CMOVE              ( src dst # -- )
0 $: mov TOS , T2			\ # bytes
	mov *SP , T1			\ dst
	POP2					\ src = TOS
	mov	T2 , T2  			\ 0 bytes?
	jeq	1 $f
\	dbg
2 $: movb	*TOS+ , *T1+
	dec	T2
	jne	2 $b
1 $: \ -dbg
	POP 
	NEXT
End-code

\ Move memory forward (dst -> src)
Code CMOVE>     ( src dst # -- )
0 $: mov TOS , T2			\ # bytes
	mov *SP , T1			\ dst
	POP2					\ src = TOS
	mov	T2 , T2  			\ 0 bytes?
	jeq	1 $f
\	dbg
	a T2 , TOS
	a T2 , T1
2 $: dec TOS
	dec T1
	movb	*TOS , *T1
	dec	T2
	jne	2 $b
1 $: \ -dbg
	POP 
	NEXT
End-code

\ T3 is input char; ( caddr u -- ) on stack
Code: filler
0 $: mov TOS , T1			\ # bytes
	POP						\ src = TOS
	mov	T1 , T1  			\ 0 bytes?
	jeq	1 $f
2 $: movb T3 , *TOS+
	dec	T1
	jne	2 $b
1 $:
	POP 
	NEXT
end-code

Code ERASE
	clr T3
	jmp 0 $b
end-code

Code BLANK
	li T3 , $2020 #
	jmp 0 $b
end-code

\	Execute these with interrupts off and with T3 = >VDPWA; T4 = tmp

Code: (vwaddr)
    mov r0 , T4
    srl r0 , &6 #
    ab vpob @> , r0
    movb r0 , *T3
    li r0 , $8e00 #
    movb r0 , *T3
    mov T4 , r0
    andi r0 , 3FFF #
Code: (vwaddr')
	ori r0 , 4000 #
	swpb r0
	movb r0 , *T3
	swpb r0
	movb r0 , *T3
	b *r11
end-code

Code: (vraddr)
    mov r0 , T4
    srl r0 , &6 #
    ab vpob @> , r0
    movb r0 , *T3
    li r0 , $8e00 #
    movb r0 , *T3
    mov T4 , r0
Code: (vraddr')
    andi r0 , 3fff #
    swpb r0
    movb r0 , *T3
    swpb r0
    movb r0 , *T3
    b *r11
end-code

\ Read from 'addr' (current bank)
Code vraddr' ( addr -- )
    li T3 , VDPWA #
    mov TOS , R0
    POP
    bl ' (vraddr') @>
    NEXT
end-code

\ Read from 'addr' (any bank)
Code vraddr ( addr -- )
    li T3 , VDPWA #
    mov TOS , R0
    bl ' (vraddr) @>
    POP
    NEXT
end-code

\ Write to 'addr' (current bank)
Code vwaddr'
    li T3 , VDPWA #
	mov TOS , R0
	bl ' (vwaddr') @>
	POP
	NEXT
end-code

\ Write to 'addr' (any bank)
Code vwaddr ( addr -- )
    li T3 , VDPWA #
    mov TOS , R0
    bl ' (vwaddr) @>
    POP
    NEXT
end-code

\ Write char to VDP (any bank)
Code vc!   ( val addr -- )
    li T3 , VDPWA #
	mov TOS , R0
	bl ' (vwaddr) @>
	dect T3
	POP
	swpb TOS
	movb TOS , *T3
	POP
	NEXT
end-code

\ Read char from VDP (any bank)
Code vc@ ( addr -- val )
    li T3 , VDPWA #
    mov TOS , R0
	bl ' (vraddr) @>
	movb VDPRD @> , TOS
	srl TOS , 8 #
	NEXT
end-code

\ Write word to VDP (any bank)
Code v! ( val addr -- )
    li T3 , VDPWA #
    mov TOS , R0
	bl ' (vwaddr) @>
	POP
	movb TOS , VDPWD @>
	swpb TOS
	movb TOS , VDPWD @>
	POP
	NEXT
end-code

\ Read word from VDP (any bank)
Code v@ ( addr -- val )
    li T3 , VDPWA #
    mov TOS , R0
	bl ' (vraddr) @>
	ai T3 , -$402 #
	movb *T3 , TOS
	swpb TOS
	movb *T3 , TOS
	swpb TOS
	NEXT
end-code

\ Write byte to next VDP addr (bank-independent)
Code ,vc!  ( val -- )
	swpb TOS
	movb TOS , VDPWD @>
	POP
	NEXT
end-code

\ Read byte from next VDP addr (bank-independent)
Code ,vc@
	PUSH
	movb VDPRD @> , TOS	
	srl TOS , 8 #
	NEXT
end-code

\ Write word to next VDP addr (bank-independent)
Code ,v!
	movb TOS , VDPWD @>
	swpb TOS
	movb TOS , VDPWD @>
	POP
	NEXT
end-code

\ Read word from next VDP addr (bank-independent)
Code ,v@
	PUSH
	movb VDPRD @> , TOS	
	swpb TOS
	movb VDPRD @> , TOS	
	swpb TOS
	NEXT
end-code

Code limi0
	limi 0 #
	NEXT
end-code

Code limi1
	limi 1 #
	NEXT
end-code

\ Copy from vdp to cpu RAM (any bank)
Code vcmove	( vaddr caddr u -- )
    li T3 , VDPWA #
	mov 2 @>(SP) , R0
	bl ' (vraddr) @>
	mov *SP , T1
	mov TOS , TOS
	jeq 2 $f
	ai T3 , -$402 #
1 $: movb *T3 , *T1+
	dec TOS
	jne 1 $b
2 $:
	POP3
	NEXT
end-code

\ Copy string to VDP at current location (any bank)
Code ,vmove 	( caddr u -- )
	mov *SP , T1
	mov TOS , TOS
	jeq 2 $f
	li T3 , VDPWD #
1 $: movb *T1+ , *T3
	dec TOS
	jne 1 $b
2 $:
	POP2
	NEXT
end-code

\ Copy from CPU to VDP ram (any bank)
Code cvmove 	( caddr vaddr u -- )
    li T3 , VDPWA #
	mov *SP , R0
	bl ' (vwaddr) @>
	mov 2 @>(SP) , T1
	mov TOS , TOS
	jeq 2 $f
	dect T3
1 $: movb *T1+ , *T3
	dec TOS
	jne 1 $b
2 $:
	POP3
	NEXT
end-code

\ Fill memory in VDP ram (any bank)
Code vfill 		( vaddr u char -- )
    li T3 , VDPWA #
	mov TOS , T2
	swpb T2
	mov 2 @>(SP) , R0
	bl ' (vwaddr) @>
	mov *SP , T1
	mov T1 , T1
	jeq 2 $f
	dect T3
1 $: movb T2 , *T3
	dec T1
	jne 1 $b
2 $: 
	POP3
	NEXT
end-code

\ write VDP register
\
\ This does NOT save values to memory, so any settings will be lost on
\ an abort.  Use (vregw) to set register values that will be remembered.
\
\ also leave T3 = >VDPWA
Code vwreg ( reg|val -- )
    li T3 , VDPWA #
    ori TOS , 8000 #
    swpb TOS
    movb TOS , *T3
    swpb TOS
    movb TOS , *T3
    POP
    NEXT
end-code

\ Set the VDP bank to the one containing the given address;
\ also leave T3 = >VDPWA
Code vbank ( addr --)
    li T3 , VDPWA #
    srl TOS , &6 #
    movb TOS , *T3
    li TOS , $8e00 #
    movb TOS , *T3
    POP
    NEXT
end-code



Code COMPARE	( addr u addr' u' -- -1/0/1 )
	mov TOS , T2
	mov *SP+ , T1
	mov *SP+ , T4
	mov *SP+ , T3
0 $:
	mov T2 , TOS
	jeq 2 $f
	mov T4 , TOS
	jeq 2 $f
 	movb *T3+ , TOS
	sb 	*T1+ , TOS
	jne 1 $f
	dec	T2
	dec	T4
	jmp	0 $b
2 $:
	\ longer string is greater
	mov 	T2 , TOS
	s	T4 , TOS
	NEXT
1 $: 
    sra TOS , 8 #
	NEXT
end-code

| Code: TOLOWER
    ci R0 , $6100 #
    jl 0  $f
    ci R0 , $7bff #
    jh 0 $f
    ai R0 , -$2000 #
0 $:    
    b *R11
end-code

\ Compare case-insensitively
Code COMPAREI   ( caddr u caddr' u' -- -1/0/1 )
    mov TOS , T2
    mov *SP+ , T1
    mov *SP+ , T4
    mov *SP+ , T3
    
    clr TOS
    clr R0
0 $:
    mov T2 , T2
    jeq 2 $f
    mov T4 , T4
    jeq 2 $f
    
    movb *T3+ , R0
    bl ' TOLOWER @>
    movb R0 , TOS
    
    movb *T1+ , R0
    bl ' TOLOWER @>
    
    s R0 , TOS
    jne 1 $f
    
3 $:    
    dec T2
    dec T4
    jmp 0 $b
2 $:
    \ longer string is greater
    mov T2 , TOS
    s   T4 , TOS
    NEXT
1 $: 
    sra TOS , 8 #
    NEXT
end-code

Code swpb
	swpb TOS
	NEXT
end-code

Code s>b    ( x|y -- x y )
    dect SP
    clr *SP
    movb TOS , 1 @>(SP)
    andi TOS , $ff #
    NEXT
end-code

Code b>s    ( x y -- x|y )
    movb 1 @>(SP) , TOS
    inct SP
    NEXT
end-code

Code sp@
	PUSH
	mov SP , TOS			\ stack minus TOS
	inct TOS
	NEXT
end-code

Code rp@
	PUSH
	mov RP , TOS
	NEXT
end-code

Code sp!
	mov TOS , SP
	mov -2 @>(SP) , TOS
\ 	don't pop!
	NEXT
end-code

Code rp!
	mov TOS , RP
	POP
	NEXT
end-code

Code 0=
	mov TOS , TOS
2 $:
	clr TOS			\ doesn't touch status
	jne 1 $f
	seto TOS
1 $: 
	NEXT
end-code

Code D0=
	soc *SP+ , TOS
	jmp 2 $b
end-code

Code 0<>
	mov TOS , TOS
2 $:
	seto TOS			\ doesn't touch status
	jne 1 $f
	clr TOS
1 $: 
	NEXT
end-code

Code D0<>
	soc *SP+ , TOS
	jmp 2 $b
end-code

Code 0<
	sra TOS , $0f #
	NEXT
end-code

Code D0<
	sra TOS , $0f #
	0POP
	NEXT	
end-code

Code 0>
	neg TOS
	sra TOS , $0f #
	NEXT
end-code

Code +
	a	*SP+ , TOS
	NEXT
end-code

\	TOS = hi1
\	*SP = lo1
\	@2(SP) = hi2
\	@4(SP) = lo2
Code D+		\ ( lo2 hi2 lo1 hi1 )
	mov TOS , T2
	mov *SP+ , T1
	POP
	a	T2 , TOS
	a	T1 , *SP
	jnc	1 $f
	inc	TOS
1 $: 
	NEXT
end-code

code NEGATE
	neg	TOS
	NEXT
end-code

\	changed dminus to dnegate
Code DNEGATE
1 $:
	inv TOS
	inv	*SP
	inc *SP
	jnc 0 $f
	inc TOS
0 $:
	NEXT
end-code

Code DABS
	mov TOS , TOS
	jlt 1 $b	\ DNEGATE
	NEXT
end-code

Code UM/MOD		( ud u1 -- u2 u3 )

\ Divide ud by u1, giving the quotient u3 and the remainder u2. 
\ All values and arithmetic are unsigned. An ambiguous
\ condition exists if u1 is zero or if the quotient lies outside 
\ the range of a single-cell unsigned integer.
\
\	ud=u3*u1+u2, 0<=u2<u1
\
	mov *SP+ , T1
	mov *SP , T2
	div TOS , T1
	mov T1 , TOS
	mov T2 , *SP
	NEXT
end-code
test" um/mod 2d. 6 um/mod 7 = swap 3 = and"
test" um/mod 1ff. f um/mod 22 = swap 1 = and"

\   Custom:  set a flag in a word
Code |!     
    soc *SP+ , *TOS
    POP
    NEXT
end-code

\   Custom:  reset a flag in a word
Code &!     
    szc *SP+ , *TOS
    POP
    NEXT
end-code

Code !
	mov *SP+ , *TOS
	POP
	NEXT
end-code

Code C!
	movb 1 @>(SP) , *TOS
	inct SP
	POP
	NEXT
end-code

\	Order says that highest word on stack goes
\	at lower address.
Code D!
	mov *SP+ , *TOS+
	mov *SP+ , *TOS+
	POP
	NEXT
end-code	

Code D@
	dect SP
	mov 2 @>(TOS) , *SP
	mov *TOS , TOS
	NEXT
end-code

Code 2+
	inct TOS
	NEXT
end-code

Code 2*
	a	TOS , TOS
	NEXT
end-code

Code 2/
	mov TOS , TOS
	jgt 1 $f
	inc TOS
1 $:
	sra TOS , 1 #
	NEXT
end-code

Code 1-
	dec TOS
	NEXT
end-code

Code 2-
	dect TOS
	NEXT
end-code

Code -
	s	TOS , *SP
	POP
	NEXT
end-code

\	!!! =cells (C_EqC) changed to ALIGNED
Code ALIGNED
	inc TOS
	andi TOS , $fffe #
	NEXT
end-code

Code CELLS
	a 	TOS , TOS
	NEXT
end-code

Code CELL+
	inct TOS
	NEXT
end-code

Code CHARS
	NEXT
end-code

Code CHAR+
	inc TOS
	NEXT
end-code

Code S>D
	PUSH
	sra TOS , $0f #
	NEXT
end-code

\	double
Code D>S
	POP
	NEXT
end-code

Code ABS
	abs TOS
	NEXT
end-code

\   Get sign of an argument
\  (I'm proud of this one, taking advantage of non-status-setting insts ;)
Code SGN
    mov TOS , TOS
    seto TOS
    jlt 1 $f
    clr TOS
    jeq 1 $f
    inc TOS
1 $:
    NEXT
end-code        


Code MIN
	c	*SP , TOS
	jgt	0 $f
	mov *SP , TOS
0 $: 0POP
	NEXT
end-code

Code MAX
	c	*SP , TOS
	jlt 0 $f
	mov	*SP , TOS
0 $: 0POP
	NEXT
end-code

Code U<
	mov TOS , T1
	clr TOS
	c	*SP+ , T1
	jhe 1 $f
	inv TOS
1 $: NEXT
end-code

Code U>
	mov TOS , T1
	clr TOS
	c 	*SP+ , T1
	jle	1 $f
	inv	TOS
1 $: NEXT
end-code

Code <
	mov TOS , T1
	seto TOS
	c	*SP+ , T1
	jlt 1 $f
	clr TOS
1 $:
	NEXT
end-code
test" < 9 9 < 0="
test" < 45 56 <"
test" < -45 56 <"
test" < -56 -45 <"

Code <=
	mov TOS , T1
	clr TOS
	c	*SP+ , T1
	jgt 1 $f
	seto TOS
1 $:
	NEXT
end-code

Code >
	mov TOS , T1
	seto TOS
	c	*SP+ , T1
	jgt 1 $f
	clr TOS
1 $:
	NEXT
end-code
test" > 8 8 > 0="
test" > 9 8 >"
test" > 7 8 > 0="
test" > 56 45 >"
test" > 56 -45 >"
test" > -45 -56 >"

Code >=
	mov TOS , T1
	clr TOS
	c	*SP+ , T1
	jlt 1 $f
	seto TOS
1 $:
	NEXT
end-code

Code =
	mov TOS , T1
	seto TOS
	c	*SP+ , T1
	jeq	1 $f
	clr TOS
1 $: NEXT
end-code
test" = 5 6 = 0="

Code <>
	mov TOS , T1
	seto TOS
	c	*SP+ , T1
	jne	1 $f
	clr TOS
1 $: NEXT
end-code
test" <> 5 6 <>"
test" <> 13 13 <> 0="

Code D=
	mov TOS , T1
	seto TOS
	c	4 @>(SP) , *SP		\ low word more likely to differ
	jne	1 $f
	c	2 @>(SP) , T1
	jeq 2 $f
1 $: clr TOS 
2 $: ai SP , 6 #
	NEXT
end-code	
test" d= 45. 23382838. d= 0="
test" d= 2938484. 2dup d="

Code WITHIN	( test low high -- flag)
	mov TOS , T1
	s *SP , T1		\ magnitude of range
	mov 2 @>(SP) , T2
	s *SP , T2		\ test - low
	ai SP , 4 #		\ cleanup stack
	seto TOS		
	c T2 , T1		\ (test-low) < range?
	jle 1 $f
	clr TOS
1 $:  
	NEXT
end-code

1 [if]
\	Match 'char' inside [caddr..caddr+u) and return length of word
Code (match) ( caddr u char -- u )
	mov TOS , T3	\ ch
	sla T3 , 8 #
	mov *SP+ , T2
	POP
	mov TOS , T1 	\ caddr
	mov T2 , T2
	jeq 1 $f
	ci T3 , $2000 #
	jne 2 $f
	
	\ checks for spaces allow ctrl chars
3 $:
	cb *TOS+ , T3
	jle 4 $f
	dec T2
	jgt 3 $b
	jmp 1 $f
2 $:
	cb *TOS+ , T3
	jeq 4 $f
	dec T2
	jgt 2 $b
	jmp 1 $f
4 $:	
    dec TOS
1 $:
	s T1 , TOS
	NEXT
end-code
	
[endif]

\ Code (TYPE) ( caddr n -- )
\	mov TOS , T2
\	POP
\ 1 $:
\	mov	T2 , T2
\	jeq 2 $f
\	movb rTOS *R+ , T1
\	srl T1 , 8 #
\	xop T1 , SYS^ #
\	#emit data
\	dec T2
\	jmp 1 $b
\ 2 $:
\	POP
\	NEXT
\ end-code

Code (TYPE) ( caddr n -- )
    mov TOS , T1
    mov *SP+ , TOS
    
[ has? enhanced-mode [if] ]
    ci TOS , $4000 #
    jhe 1 $f
[ [else] ]
    ci TOS , $2000 #
    jhe 1 $f
[ [endif] ]
    
    \ oops, string is in ROM, which won't be visible in the XOP's
    \ ROM, so just emit char-by-char
    
2 $:
   mov  T1 , T1
   jeq  4 $f
   movb rTOS *R+ , R0
   srl  R0 , 8 #
   xop  R0 , SYS^ #
   #emit data
   dec  T1
   jmp  2 $b
    
1 $:    
    xop TOS , SYS^ #
    #type data
 4 $:
    POP
    NEXT
end-code

DeferROM TYPE


Code >single-number ( caddr n base -- num f | bad-ch )
    mov 2 @>(SP) , T2   \ caddr
    clr T3              \ + T4 num
    mov *SP , T1        \ n
1 $:
    jeq 7 $f
    
    clr R0
    movb *T2+ , R0       \ fetch char
    ci R0 , $3000 #
    jl 8 $f
    ci R0 , $3900 #
    jle 2 $f            \ 0-9
    ci R0 , $4100 #
    jl 8 $f
    ci R0 , $5B00 #
    jle 3 $f            \ A-Z
    ci R0 , $6100 #
    jl 8 $f
    ci R0 , $7B00 #
    jh 8 $f
    ai R0 , -$2000 #    \ a-z
3 $:
    ai R0 , $3A00 $4100 - #    
2 $:
    ai R0 , -$3000 #
    srl R0 , 8 #
    
    \ in BASE range?
    c R0 , TOS
    jhe 9 $f
    
    mpy TOS , T3
    a R0 , T4
    mov T4 , T3
    
    dec T1
    jmp 1 $b

7 $:    
    inct SP
    mov T3 , *SP
    clr TOS
    NEXT
    
8 $:
    srl R0 , 8 #    
9 $: \ bad
    inct SP
    mov T2 , *SP
    mov R0 , TOS
    NEXT   
end-code

\   Parse a number at addr in the current base
\   Return 1 for single-precision number, -1 for double-precision, and 0 for not-a-number
\
Code NUMBER ( addr base -- u 1 | ud -1 | 0 )
    ai RP , -8 #
    clr *RP              \ dpl flag
    inc *RP              \ assume single precision
    
    clr 2 @>(RP)         \ sign flag
    
    mov R7 , 4 @>(RP)     \ save R8 and R9 
    mov R8 , 6 @>(RP)     \ save R8 and R9 
    
    \ TOS is base
    
    mov *SP , T4        \ caddr
    
    clr T1              \ T1:T2 = number
    clr T2
    
    movb *T4+ , T3      \ counted string
    srl T3 , 8 #        \ n
    jeq 8 $f            \ nothin'
    
    \ check negative
    clr R0
    movb *T4+ , R0
    ci R0 , $2D00 #
    jne 5 $f

    seto 2 @>(RP)       \ negate later
    dec T3              
    jeq 8 $f            \ um, nothing
    
    \ check next char after negate
    
    movb *T4+ , R0
    
5 $:    
    ci R0 , $2600 #     \ '&' ?
    jne 5 $f
    
    li TOS , &10 #       \ decimal
    jmp 6 $f
         
5 $:
    ci R0 , $2400 #     \ '$' ?
    jne 4 $f

    li TOS , &16 #      \ hex      
    jmp 6 $f
    
1 $:
    jeq 9 $f
    clr R0
    movb *T4+ , R0       \ fetch char
4 $:    
    ci R0 , $3000 #
    jl 7 $f
    ci R0 , $39FF #
    jle 2 $f            \ 0-9
    ci R0 , $4100 #
    jl 8 $f
    ci R0 , $5BFF #
    jle 3 $f            \ A-Z
    ci R0 , $6100 #
    jl 8 $f
    ci R0 , $7BFF #
    jh 8 $f
    ai R0 , -$2000 #    \ a-z
3 $:
    ai R0 , $3A00 $4100 - #    
2 $:
    ai R0 , -$3000 #
    srl R0 , 8 #
    
    \ in BASE range?
    c R0 , TOS
    jhe 8 $f
    
    \ T1:T2 (R2:R3) is number
    \ R0 = digit value
    \ TOS = base
    \ R7:R8 = work
    
    \ multiply T1:T2 by base
    
    \ add hi*base to accumulator
    clr R8
    mov T1 , R7
    jeq 0 $f        \ save effort if number is small
    
    mpy TOS , R7     \ R7:R8 = hi*base.hi | hi*base.lo

0 $:    
    \ add lo*base to accumulator
    mov T2 , T1
    
    mpy TOS , T1
    a R8 , T1
    
    \ now add digit
    a R0 , T2
    jnc 6 $f
    inc T1
    
6 $:    
    dec T3
    jmp 1 $b

7 $: \ maybe bad
    ci R0 , $2E00 #     \  '.'
    jne 8 $f
    
    seto *RP            \ double
    jmp 6 $b
    
8 $: \ bad
    ai RP , 4 #
    ai SP , 4 #
    clr TOS
    jmp 5 $f
    
9 $:
    \ exit cleanly
    
    \ get the flag
    mov *RP+ , TOS
    
    \ negate as needed
    jlt 2 $f

    \ single-prec
    mov *RP+ , R0
    jeq 3 $f
    neg T2 
3 $:
    mov T2 , *SP
    jmp 5 $f
    
2 $:    
    \ double-prec
    mov *RP+ , R0
    jeq 1 $f
    
    inv T1
    inv T2
    inc T1
    jnc 1 $f
    inc T2
1 $:
    \ push the two number pieces
    dect SP
    mov T2 , 2 @>(SP)
    mov T1 , *SP

5 $:
    mov *RP+ , R7    
    mov *RP+ , R8    
    NEXT   
    
end-code



