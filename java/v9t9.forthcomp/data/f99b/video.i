
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

Create  v-mode      16 allot

v-mode   0 +     Constant v-screen      \ VDP addr of screen
v-mode   2 +     Constant v-screensz    \ VDP addr of screen
v-mode   4 +     Constant v-patts       \ VDP addr of patterns
v-mode   6 +     Constant v-pattsz      \ VDP size of pattern table
v-mode   8 +     Constant v-colors      \ VDP addr of colors
v-mode  10 +     Constant v-colorsz     \ VDP size of color table
v-mode  12 +     Constant v-sprites     \ VDP addr of sprites
v-mode  14 +     Constant v-sprcol      \ VDP addr of sprite color table (0 if not sprite 2 mode)
v-mode  16 +     Constant v-sprpat      \ VDP addr of sprite patterns
v-mode  18 +     Constant v-sprmot      \ VDP addr of sprite motion
v-mode  20 +     Constant v-free        \ usable space

Create  v-state     8 allot

v-state  0 +     Constant v-sx  \ chars
v-state  1 +     Constant v-sy  \ chars
Variable v-sx
Variable v-sy
Variable v-curs

:   text-mode
    TextModeRegs write-vregs
    0 v-screen !
    960 v-screensz !
    $800 v-patts !
    40 v-sx !
    24 v-sy !

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
    \ swap >r
    \ 0 do  j VDPWD c!  loop 
    \ rdrop
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


