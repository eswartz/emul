
: s" postpone (s") ; immediate target-only

: pick ( n -- v )
    1+ cells (context>) [ CTX_SP field, ] + @  
;

: rpick ( n -- v )
    cells (context>) [ CTX_RP field, ] + @  
;

\ Move memory backward (src -> dst)
: cmove              ( src dst # -- )  
    1 1 (cmove)
;

\ Move memory forward (src -> dst)
: cmove>            ( src dst # -- )
    -1 -1 (cmove)
;

: abs     ( n -- p )
    dup 0< if negate then
;

: dabs     ( nd -- pd )
    dup 0< if dnegate then
;

: aligned   ( addr -- addr )
    1+  $fffe and   \ TODO: why cell# not here?
;

: /STRING    ( addr n delta -- addr+delta n-delta )
    >r swap r@ + swap r> -
;

: m/mod      ( ud un -- un.r ud.q )
    >r  s>d dup r>
    s>d
    dum/mod
    2>r drop 2r>
;

\   Custom:  set a flag in a word
: |!        ( mask addr -- )
    swap over @ OR swap !     
;

\   Custom:  reset a flag in a word
: &!        ( mask addr -- )
    swap NOT over @ AND swap !     
;

: fill  ( addr n ch -- )
    rot rot 1 (cfill)
;

: erase ( buff size -- )
    0 fill
;

:: compare     ( addr u addr' u' -- -1/0/1 )
    u u' min 0 do
        addr i + c@
        addr' i + c@
        - ?dup if unloop exit then
    loop
    u u' -  \ length is answer
;

: tolower ( ch -- ch )
   dup [char] a [ [char] z 1+ literal ] within $20 and -
;

:: comparef     ( addr u addr' u' -- -1/0/1 )
    u u' min 0 do
        addr i + c@ tolower
        addr' i + c@ tolower
        - ?dup if unloop exit then
    loop
    u u' -  \ length is answer
;

