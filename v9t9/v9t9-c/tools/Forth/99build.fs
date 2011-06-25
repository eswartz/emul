\	99build.fs					-- main module for building v9t9 FORTH
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
\	$Id: 99build.fs,v 1.31 2009-02-25 02:18:24 ejs Exp $

\	set level of self-diagnostics to perform on boot (0=none)
0 constant init-test-level

variable startmem here startmem !

\	get-rom-addr defined in command line as a constant
get-rom-addr constant ROM-start-addr

include site-forth/cross.fs

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\

Warnings on

unlock

0000 10000 region address-space


\ ." Checking has?: ...." order  
T has? enhanced-mode H [if] 
ROM-start-addr $4000 region rom-dictionary
4000 9800 region ram-dictionary
[else]
ROM-start-addr 8000 over - region rom-dictionary
A000 6000 region ram-dictionary
[endif]

0 10000 makekernel

only forth also definitions

\ \\\\\\\\\\\\\\\\\\\\\\\\\

\ also environment also forth
also cross also target also forth
: env-has?
	name T environment? H
        IF      \ environment variable is present, return its value
        ELSE    \ environment variable is not present, return false
                \ !! JAW abort is just for testing
                false true ABORT" arg"
        THEN
;	immediate
previous previous previous

\ \\\\\\\\\\\\\\\\\\\\\\\\\\

\ only forth also definitions

\	Machine definitions

>cross


>target

." Before ASM:" order cr

include 99asm.fs
include 99equs.fs

only forth also definitions

\ Asm

variable out-fileid	 stdout out-fileid !
variable copy-to-scrn copy-to-scrn off

: my-emit
	dup
	out-fileid @ emit-file drop
	\ write to screen only if it's not the current output
	\ and we want to copy to screen
	stdout out-fileid @ over <> 
	copy-to-scrn @ and if 
		emit-file drop 
	else 
		2drop 
	then
;

' my-emit IS emit

: my-type
	2dup
	out-fileid @ write-file drop
	stdout out-fileid @ over <> 
	copy-to-scrn @ and if
		write-file drop 
	else 
		2drop drop 
	then
;

' my-type IS type

: stdout>file ( caddr u -- )
	r/w create-file throw out-fileid !
	copy-to-scrn off
;

: >stdout
	out-fileid @ close-file
	stdout out-fileid !
;


\ \\\\\\\\\\\\

>minimal also cross also minimal also forth

: -visible
	copy-to-scrn off
;

: visible
	copy-to-scrn on
;

: error"
	copy-to-scrn @
	s" ERROR: " type
	visible $22 parse type
	cr
	copy-to-scrn !
;

\ \\\\\\\\\\\\\\\\\\\

\	Testing words.
\	Using:		test" word ... "
\	will define a test for "word" that consists of "...".
\	At runtime, the test will be executed, and leaving "0" on the stack
\	indicates failure, "1" indicates success.  If fails, the name
\	is printed.
\
\	]test" word ... " will perform operations outside of code
\	(a real test" should follow)

variable test-fileid
variable test-level init-test-level test-level !

: write"
	test-fileid @ write-file drop
;

: cr"
	$a pad c! pad 1 write"
;

: write-test-header
	\ init code
	s" : [name. ( xt -- caddr u ) xt>nfa dup 1+ swap c@ $1f and ; " write" cr"
	\ test run:  xt is :noname def, txt is word to blame ;)
	s" : (runtest sp0 @ sp! ; " write" cr"
	s" : runtest) ( txt t/f -- ) " write" cr"
	s" 	   swap if 2a emit [name. type 2b emit drop else 5b emit [name. type 5d emit then  2e emit ; " write" cr"
	\ header for test runner
	s" : runtests 5b emit " write" cr"
;

: create-test-file
	test-fileid @ 0= if
		s" 99tests.fth" r/w create-file throw test-fileid !
		write-test-header
	then
;

\	Add test to tests list.
: #test"	( level "word test" -- )
	create-test-file
	test-level @ < if
		$20 parse \ write"	
		\ clean stack
		s"  (runtest " write"
		\ then it's the test
		$22 parse write"
		\ token for blame if error
		s"  ['] " write" write"
		\ execute
		s"  runtest) " write" cr"
	else
		$20 parse 2drop $22 parse 2drop
	then
