
\ clear window
Code cls   ( -- )
    clr T1      xop T1 , SYS^ #    #cls data   NEXT    
end-code

\ create text window
\ Code win!               ( rows cols row col -- )
\    mov *SP+ , T1     sla TOS , 8 #      andi T1 , $ff #  soc TOS , T1        \ offsets in T1
\
\    mov *SP+ , TOS     
\    mov *SP+ , T2     sla TOS , 8 #      andi T2 , $ff #  soc TOS , T2        \ size coords in T2
\
\    xop T1 , SYS^ #        #win data       POP             NEXT
\ end-code

\ set text window
Code win!     ( cols|rows col|row -- )
    mov *SP , T1
    xop TOS , SYS^ #        
    #win data       
    POP2             
    NEXT
end-code

\ : win!       ( cols rows col row -- )
\    b>s >r b>s r> (win!)
\ ;


\ full screen window
: full!
    -1 0 win!
;

\ get dimensions of window
\ Code win@               ( -- rows cols row col )
\    xop T1 , SYS^ #
\    #win? data
\    PUSH                mov T2 , TOS          andi TOS , $ff #
\    PUSH                srl T2 , 8 #          mov T2 , TOS    
\    PUSH                mov T1 , TOS          andi TOS , $ff #
\    PUSH                srl T1 , 8 #          mov T1 , TOS
\    NEXT
\ end-code

Code win@    ( -- cols|rows col|row )
    dect SP
    mov TOS , *SP
    xop TOS , SYS^ #
    #win? data
    dect SP
    mov T1 , *SP
    NEXT
end-code

\ Get the X/Y cursor in the window
Code xy@    ( -- x|y )
    PUSH
    xop TOS , SYS^ #
    #xy data
    NEXT
end-code

\ Get the address (and shift) of an X/Y coordinate in the window
Code xyad@    ( x|y -- shift addr )
    PUSH
    xop TOS , SYS^ #
    #xyad data
    mov T1 , *SP
    NEXT
end-code


Code line  ( x1 y1 x2 y2 op|c -- )
    PUSH
    xop *SP , SYS^ #
    #line data
    ai SP , &10 #
    POP
    NEXT
end-code

Code circle  ( x y r op|c -- )
    PUSH
    xop *SP , SYS^ #
    #circle data
    ai SP , &8 #
    POP
    NEXT
end-code


Code rect  ( x1 y1 dx dy op|c -- )
    PUSH
    xop *SP , SYS^ #
    #rect data
    ai SP , &10 #
    POP
    NEXT
end-code


Code pixel ( x1 y1 op|c -- )
    PUSH
    xop *SP , SYS^ #    
    #pixel data
    ai SP , 6 #
    POP
    NEXT
end-code

0 [IF]
Code >set
    clr T1     
    jmp 1 $f
end-code
Code >reset
    li T1 , 1 #
    jmp 1 $f 
end-code
Code >xor
    li T1 , 2 #  
1 $:
    xop T1 , SYS^ #    
    #bitfnc data    
    NEXT
end-code
[ENDIF]

