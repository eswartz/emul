

\ variable dp
\ here    dp !

1 <export

User STATE


\ : here dp @ ; 
\ : , here ! 2 dp +! ; 
\ : c, here c! 1 dp +! ;


: literal ( n -- )  
    dup -8 >= over 8 < and  if
        $f and $20 or c,  else
    dup -128 >= over 128 < and  if
        $78 c, c,
    else
        $79 c, ,
    then then
; immediate target-only
 
: dliteral ( d -- )  
        $7e c,
    2dup -8. d>= over 8. d< and  if
        drop $f and $20 or c,  else
    2dup -128. d>= over 128. d< and  if
        drop $78 c, c,
    else
        $79 c, ,
    then then
; immediate target-only 
 
: >call  1 urshift $8000 or  ; 
 
: compile,
  dup ['] ;s      = if  Iexit c,          drop    else
    dup ['] branch  = if  IbranchB c,     drop    else
    dup ['] 0branch = if  I0branchB c,    drop    else
    dup ['] unloop  = if  Irdrop_d ,      drop    else
    dup ['] i       = if  IatR c,         drop    else
                          >call ,   then then then then then
    ;

export>


1 [if]
    : RamVar  ( n -- )  create  
    
        negate  ramptr +!  
        ramptr @  ,
    
        ( be sure compiler doesn't optimize )
    does>
        @
    
    ;  
[else]
    : RamVar  ( n -- )    
    
        create immediate
        
        negate  ramptr +!  
        ramptr @  , 
    
    does>
    
        \ oops, for "state" itself, this um compiles a ton of shit
        \ into the dictionary -- prolly a bug
        state @ if 
        
            $79 c, 
            @
            ,  $70 c,
        else
            @
        then
    
    ;  

[then]

\ cell   RamVar state
\ variable state

