
\   Write VWADDR 
\
:   vwaddr ( addr -- )
    dup VDPWA c! swpb VDPWA c!
;

:   gwaddr ( addr -- )
    dup swpb GPLWA c! GPLWA c!
;

:   gvmove ( gaddr vaddr len -- )
    rot gwaddr
    swap  $4000 or  vwaddr
    0 do  GPLRD c@ VDPWD c! loop
;
