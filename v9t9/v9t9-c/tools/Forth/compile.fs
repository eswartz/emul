\	compile.fs					-- FORTH compiler words
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
\	$Id: compile.fs,v 1.25 2009-02-25 04:19:56 ejs Exp $


\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

\	Words required by cross-compiler, not in standard.

\	noop lit :dodoes :docol :dovar :douser :docon ;s branch ?branch

[IFUNDEF] noop
: noop ;
[THEN]

[IFUNDEF] lit
error" need prim lit"
[THEN]

[IFUNDEF] :dodoes
error" need prim :dodoes"
[THEN]

[IFUNDEF] :docol
error" need prim :docol"
[THEN]

[IFUNDEF] :dovar
error" need prim :dovar"
[THEN]

[IFUNDEF] :douser
error" need prim :douser"
[THEN]

[IFUNDEF] :docon
error" need prim :docon"
[THEN]

[IFUNDEF] :dodefer
error" need prim :dodefer"
[THEN]

[IFUNDEF] ;s
error" need prim ;s"
[THEN]

[IFUNDEF] branch
error" need prim branch"
[THEN]

[IFUNDEF] ?branch
error" need prim ?branch"
[THEN]

\ \\\\\\\\\\\\\\\\\\\

: lastnfa
    latest lfa>nfa
;

: lastxt
	lastnfa nfa>xt
;

1 include common.fs

\ -------------------------------------------

\ !!! must not be hidden else cross.fs fails to find it
: does!	( addr cfa -- )
	bl-dodoes over !
	cell+ !
;

\	For a word which pushes the PFA
| : does,	( code -- )
	here does! 2 cells allot
;

\	For a code word
| : code, ( addr -- )
	,
;

| : docol!	( cfa -- )
	bl-docol swap !
	-2 dp +!			\ lose extra CFA word
;

| : xt! ( addr cfa -- )
	cell+ !
;

\ called for creating words to inject :docol code into definition
: docol,	( addr -- )
	BL-DOCOL ,
;

| : dodefer!	( cfa -- )
	BL-@ swap !
	-2 dp +!			\ lose extra CFA word
	['] :dodefer ,
;

| : docon!  ( cfa -- )
    BL-DOCON swap !
    -2 dp +!            \ lose extra CFA word
;

\ --------------------------------------------

[IFUNDEF] (compile)
\	compile the following word in the IP stream
\	(needed cross compiler)
: (compile)
	r> dup cell+ >r @ ,	
;
[THEN]


\ \\\\\\\\\\\\\\\\\\\\\\\\

[[[ $20 checkmemory ]]]
: ?error
	swap if
		message abort
	else
		drop
	then
;

\ : ?exec
\ 	state @ -14 ?error
\ ;

: ?comp
	state @ 0= -&14 ?error
;

\	Differs from old version:
\	set $80 for visible definition.
\	Note: this affects xt>nfa, since it stops when
\	the LFA's length byte has $80.  
\	If we for some reason to "latest xt>nfa" while compiling
\	a word, it will fail...  ;)
: smudge
	lastnfa 
	dup c@ |srch xor swap c!
;


\	Given an address inside the current bank the desired
\	gap space, return 0 if there is enough room, else
\	return start address of new bank.

0 [if]

