
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

Create  v-mode      48 allot

: +field    ( "name" ptr -- ptr' )    dup Constant 2+ ; immediate

v-mode
    +field  v-screen      \ VDP addr of screen
    +field  v-screensz    \ VDP size of screen
    +field  v-patts       \ VDP addr of patterns
    +field  v-pattsz      \ VDP size of pattern table
    +field  v-colors      \ VDP addr of colors
    +field  v-colorsz     \ VDP size of color table
    +field  v-sprites     \ VDP addr of sprites
    +field  v-sprcol      \ VDP addr of sprite color table (0 if not sprite 2 mode)
    +field  v-sprpat      \ VDP addr of sprite patterns
    +field  v-sprmot      \ VDP addr of sprite motion
    +field  v-free        \ usable space
    +field  v-width       \ chars across
    +field  v-height      \ chars down
    
    +field  v-coordaddr   ( x y -- addr bit )
    +field  v-drawchar    ( ch addr bit -- )
    +field  v-savechar    ( addr bit buff -- )
    +field  v-restorechar ( buff addr bit -- )
    +field  v-drawcursor  ( addr bit -- )
drop
 
Create v-curs-under     8 allot

Variable v-curs                         \ state of cursor (0=off, 1=on)
Variable v-cursor-timer                 \ current iter
20 Constant v-cursor-blink              \ 30/60 sec
 
:   write-mode-params   ( table -- )
    begin
        dup @ 
    while
        dup  d@  swap !
        4 +
    repeat
    drop
;

:   switch-mode
    term-reset
    cls    
    load-font
    true vid-show
;

:   v-cursor-on ( addr bit -- )
    v-curs @ not if
        2dup
        v-curs-under v-savechar @  execute
        
        v-drawcursor @ execute
        true v-curs !
    else
        2drop
    then
;
:   v-cursor-off  ( addr bit -- )
    v-curs @ if
        2>r v-curs-under 2r>  v-restorechar @  execute
        false v-curs !
    else
        2drop
    then
;

:   update-cursor
    1 v-cursor-timer +!
    v-cursor-timer @  v-cursor-blink >= if
        0 v-cursor-timer !
        curs-addr
        v-curs @ if 
            v-cursor-off
        else
            v-cursor-on
        then
    then
;



\ ---------------------

: txt-coordaddr ( x y -- addr bit )
    v-width @ *  +
    v-screen @ + 
    0
;

: txt-waddr ( addr bit -- )
    drop  $4000 OR  vwaddr  
;
: txt-raddr ( addr bit -- )
    drop  vwaddr  VDPRD c@  
;
: txt-drawchar ( ch addr bit -- )
    txt-waddr VDPWD c!
;
: txt-savechar ( addr bit buff -- )
    >r txt-raddr r>  c!
;
: txt-restorechar ( buff addr bit -- )
    txt-waddr  c@  VDPWD c!
;
: txt-drawcursor ( addr bit -- )
    txt-waddr  [CHAR] _  VDPWD c!
;


Create text-mode-params
    v-screen , 0 ,      v-screensz , 960 ,
    v-patts , $800 ,    v-pattsz , $800 ,
    v-colors , $0 ,     v-colorsz , $0 ,
    v-sprites , $0 ,    
    v-sprcol , $0 ,
    v-sprpat , $0 ,     v-sprmot , $0 ,
    v-free , $1000 ,
    v-width , 40 ,      v-height , 24 ,
    
    v-coordaddr ,       ' txt-coordaddr , 
    v-drawchar ,        ' txt-drawchar ,
    v-savechar ,        ' txt-savechar , 
    v-restorechar ,     ' txt-restorechar ,
    v-drawcursor ,      ' txt-drawcursor ,
    0 , 


create  TextModeRegs
    $8000 , $81B0 , $8200 , $8401 , 0 ,
    
:   text-mode
    TextModeRegs write-vregs
    text-mode-params write-mode-params
    switch-mode
;

\ -------------------

: load-font
    grom_font8x8  v-patts @   $800  gvmove
;

\ -------------------

1 <EXPORT

:   vfill ( ch addr len -- )
    swap $4000 or vwaddr
    VDPWD swap 0 (cfill) 
;

:   v-clear ( ch -- )
    v-screen @ v-screensz @ vfill
;


EXPORT>

:   video-init
    \ reset latches
    VDPRD c@ drop
    0 GPLWD c! 
    
    text-mode
    
    $8717 vwaddr
    
;


