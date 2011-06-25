
\   Extras

:   VALUE   ( c"..." n -- )
    CONSTANT
;

:   TO      ( c"..." n -- )
    ' >BODY 2- 
    state @ if
        [compile] lit , [compile] !
    else
        !
    then
;   immediate


\   Locals word set

\   dictionary space for storing names and XTs for temporary locals
| User lp

\   table of pointers to local defs
| User lptab

\   current local #
| User lpidx

\   old (find) xt
| User lpfind

| 8 constant #locals
  
| : localfind    ( c-addr lfa -- c-addr 0 | nfa 1 )
    over count  
    lptab @  dup lpidx @ cells +  swap  ?do 
        2dup  i @ count  compare 0= if
            2drop 
            i 1  unloop exit
        then
    loop
    2drop   lpfind @  execute
;
 
\   Hack until we can dynamically hook things 
:  L:
    ['] (find) (IS?)  dup ['] localfind = 0= if
        lpfind !
        ['] localfind ['] (find) (IS)
    else
        drop
    then 
    here $800 +  
    dup  #locals cells  erase 
    dup  lptab !  #locals cells + lp !
    0  lpidx !
    :
;

:  ;L
    [compile] ;
    lpfind @ ['] (find) (IS)
;   immediate


:   (local)      ( c-addr u -- )
    \ add new entry to lptab
    lp @ >r
    r@  lptab @ 
    
    r@ |immed |srch or or c!    
    over 1+ aligned  lp +!
    r> swap cmove
    
    \ now put down XT
    lp @   
    
    1 lpidx +!
;


