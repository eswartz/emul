
\   Write VWADDR 
\
:   vwaddr ( addr -- )
    \ dup VDPWA c! swpb VDPWA c!
    VDPWA !
;

:   gwaddr ( addr -- )
    \ dup swpb GPLWA c! GPLWA c!
    GPLWA !
;

:   graddr ( -- addr )
    \ GPLRA c@ 8 lsh  GPLRA c@  or 1-
    GPLRA @ 1- 
;

:   g@  ( addr -- )
    gwaddr  GPLRD c@  8 lsh  GPLRD c@  or 
;

:   gc@  ( addr -- )
    gwaddr  GPLRD c@
;

:   gvmove ( gaddr vaddr len -- )
    rot gwaddr
    swap  $4000 or  vwaddr
    0 do  GPLRD c@ VDPWD c! loop
;

:   >bit ( mask -- lowest bit# )
    16 0 do
        dup  1 and  if
            drop i unloop exit
        then
        1 ursh
    loop
    
    drop true
;

( addr of buffer )
User    #ptr

:   pad ( -- addr )
    here 16 + 
; 

:   <#  ( -- )
    pad #ptr !
;

:   #>  ( -- c-addr len )
    #ptr @  pad  over - 
;

:   #c ( ch -- )
    -1 #ptr +!  #ptr @ c!   
;

:   sign ( sd -- ud )
    dup 0< if dnegate $2D #c then
;

:   digit ( n -- ch )
    dup 9 > if 55 else 48 then + 
;

:   #  ( ud -- ud )
    d>q  base @ s>d  dum/mod 2drop drop  digit #c
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

:   .d  ( n -- )
    s>d <# sign #s #> dtype
;

