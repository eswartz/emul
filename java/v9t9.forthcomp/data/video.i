
create  TextModeRegs
    $8000 , $81B0 , $8200 , $8400 , 0 ,

:   write-vregs ( regs -- )
    begin 
        dup @ dup
    while
        vwaddr
        2+
    repeat   
    drop
;

\ ---------------------

Variable screen-addr
Variable screen-size

:   text-mode
    TextModeRegs write-vregs
    0 screen-addr !
    960 screen-size !
;

\ -------------------

:   vfill ( ch addr len -- )
    swap $4000 or vwaddr
    0 do  dup VDPWD c!  loop 
    drop
;

:   screen-fill ( ch -- )
    screen-addr @ screen-size @ vfill
;