;

\	Add test to code outside method
\ : #]test"	( level "test" -- )
\	create-test-file
\	test-level @ < if
\		s" [ " write"
\		$22 parse write"
\		s"  ] " write"
\		cr"
\	else
\		$22 parse 2drop
\	then
\ ;

\	force test
: test"  0 #test" ;

\	various test levels
: 1test" 1 #test" ;
: 2test" 2 #test" ;
: 3test" 3 #test" ;

\   define something if testing
: |test  test-level @ >= if [compile] \ then ; immediate

\ : ]test" 0 #]test" ;

: close-test-file
	test-fileid @ if
		s"  5d emit ; " write" cr"
		test-fileid @ close-file
	else
		create-test-file
		s" " write"
		recurse
	then
;

>minimal
\ also minimal definitions previous

: append-test-file
	close-test-file
    s" 99tests.fth" included
;

previous previous

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
\ include 99memory.fs

>cross
\ $2800 constant tram-start
\ variable tram

\ : tram ." Getting TRAM" tup T StartRAM . H dup ? ; 
\ : tram ." Getting TRAM: " >ram there dup . >rom ; 
\ >target
\ tram-start tram !

>cross

\	go direct threading!
\	Overrides for dictionary creating words.
\	We want a direct threading model instead of
\	the indirect threading model, for better 
\	performance.

\	for direct threading, the code field contains actual
\	code, not a pointer to it.
\	for primitives, assembly starts here.
\	for colon defs, a BL *DODOES starts here.
\	for constants, a BL @>DOCON starts here.
\	for variables, a BL @>DOVAR starts here.

0 include common.fs

:noname	( tcfa -- )		\ compiles call to tcfa at current position
\	!!! ugly hack: defer/is words
\	appear to point to the wrong CFA.
	T dup 1 cells - @ $06A0 = if 1 cells - then H
	T , H						\ tcfa -> code
;	IS colon,

