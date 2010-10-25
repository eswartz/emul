
:   ints-on
    INT_VDP (>context) [ CTX_INT field, ] 
;
:   ints-off
    0 (>context) [ CTX_INT field, ] 
;

:   ints-check
    ints-on ints-off
;

\   Last VDP status
Variable vdp-status

\   Timer
DVariable vdp-ticks 

:   vdp-int-handler 
    'INTS c@  
    dup M_INT_VDP and if 
        \ acknowledge interrupt
        $fe and  'INTSP c!
        
        \ acknowledge VDP
        VDPST c@  vdp-status c!
        
        \ timer
        1. vdp-ticks d+!
        
        \ scan keyboard
        1 kbdtimer +!    
        
        kbd-scan
        
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
    'INTS c@  1 r> lsh  or  'INTS c!

;

:   call-int    ( num -- )
    >int-vec @  
    7 (context>) [ CTX_INT field, ] >r 
    execute
;

:   ints-init
    ints-off
    ['] vdp-int-handler  INT_VDP     enable-int
    ['] nmi-int-handler  INT_NMI     enable-int
    ints-on
;

