
Variable vx
Variable vy

Variable win-x
Variable win-y
Variable win-sx
Variable win-sy

\   Reset terminal from the current mode
: term-reset
    v-width @  win-sx !
    v-height @  win-sy !
    
    0 vx !
    0 vy !
;

:   curs-addr ( -- addr bit )
    vx @ win-x @ +  vy @ win-y @ +  
    v-coordaddr @  execute
;

:   advance-cursor
    1 vx +!  vx @ win-sx @ >= if 
        0 vx !  1 vy +!  vy @ win-sy @ >= if
            0 vy !
        then
    then   
;


:   cursor-off
    curs-addr v-cursor-off
;

1 <EXPORT

:   cls  32 v-clear  term-reset ;

:   emit    ( ch -- )
    cursor-off
    curs-addr  v-drawchar @ execute  
    advance-cursor
;

:   cr  13 emit  ;
:   space  32 emit  ;

EXPORT>


