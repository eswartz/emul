
variable dp
here    dp !


: here dp @ ; 
: , here ! 2 dp +! ; 
: c, here c! 1 dp +! ;
 
: literal ( n -- ) dup -8 >= over 8 < and  if
    $f and $20 or c,  else
dup -128 >= over 128 < and  if
    $78 c, c,
else
    $79 c, ,
then then
; immediate target-only
 
: dliteral ( d -- ) 
    $7e c,
2dup -8. d>= over 8. d< and  if
    drop $f and $20 or c,  else
2dup -128. d>= over 128. d< and  if
    drop $78 c, c,
else
    $79 c, , ,
then then
; immediate target-only
 
: compile, 1 urshift $8000 OR postpone LITERAL ;

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
    dup d0< if dnegate then
;

: aligned   ( addr -- addr )
    #cell +  #cell not and
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
    0
    u u' min 0 do
        addr u + c@
        addr' u' + c@
        - dup if leave else drop then
    loop
    ?dup if
        \ answer
    else
        u u' -  \ length is answer
    then
;

