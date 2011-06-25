: [name. ( xt -- caddr u ) xt>nfa dup 1+ swap c@ $1f and ; 
: (runtest sp0 @ sp! ; 
: runtest) ( txt t/f -- ) 
	   swap if 2a emit [name. type 2b emit drop else 5b emit [name. type 5d emit then  2e emit ; 
: runtests 5b emit 
