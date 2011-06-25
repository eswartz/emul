
: bgflip
    begin &16 0 do 100 delay i border! loop again
  ;
  
: blflip
    begin 400 delay false blank! 400 delay true blank!  again
;

: bars
    gfx
    \ set solid pattern
    $1000 $800 do 
        $ff i vc!
    loop
    
    \ set fg colors 
    $3A0 $390 do
        i 4 lshift  i vc!
    loop

    \ draw bars
    $8 0 do
        $100 $80 do
            i $f8 = 0= if i emit then  i emit
            8 
        +loop
        cr
    loop
        
;

create pattern
    $01 c, $03 c, $07 c, $0f c, $1f c, $3f c, $7f c, $ff c,
    $ff c, $7f c, $3f c, $1f c, $0f c, $07 c, $03 c, $01 c,
    $80 c, $c0 c, $e0 c, $f0 c, $f8 c, $fc c, $fe c, $ff c,
    $ff c, $fe c, $fc c, $f8 c, $f0 c, $e0 c, $c0 c, $80 c,
    
: sprites
    pattern  $a8 sprpat  &32 cvmove  limi0
    &32 0 do   i sprite vwaddr
        (rnd) $ff and dup $d0 >= if $10 + then ,vc!  (rnd) ,vc!
        $a8 ,vc! i ,vc!
        #spc (vtab) if   i sprcol vwaddr
            i 16 + i do  i $f and ,vc!  loop  then
    loop limi1 ;

\ Make the graphics mode colors standard
: stdcol
    #col (vtab) $20 over + swap do
        $10 i vc!
    loop 
;

: fillmem ( val vaddr cnt -- )
    limi0
    swap vwaddr 0 do
        dup ,vc!
    loop
    limi1
;


: rndmem ( vaddr cnt -- )
    limi0
    swap vwaddr 2 urshift 0 do
        (rnd) s>b ,vc! ,vc! (rnd) s>b ,vc! ,vc!
    loop
    limi1
;

\ Simulacrum of lines
: lines
    &192 0 do
        limi0
        i &256 * &256 i vfill
        limi1
    loop
;

\ move sprites around
: sprmov ( random? -- ) limi0 >r &32 0 do
        i sprite dup vbank vraddr'
        ,vc@ ,vc@ swap
        j  0= if  \ get deltas
            (rnd) dup  $c000 and &14 rshift  swap 8 lshift &14 rshift
        else 1 1  then 
        i sprite vwaddr'
        >r + $ff and dup $d0 >= if 16 + then ,vc!
        r> + ,vc!
    loop limi1 rdrop ;
: sprmovs
    begin dup sprmov key? 0= while repeat key drop
    drop ;

\ Make sprites using the "CC" bit and exhibiting logical color OR'ing
: spror
    limi0
    
    $a8 sprpat $20 $ff vfill
    
    0 sprite  vwaddr  $80 ,vc! $80 ,vc!
    1 sprite  vwaddr  $88 ,vc! $88 ,vc!
    2 sprite  vwaddr  $8c ,vc! $8c ,vc!
    3 sprite  vwaddr  $7c ,vc! $7c ,vc!
    
    $ac sprpat vwaddr
    $20 0 do
        $aa ,vc! $55 ,vc! 
    2 +loop
    
    0 sprcol  $f  $41 vfill
    1 sprcol  $f  $42 vfill
    2 sprcol  $f  $44 vfill
    3 sprcol  $f  $48 vfill
    
    $20 4 do 
        $ac i sprite 2+ vc! 
        i sprcol $10 i $40 or vfill
    loop
    
    limi1
;

: pal ( idx -- )
    limi0
    $1000 vwreg
    $10 over + swap do
        i  $38 and   i 7 and  or  VDPCL c!
        i  $e0 and 5 rshift  VDPCL c!
    loop
    
    limi1
;

: vwait
    limi0
    $f00 vwreg
    begin VDPST c@ $80 and until
    limi1 
 ;
    
: pals 
    $ff 0 do i pal vwait loop 
;


\ Wait for MSX command to complete
\   Assumes status reg 2 is active
Code: (vwaitcmd)
1 $:
    movb VDPST @> , T1
    andi T1 , $0100 #
    jne 1 $b
    b *R11
end-code

\ Setup status reg 2
Code: (vsr2)
    li T1 , $028f #
    movb T1 , VDPWA #
    swpb T1
    movb T1 , VDPWA #
    b *R11
end-code
    
\ Wait for command to complete
Code vwaitcmd
    bl ' (vsr2) @>
    bl ' (vwaitcmd) @>
    NEXT
end-code

: rndpix
        limi0
    begin
        (rnd) s>b (rnd) pixel
    again
        limi1
;

: design
    &192 0 do
        0 i  &255  &192 i -  i 256 192 */ line
    loop
    &256 0 do
        i &192  &256 i -  0 i line
    loop
;

: pause begin key? while key drop repeat  key drop ;
: trace [char] ( emit r@ u. [char] ) emit space .s cr pause ;

User 'tstbuf
: tstbuf
    'tstbuf @ ?dup 0= if  
        here dup 'tstbuf ! 1024 allot 
        dup 1024 bl fill
    then
;
: >line ( caddr u -- ) 
    tstbuf swap cmove 
    tstbuf
;

   
   
\   Draw an intersecting bar character
\
: xbar ( addr part -- )
    >r
    dup vc@ r@ &240 &255 within if ( part addr ch )
        r> or
    else
        drop r>
    then
    swap vc!
;

\   Draw a solid horizontal bar, 
\   which can intersect other chars at corners
\   
: hbar  ( length vaddr -- )
    dup $f2 xbar            \ draw left half-bar
    ( length vaddr )        
    1+ 2dup swap &243 vfill \ draw middle bar
    + $f1 xbar ;            \ draw right half-bar
    
\   Draw a vertical bar
: vbar  ( length vaddr -- )
    dup $f8 xbar            \ draw top half-bar
            
    \ draw middle bar
    &80 + swap                ( vaddr length )
    0 ?do
        $fc over vc!
        &80 + 
    loop
    
    $f4 xbar ;            \ draw right half-bar

\   Draw a text box
: tbox  ( w h x y -- )
    b>s xyad@ nip     ( w h addr ) 
    
    rot 2dup swap hbar   ( h addr w )
    
    >r 2dup vbar   ( h addr )   ( r: w)
     
    2dup            
    r@ + 1+  vbar   ( h addr )
    
    swap 1+ &80 * + ( addr+h+1 ) 
    r> swap hbar
;

   