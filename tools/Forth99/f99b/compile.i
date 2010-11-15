

: create ; target-only
: : ; target-only
: ; $70 c, ; immediate target-only 


Variable lastxt

: xt! ( xt addr -- ) swap  1 ursh $8000 or  swap ! ;
: (does>) r> lastxt xt!  ; 
: does> postpone (does>) postpone rdrop ; immediate target-only


[IFUNDEF] STATE
User STATE
[THEN]

[IFUNDEF] [
: [
    0 state !
; immediate target-only
[THEN]