\ Set the registers and tables for a mode, but nothing else
Code (mode)  ( # -- )
    XOP TOS , SYS^ #
    #mode data      
    POP
    NEXT
end-code

\ Reset the screen (terminal, palette, etc) but not registers
Code vreset
    XOP TOS , SYS^ #
    #vrst data      
    NEXT
end-code

\ Reset the video registers
Code vregrst
    XOP TOS , SYS^ #
    #vrstr data      
    NEXT
end-code

\ Reset the palette
Code vstdpal
    XOP TOS , SYS^ #
    #pal data
    NEXT
end-code

\ Set a mode, reset the screen, and restore the palette
: mode  ( mode# -- )
    (mode) vreset vstdpal
;

\ Set the video page (reads/writes)
Code (vrwpg)     ( 0|1|2|3 -- )
    xop TOS , SYS^ #
    #vrwpage data
    POP
    NEXT
end-code

\ Set the visible video page (only)
Code (vpg)
    xop TOS , SYS^ #
    #vpage data
    POP
    NEXT
end-code

\ Set the r/w and visible page
: vpage dup (vrwpg) (vpg) ;

$000    constant    text
$001    constant    gfx
$002    constant    bitmap
$002    constant    gfx2
$003    constant    gfx3           
$004    constant    gfx4
$005    constant    gfx5
$006    constant    gfx6
$007    constant    gfx7
$008    constant    text2
$009    constant    mono
$00A    constant    multi

\ tables for (vtab)
&0 constant #scr
&1 constant #ssz
&2 constant #col
&3 constant #csz
&4 constant #pat
&5 constant #psz
&6 constant #spt
&7 constant #spr
&8 constant #smt
&9 constant #spc
&10 constant #fre

Code (vtab)    ( n -- addr)
    xop TOS , SYS^ #
    #vtab data
    mov *TOS , TOS
    NEXT
end-code
    
Code (fgbg!)   ( fg|bg -- )
    XOP TOS , SYS^ #
    #fgbg! data     
    POP             NEXT    
end-code

Code (fgbg@)   ( -- fg|bg )
    PUSH  
    XOP TOS , SYS^ #
    #fgbg@ data     
    NEXT    
end-code

: fg@   ( -- fg )
    (fgbg@) s>b drop ;
: bg@   ( -- bg )
    (fgbg@) s>b nip ;

: fg!   ( fg -- )
    (fgbg@) s>b nip b>s (fgbg!) ;
: bg!   ( bg -- )
    (fgbg@) s>b drop swap b>s (fgbg!) ;

Code <video
    li T1 , $A0 #     \ no blink or sprites 
1 $:
    xop T1 , SYS^ #
    #vintflags data      
    NEXT
end-code
Code video>
    clr T1 
    jmp 1 $b
end-code

Code (vr@)   ( index -- val )
    xop TOS , SYS^ #
    #vregr data
    NEXT
end-code

Code (vr!)   ( value index -- )
    PUSH
    xop *SP , SYS^ #
    #vregw data
    POP3
    NEXT
end-code

: blink!    ( t/f -- )
    vblink c!
;

: yres!     ( t/f -- )
    $80 and  9 (vr@) $7f and  or  9 (vr!) 
;

\ : border! ( color -- )
\    limi0
\    $f and
\    fg@ 4 lshift
\    or [ VDPWA ] literal c!  
\    $87 [ VDPWA ] literal c!
\    limi1
\ ;

\ : blank! ( t/f -- )
\    limi0
\    $40 and  $b0 or  [ VDPWA ] literal c!  $81 [ VDPWA ] literal c!
\    limi1
\ ;

Code bit8x8
    clr T1      jmp 1 $f
end-code
Code bit6x8
    li T1 , 1 # jmp 1 $f
end-code
Code bit5x6
    li T1 , 2 # 
 1 $:
    xop T1 , SYS^ #
    #vbfnt data     
    NEXT
end-code

Code (rnd) ( -- random )
    PUSH
    xop TOS , SYS^ # 
    #random data
    NEXT
end-code


: delay
    0 do loop ;
    
: csleep    ( centiseconds -- )
    s>d ticks d+  begin 2dup ticks d<  until  2drop 
;

: sprite ( n -- addr )
    2 lshift #spr (vtab) + 
;

: sprcol ( n -- addr )
    4 lshift #spc (vtab) + 
;


: sprpat ( n -- addr )
    3 lshift #spt (vtab) +
;

: sprmot ( n -- addr )
    2 lshift #smt (vtab) +
;

: sprmag ( t/f -- )
    $1 and  $1 (vr@) $fe and  or $1 (vr!)
;

: sprsz ( t/f -- )
    $2 and  $1 (vr@) $fd and  or $1 (vr!)
;

\ Get the size of a save/restore buffer
Code #vrs
    PUSH
    clr TOS
    xop TOS , SYS^ #
    #vsvrs data
    NEXT
end-code

\ Save the mode to a buffer
Code vsave   ( buf -- )
    li T1 , 1 #
1 $:    
    mov TOS , T2
    xop T1 , SYS^ #
    #vsvrs data
    POP
    NEXT
end-code

\ Restore the mode from saved VRegs
Code vrestore ( buf -- )
    li T1 , 2 #
    jmp 1 $b

end-code
