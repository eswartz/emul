
0 [if]

This is a benchmarking file.  Run ' word reps bench.

[endif]

variable (sp)

: bench ( xt reps -- ticks )
 ticks >r
0 do
	sp@ >r
	dup execute 
	r> sp!
loop
drop
 r> ticks
 swap -
;

: "(dump)
s" : (dump) >r  over +  ( addr addr+cnt ) swap do i per-line j (dumpline) key? if leave else per-line then +loop rdrop ;" 
;

: test-eval
s" 8 constant per-line : 2u. 0 <# # # #> type ; : 4u. 0 <# # # # # #> type ; " evaluate
s" \ dump one line " evaluate

\ too long?? >128 chars
\ s" : (dumpline) >r over 4u. space [char] = emit space 2dup over + swap do  i j execute 2u. space loop ( addr cnt ) per-line over ?do 3 spaces loop 0 ?do dup i + j execute dup $20 $7f within 0= if drop [char] . then emit loop drop rdrop cr ;" evaluate

s" : (dumpline) >r over 4u. space [char] = emit space 2dup over + swap do  i j execute 2u. space loop per-line over ;" evaluate

\ too long??
\ s" : (dump) ( addr cnt xt -- ) >r  over +  ( addr addr+cnt ) swap do i per-line j (dumpline) key? if leave else per-line then +loop rdrop ; : dump base @ >r hex ['] c@ (dump) r> base ! ;" evaluate

"(dump) evaluate
s" : dump base @ >r hex ['] c@ (dump) r> base ! ;" evaluate

;

\ direct threading @ 50 w/ Forth TRAVERSE: 12465
\ direct threading @ 50 w/ prim TRAVERSE: 12358
\ indirect threading: 


: test-words
	latest		\ !!! need real wordlist
	begin
		dup lfa>nfa
		@ dup
		0= 
	until
	drop 2drop
;