: (bank+)	  ( addr gap -- 0 | bank )
	>r
	dup (dp0  dp0) 1fff or  within if
		dp0) r@ -  >= if 
		   	(dp1
		else
			0
		then
	else
		drop 0
	then
	rdrop
;

\	Adjust DP to the next bank if necessary
: (bank?)
	.s
	here $180 (bank+) dup .s if					\ $80 word, $100 tib
		." Switching RAM banks to " dup u. cr
		dp ! .s
	else
		drop
	then
	\ here ." [" u. ." ]"
;

[then]

\ Split up big routine for bank crossing

| : parse-name ( c"..."  -- caddr 0 | nfa 1|-1 ) \ "word

    (skip-spaces) bl parse ( caddr u )
    ?dup 0= if
        -&16 message quit
    then

    (lookup)
;

| : name"  ( c"word" -- cstr ) 
	\ Get name
	parse-name
	
	if ." redefining " drop here
	then 
;

\ Optionally pre-parse the string in the input stream and manipulate the dictionary
\ before the definition is started.
DeferROM (creat)  ( c"xxx" -- c"xxx" )

: CREATE
    (creat)     

	align

[ has? profiling [if] ]
	\ Space for profiling 
	here  >r 0 ,
[ [then] ]

    \ Put LFA --> ptr to previous LFA
    here 
    latest ,

	name"
    
    \ Get space for name
    c@ width min
    1+ aligned allot
        
[ has? profiling [if] ]
	\ Add profiling point
	r> ,
[ [then] ]

	\ current @ !		\ !!!
	>latest !
	
	lastnfa id.
    
	smudge

	\ lay down CFA
	['] :dovar does,
;


| : message     ( flag # -- )
    dup -&13 = if ." undefined" else
    dup -&9 = if ." interpret mode only" else
    dup -&10 = if ." loading only" else
    dup -&8 = if ." block i/o error" else
    dup -&14 = if ." compilation only" else
    dup -&16 = if ." empty name" else
    dup -&22 = if ." control nesting" else
    dup -&23 = if ." not a DEFERred word" else
    ." ?" dup .
    then then then then then then then then
    drop
    cr
;

\ \\\\\\\\\\\\\

: per-line
    win@ drop s>b drop  8 - 2 rshift 1- 1 max  aligned
;

: 2u.
	0 <# # # #> type
;

: 4u.
	0 <# # # # # #> type
;

\ dump one line
: (dmpln) ( addr cnt xt -- )
	>r	\ save xt
	( addr cnt )
	over 4u. space [char] = emit space
	2dup over + swap
	do 
		i j execute 2u. space
	loop	
	( addr cnt )
	per-line over ?do 3 spaces loop
	( addr cnt )
	0 ?do dup i + j execute dup $20 $7f within 0= 
			if drop [char] . then emit 
	loop drop
	rdrop
	cr
;

: (dmp) ( addr cnt xt -- )
	>r cr
	over +	 ( addr addr+cnt )
	swap
	do
		i per-line 
		j (dmpln)
		(pause?) if leave else per-line then
	+loop
	rdrop
;

: dump
	base @ >r hex
	['] c@ (dmp)
	r> base !
;

: vdump
	base @ >r hex
	['] vc@ (dmp)
	r> base !
;

\ \\\\\\\\\\\\\\\\\\\\\\

[IFUNDEF] STATE
User STATE
[THEN]

[IFUNDEF] [
: [
	0 state !
; immediate
[THEN]


[IFUNDEF] [COMPILE]
: [COMPILE]
	bl word find
	if
		postpone literal compile,
	else
    	huh?
	then

; immediate
[THEN]

[IFUNDEF] POSTPONE
: POSTPONE
	bl word find
	if
		compile,
	else
		huh?
	then		
; immediate
[THEN]

[IFUNDEF] COMPILE,
: COMPILE,
	,
;
[THEN]



User csp

| User leave-list		\ pointer to linked list of leaves, 
					\ @ branch pos of last leave


: !csp
	sp@ csp !
	0 leave-list !
;

: ?csp
	sp@ csp @ - -&22 ?error
;

\ Optionally add behavior at a colon definition
\ User #(:)  ( -- )

\ Optionally finish a definition at a semicolon
\ User #(;) ( -- )

\ | : exec-hook
\    @ ?dup if execute then
\ ;

: :
	\ ?exec
	!csp
	create smudge ]
	lastxt docol!
   \ #(:) exec-hook
;

: ;
   \ #(;) exec-hook
	?csp
	[compile] ;s
	smudge postpone [
; immediate

\	change the cfa of the newly created word to
\	jump to the code inside the creating word after does>
: (does>)
	r> lastxt xt!		\ drops a level, so code following dodoes> 
						\ is not executed during create stage
;

\	at compile time, ensure that the runtime of the creating word
\	will modify the cfa of the newly created word to point into
\	the embedded colon definition (docol,)
: DOES>
	[compile] (does>)
	docol,
; immediate

: ?pairs	( i*x tag tag' -- )
	- -&22 ?error
;

\ is there an official name for this?
[IFUNDEF] BACK
: BACK 
	here - ,
;
[THEN]

[IFUNDEF] BEGIN
: BEGIN
	?comp here 1 
; immediate
[THEN]

[IFUNDEF] IF
: IF
	?comp
	[compile] ?branch
	here 0 ,
	2
; immediate
[THEN]

[IFUNDEF] ELSE
: ELSE
	?comp
	2 ?pairs
	[compile] branch
	here 0 ,
	swap 2
	postpone then 2
; immediate
[THEN]

[IFUNDEF] THEN
: THEN
	?comp 
	2 ?pairs
	here over - swap !
; immediate
[THEN]

| : (leave)
    here
    leave-list @ ,      \ store last fixup addr
    leave-list !        \ store new addr
;

[IFUNDEF] DO
: DO
	?comp
	[compile] (do)
	here 3
; immediate

: ?DO
    ?comp
    [compile] (?do) (leave)
    here 3
; immediate

[THEN]

[IFUNDEF] LOOP
| : leave-resolve
\	handle LEAVE references 

	leave-list @
	begin		
	 	dup			\ ( pos pos )
	while
		dup @ swap			\ ( new-pos pos )
		here over - 	 	\ ( new-pos pos jmp )
		swap !
	repeat
	leave-list !
;

| : loop-compile
\	normal loop part

	swap 3 ?pairs 
	compile, back
	

	leave-resolve
	[compile] unloop
;

: LOOP
	?comp
	['] (loop) loop-compile
; immediate

: +LOOP
	?comp
	['] (+loop) loop-compile
; immediate
[THEN]

[IFUNDEF] UNLOOP
: UNLOOP
	r> rdrop rdrop >r
;
[THEN]
test" unloop 3 >r 2 1 do loop r> 3 ="


[IFUNDEF] LEAVE
: LEAVE
	?comp

	[compile] unloop
	[compile] branch

    (leave)
; immediate
[THEN]

[IFUNDEF] BEGIN
: BEGIN
	?comp
	here 1
; immediate
[THEN]

[IFUNDEF] UNTIL
: UNTIL
	?comp
	1 ?pairs
	[compile] ?branch
	back
; immediate
[THEN]

[IFUNDEF] AGAIN
: AGAIN
	?comp
	1 ?pairs
	[compile] branch
	back
; immediate
[THEN]

[IFUNDEF] WHILE
: WHILE
	postpone if
	2+
; immediate
[THEN]

[IFUNDEF] REPEAT
: REPEAT
	?comp
	>r >r postpone again
	r> r> 2- 
	postpone then
; immediate
[THEN]

[IFUNDEF] CASE
: CASE
	?comp
	csp @ 		\ save old params
	!csp 
	4
; immediate
[THEN]

[IFUNDEF] OF
: OF
	?comp
	4 ?pairs
	[compile] (of)
	here 0 , 
	5
; immediate
[THEN]

[IFUNDEF] ENDOF
: ENDOF
	?comp
	5 ?pairs
	[compile] branch
	here 0 , 
	swap 2 postpone then
	4
; immediate
[THEN]

[IFUNDEF] ENDCASE
: ENDCASE
	?comp
	4 ?pairs
	[compile] drop
	begin
		sp@ csp @ = 0= 
	while
		2 postpone then
	repeat
	csp !
; immediate
[THEN]

[IFUNDEF] RECURSE
: RECURSE
	lastxt compile,
; immediate
[THEN]

[IFUNDEF] EXIT
: EXIT
	?comp
	[compile] ;s
; immediate
[THEN]

[IFUNDEF] DEFER
: DEFER
	create lastxt dodefer!
	['] noop ,
;
[THEN]

[IFUNDEF] IS
| : is-check
    ' dup
    @ $6a0
     <> -&23 ?error 
    cell+ 
;

: IS ( xt "deferred" -- )
	is-check (IS)
;
: IS? ( "deferred" -- xt )
    is-check (IS?)
;
[THEN]


[IFUNDEF] CONSTANT
: CONSTANT
    create 
	lastxt docon!
	,
;
[THEN]

[IFUNDEF] VARIABLE
: VARIABLE
	create 0 ,
	['] :dovar lastxt xt!
;
[THEN]

has? user-vars [if]

    [IFUNDEF] UDP
    User UDP
    [THEN]
    
    [IFUNDEF] USER
    : USER
        create UDP @ ,
        2 UDP +!
        ['] :douser lastxt xt!
    ;
    [THEN]

[else]

    : USER VARIABLE ;
    
[then]

[IFUNDEF] :NONAME
: :NONAME
	align

	\ LFA
	here latest ,

	\ NFA
	$0000 ,

	\ link
	>latest !

	]
   
	\ CFA
	here docol,

	!csp
;
[THEN]


\ private code
: Code postpone \ ; immediate

\ named code
: Code:
	create 
	lastxt dp !
;

: end-code ; 

\ \\\\\\\

\ variable envlast

: env:	( "name" value -- )
	create , \ envlast @ ,		\ value, link to previous
	\ latest envlast !
	does> @
;

: has?
    parse-name
	if 
		nfa>xt execute
	else
		nip 0
	then	
;

\ 0 ( has? standard-threading ) 	constant standard-threading 
\ 1 ( has? inlining-next )	constant inlining-next
\ has? profiling			constant profiling

