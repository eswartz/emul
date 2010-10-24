
create  TextModeRegs
    $8000 , $81B0 , $8200 , $8400 , 0 ,

:   write-vregs ( regs -- )
    begin 
        dup @ dup
    while
        write-vreg
        2+
    repeat   
    drop
;

:   text-mode
    
;