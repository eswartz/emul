
create  TextModeRegs
    $8000 , $81B0 , $8200 , $8401 , 0 ,

create  VRegSave      16 allot

:   w>b ( w -- lo hi )
    dup $ff and swap 8 ursh  
;

:   write-vregaddr ( reg -- )
    dup  
    vwaddr
    $7fff and w>b   dup 16 <  if  VRegSave + c!  else drop then
;

:   get-vreg ( reg# -- val )
    VRegSave + c@
;

:   write-vregs ( regaddrlist -- )
    begin 
        dup @ dup
    while
        write-vregaddr
        2+
    repeat   
    drop
;

: vid-show ( f -- )
    $40 and 
    1 get-vreg  or
    $8100 or  write-vregaddr
;

\ ---------------------

Variable v-addr
Variable v-size
Variable v-patts

:   text-mode
    TextModeRegs write-vregs
    0 v-addr !
    960 v-size !
    $800 v-patts !
    cls
    load-font
    true vid-show
;

\ -------------------

: load-font
    grom_font8x8  v-patts @   $800  gvmove
;

\ -------------------

1 <EXPORT

:   vfill ( ch addr len -- )
    swap $4000 or vwaddr
    0 do  dup VDPWD c!  loop 
    drop
;

:   v-clear ( ch -- )
    v-addr @ v-size @ vfill
;

:   cls  32 v-clear ;

EXPORT>

:   video-init
    \ reset latches
    VDPRD c@ drop
    0 GPLWD c! 
    
    text-mode
    
    $8717 vwaddr
    
;


