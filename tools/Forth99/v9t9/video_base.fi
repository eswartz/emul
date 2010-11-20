

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

: txt-setupmode
;

: txt-updatecolors
    $8700  or write-vregaddr
;

\ -------------------

: std-setfont   ( addr -- )
    v-patts @   $800  gvmove
;


\ -------------------

Create TextFunctions 
    v-coordaddr ,       ' txt-coordaddr , 
    v-drawchar ,        ' txt-drawchar ,
    v-savechar ,        ' txt-savechar , 
    v-restorechar ,     ' txt-restorechar ,
    v-drawcursor ,      ' txt-drawcursor ,
    v-setfont ,         ' std-setfont ,
    0 , 



\       0=text mode
\           >0000 = screen
\           >0800 = patts
\           >1000+= free
\
Create TextModeParams
    v-screen , 0 ,      v-screensz , 960 ,
    v-patts , $800 ,    v-pattsz , $800 ,
    v-colors , $0 ,     v-colorsz , $0 ,
    v-sprites , $0 ,    v-sprcol , $0 ,
    v-sprpat , $0 ,     v-sprmot , $0 ,
    v-free , $1000 ,

    v-width , 40 ,      v-height , 24 ,

    v-setupmode ,       ' txt-setupmode ,
    v-updatecolors ,    ' txt-updatecolors ,
    
    0 ,
    
create  TextModeRegs
    $8000 , $81B0 , $8200 , $8401 , 0 ,
    
: text-mode
    TextFunctions write-var-list
    TextModeParams write-var-list
        
    TextModeRegs write-vregs
;

\ -------------------

: (sprite-setup)
    $00  v-sprites @  $80  vfill
    $00  v-sprpat @   $800 vfill
    $00  v-sprmot @   $80 vfill
;


: std-sprite-setup
    (sprite-setup)
    $D0  v-sprites @ vc!
;

: v9938-sprite-setup
    (sprite-setup)
    $D8  v-sprites @ vc!
    $00  v-sprcol @  $200  vfill
;


\ -------------------

\       1=graphics mode
\           >0000 = screen
\           >0300 = sprites
\           >0380 = colors
\           >03A0 = sprite motion
\           >0420 = sprite patterns (really 0->800)
\           >0800 = char patts
\           >1000+= free
\
Create GfxModeRegs
    $8000 , $81A0 , $8200 , $830E , $8401 , $8506 , $8600 , 0 ,

Create GfxModeParams
    v-screen , 0 ,      v-screensz , 768 ,
    v-colors , $380 ,   v-colorsz , $20 ,
    v-patts , $800 ,    v-pattsz , $800 ,
    v-sprites , $300 ,  v-sprcol , $0 ,
    v-sprpat , $0 ,     v-sprmot , $3A0 ,
    v-free , $1000 ,
    
    v-width , 32 ,      v-height , 24 ,
    
    v-setupmode ,       ' gfx-setupmode ,
    v-updatecolors ,    ' gfx-updatecolors ,
    
    0 ,

: gfx-setupmode
    std-sprite-setup
;

: gfx-updatecolors
    v-colors @  v-colorsz @  vfill
;

: gfx-mode
    TextFunctions write-var-list
    GfxModeParams write-var-list
      
    GfxModeRegs write-vregs
;


\ -------------------


\       8=text 2 mode
\           >0000 = screen (to >870 for 212-line mode)
\           >0A00 = colors (blinks)
\           >1000 = patts
\           >1800+= free
\
Create Text2ModeRegs
    $8004 , $81B0 , $8200 , $832F , $8A00 , $8402 , $8D22 , 0 ,

Create Text2ModeParams
    v-screen , 0 ,      v-screensz , 2160 ,
    v-colors , $a00 ,   v-colorsz , 2160 8 / ,
    v-patts , $1000 ,   v-pattsz , $800 ,
    v-sprites , 0 ,     v-sprcol , 0 ,
    v-sprpat , 0 ,      v-sprmot , 0 , 
    v-free , $1800 ,
     
    v-width , 80 ,      v-height , 24 ,
    
    v-setupmode ,       ' txt2-setupmode ,
    v-updatecolors ,    ' txt-updatecolors ,

    0 ,
    
: txt2-setupmode
    txt-setupmode
    $00  v-colors @ v-colorsz @  vfill
;
    
: text2-mode
    TextFunctions write-var-list
    Text2ModeParams write-var-list
        
    Text2ModeRegs write-vregs

;
    