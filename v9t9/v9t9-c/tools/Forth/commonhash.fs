\	commonhash.fs				--	hash routines for dictionary (not used)
\
\	(c) 1996-2009 Edward Swartz
\
\   This program is free software; you can redistribute it and/or modify
\   it under the terms of the GNU General Public License as published by
\   the Free Software Foundation; either version 2 of the License, or
\   (at your option) any later version.
\ 
\   This program is distributed in the hope that it will be useful, but
\   WITHOUT ANY WARRANTY; without even the implied warranty of
\   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
\   General Public License for more details.
\ 
\   You should have received a copy of the GNU General Public License
\   along with this program; if not, write to the Free Software
\   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
\   02111-1307, USA.  
\
\	$Id: commonhash.fs,v 1.5 2009-01-03 16:23:32 ejs Exp $

\	Hash index is MOD'ded by this
\ &17 constant hash-buckets
&59 constant hash-buckets

\	Each bucket has this many entries;
\	last one points to next bucket in chain
&16 constant hash-bucket-size


\	Each entry has pointer to XT
\	which can be used to find the name again

: hash	( caddr u -- idx )
\	.s
\	2dup ." Hashing: " type
	[char] [ emit
	0 swap			\ TOS is counter
	0 ?do
		1 cshift
		over i + 
		c@ dup 
		[char] a [char] z within if
		   $20 -
		then
\ [ order ] .s cr		
		XOR	 
	loop
	0 hash-buckets UM/MOD drop	\ select bucket (unsigned mod)
	nip					\ lose caddr
	[char] ] emit
\	dup ." =" . cr
;


: hash  ( caddr u -- idx )
    [char] [ emit
    0 swap          \ TOS is counter
    0 ?do
        1 cshift    over i +    c@ dup 
        [char] a [char] z within if $20 - then
        XOR  
    loop
    0 hash-buckets UM/MOD drop  \ select bucket (unsigned mod)
    nip                 \ lose caddr
    [char] ] emit
;


\	Allocate new bucket and store pointer at ptr
\
: bucket-allot ( ptr -- )
\	." allocating new bucket from " dup . cr
	T $aa55 , here hash-bucket-size cells dup allot 
	over swap erase
\	dup ." -> " . 
	swap 
	! H
;

\	Point to end of a chain of buckets
\	bucket will never be 0 on entry.
order
: chain>end	( bucket -- entry )
\	." bucket>end" cr
\  .s	dup 100 dump
\	.s
	begin
		dup 	\ save original bucket
		hash-bucket-size 1- T cells H +  \ point to end
   		dup 
\		.s 
		T @ H  	\ if non-NULL, we filled the bucket
	while
\	." next bucket..." dup . cr
		nip T @ H	\ point to new bucket now
	repeat

	\ scan list for empty spot
	( last-bucket last-bucket-end-ptr )
\ .s
    swap begin
		dup T @ H 
	while
		T cell+ H
	repeat
\ .s
	\ must terminate in 0...hash-bucket-size - 1
	\ since the next-ptr is 0
	( last-bucket-end-ptr last-bucket-entry )

	\ now see if we're pointing to next ptr
	2dup = if
		bucket-allot
		T @ H
	else
		nip
	then
\	.s
;

: >bucket	( wl idx -- bucket )
	hash-bucket-size T cells H * +	\ pointer to bucket
;

\	Find empty entry for string
: hash>new ( wl caddr u -- entry )
	hash >bucket
	chain>end
;

 [if]

\	Search one bucket for a match
: match-entry 	( caddr xt -- 1 | 0 )
	dup if
		\ xt>nfa 
		\ dup id. 
		(nfa=)
	else
		2drop 0
	then
;

: bucket>find	( caddr bucketptr -- xt 1 | bucketptr 0 )
\	." bucket>find" .s cr
	hash-bucket-size 1- cells over + swap do
		( caddr )
		dup  i @  match-entry
		if 
			drop  
			i @ 
			1 
			unloop 
		\	.s 
			exit 
		then
		T cell H
	+loop
	drop 0
;

\	Find an entry in the hash chain
: chain>find	( caddr bucket -- entry 1 | 0 )
\	." chain>find" .s cr

	\ scan list for match
	begin
		2dup bucket>find	   ( caddr bucket : bucketptr 0 | xt 1 )
		if 
			nip nip 1 
\			.s 
			exit
		else
			T hash-bucket-size 1- cells + @ H
			dup 0= \ .s
		then
	until
	drop 0
;

: hash>find ( caddr wl -- entry nfa )
	>r dup count hash r> swap >bucket
	( caddr bucket ) 
	chain>find
\	.s
;

\ \\\\\\\\\\\\\\\\\\\\\\\

\	List words

[IFUNDEF] WORDS
\ use hash list for testing
: WORDS
	forth-wordlist
	hash-buckets 0 do
		dup i hash-bucket-size cells * +
		hash-bucket-size 1- 0 do
			dup i cells + @
			?dup if  \ ." [" dup . ." ]"
				\ xt>nfa 
				id. space
			then
		loop
		drop
	loop
	drop
;
[ENDIF]

: eval-me
	s" : test 10 0 DO 10 0 DO I J + DUP 9 = IF LEAVE THEN KEY? IF KEY EMIT ELSE $2A EMIT THEN LOOP CR LOOP ;"
;

[then]
