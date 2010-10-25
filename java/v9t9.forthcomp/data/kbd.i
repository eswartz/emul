
Variable    kbdtimer
Variable    kbddelay
Variable    kbdrate

Variable    kbdshift
Variable    kbdscan
Variable    kbdlast
Variable    kbdflag

16          Constant    kbdbufsize

Variable    kbdbuf      kbdbufsize allot
Variable    kbdtail
Variable    kbdhead

Variable    timeout
Variable    randnoise

:   read-row  ( col -- bits )
    'KBD c!   'KBD c@
;

:   toupper     ( ch -- ch )
    dup $61 >=  over $7B <=  or  -$20 and  -
;

:   buffer-key  ( ch -- )
    \ restart timer
    0 kbdtimer c!

    \ remember the key
    kbdscan c!
    
    \ add to buffer
    kbdtail c@  dup  kbdbuf +  kbdscan c@  swap  c!

    \ advance pointer, unless we hit the head    
    1+  kbdbufsize  1- and  kbdhead c@  over
    - if
        kbdtail c!
    else
        drop
    then 
;

:   repeat-key  ( ch -- )
    kbdscan c@  over =  if
        \ same key: see if enough time has elapsed since last key
        kbdflag c@ 0<       \ repeating? 
        if
            kbdtimer c@ kbddelay c@  <  if  drop exit  then
        else
            \ see if time to repeat
            kbdtimer c@ kbdrate c@  <  if  drop exit  then
            
            $80 kbdflag +!
        then
    else
        \ new key: reset repeat flag
        kbdflag c@  $7f and  kbdflag c! 
    then
    
    buffer-key
;

:   handle-key  ( tableoffs -- )
    graddr >r
         
        kbdshift c@  3 rsh  grom_kbdlist +      \ get shifted table ptr
        
        g@      \ get table entry
        +       \ and char offset
        
        gc@     \ read char
        
    r> gwaddr
    
    \ check alpha and uppercase if needed
    $80 'KBDA c!  'KBDA c@  if  toupper  then  $00 'KBDA c!
    
    \ HACK!  Fctn-Shift-S  -->  Ctrl-H
    dup 211 = if  
        $30 kbdshift c@ = not  $08 or  and 
    then
    
    dup if  repeat-key  else  drop  then 
;

\   Actions when any key is detected
:   key-actions
    timeout @ randnoise +!
    kbdscan c@ randnoise +!
     
    0 timeout !
    
    true vid-show
;

:   kbd-scan
    \ check break... TODO
    
    \ clear alpha lock line and read break-able bits
    $0 'KBDA c!
    
    0 read-row
    
    $70 and  kbdshift c!           \ save shift bits
    
    0 kbdscan c!
    
    6 0 do 
        i   read-row
        
        i 0= if $07 and then        \ ignore shifts in first row
         
        dup if
            i  3 lsh            \ table row offset
            
            swap >bit swap +    \ column
                        
            handle-key leave
        else
            drop
        then
    loop
    
    kbdscan c@ 0= if
        \ no key!
        \ any shifts at least?
        kbdshift c@ 0= if exit then
    then
    
    key-actions
    
;

:   kbd-init
    30 kbddelay !   \ 1/2 s before repeat
    3 kbdrate !     \ 1/20 s delay between repeat
    0 kbdflag c!
    0 kbdscan c!
    0 kbdlast c!
    0 kbdtail c!
    0 kbdhead c!
;

true <EXPORT

:   key?    ( -- f )
    kbdhead c@  kbdtail c@  = not  
;

:   key     ( -- key )
    key? if
        kbdhead c@  
        dup  1+ kbdbufsize 1- and kbdhead c!    \ advance
        kbdbuf + c@ 
    else
        0
    then
;

EXPORT>
