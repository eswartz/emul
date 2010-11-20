

\ : create ; target-only
\ : : ; target-only
\ : ; $70 c, ; immediate target-only 


: lastnfa
    latest lfa>nfa
;

: lastxt
    lastnfa nfa>xt
;

: (does>)  r> lastxt swap  >call  swap ! ; 
: does> postpone (does>) postpone rdrop ; immediate target-only


[IFUNDEF] STATE
User STATE
[THEN]

[IFUNDEF] [
: [
    0 state !
; immediate target-only
[THEN]


\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

\   Words required by cross-compiler, not in standard.

[IFUNDEF] noop
: noop ;
[THEN]

\ -------------------------------------------

| : docol!  ( cfa -- )
    drop
;


| : dovar!  ( cfa -- )
    IbranchX over c!
    Idovar swap c!
;


| : douser!  ( idx cfa -- )
    >r
    over $100 < if  Iupidx r@ c! r> 1+ c!  else
                    IlitW r@ c! r@ 1+ !  Iuser r> c!  then
;

| : dodoes,  ( -- )
    align
\    here 
    IRfrom c,
;


| : xt! ( addr cfa -- )
    !
;

\ called for creating words to inject :docol code into definition
: docol,    ( addr -- )
;

| : dodefer!    ( cfa -- )
    \ TODO
;

\ --------------------------------------------

[IFUNDEF] (compile)
\   compile the following word in the IP stream
\   (needed cross compiler)
: (compile)
    r> dup cell+ >r @ , 
;
[THEN]


\ \\\\\\\\\\\\\\\\\\\\\\\\

\ : ?exec
\   state @ -14 ?error
\ ;

: ?comp
    state @ 0= -&14 ?error
;

\   Differs from old version:
\   set $80 for visible definition.
\   Note: this affects xt>nfa, since it stops when
\   the LFA's length byte has $80.  
\   If we for some reason to "latest xt>nfa" while compiling
\   a word, it will fail...  ;)
: smudge
    lastnfa 
    dup c@ |srch xor swap c!
;


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
\ DeferROM (creat)  ( c"xxx" -- c"xxx" )

: CREATE
\    (creat)     

    align

    \ Put LFA --> ptr to previous LFA
    here 
    latest ,

    name"
    
    \ Get space for name
    c@ width min
    1+ aligned allot

    here (register-symbol)
    
    \ current @ !       \ !!!
    >latest !
    
    lastnfa id.
    
    smudge

    \ lay down CFA
    here dovar!
; target-only

\ \\\\\\\\\\\\\

: per-line
    win-sx 8 - 2 rshift 1- 1 max  aligned
;

: 2u.
    0 <# # # #> type
;

: 4u.
    0 <# # # # # #> type
;

\ dump one line
: (dmpln) ( addr cnt xt -- )
    >r  \ save xt
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
    over +   ( addr addr+cnt )
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
; immediate target-only
[THEN]


[IFUNDEF] [COMPILE]
: [COMPILE]
    bl word find
    if
        postpone literal compile,
    else
        huh?
    then

; immediate target-only
[THEN]

[IFUNDEF] POSTPONE
: POSTPONE
    bl word find
    if
        compile,
    else
        huh?
    then        
; immediate target-only
[THEN]



User csp

| User leave-list       \ pointer to linked list of leaves, 
                    \ @ branch pos of last leave


: !csp
    sp@ csp !
    0 leave-list !
; target-only

: ?csp
    sp@ csp @ - -&22 ?error
; target-only

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
; target-only

: ;
   \ #(;) exec-hook
    ?csp
    [compile] ;s
    smudge postpone [
 ; immediate target-only  

\   change the cfa of the newly created word to
\   jump to the code inside the creating word after does>
: (does>)
    r> lastxt xt!       \ drops a level, so code following dodoes> 
                        \ is not executed during create stage
; target-only

0 [if]
\   at compile time, ensure that the runtime of the creating word
\   will modify the cfa of the newly created word to point into
\   the embedded colon definition (docol,)
: DOES>
    [compile] (does>)
    docol,
; immediate target-only
[then]

: ?pairs    ( i*x tag tag' -- )
    - -&22 ?error
;

\ target
| : jmpoffs, ( addr offs -- )
    dup -129 128 within if
        1-
        swap c!
    else
        true -&24 ?error
    then
;
| : jmpoffsalloc, ( diff opc -- )
    swap dup -128 127 within if
        dup 0>= if 1- then  
        swap c, c,
    else
        true -&24 ?error
    then
;

\ is there an official name for this?
[IFUNDEF] BACK
: BACK  ( addr opc -- )
    >r here - 
    r> jmpoffsalloc,
; target-only
[THEN]

[IFUNDEF] BEGIN
: BEGIN
    ?comp here 1 
; immediate target-only
[THEN]

[IFUNDEF] IF
: IF
    ?comp
    [compile] 0branch
    here 0 c, 
    2
; immediate target-only
[THEN]


[IFUNDEF] THEN
: THEN
    ?comp 
    2 ?pairs
    here over - jmpoffs,
; immediate target-only
[THEN]

[IFUNDEF] ELSE
: ELSE
    ?comp
    2 ?pairs
    [compile] branch
    here 0 c, 
    swap 2
    postpone then 2
; immediate target-only
[THEN]


| : (leave)
    IbranchB c,  here  0 c,
    leave-list @ ,      \ store last fixup addr
    leave-list !        \ store new addr
; target-only

[IFUNDEF] DO
: DO
    ?comp
    ItoR_d ,
    here 
    0  \ not ?do 
    3
; immediate target-only

: ?DO
    ?comp
    \ [compile] (?do) (leave)
    Idup_d , ItoR_d , Isub c, I0branchB c,
    here 0 c,
    
    here -1    \ ?do
    3
; immediate target-only

[THEN]

[IFUNDEF] LOOP
| : leave-resolve
\   handle LEAVE references 

    leave-list @
    begin       
        dup         \ ( pos pos )
    while
        dup @ swap          \ ( new-pos pos )
        here over -         \ ( new-pos pos jmp )
        jmpoffs,
    repeat
    leave-list !
; target-only

| : loop-compile
\   normal loop part

    swap 3 ?pairs 
    
    swap >r  \ ?do
    
    \ loop opcode is one byte
    c,  
    
    \ branch follows
    I0branchB  back
    
    \ for ?do, provide jump for exit
    r> if  here over - jmpoffs, then

    leave-resolve
    [compile] unloop
; target-only

: LOOP
    ?comp
    IloopUp loop-compile
; immediate target-only

: +LOOP
    ?comp
    IplusLoopUp loop-compile
; immediate target-only

: ULOOP
    ?comp
    IuloopUp loop-compile
; immediate target-only

: +ULOOP
    ?comp
    IuloopUp loop-compile
; immediate target-only
[THEN]

[IFUNDEF] UNLOOP
: UNLOOP
    r> rdrop rdrop >r
; target-only
[THEN]
test" unloop 3 >r 2 1 do loop r> 3 ="


[IFUNDEF] LEAVE
: LEAVE
    ?comp

    [compile] branch

    (leave)
; immediate target-only
[THEN]

[IFUNDEF] BEGIN
: BEGIN
    ?comp
    here 1
; immediate target-only
[THEN]

[IFUNDEF] UNTIL
: UNTIL
    ?comp
    1 ?pairs
    I0branchB back
; immediate target-only
[THEN]

[IFUNDEF] AGAIN
: AGAIN
    ?comp
    1 ?pairs
    IbranchB back
; immediate target-only
[THEN]

[IFUNDEF] WHILE
: WHILE
    postpone if
    2+
; immediate target-only
[THEN]

[IFUNDEF] REPEAT
: REPEAT
    ?comp
    >r >r postpone again
    r> r> 2- 
    postpone then
; immediate target-only
[THEN]

0 [if]
    [IFUNDEF] CASE
    : CASE
        ?comp
        csp @       \ save old params
        !csp 
        4
    ; immediate target-only
    [THEN]
[then]
    
0 [if]
    [IFUNDEF] OF
    : OF
        ?comp
        4 ?pairs
        [compile] (of)
        here 0 , 
        5
    ; immediate target-only
    [THEN]
[then]
    
0 [if]
    [IFUNDEF] ENDOF
    : ENDOF
        ?comp
        5 ?pairs
        [compile] branch
        here 0 , 
        swap 2 postpone then
        4
    ; immediate target-only
    [THEN]
[then]
    
0 [if]
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
    ; immediate target-only
    [THEN]
[then]

[IFUNDEF] RECURSE
: RECURSE
    lastxt compile,
; immediate target-only
[THEN]

[IFUNDEF] EXIT
: EXIT
    ?comp
    [compile] ;s
; immediate target-only
[THEN]

[IFUNDEF] DEFER
: DEFER
    create lastxt dodefer!
    ['] noop ,
; target-only
[THEN]

0 [if]
    [IFUNDEF] IS
    | : is-check
        ' dup
        @ $6a0
         <> -&23 ?error 
        cell+ 
    ; target-only
    
    : IS ( xt "deferred" -- )
        is-check (IS)
    ; target-only
    : IS? ( "deferred" -- xt )
        is-check (IS?)
    ; target-only
    [THEN]
[then]

[IFUNDEF] CONSTANT
: CONSTANT
    create 
    lastxt 
    literal
; target-only
[THEN]

[IFUNDEF] VARIABLE
: VARIABLE
    create 0 ,
    lastxt dovar!
; target-only
[THEN]

[IFUNDEF] UDP
User UDP target-only
[THEN]

[IFUNDEF] USER
: USER
    create UDP @ 
    2 UDP +!
    lastxt douser!
; target-only
[THEN]

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
; target-only
[THEN]


\ \\\\\\\

\ variable envlast

: env:  ( "name" value -- )
    create , \ envlast @ ,      \ value, link to previous
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

\ 0 ( has? standard-threading )     constant standard-threading 
\ 1 ( has? inlining-next )  constant inlining-next
\ has? profiling            constant profiling

