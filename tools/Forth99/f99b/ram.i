
Variable ramptr     RamTop ramptr !

: RamVar  ( n -- )  create  

    negate  ramptr +!  
    ramptr @  ,

    ( be sure compiler doesn't optimize )
does>
    @

;  


cell   RamVar state

User rp0
User sp0
User (pad)
User (#pad)
User (spad)

