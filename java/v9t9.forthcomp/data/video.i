
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

\   (Not a good reason for locals, since they're much slower, but just have something for now)

::   write-vregs ( reglist -- )
    begin 
        reglist @ dup
    while
        write-vregaddr
        2 'reglist +!
    repeat   
    drop
;

: vid-show ( f -- )
    $40 and 
    1 get-vreg  or
    $8100 or  write-vregaddr
;

\ ---------------------

Variable v-screen
Variable v-size
Variable v-patts

Variable vx
Variable vy

Variable v-sx
Variable win-x
Variable win-y
Variable win-sx
Variable win-sy

:   text-mode
    TextModeRegs write-vregs
    0 v-screen !
    960 v-size !
    $800 v-patts !
    40 v-sx !
    40 win-sx !
    24 win-sy !

    term-reset
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
    v-screen @ v-size @ vfill
;

:   term-reset 
    0 vx !
    0 vy !
;

:   cls  32 v-clear  term-reset ;

:   curs-addr ( -- )
    vy @ win-y @ +  v-sx @ *  vx @ win-x @ +
    v-screen @ + 
;

:   emit    ( ch -- )
    curs-addr  $4000 or  vwaddr  swap  c!
    1 vx +!  vx @ win-sx @ >= if 
        0 vx !  1 vy +!  vy @ win-sy @ >= if
            0 vy !
        then
    then   
;

:   cr  13 emit  ;
:   space  32 emit  ;

EXPORT>

:   video-init
    \ reset latches
    VDPRD c@ drop
    0 GPLWD c! 
    
    text-mode
    
    $8717 vwaddr
    
;


