\	include.fs					-- included implementation
\
\	(c) 1996-2008 Edward Swartz
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
\	$Id: include.fs,v 1.1 2008-12-17 05:56:27 ejs Exp $

Code dsrlnk 	( pab code -- status )
	mov TOS , T1
	POP
	ai TOS , 9 #
	mov TOS , $8356 @>
	xop T1 , 1 #
	&19 data
	mov T1 , TOS
	NEXT
end-code

: dsr# ( code pab -- status )
	tuck vc!
	8 dsrlnk
;


$8370 constant pabtos 

\	reclen is needed to get a record buffer;
\	it should be $100 for byte files
: +pab		( caddr u buf -- vaddr )
	limi0

	\ get link
	pabtos @ 

	\ get buffer space
	over negate  pabtos +!  

	\ save buffer addr and link
	pabtos @ >r  >r

	\ get space for pab, filename, and link
	2dup + &13 + negate  pabtos +!

	\ write pab
	pabtos @  vwaddr

	\ link
	r>  ,v!

	\ pab
	$00 ,vc!  $10 ,vc!
	\ addr
	r> ,v!  

	\ reclen
	,vc!
	
	0 ,vc!  0 ,v!  0 ,vc! 

	\ copy length
	dup ,vc!		( caddr u )

	\ copy filename
	,vmove

	\ add some blanks
	&10 0 do bl ,vc! loop

	pabtos @  &2 +

	limi1
;

: -pab		( vaddr -- )
	pabtos @  v@  pabtos !
;

: (rdln)		( caddr u fileid -- u status )
	>r
	$2 r@ dsr#
	dup 0= if
		drop
		r@ 2+  v@  swap  ( caddr vaddr u )
		r@ 5 +  vc@	( caddr u ch# )
		min  dup >r
		limi0  swap rot rot  vcmove limi1
		r> 0
	else
		\ EOF or error
		nip nip 0 swap
	then
	rdrop
;

: INCLUDE-FILE		( i*x fileid -- j*x )
	<input
	loadfile !
	1 loadline !
	0 blk !
	begin refill while interpret repeat
	\ $8370 ?
	1  loadfile @  dsr#  drop
	input>
;

: INCLUDED ( addr c -- )
	$50 +pab  >r
	0 r@ dsr#
	
	?dup if 
		." not found: " . cr
	else
		r@ include-file
	then

	rdrop -pab

;

