\	interp.fs					-- FORTH interpreter words
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
\	$Id: interp.fs,v 1.22 2009-01-11 17:46:42 ejs Exp $

[IFUNDEF] (
: (
	$29 parse 2drop
;	immediate
[THEN]

[IFUNDEF] >IN
user >IN
[THEN]

[IFUNDEF] CHAR
: CHAR	( -- c )
	bl (parse-word)
	drop c@
;
[THEN]

[IFUNDEF] REFILL
: REFILL	( -- flag )

\   Attempt to fill the input buffer from the input source, returning
\   a true flag if successful.
\
\   When the input source is the user input device, attempt to receive
\   input into the terminal input buffer. If successful, make the
\   result the input buffer, set >IN to zero, and return true. Receipt
\   of a line containing no characters is considered successful. If
\   there is no input available from the current input source, return
\   false.
\
\   When the input source is a string from EVALUATE, return false and
\   perform no other action.
\
\   When the input source is a block, make the next block the input source
\   and current input buffer by adding one to the value of BLK and setting
\   >IN to zero. Return true if the new value of BLK is a valid block number,
\    otherwise false.
 
    blk @ ?dup if
               1 blk +!
               block >tib !  chars/block #tib !
               0 >in !
               true
    
    else
    	source-id
    	dup -1 = if			\ evaluate string
    		drop false		\ end.
    	else ?dup 0= if			\ user input device
    		(tib0) $100 accept #tib !
    		(tib0) >tib !
    		0 >in ! 
    		bl emit
    		true
    	else 
    		( fileid )
    		(tib0) $100 rot (rdln) 0=
    		if
    
    \  ." [" (tib0) over type ." ]" cr
    
    			#tib !
    			(tib0) >tib !
    			0 >in !
    			true
    		else
    \			." end of file" cr
    			\ drop false
    		then
    	then then
    then
;
[THEN]

[IFUNDEF] QUERY
\ : QUERY
\ ;
[THEN]

[IFUNDEF] RESTORE-INPUT
: RESTORE-INPUT	( xn ... x1 n -- )
	6 <> abort" invalid restore-input"
	>in !
	blk !
	#tib ! >tib !
	loadline ! loadfile !
;
[THEN]

[IFUNDEF] SAVE-INPUT
: SAVE-INPUT	( -- xn ... x1 n )
	loadfile @ loadline @
	>tib @ #tib @
	blk @
	>in @
	6
;
[THEN]

[IFUNDEF] SOURCE-ID
: SOURCE-ID
	loadline @ 0< if
		-1
	else
		loadfile @
	then
;
[THEN]

[IFUNDEF] TIB
User >tib
: TIB
	>tib @
;
[THEN]


[IFUNDEF] INTERPRET

\	INTERPRETER
\	===========

\	Source state is represented as follows:
\	If blk=0, 'source-id' is 0 for keyboard, -1 for evaluate string, >0 for text file.
\	else blk=blk # for source.
\	For non-block stuff, we use loadfile/loadline to keep track of file source,
\	and >tib/#tib for all input strings.
\	loadfile=0 for user input, loadline<0 for evaluate string.

User loadfile
User loadline

\	Push the input state.
: <input		( xn ... x1 n -- R: xn ... x1 n )
	r>
	loadfile @ >r loadline @ >r
	>tib @ >r	#tib @ >r
	blk @ >r	
	>in @ >r
	>r	
;

: input>
	r>
	r> >in !
	r> blk !
	r> #tib !	r> >tib !
	r> loadline ! r> loadfile !
	>r
;

0 [if]

: (>c)		( caddr u naddr -- )
\	make counted string at naddr
	2dup c!				\ set length byte
	1+ swap cmove>		\ move data
;

[else]

Code (>c)  ( caddr u naddr -- )
    movb 1 @>(SP) , *TOS+
    mov *SP , T1
    mov TOS , *SP
    mov T1 , TOS
    b ' CMOVE> @> 
end-code

[endif]

: ,"
    postpone s" dup >r  here (>c)  r> 1+ aligned allot  
;


: (lookup)		( c-addr u -- caddr 0 | nfa 1|-1 )
	here (>c)	\ make counted string + NFA
\ context @ @ (find) dup 0= if ... 

[ 1 [if] ]
	here latest
	(find) 
[ [else] ]
	here find dup >r if xt>nfa else drop then r>
	
[ [then] ]
;

: ?stack
	depth 0< if
		." stack empty!" cr
		abort
	then
;

: huh?	( caddr -- )
	count type space
	[char] ? emit cr
;

[IFUNDEF] NUMBER

\	Interpret counted string as number,
\	and store decimal point location in DPL.

User dpl

: number	( addr -- ud )
\	.s
[ 0 [if] ]

    0 0 rot
    
[ [endif] ]

    count 

	\ see if first char is '-'
	over c@ [char] - = dup 
	>r						\ store sign flag
	if (skip) then

	\ check for base conversion
	base @ >r				\ save original base
	over c@ [char] $ = if
		hex	(skip)			\ use hex for '$'
	else over c@ [char] & = if
		decimal	(skip)		\ use decimal for '&'
	then then

	-1 dpl !

[ 1 [if] ]

    ( caddr n )
    2dup
	base @ >single-number    ( num f  || caddr n  caddr-bad+1 bad-ch  )
	?dup if		\ invalid char?
		$2E = if	\ did we stop at '.'?
			dpl !	       	\ don't store offset... too much work ;)
			2>r 0 0 2r>     \ insert work number
			>number         \ full dbl-prec parse
			(skip)
			>number         \ again, due to the '.' which we know about
		else
		    0 0 1
		then
		dup if 
			here huh? 2drop 2drop quit 	\ error
		then
		2drop
	else
	   nip nip s>d
	then
[ [else] ]

    >number
    dup if      \ any invalid chars?
        over c@ $2E = if    \ did we stop at '.'?
            over dpl !      \ don't store offset... too much work ;)
            (skip)          \ skip '.'
            >number
        then
        dup if 
            here huh? 2drop 2drop quit  \ error
        then
    then
    2drop

[ [endif] ]


	r> base !		\ original base

	r>				\ sign flag
	if dnegate then

\	.s
;

[ENDIF]

| : interpreter
\        ( i*x c-addr u -- j*x )
\
\	Interpret one word
	(lookup)		\ ( caddr 0 | nfa 1 )
	if
		dup nfa>imm? 0=
		state @ and 	\ compiling and not immediate?
		if
			nfa>xt compile,
		else
			nfa>xt execute
		then
	else
		\ number dpl @ 1+ if
		base @  number  ?dup 
		if
		    0< if 
			     postpone dliteral
		    else 
			     postpone literal
		    then
		else
		     here huh? 2drop 2drop quit  \ error
		then 
	then
;

: interpret
	begin
		?stack
		bl (parse-word)	
		dup
	while
		interpreter
	repeat
	2drop
;

: EVALUATE	( i*x c-addr u -- j*x )
	<input
	-1 loadline !
	0 loadfile !
	0 blk !
	#tib ! 	>tib !  
	0 >in !
	interpret
	input>
;
[THEN]

[IFUNDEF] QUIT

: (clrsrc)
    0 blk ! 0 loadfile ! 0 loadline !
;

: QUIT
	begin
        sp0 @ sp!
        rp0 @ rp! 
        (clrsrc)
        postpone [ 	
        .s cr
        begin	
			limi1
			refill
        while
			interpret

			\ print comments only when using user input
			blk @ source-id and 0= if 
				state @ 0= if
					."  ok" .s
				then
				cr
			then
		repeat
	again
;
[THEN]

[IFUNDEF] SOURCE
: SOURCE	( -- caddr u )
	blk @ ?dup if
		block chars/block
	else
		>tib @ #tib @
	then
;
[THEN]

[IFUNDEF] [CHAR]
: [CHAR]
    char postpone literal ; immediate
[THEN]

[IFUNDEF] [IF]   ( i.e., should we define the directives? )

\ User parsed
\ [[[ $20 tudp @ + tudp !  $20 tup @ + tup ! ]]]
\ 
: str= compare 0= ;
\ : toupper dup $61 $7b within if $20 - then ;
\ 
\ : upcase ( str -- )
\ count bounds
\     ?DO I c@ toupper I c! LOOP ;
\ : place  ( addr len to -- ) \ gforth
\     over >r  rot over 1+  r> move c! ;
\ : bounds ( addr u -- addr+u addr ) 
\     over + swap ;
: comment? ( c-addr u -- c-addr u )
        2dup s" (" str=
        IF    postpone (
        ELSE  2dup s" \" str= IF postpone \ THEN
        THEN ;

: [ELSE]
    1 BEGIN
	BEGIN bl word count dup WHILE
	    comment? 
	    2dup s" [IF]" str= >r 
	    2dup s" [IFUNDEF]" str= >r
	    2dup s" [IFDEF]" str= r> or r> or
	    IF   2drop 1+
	    ELSE 2dup s" [ELSE]" str=
		IF   2drop 1- dup
		    IF 1+
		    THEN
		ELSE
		    2dup s" [ENDIF]" str= >r
		    s" [THEN]" str= r> or
		    IF 1- THEN
		THEN
	    THEN
	    ?dup 0= if exit then
	REPEAT
	2drop refill 0=
    UNTIL drop 
; immediate
  
: [THEN] ( -- ) ; immediate

: [ENDIF] ( -- ) ; immediate

: [IF] ( flag -- )
    0= IF postpone [ELSE] THEN ; immediate 

: defined? bl word find nip ;
: [IFUNDEF] defined? 0= postpone [IF] ; immediate
: [IFDEF] defined? postpone [IF] ; immediate

[THEN]      ( done defining the directives )


