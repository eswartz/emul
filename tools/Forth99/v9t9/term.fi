
cell    RamVar vx
cell    RamVar vy

0 Value win-x
0 Value win-y
0 Value win-sx
0 Value win-sy

\   Reset terminal from the current mode
: term-reset
    0 to win-x
    0 to win-y
    v-width @  to win-sx
    v-height @  to win-sy
    
    0 vx !
    0 vy !
;

:   curs-addr ( -- addr bit )
    vx @ win-x +  vy @ win-y +  
    v-coordaddr @  execute
;

:   advance-row
    0 vx !  1 vy +!  
    vy @ win-sy >= if
        0 vy !
    then
;

:   advance-cursor
    1 vx +!  
    vx @ win-sx >= if
        advance-row 
    then   
;


:   cursor-off
    curs-addr v-cursor-off
;

:   crlf
    advance-row 
;  

:   bksp
    -1 vx +!
    vx @ 0< if
        win-sx 1-  vx !
        -1 vy +!
        vy @ 0< if
            win-sy 1-  vy !
        then
    then
;

1 <EXPORT

:   cls  32 v-clear  term-reset ;

:   emit    ( ch -- )
    cursor-off
    dup 13 = if
        drop crlf
    else dup 8 = if
        drop bksp
    else
        curs-addr  v-drawchar @ execute  
        advance-cursor
    then then 
;    target-only

:   cr  13 emit  ; 
:   space  32 emit  ;

:   type ( caddr n -- )
    over + swap do i c@ emit loop     
;

EXPORT>


