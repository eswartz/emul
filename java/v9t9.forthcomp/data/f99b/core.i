
User    base
User    dp

: here dp @ ;

: decimal 10 base ! ;
: hex 16 base ! ;



( addr of buffer )
User    #ptr

:   pad ( -- addr )
    here 16 + 
; 

:   #c ( ch -- )
    -1 #ptr +!  #ptr @ c!   
;

:   <#  ( -- )
    pad #ptr !  32 #c
;

:   #>  ( ud -- c-addr len )
    2drop #ptr @  pad  over - 
;

:   sign ( sd -- ud )
    dup 0< if dnegate $2D #c then
;

:   digit ( n -- ch )
    dup 9 > if 55 else 48 then + 
;

:   #  ( ud -- ud )
    d>q  base @ s>d  dum/mod 2>r drop  digit #c 2r>
;

:   #s ( ud -- )
    begin
        2dup or
    while
        #
    repeat
;

:   demit ( ch -- )
    'DBG c!
;

:   dtype ( c-addr len -- )
    over + swap ?do
        i c@ 'DBG c!
    loop
;


:   d.d  ( n -- )
    <# sign # #s #> dtype
;

:   .d  ( n -- )
    s>d d.d
;

:   quit
    10 base !
    
    begin
        key? if key emit 
        else ints-check
        then
    again
;

( BOGUS -- just for defining words at host time )
:   constant    ;

