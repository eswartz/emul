
variable dp
here    dp !


: here dp @ ; 
: , here ! 2 dp +! ; 
: c, here c! 1 dp +! ;
 
: literal ( n -- ) dup -8 >= over 8 < and  if
    $f and $20 or c,  else
dup -128 >= over 128 < and  if
    $78 c, c,
else
    $79 c, ,
then then
; immediate target-only
 

: create ; target-only
: : ; target-only
: ; $70 c, ; immediate target-only 
: compile, 1 ursh $8000 OR postpone LITERAL ;
: s" postpone (s") ; immediate target-only

Variable lastxt

: xt! ( xt addr -- ) swap  1 ursh $8000 or  swap ! ;
: (does>) r> lastxt xt!  ; 
: does> postpone (does>) postpone rdrop ; immediate target-only