\  for constant/variable references
:noname
  >tempdp ]comp 
	T , H comp[ tempdp>
; 	IS colon-resolve

:noname 
	BL-DODOES T , H
	addr,
;	IS dodoes,

:noname  ( -- ) 
	BL-DOCOL T , H
; 	IS docol,
 
:noname ( -- )
; 	IS doprim,

\	for dodefer, etc?
:noname ( ghost -- ) 
	." doer," cr
 	BL-@ tdp @ T 1 cells - H T ! H addr,
	$80 flag!
;	IS doer,

 

\ \ EJS 001130 GF0.5.0
>target

Build:  ( n -- ) T  BL-DOCON there 2 cells - !  there cell - !  H  ;
DO: ( ghost -- n )  T cell - @ H ;DO
Builder Constant


\   If no User vars are supported, just treat them like constants
\   which point to a fixed StartUser area.
has? user-vars 0= [if]

Build: T BL-DODOES there 2 cells - !  
        tudp @ tup @ + 
        
        BL-DOCON there 2 cells - !  there cell - !
        
        tudp @ 2 + tudp !  H ;
DO: ABORT" CROSS: Don't execute" ;DO
Builder User

\	DeferROMs MUST BE (IS)'ed at startup!
\ We allocate the (variable) address for this in the User space.
BuildSmart:  ( -- ) 
    T tudp @ tup @ + 
    A,        
    tudp @ 2 + tudp !  H ;
by: :dordefer ( ghost -- ) ABORT" CROSS: Don't execute" ;DO
Builder DeferROM

: IS
	T ' cell+ ! H
;

: ROMIS
	T ' cell+ @ ! H
;
>cross


:noname
    \ case of "create foo <allot> does> <code> ;"
    \ we have | BL *DODOES | <variable info> | <doesjump> | <code>
    T cfalign H
    BL-DOCOL T , H
;   IS doeshandler,


[else]	\ ---------- actual user vars


>target



\	DeferROMs MUST BE (IS)'ed at startup!
\ BuildSmart:  ( -- ) tudp @ dup T 1 cells + H tudp ! T A, H
BuildSmart:  ( -- )
    T tudp @ tup @ + 
    A,        
    tudp @ 2 + tudp !  H ; 
;
\ BuildSmart:  ( -- ) >ram there 0 T A, H >rom T A, H ;
by: :dordefer ( ghost -- ) ABORT" CROSS: Don't execute" ;DO
Builder DeferROM


>cross


[then]	\ ----------- [IF] user-vars




\ \\\\\\\\\\\\\\\\\\\\

\	Dictionary and hash table maintenance.

\	too damn slow and memory hogging		----------------------
0 [if]

>cross

variable FORTH-wordlis
\ right circular shift
\ x n cshift == ( x>>n | x << 16-n )
: cshift
	>r
	$ffff and 
	dup r@ \ .s 
	rshift swap $10 r> - \ .s 
	lshift 
	\ .s 
	OR $ffff and
;

." cshift: "
$000a 5 cshift . cr

0 include commonhash.fs

\	preallocate buckets for hash table

T here
hash-buckets hash-bucket-size * cells dup allot
over swap erase
dup ." hash buckets start at " . cr

\ .s
\ save target ptr
 FORTH-wordlis H !

previous

>minimal
also cross definitions 

>cross

create newname $20 allot

\	Our own header routine.
\	We need to possibly allocate dict
\	space for hash table expansion --
\	so, we get the entry for the new name first,
\	then lay down the header.

\	Read name from  input stream and
\	add it to hash table.  Return tptr to
\	the place we should store the NFA.

variable newcfa
: add-hash-entry ( "name" -- addr )
	." add-hash-entry" cr
	forth-wordlis @
 	bl word count 

T	hash>new H 				\ leaves new entry addr

;

:noname	( "name" -- )
	>in @
	add-hash-entry
	>r >in ! r>

    T align H view,
    tlast @ dup 0> IF  T 1 cells - H THEN T A, H  there tlast !

	\ write NFA to hash table
	T here cfaligned swap ! H

    >in @ T name, H  >in !

; IS header,

\ see name,

[then]		\ hash table stuff	-------------------------------

\ \\\\\\\\\\\\\\\\\\\\

T has? profiling H [if]	\ ------------------------------------

\	Statistics:  we add a field to each word which
\	points into RAM.  Increment this pointer for each
\	execution of the word.

:noname	( "name" -- )
    T align H view,
    tlast @ dup 0> IF  T cell - H THEN T A, H  there tlast !
    >in @ T name, H  >in !

	\ write the address of the profiling word
	tram @ $3000 >= if
		abort" out of profiling space, adjust start-grom-image"
	then
	tram @ T A, cell H tram +!
; IS header,


[then]

\ \\\\\\\\\\\\\\\\\\\\

>cross

\   These routines are called from the cross compiler
\   at locations where it's useful to dump a disassembly
\   or a dictionary header.  They assume that the dictionary
\   is linearly organized (i.e., names are inline with code)
\   and that 'there' increments linearly.
\
\   Also, these are called in between definitions to ensure
\   that we don't span non-contiguous ROM areas, moving
\   them out of the way as needed.

variable code-start     ROM-start-addr code-start !
variable ended-code     1 ended-code !


Variable prevcfa

\ -------------------------------

\ In this model, ROM and RAM are contiguous!

T has? enhanced-mode H [if]

: checkmemory ( delta -- )
    there prevcfa !
;

\   These routines are called from the cross compiler
\   at locations where it's useful to dump a disassembly
\   or a dictionary header.  They assume that the dictionary
\   is linearly organized (i.e., names are inline with code)
\   and that 'there' increments linearly.

\   Move the last routine to the next bank if it
\   crossed a ROM boundary
\
: next-bank ( addr -- addr )
;

: test-definition-crossing
    0
;


[else]

\	move 'there' to module area
\	if out of CPU ROM space
\	not too bright -- literal strings make big definitions

\	eat up 0...$2000, then $6000...$7fff, then high-ram-start...$ffff
\	
\
: checkmemory ( delta -- )
	>r
	there $0000 $2100 within if
		there $2000 r@ - >=  if
			." Switching to module ROM bank..." cr
			$6000 tdp !
			$aa55  T  , H 
		then
	else there $6000 $8002 within if
		there $8000 r@ - >=  if
			." Switching to high memory bank..." cr
			high-ram-start 3 cells + tdp !
		then
	else there $10000 >= if
			abort" All ROMable dictionaries full!" cr
	then
	then
	then
	rdrop
	there prevcfa !
;


\	Move the last routine to the next bank if it
\	crossed a ROM boundary
\
: next-bank	( addr -- addr )
	$e000 and  
	$0000 = if  $6002     $AA55 $6000 T ! H  
	else $A000 
	then
;

: test-definition-crossing
    tlastcfa @  $a000 < if
        prevcfa @  $e000 and
        there    $e000 and 2dup . . = 0=
   else
    0
   then 
;

[endif]

: migrate-split-routine
	test-definition-crossing
	if
		." crossed banks" cr
		\ copy the definition to the next bank 
		code-start @ >image  	\ src
		prevcfa @ next-bank dup >r  >image \ dst
		there code-start @ - dup >r \ n
		cmove
		
		\ clear out the moved routine
		code-start @ >image
		there >image over -  erase

		\ update ghost xt
		2r@ drop	( new-here )
		\ Last-Ghost @ ghost>cfa .
		;Resolve cell+ !
		
		\ >taddr .s !
		
		\ emit updated symbol record
		2r@ drop	( new-here )
		tlast @  code-start @ >= if 
			tlastcfa @ tlast @ - tcell + +
		then
		." sym!" 4 u.r cr

		\ bump the dictionary
		r> r>  ( size new-here ) swap over + tdp !

		\ update link if this was a named def
		tlast @  code-start @ >= if 
			tcell + tlast !
		else
			drop
		then

        tlast @ prevcfa !
    else
        tlastcfa @ prevcfa !        
	then
;


\	print all data accumulated since last time
\	if ended-code, then it's data, else ignore, since it's code
\
: (print-data)
	ended-code @ if
		code-start @		\ start
		dup
		there swap -
		dup 2000 > if 		\ changed banks
			code-start @ dup 1fff or 1+ over - T tdump H cr
			2drop there e000 and dup there swap -
		then
		 T tdump H cr
	then
;

: (doc-code)
	also cross also assembler \ also asm-hidden
	(print-data)
	ended-code @ if
		." Assembling at @>" there dup code-start ! . cr
		0 ended-code !
	then
;

: (print-code)
	cr code-start @ there over - T dis H cr
	there code-start ! 
;

: (doc-end-code)
	previous previous \ previous
	migrate-split-routine
\	$10 checkmemory
	(print-code)
	1 ended-code !
;



' (doc-code) IS (code)
' (doc-end-code) IS (end-code)

: (doc-end-colon)
	(fini,)
	migrate-split-routine
\	$10 checkmemory

	(print-data)
	1 ended-code !
	there code-start !
;                                 

' (doc-end-colon) IS fini,

previous previous	\ no more asm

s" nforthB.lst" stdout>file 


\ \\\\\\\\\\\\\\\\\\\\\\\\\\

\ \\\\\\\\\\\

include 99memory.fs

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\

s" 99kernel.fs" included

\ \\\\\\\\\\\\\\\\\\\\\\

\ finish off memory dump

(print-data)

visible

.regions

turnkey


.stats
.unresolved

unlock

\	Write end of ROM to end of nforth.rom
T has? enhanced-mode H [if]
ROM-start-addr $4000 over - save-region nforth.prm
[else]
ROM-start-addr $2000 over - save-region nforth.prm
$6000 $2000 save-region nforthc.bin
[endif]

\ 	Write high RAM dictionary...
\	in GROM, the memory is stored as repeating <$aa55> <start> <stop> <data...>
\	where <start>/<stop> are the ranges of RAM to copy the following data to.

there high-ram-start $10000 within
[if]
	$aa55 high-ram-start T ! H					\ magic
	high-ram-start dup T cell+ ! H	\ start addr in RAM
	there high-ram-start T cell+ cell+ ! H		\ last addr in RAM
	high-ram-start  
		there high-ram-start - \ 1fff or
		save-region nforthg.bin
[else]
		high-ram-start 0 save-region nforthg.bin
[then]

there ." HERE is " . cr
\ tram @ $a000 - ." Used " . ." bytes of high RAM space for storage" cr 

>stdout







