
16          Constant    kbdbufsize

Create      kbdbuf      kbdbufsize allot

Create      kbdstate    10 allot

    : kbdtimer kbdstate ;
    : kbddelay [ kbdstate 1 + ] LITERAL ;
    : kbdrate [ kbdstate 2 + ] LITERAL ;

    : kbdshift [ kbdstate 3 + ] LITERAL ;
    : kbdscan [ kbdstate 4 + ] LITERAL ;
    : kbdlast [ kbdstate 5 + ] LITERAL ;
    : kbdflag [ kbdstate 6 + ] LITERAL ; 

    : kbdtail [ kbdstate 7 + ] LITERAL ;
    : kbdhead [ kbdstate 8 + ] LITERAL ;

Variable    timeout
Variable    randnoise

:   read-row  ( col -- bits )
    'KBD c!   'KBD c@
;

:   toupper     ( ch -- ch )
    dup $61 >=  over $7B <=  or  -$20 and  -
;

:   buffer-key  ( ch -- )
    \ remember the key
    kbdscan c!

    \ restart timer
    0 kbdtimer c!
    
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
        kbdflag c@        \ repeating? 
        if
            kbdtimer c@ kbddelay c@  <  if  drop exit  then
        else
            \ see if time to repeat
            kbdtimer c@ kbdrate c@  <  if  drop exit  then
            
            true kbdflag c!
        then
    else
        \ new key: reset repeat flag
        0 kbdflag c! 
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

    dup .d 10 demit
    
    ?dup if  repeat-key  then 
;

\   Actions when any key is detected
:   key-actions
    timeout @ randnoise +!
    kbdscan c@ randnoise +!
     
    0 timeout !
    
    true vid-show
;

:   kbd-scan
    \ clear alpha lock line and read break-able bits
    $0 'KBDA c!
    
    0 read-row
    
    dup $72 = if                    \ ctrl+fctn+shift+space (abort)?
        abort
    then
    
    $70 and  kbdshift c!           \ save shift bits
    
    6 0 do 
        i   read-row
        
        i 0= if $07 and then        \ ignore shifts in first row
         
        ?dup if
            i  3 lsh            \ table row offset
            
            swap >bit swap +    \ column
                        
            handle-key leave
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
    30 kbddelay c!   \ 1/2 s before repeat
    3 kbdrate c!     \ 1/20 s delay between repeat
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
