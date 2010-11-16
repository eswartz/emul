
:   ints-on
    INT_KBD (>context) [ CTX_INT field, ] 
;
:   ints-off
    0 (>context) [ CTX_INT field, ] 
;

:   ints-check
    ints-on ints-off
;

\   Last VDP status
1       RamVar vdp-status

\   Timer
2 cells RamVar vdp-ticks 

:   vdp-int-handler 
    'INTS c@  
    dup M_INT_VDP and if 
        \ acknowledge interrupt
        [ M_INT_VDP invert literal ] and  'INTSP c!
        
        \ acknowledge VDP
        VDPST c@  vdp-status c!
        
        \ timer
        1. vdp-ticks d+!
        
        \ keyboard timer
        kbdtimer c@ 1+ kbdtimer c!    
        
        \ scan keyboard
        
        kbd-scan
        
        \ acknowledge interrupt
        \ 'INTS c@  [ M_INT_KBD invert literal ]  and  'INTSP c!
        
        
        update-cursor
    else
        drop
    then
    
    exiti
;

:   kbd-int-handler
    'INTS c@  
    dup M_INT_KBD and if
        \ [char] K demit
     
        \ acknowledge interrupt
        [ M_INT_KBD invert literal ]  and  'INTSP c!
        
        kbd-scan
    else
        drop
    then
    exiti
;

:   nmi-int-handler
    abort
    exiti       \ NOTREACHED
;


:   >int-vec ( num -- )
    2*  IntVecs +
;
:   set-int-vec ( addr num -- )
    >int-vec  !
;

:   enable-int ( addr num -- )
    dup >r   set-int-vec

    \ enable bit
    'INTS c@  1 r> lshift  or  'INTS c!

;

:   call-int    ( num -- )
    >int-vec @  
    7 (context>) [ CTX_INT field, ] >r 
    execute
;

:   ints-init
    ints-off
 \   ['] kbd-int-handler  INT_KBD     enable-int
    ['] vdp-int-handler  INT_VDP     enable-int
    ['] nmi-int-handler  INT_NMI     set-int-vec
    ['] bootup           INT_RESET   set-int-vec
;

