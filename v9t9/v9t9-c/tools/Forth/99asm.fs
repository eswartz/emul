\	99asm.fs					-- 9900 assembler
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
\	$Id: 99asm.fs,v 1.8 2009-01-03 23:46:45 ejs Exp $

\
\	syntax:
\
\	<opcode> <operand> [ , <operand> ]
\	<opcode> knows how many operands to expect, and
\	the instruction is assembled as soon as the last
\	operand is executed.
\
\	Assembler syntax:
\
\	reg 	= R 0..15
\	idx 	= *R 0..15
\	addr	= <addr> @>
\	idx+	= *R+ 0..15
\	idx+addr= <addr> @>(R) 0..15
\	imm/cnt	= 0..ffff #
\
\	If fat-ass defined:	
\		R0...R15, *R0...*R15, *R0+...*R15+, @>(R0)..@>(R15)
\
\	Jump targets and labels:
\		jmp 3 $f	jmp 3 $b	\ forward, backward
\	3 $:						\ definition
\
\	<opcode>  			( op@ arg )
\	<first operand> 	( op'@ arg reg/addr/imm )	
\	<second operand>	( op''@ arg reg/addr/imm )
\


[IFUNDEF] >cross
: T ; immediate
: H ; immediate
[ELSE]
[THEN]
hex

\ only forth also definitions
\	base address of image -- here for non-cross, 0 for cross

only forth also definitions
vocabulary assembler 


variable tbase 

: >assembler
	also assembler definitions previous
; immediate

: <A> previous forth also assembler ; immediate

\ only forth also definitions
cr order cr
>assembler also assembler

\	Define FAT-ASS to get *R5+ as well as 5 *R+ 
1 constant fat-ass

: >4u. 	( # -- printed )
	[char] > emit s>d <# # # # # #> type
;

: >1u.
	[char] > emit s>d <# # #> type
;

: >2u.
	[char] > emit s>d <# # # #> type
;

: R.
	[char] R emit base @ $A base ! swap s>d <# #S #> type base !
;

: 2.
	s>d <# # # #> type
;

\	operand types: the arg word says
\	what we expect for this opcode.
\	low byte has two of the (... values in its nybbles
\	(>1 shift to the first one, >2 to the second)
\	the high nybble tells which args we've gotten.

1	constant (G		\ general
2	constant (4		\ 4-bit field
2	constant (C		\ register or count
2 	constant (R		\ alias
3	constant (16	\ data (whole word)
3	constant (D		\ data (whole word)
4	constant (8		\ 8-bit field
4	constant (B		\ bit field (8 bits)
5	constant (J		\ jump (8 bits)
6	constant (I		\ immediate
7	constant (S		\ register at bit 6
F	constant (#		\ type mask

0	constant >1		\ shift to arg 1
4	constant >2		\ shift to arg 2

8000 constant	1)	\ got arg 1
4000 constant	2)	\ got arg 2

variable InstGoing	\ true if incomplete opcode
order
InstGoing	off

: InstGoing?
	InstGoing @ 0= if
		cr ." Too many arguments"
		1 throw
	then
;

: asmerror"
	['] cr , postpone (.") ," ['] cr ,
; immediate

\	opcode types

: .argdesc	( typ -- )
	dup (G = if ." general" else
	dup (C = if ." register/count" else
	dup (D = if ." data" else
	dup (B = if ." bit count" else
	dup (J = if ." jump" else
	dup (I = if ." immediate" else
	dup (S = if ." register" else
	dup 0 = if ." end of inst" else		\ error code
				." ???arg = " dup .
	then then then then then then then then
	drop
;

\ 0 [if]
: .inst ( op@ arg -- )
	." -- "
	drop T here <A> swap ?do
		i T @ <A> ." >" . 
		T cell <A>		\ !!! tcell
	+loop
;
\ [then]

order
: >op)	( arg -- typ' )
	dup 2) and if drop 0 else
	dup 1) and if >2 rshift (# and else
	>1 rshift (# and then then
;

: >end? ( op@ arg -- op@ arg | )
	dup 2) and if
		true				\ got two args, quit
	else dup 1) and if
		dup >2 rshift (# and 0=	\ if non-zero, want second arg
	else
		dup (# >2 lshift (# or and 0=	\ want any args?
	then then
	if 2drop \ .inst 
		InstGoing off 
	then
;

0 [if]
: <<)	( arg typ' -- arg' shiftcnt )
	over 1) and if		\ ( arg typ' arg&1 )
		dup (G = if	 	\ ( arg t/f )
			drop
			2) or 6 	\ ( arg' shiftcnt )
		else dup (D = if
			drop
			2) or 16	\ i.e., a whole new word
		else (R = if
			2) or 4
		else
			2) or 4 
		then then then
	else
		dup (J = if 
			drop
			1) or -1 
		else (D = if
			1) or 16
		else
			1) or 0 
		then then
	then
;

: op)	( arg typ -- arg' shift 1 | arg 0 )
	swap				\ ( typ arg )

	dup >op)			\ ( typ arg typ' )
	>r 					\ save typ' ( R: arg typ' )

	swap r@				\ ( arg typ typ' )

	\ (G matches (G or (C
	2dup				\ ( arg typ typ' typ typ' )
	(G = swap (C = and	\ ( arg typ typ' typ'==(G&&typ==(C )
	>r
	\ (D matches (D or (I
	2dup				\ ( arg typ typ' typ typ' )
	(D = swap (I = and	\ ( arg typ typ' typ'==(D&&typ==(I )
	>r
	\ (I matches anything
	over (I =
	>r
	= 
	r> r> r> or or or

	\  ( arg t/f )
	0= if
		." wanted " r> .argdesc cr
		
		InstGoing off
		0				\ error
	else
		r> 				\ ( arg typ' )
		<<)				\ ( arg' shf )
		1				\ success
	then
;

[else]
: <<)	( arg typ' -- arg' shiftcnt )
	over 1) and if		\ ( arg typ' arg&1 )
		case
\		dup (G = if	 	\ ( arg t/f )
		(G of
\			drop
			2) or 6 	\ ( arg' shiftcnt )
		endof
\		else dup (D = if
		(D of
\			drop
			2) or 16	\ i.e., a whole new word
		endof
\		else dup (R = if
		(R of
\			drop
			2) or 4
		endof
\		else (S = if
		(S of
			2) or 6
		endof
		
			2) or 4 
\		then then then then
		endcase
	else
		dup (J = if 
			drop
			1) or -1 
		else (D = if
			1) or 16
		else
			1) or 0 
		then then
	then
;

: op)	( arg typ -- arg' shift 1 | arg 0 )
	dup >r				\ save orig typ
	swap				\ ( typ arg )

	dup >op)			\ ( typ arg typ' )
	>r 					\ save typ' ( R: arg typ' )

	swap r@				\ ( arg typ typ' )

	\ (G matches (G or (C or (S
	2dup				\ ( arg typ typ' typ typ' )
	(G = swap (C = and	\ ( arg typ typ' typ'==(G&&typ==(C )
	>r
	2dup				\ ( arg typ typ' typ typ' )
	(S = swap (C = and	\ ( arg typ typ' typ'==(G&&typ==(C )
	>r
	\ (D matches (D or (I
	2dup				\ ( arg typ typ' typ typ' )
	(D = swap (I = and	\ ( arg typ typ' typ'==(D&&typ==(I )
	>r
	\ (I matches anything
	over (I =
	>r
	= 
	r> or
	r> or
	r> or
	r> or

	\  ( arg t/f )
	0= if
		." wanted " rdrop r> .argdesc cr
		
		InstGoing off
		0				\ error
	else
		r> rdrop		\ ( arg typ' )
		<<)				\ ( arg' shf )
		1				\ success
	then
;

[endif]

	
\	set bits in instruction and tell if done
\	-1 on stack is flag that new args aren't needed
\	(don't need to test explicitly, since new inst will drop it,
\	and new op will match (1 and (2 and error out).
order cr
: >op!	( op@ arg' bits -- op@ arg' | )
	2 pick T @ <A> or 		\ set bits ( op@ arg' bits )
	2 pick T ! <A>
	>end? 
;
order

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

: Register?
	dup f invert and if
		." suspicious register R" dup . cr
	then
;

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\

\ >assembler also asm-hidden

: R	( op@ arg reg# -- op@ arg )
	InstGoing? 
	Register?
	>r
	(C op)				\ ( op@ arg' shift 1 | opc arg 0 )
	if
		r>				\ ( op@ arg' shift reg# )
		swap lshift		\ ( op@ arg' regmask )
		>op!			\ ( op@ arg' | )
	else
		rdrop
	then
;
cr ." R:" order cr

: R: ( num -- )
	create 
		,
	does>
		@ R 
;

[IFDEF] FAT-ASS
0 R: R0		1 R: R1 	2 R: R2		3 R: R3	
4 R: R4		5 R: R5		6 R: R6		7 R: R7
8 R: R8		9 R: R9		$A R: R10	$B R: R11
$C R: R12	$D R: R13	$E R: R14	$F R: R15
[THEN]

\ \\\\\\\\\\\\\\

: *R ( op@ arg reg# -- op@ arg )
	InstGoing?
	Register?
	>r
	(G op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r>				\ ( op@ arg' shift reg# )
		10 or			\ add *Rx mask
		swap lshift		\ ( op@ arg' regmask )
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then
;

: *R: ( num -- )
	create 
		,
	does>
		@ *R
;

[IFDEF] FAT-ASS
0 *R: *R0		1 *R: *R1	 	2 *R: *R2		3 *R: *R3	
4 *R: *R4		5 *R: *R5		6 *R: *R6		7 *R: *R7
8 *R: *R8		9 *R: *R9		$A *R: *R10		$B *R: *R11
$C *R: *R12		$D *R: *R13		$E *R: *R14		$F *R: *R15
[THEN]

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

: *R+ ( op@ arg reg# -- op@ arg )
	InstGoing?
	Register?
	>r
	(G op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r>				\ ( op@ arg' shift reg# )
		30 or			\ add *Rx+ mask
		swap lshift		\ ( op@ arg' regmask )
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then
;

: *R+: ( num -- )
	create 
		,
	does>
		@ *R+
;

[IFDEF] FAT-ASS
0 *R+: *R0+		1 *R+: *R1+	 	2 *R+: *R2+		3 *R+: *R3+
4 *R+: *R4+		5 *R+: *R5+		6 *R+: *R6+		7 *R+: *R7+
8 *R+: *R8+		9 *R+: *R9+		$A *R+: *R10+	$B *R+: *R11+
$C *R+: *R12+	$D *R+: *R13+	$E *R+: *R14+	$F *R+: *R15+
[THEN]

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

: @>(R) ( op@ arg addr reg# -- op@ arg )
	InstGoing?
	Register?
	2>r					\ hide reg# and addr#
	(G op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r>				\ ( op@ arg' shift reg# )
		20 or			\ add *Rx+ mask
		swap lshift		\ ( op@ arg' regmask )
		r> T A, <A>		\ add address
		>op!			\ ( op@' arg' | )
	else
		2rdrop
	then
;

: @>(R): ( num -- )
	create 
		,
	does>
		@ @>(R)
;

[IFDEF] FAT-ASS
0 @>(R): @>(R0)		1 @>(R): @>(R1)	 	2 @>(R): @>(R2)		3 @>(R): @>(R3)
4 @>(R): @>(R4)		5 @>(R): @>(R5)		6 @>(R): @>(R6)		7 @>(R): @>(R7)
8 @>(R): @>(R8)		9 @>(R): @>(R9)		$A @>(R): @>(R10)	$B @>(R): @>(R11)
$C @>(R): @>(R12)	$D @>(R): @>(R13)	$E @>(R): @>(R14)	$F @>(R): @>(R15)
[THEN]



: @> 	( op@ arg addr reg# -- op@ arg )
	InstGoing?
	>r					\ hide addr#
	(G op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		20 				\ add @>xxxx mask
		swap lshift		\ ( op@ arg' regmask )
		r> T A, <A>			\ add address
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then
;

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

: # 	( op@ arg num -- op@ arg )
	InstGoing?
	>r					\ hide num
	(I op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r> swap 		\ ( op@ arg' data shift )
		dup 16 = if
			drop T , <A> 0	\ compile new word
		else
			lshift		\ ( op@ arg' data )
		then
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then
;

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

: $+	( op@ arg offset -- op@ arg )
	InstGoing?

	>r					\ hide num
	(J op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r> swap 		\ ( op@ arg' data shift )
		drop
		2/ 1-			\ calc offset
		dup -81 80 within
		0= if
			." warning: jump out of range = " dup . cr
		then
		ff and
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then

;

: $		( op@ arg addr -- op@ arg )
	InstGoing?

	>r					\ hide num
	(J op)				\ ( op@ arg' shift 1 | op@ arg 0 )
	if
		r> swap 		\ ( op@ arg' data shift )
		drop
		T here <A> 2dup 2>r -
		2/ 				\ calc offset
		dup -81 80 within
		0= if
			2r>
			." warning: jump from " 2 - . ." out of range to " . cr
		else
			2rdrop
		then
		ff and
		>op!			\ ( op@' arg' | )
	else
		rdrop
	then

;

\	values are relative to tbase
\	<reschain> <addr>
\	reschain==ffff if no unresolved references
\	else reschain points to jump instruction chain
\	addr holds value of label; may be valid even if reschain <> ffff
\	(i.e., when redefining a label); ffff if not defined
\

create #Labels	10 2* cells allot

: #>Label	( num -- laddr )
	dup 0>= over 10 < and if
		2* cells #Labels +
	else
		abort" label number out of range (0-9): " .
		#Labels
	then
;

: Label>#	( laddr -- num )
	#Labels - 2 cells /
;

: clear-labels
	#Labels 10 0 do 
		ffff over ! ffff over cell+ ! 2 cells +
	loop
	drop
;

\	is the label unresolved?
\
: $>fwd? ( label -- t/f )
	@ ffff <>
;


\	add reference to label by making
\	list through the offsets of other jumps:
\
\	>xxxx jmp 1 $f	--> jmp $
\	...
\	>yyyy jne 1 $f	--> jmp >xxxx
\	...
\	$f must call $ to fixup the jump.
\
: $>ref ( label addr -- paddr )
	over @				\ label addr paddr
	dup ffff = if drop T here <A> 2 - then	\ undefined jmp
	-rot swap !		\ paddr
;

\	reference a label forward
\	jmp 3 $f
\
: $f	( op@ arg num -- )
	#>Label			\ get addr
	T here <A> 2 - $>ref	\ add reference
	$				\ "jump" to previous ref
;

\	resolve reference to label
\	undefined labels --> 0
: $>res ( label -- resaddr )
	cell+ @ dup ffff = if
		." error: label not defined yet" cr
	then
;

\	reference a label backward
\	jmp 3 $b
: $b	( op@ arg num -- )
	#>Label 			\ get addr
	$>res			\ resolve (or error)
	tbase @ + $		\ jump
;

\	define label
\	for all previous references, fix them up
\	and store new address at label
\
: $>fixup ( raddr opc taddr -- )
	rot	dup >r		\ ( opc taddr raddr )
	- 2 - 2/		\ ( opc jmpoffs )
	swap FF00 
	and or			\ ( opc' )
	r> T ! <A>
;

: op>joffs ( opc -- jump-offset )
	ff and dup 80 >= if ff invert or then 2* 2 +
;

: $>resolve ( label taddr -- )
	over @ 			\ ( label taddr raddr )
	swap >r
	begin			\ ( label raddr )
		dup			\ save raddr
		T @ <A>
		
		2dup r@ $>fixup
		op>joffs	\ ( label raddr joffs )
		swap over + swap
		0=
	until
	rdrop 2drop
;


: $>set ( label taddr -- )
	over Label># ." Label " . ." = @" dup >4u. cr
	over ffff swap !		\ not unresolved
	swap cell+ !			\ save new addr
;

: $>def	( label addr -- )
	over $>fwd? if
		2dup $>resolve
	\	else
	\ 	label redefined
	then
	$>set
;

\	define a label
\	3 $:
: $:	( num -- )
	#>Label			\ get addr
	T here <A> $>def		\ define
;


\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

\	Instruction formats.

: inst:		( opc arg1 arg2 -- )
	create
		>2 lshift or , ,	
	does>

		\ see if inst was finished
		InstGoing @ if
			asmerror" Previous instruction not finished!"
			nip nip
		then
		InstGoing on
		T here <A>
		\ read opcode and write to memory
		swap dup cell + @ T , <A>
		\ read argflags
		@
		\ leaves ( op@ flags )
		>end?
;

: DATA
	t , <A>
;

\ cr cr cr order cr cr cr 

0000	0	0	inst:	(DATA)
0200	(R	(D	inst:	LI
0220	(R	(D	inst:	AI
0240	(R	(D	inst:	ANDI
0260	(R	(D	inst:	ORI
0280	(R	(D	inst:	CI
02A0	(R	0	inst:	STWP
02C0	(R	0	inst:	STST
02E0	(D	0	inst:	LWPI

0300	(D	0	inst:	LIMI
0340	0	0	inst:	IDLE
0360	0	0	inst:	RSET
0380	0	0	inst:	RTWP
03A0	0	0	inst:	CKON
03C0	0	0	inst:	CKOF
03E0	0	0	inst:	LREX

0400	(G	0	inst:	BLWP
0440	(G	0	inst:	B
0480	(G	0	inst:	X
04C0	(G	0	inst:	CLR
0500	(G	0	inst:	NEG
0540	(G	0	inst:	INV
0580	(G	0	inst:	INC
05C0	(G	0	inst:	INCT
0600	(G	0	inst:	DEC
0640	(G	0	inst:	DECT
0680	(G	0	inst:	BL
06C0	(G	0	inst:	SWPB
0700	(G	0	inst:	SETO
0740	(G	0	inst:	ABS

0800	(R	(C	inst:	SRA
0900	(R	(C	inst:	SRL
0A00	(R	(C	inst:	SLA
0B00	(R	(C	inst:	SRC

0C00	(C	0	inst:	DSR			\ v9t9
0D40	0	0	inst:	KYSL		\ v9t9
0D60	(R	0	inst:	TICKS		\ v9t9
0DC0	(R	0	inst:	EMITCHAR	\ v9t9
0DE0	0	0	inst:	DBG			\ v9t9
0DE1	0	0	inst:	-DBG		\ v9t9

1000	(J	0	inst:	JMP
1100	(J	0	inst:	JLT
1200	(J	0	inst:	JLE
1300	(J	0	inst:	JEQ
1400	(J	0	inst:	JHE
1500	(J	0	inst:	JGT
1600	(J	0	inst:	JNE
1700	(J	0	inst:	JNC
1800	(J	0	inst:	JOC
1900	(J	0	inst:	JNO
1A00	(J	0	inst:	JL
1B00	(J	0	inst:	JH
1C00	(J	0	inst:	JOP

1D00	(B	0	inst:	SBO
1E00	(B	0	inst:	SBZ
1F00	(B	0	inst:	TB

2000	(G	(S	inst:	COC		\ not (R
2400	(G	(S	inst:	CZC		\ not (R
2800	(G	(S	inst:	XOR		\ not (R
2C00	(G	(S	inst:	XOP

3000	(G	(S	inst:	LDCR
3400	(G	(S	inst:	STCR
3800	(G	(S	inst:	MPY
3C00	(G	(S	inst:	DIV

4000	(G	(G	inst:	SZC
5000	(G	(G	inst:	SZCB
6000	(G	(G	inst:	S
7000	(G	(G	inst:	SB
8000	(G	(G	inst:	C
9000	(G	(G	inst:	CB
A000	(G	(G	inst:	A
B000	(G	(G	inst:	AB
C000 	(G	(G	inst:	MOV	
D000 	(G	(G	inst:	MOVB
E000 	(G	(G	inst:	SOC
F000 	(G	(G	inst:	SOCB


\	Disassembler

\ >assembler-hidden

create opcode-table
	' (DATA) , ' LI , ' AI , ' ANDI , ' ORI , ' CI , ' STWP , ' STST , ' LWPI ,
	' LIMI , ' IDLE , ' RSET , ' RTWP , ' CKON , ' CKOF , ' LREX ,
	' BLWP , ' B , ' X , ' CLR , ' NEG , ' INV , ' INC , ' INCT , 
	' DEC , ' DECT , ' BL , ' SWPB , ' SETO , ' ABS ,
	' SRA , ' SRL , ' SLA , ' SRC , ' DSR , ' KYSL , ' EMITCHAR , 
	' DBG , ' -DBG , ' TICKS ,
	' JMP , ' JLT , ' JLE , ' JEQ , ' JHE , ' JGT , ' JNE , ' JNC ,
	' JOC , ' JNO , ' JL , ' JH , ' JOP , ' SBO , ' SBZ , ' TB ,
	' COC , ' CZC , ' XOR , ' XOP , ' LDCR , ' STCR , ' MPY , ' DIV ,
	' SZC , ' SZCB , ' S , ' SB , ' C , ' CB , 
	' A , ' AB , ' MOV , ' MOVB , ' SOC , ' SOCB ,
	0 ,

: inst>info	( xt -- flags opc )
	>body dup @ swap cell + @
;

: inst>op	( xt -- opc )
	inst>info nip
;
: inst>args	( xt -- flags )
	inst>info drop
;

: instlookup ( opc -- xt )
	>r
	opcode-table cell+		\ skip (DATA)
	begin
		dup @				\ ( opc ptr xt )
		dup if
			inst>op			\ ( opc ptr opc' )
			r@ U<=
		else
			0 nip
		then
	while
		cell+
	repeat

	\ here, ptr points to opcode past the one we want.
	cell -
 	@

	rdrop
;


: op-show-(G ( addr opc arg# typ -- addr )
	<<) nip rshift
	dup 30 and		
	swap F and
	over 0 = if R. else
	over 10 = if [char] * emit R. else
	over 20 = if 
		[char] @ emit  	\ ( addr typ reg )
		2>r 2 + dup T @ <A> >4u. 2r>
		?dup if [char] ( emit R. [char] ) emit then
	else
		[char] * emit R. [char] + emit
	then then then
	drop
;

: op-show-(4  ( addr opc arg# typ -- addr )
	over >r <<) nip rshift F and 
	r> 0= if R. else >1u. then
;
: op-show-(8  ( addr opc arg# typ -- addr )
	2drop FF and >2u.
;
: op-show-(16  ( addr opc arg# typ -- addr )
	2drop drop 2 + dup T @ <A> >4u.
;
: op-show-(J ( addr opc arg# typ -- addr )
	2drop FF and
	dup 80 u>= if ff invert or then
	1 lshift over + 2 +
	>4u.
;


: op-show	( addr typ opc arg# -- addr )
	2 pick >r
	r@ (G = if rot op-show-(G else
	r@ (4 = if rot op-show-(4 else
	r@ (16 = if rot op-show-(16 else
	r@ (8 = if rot op-show-(8 else
	r@ (J = if rot op-show-(J else
	r@ (S = if rot op-show-(4 else
	r@ (I = if rot op-show-(16 else drop ." [???op=" . ." |typ=" . 
	then then then then then then then
	rdrop
;

: instdis ( addr opc -- addr )
	dup >r
	instlookup

	\ get name
	dup >head name>string
	2dup
	type
	nip negate 6 + spaces  

	inst>args >r
	r@ >1 rshift (# and dup
	if 2r@ drop 0 op-show
		r@ >2 rshift (# and  dup
		if [char] , emit 
			2r@ drop 1) op-show else
			drop then
	else drop then

	2rdrop
;

8 constant per-line

[IFUNDEF] >target
\ only forth also definitions
[ELSE]
>target
[THEN]

: dis ( addr cnt -- )
	over + swap ?do
		i >4u.		\ print address
		[char] = emit
		i T @ <A>
		dup >4u.	\ print opcode
		20 emit
		i swap instdis 
		cr
		i - 2 +		\ add #cells read
	+loop
;


: tdump ( addr cnt -- )
	dup 0< if exit then
	over + swap ?do
		i >4u. $20 emit [char] = emit $20 emit
		i' i per-line + min dup i ?do 
			i  T c@ <A> 2. $20 emit
		loop	
		i - 
		dup
			per-line swap - 0 ?do ."    " loop
		dup
			0 ?do j i +  T c@ <A> dup $20 $7f within 0= 
				if drop [char] . then emit 
			loop
		cr
	+loop
;



\	syntactical sugar for assembler

\ >assembler-hidden  also asm-hidden

>assembler

get-order
[IFUNDEF] >cross
[ELSE]
only forth also cross
: [  postpone [ ;
: ]  postpone ] ;

[THEN]
order cr cr
set-order

: , 	( opc arg -- opc arg )
	InstGoing @ if
		dup 1) and 0= if
\		." no operand or unresolved operand preceding comma"
			InstGoing off
			2drop
		then
	else
		,
	then
;

[IFDEF] >cross
0 tbase !
[ELSE]
T here <A> tbase !
[THEN]

clear-labels

\ >root

[IFDEF] >cross
>cross
[THEN]



