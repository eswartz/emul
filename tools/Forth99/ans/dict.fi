\   dict.fs                     -- FORTH dictionary manipulation
\
\   (c) 1996-2009 Edward Swartz
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
\   $Id: dict.fs,v 1.15 2009-02-25 02:18:24 ejs Exp $

\   These cover memory spaces, threading and compiler issues
\   related to structure of the dictionary.
\

User >latest        \ latest definition

\   Return latest definition's nfa
: latest
    >latest @
;


\ $1F constant &nmask
| $40 constant |immed
| $80 constant |srch

\   Dictionary entry:
\
\   [ word: previus entry ]
\   [ NFA: byte flags + length; name; space terminator to align ]
\   XT (under direct threading).  For code words, the code directly
\   lives here.  Colon words contain "BL *DOCOL" (R8).
\   Does words contain "BL *DODOES" (R7).


\   \\\\\\\\\\\\\\

\   Custom dictionary stuff

[IFUNDEF] TRAVERSE
: traverse  ( xt|nfa dir -- nfa|xt )
\ traverse from one end of definition to the other
\ dir < 0 means xt->nfa, dir>0 means nfa->xt

    0> if
        \ nfa -> xt

        dup c@ $1f AND  \ get the length byte
        + 1+ aligned

        \ [ has? profiling [if] ] cell+ [ [then] ]
    else
        \ xt -> nfa
        \ [ has? profiling [if] ] #cell - [ [then] ]

        \ go from xt to nfa end
        1-

        \ step back over alignment space
        dup c@ $20 = if 1- then 

        0       ( addr count )

        \ step back until we hit a byte /*with $80 set*/ whose length matches
        \ don't check the $80 flag since this makes hidden defs hard to handle
        begin 
            over c@         ( addr len cur )
            $1f and         ( addr len curlen )
            over <>         ( addr len done! )
        while
            1+ swap 1- swap ( addr-1 len+1 )
        repeat
        
        drop
    then
;

[THEN]


: nfa>xt
    1 traverse
;

: xt>nfa
    -1 traverse
;

test" xt>nfa ['] traverse xt>nfa c@ $88 = "
test" nfa>xt ['] traverse dup xt>nfa nfa>xt = "


: nfa>imm?  ( nfa -- t/f )
    c@ |immed and 0<>
;

: lfa>nfa
    2+
;

: >id       ( nfa -- caddr u )
    count $1f and 
;

: id.       ( nfa -- )
    >id type space
;

$1F constant width      \ max length of a name

\   \\\\\\\\\\\\\\\\\

\   dictionary words

[IFUNDEF] DP
User DP
[THEN]

[IFUNDEF] '
: ' 
\   Skip leading space delimiters. Parse name delimited by a space. Find name and return xt, the execution token for name. An
\   ambiguous condition exists if name is not found.
\
\   Similarly, the use of ' and ['] with compiling words is unclear if the precise compilation behavior of those words is not
\   specified, so ANS Forth does not permit a Standard Program to use ' or ['] with compiling words.
    bl word find 
    0= if count type ."  not found" 0 then      \ !!!
;
[THEN]

[IFUNDEF] ,
: ,
\   Reserve one cell of data space and store x in the cell. If the data-space pointer is aligned when , begins execution, it
\   will remain aligned when , finishes execution. An ambiguous condition exists if the data-space pointer is not aligned
\   prior to execution of ,.
    here ! #cell dp +!  
;
[THEN]

[IFUNDEF] >BODY
: >BODY
\        ( xt -- a-addr )
\   a-addr is the data-field address corresponding to xt. An ambiguous condition exists if xt is not for a word defined via
\   CREATE.
    2 cells +
;
[THEN]

[IFUNDEF] ALIGN
: ALIGN
    here aligned dp !
;
[THEN]

[IFUNDEF] ALIGNED
: ALIGNED   ( addr -- addr )
    #cell  1-  swap over +  swap and
;
[THEN]

[IFUNDEF] ALLOT
: ALLOT
    here + dp !
;
[THEN]

[IFUNDEF] C,
: C,
    here c!  1  dp +!
;
[THEN]

[IFUNDEF] FIND

: (find)    ( c-addr lfa -- c-addr 0 | nfa 1 )
    drop 0
;

: FIND  \ ( c-addr -- c-addr 0  |  xt 1  |  xt -1 )
\   Find the definition named in the counted string at c-addr. If the definition is not found after searching all the word
\   lists in the search order, return c-addr and zero. If the definition is found, return xt. If the definition is immediate,
\   also return one (1); otherwise also return minus-one (-1). For a given string, the values returned by FIND while compiling
\   may differ from those returned while not compiling.

[ 1 [if] ]
    latest          \ !!! need real wordlist
    (find)          \ ( c-addr 0 | nfa 1 )

[ [else] ]

\   Use hash table
    forth-wordlist  \ !!! need latest wordlist
    hash>find
\   over .

[ [then] ]

    if
        dup nfa>xt 
        swap c@ 
        |immed and if 1 else -1 then
    else
        0
    then
;
[THEN]
\ |test : wordtofind s" _2*" ;
\ test" find hex 21 wordtofind pad swap cmove pad 2 over c! find 2dup . . if execute 42 = else 0 then decimal "

[IFUNDEF] HERE
: HERE
    dp @
;
[THEN]

[IFUNDEF] [']
: [']
    ' postpone literal
; immediate
[THEN]

[IFUNDEF] IMMEDIATE
: IMMEDIATE
    latest lfa>nfa dup c@ |immed or swap c!
;
[THEN]

[IFUNDEF] UNUSED
: UNUSED
    0 here -
;
[THEN]

[IFUNDEF] WORDS
: dwalk ( xt root -- )  ( xt: nfa -- 0 | 1 )
    swap >r
    begin
        dup lfa>nfa r@ execute   ( lfa flag )
        if 
            TRUE 
        else
            @ dup 0=
        then
    until
    drop rdrop
;

| : .word ( nfa -- 0 continue | 1 stop )
   id. (pause?) 
;

: WORDS
    ['] .word 
    latest      \ !!! need real wordlist
    dwalk
;
[THEN]


