\	files.fs					-- FORTH file word set
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
\	$Id: files-bad.fs,v 1.5 2009-01-03 23:46:45 ejs Exp $

\	ior is the I/O error from 0 to 7
\	fam is 0=update, 1=output, 2=input, 3=append
\	fileid is a PAB addr

\
\	VDP allocation --
\	
\	FILES() must be set to less than 3 so that bitmap
\	mode can have space for a pab (>3800...xxx).
\
\	We adjust >8370 to keep track of open files.
\

$8380 constant dskws

\	DSRLNK is for use with non-bytefiles 

Code: (dsrlnk)
	[ dskws ] data
	[ there 2 + ] data

	limi 0 #
\	dbg
	mov *R13 , R5		\ offset 8/10

	li R6 , $2e00 #

	mov	$8356 @> , R0	\ get ptr to name
	mov	R0 , R9				
	ai	R9 , -8 #		\ point to error code

	bl	' vraddr @>
	movb VDPRD @> , R1	\ get filename len
	movb R1 , R3		\ save
	srl	R3 , 8 #				
	seto R4				\ # chars

	li	R2 , $834a #	\ buffer [in xopws]
0 $: inc R0   	 		\ move device name
	inc	R4
	c	R4 , R3
	jeq	1 $f
	bl 	' vraddr @>
	movb VDPRD @> , R1
	movb R1 , *R2+
	cb	R1 , R6			\ '.'
	jne	0 $b
1 $: mov R4 , R4			\ any chars read?
	jeq	9 $f
	ci	R4 , 7 #
	jgt	9 $f				\ too many?
	clr	$83d0 @>
	mov	R4 , $8354 @>		\ # chars in device name
	inc	R4
	a	R4 , $8356 @>		\ point to '.' in name

	lwpi $83e0 #			\ GPLWS

	clr	R1					\ init card counter
	li	R12 , $f00 #
	li R6 , $aa00 #
3 $: sbz 0 #
	ai	R12 , $100 #		\ start scan at >1000
	clr	$83d0 @>
	ci	R12 , $2000 #		\ last base?
	jeq	8 $f
	mov	R12 , $83d0 @>		\ store CRU
	sbo	0 #					\ turn on rom
	li	R2 , $4000 #
	cb	*R2 , R6			\ legal rom?
	jne	3 $b
	a	&10 dskws + @> , R2	\ add offset
	jmp	5 $f
4 $: mov $83d2 @> , R2
	sbo	0 #
5 $: mov *R2 , R2			\ any devices?
	jeq	3 $b	    		\ nope... next rom pleez
	mov	R2 , $83d2 @>		\ save next link
	inct R2
	mov	*R2+ , R9			\ get routine addr
	movb $8355 @> , R4		\ get len of caller
	jeq	7 $f				\ ??? no length?
	cb	R4 , *R2+			\ match name
	jne	4 $b
	srl	R4 , 8 #
	li	R6 , $834a #
6 $: cb	*R6+ , *R2+
	jne	4 $b
	dec	R4
	jne	6 $b
7 $: inc R1	    			\ increment card #
	bl	*R9					\ run it
	jmp	4 $b				\ if no error, DSR skips this word

	sbz	0 #					\ turn off rom
	lwpi dskws #
	mov	R9 , R0				\ get error code
	bl ' vraddr @>
	movb VDPRD @> , R1

	srl	R1 , &13 #
	jeq 9 $f				\ no error

	ori R0 , $4000 #
	bl ' vwaddr @>
	mov R1 , R2
	sla R2 , &13 #
	andi R2 , $7fff #
	movb R2 , VDPWD @>		\ clear error (else it will look like binary)

	inc R1					\ bias
	-dbg
8 $: 
	mov R1 , *R13
\	-dbg
	rtwp
9 $: clr *R13
\	-dbg
	rtwp
end-code

Code dsrlnk	( pab 8/10 -- ior )
	mov TOS , R0
	mov *SP , R1
	ai R1 , 9 #
	mov R1 , $8356 @>
	blwp ' (dsrlnk) @>
	0POP
	mov R0 , TOS
	NEXT
end-code


$8370 constant pabtos 

\	for PAB files, the record is 9 bytes plus the filename:
\	<1:opcode> <1:flags> <2:vdpbuff> <1:reclen> 
\	<1:curlen> <2:recnum> <1:status> <1:fnlen>
\
\	for byte files, the record is
\	<1:opcode> <1:flags> <2:curoffs> <1:byteoffs> 
\	<1:eof> <2:numsecs> <1:unused> <1:fnlen>

: p>buf		( pab -- buffer )
	\ skip filename to point to buffer space
	9 + dup vc@ &11 + + 
;

: p>fl		( pab -- @flags )
	1+
;

: p>ad		( pab -- @vaddr )
	2+
;

: p>len		( pab -- @len )
	4 +
;

: p>cnt		( pab -- @cnt )
	5 +
;

: p>rec		( pab -- @rec )
	6 +
;

\	the filename is a counted string
: p>fn
	9 +
;

: p>bcursec
	2 +
;

: p>bcuroffs
	4 +
;

: p>beofoffs
	5 +
;

: p>beofsec
	6 +
;

\	\\\\\\\\\\\\\\\\\\
\
\	Support for byte files:
\
\	ANS forth requires that files support byte access
\	as a stream.  TI files don't have this.  We need to
\	emulate it by using blocks.
\
\	We use the pab to store the long offset in the file,
\	and set $80 in the flags byte.

$80 constant b<fmt		\ binary/bytefile format: 
						\ on open, !bytefile, after, ==bytefile
$40 constant b<drt		\ dirty buffer

: p>bin?
	p>fl vc@ b<fmt AND 0<>
;

\	Query the dirty flag
\
: p>drt?		( pab -- )
	p>fl vc@ b<drt AND 0<>
;

\	Set the dirty flag
\
: p>drt!		( pab -- )
	p>fl dup vc@ b<drt OR swap vc!
;

\	Reset the dirty flag
\
: p>drt0		( pab -- )
	p>fl dup vc@ b<drt invert AND swap vc!
;


\	Calculate sector/offset from position
: >s+o					( doffs -- sec offs )
	$100 um/mod swap
;

\	Calculate position from sector/offset
: <s+o					( sec offs -- doffs )
	>r $100 um* 
	r> s>d d+
;

\	Set new sector/offset
: b>s+o!				( sec offs pab -- )
	swap over 			( sec pab offs pab )
	p>bcuroffs  vc!		\ offset
	p>bcursec v!		\ sector
;

\	Set new sector/offset filesize
: b>s+o!sz				( sec offs pab -- )
	swap over 			( sec pab offs pab )
	p>beofoffs  vc!		\ offset
	p>beofsec v!		\ sector
;

\	Set new position
: b>pos!				( dpos pab -- )
	>r >s+o r> b>s+o! 
;

\	Set new filesize
: b>sz!					( dsz pab -- )
	>r >s+o r> b>s+o!sz 
;

\	Get the sector and offset from the pab
: b>s+o@				( pab -- sec offs )
	dup p>bcursec v@		\ sector
	swap p>bcuroffs vc@		\ offset
;

\	Get the filesize (sector and offset) cached the pab
: b>s+o@sz				( pab -- sec offs )
	dup p>beofsec v@		\ sector
	swap p>beofoffs vc@		\ offset
;

\	Get the position from the pab
: b>pos@				( pab -- dpos )
	b>s+o@ <s+o
;

\	Get the filesize from the pab
: b>sz@				( pab -- dsz )
	b>s+o@sz <s+o
;

\	See if the file is at or past EOF
\
: b>eof?		( pab -- t|f )
	>r
	r@ b>pos@ 
	r@ b>sz@
	d< 0=
	rdrop
;

\	DSROP is for use with bytefiles
\
\	R0 = 0 for get info, >0 for read/write # sectors
\	( drv name paramblk sub -- ior )
Code (dsrop)
\	dbg
	mov *SP , R0
	ai R0 , -$8300 #
    swpb R0
	movb R0 , $8350 @>		\ offset to param block
	mov 2 @>(SP) , $834e @>	\ vdp name
	movb 5 @>(SP) , $834c @>
	ori TOS , $0100 #		\ DSR subroutine name

	mov TOS , $83e0 &12 + @> \ copy to GPLWS R6

	ai SP , 6 #				\ lose args

	lwpi $83e0 #
	li R13 , GPLRD #
	li R14 , $0100 #
	li R15 , VDPWA #

	li R12 , $1000 #		\ our CRU base
	sbo 0 #					\ turn device on
	li R1 , $400A #			\ subprograms
	mov *R1 , R1
	jeq 0 $f

\	only compares 1 char subprogram names
1 $: c 4 @>(R1) , R6		\ same name?
	jeq 3 $f				\ matched
	mov *R1 , R1			\ next
	jne 1 $b

0 $: seto $8350 #			\ fake error
	jmp 4 $f
	
3 $: mov 2 @>(R1) , R11		\ addr
	mov R12 , $83D0 @>		\ save CRU addr
	bl	*R11				\ call routine
	jmp 4 $f				\ nop for error

4 $:
	li R12 , $1000 #
	sbo 0 #					\ turn device off
	lwpi $8300 #

	movb $8350 @> , TOS
	srl TOS , 8 #			\ error code
\	-dbg
	NEXT
end-code

$8340 constant $i/o

\	Read (r/w <> 0) or write (r/w == 0) a sector
\	from a file.

: (sec)		( drv vname #secs sec# vaddr r/w -- ior )
\	." [sec" .s 
	>r
	$i/o !		\ param block: @0 = buffer addr
	$i/o 2+ ! 	\ param block: @2 = sector #
	dup $834D c!	\ read # sectors
	r> swap >r
	if $14 else $15 then	( R: #sec )
	$i/o swap
	(dsrop)
	?dup 0= if 
		\ did it really read/write all the sectors?
		$834D c@ r> <> if 1 else 0 then
	else
		rdrop
	then
\	.s ." ]"
;

\	Read (r/w <> 0) or write (r/w == 0) DSR info for a file.
\	Always uses $i/o as a buffer.

: (hdr)		( drv vname r/w -- ior )
\	." [hdr" .s 
	0 $834D c!		\ header action
	$i/o swap		\ drv vname $i/o r/w
	if $14 else $15 then
	(dsrop)
	?dup 0= if 
		\ did it really read/write the header?
		$8350 c@
	then
\	.s ." ]"
;

\	Get internal DSR idea about filename
\	we always have enough spaces in fname

: b>fn		( pab -- drv vname )

[ 0 [if] ]
	\ step through filename
	\ and find last period; after this
	\ is the filename, before it is the drive
	\ (unless jerky used a dsk.name.xxxx thing)
	p>fn dup vc@ over + do
		i vc@ [char] . = if
			i 1- vc@ [char] 0 -
			i 1+ 
			unloop exit
		then
	-1 +loop

[ [else] ]

	\ assume DSK?.NAME for now
	p>fn 5 + dup 
	1- vc@ [char] 0 - 
	swap 1+

[ [then] ]
;


\	Write file size into to $i/o header
: s+o>hdr		( sec offs -- )
\ ." >out" .s
	dup $i/o 6 + c!			\ eof
	if 1+ then 
	$i/o 2+ !	\ round # sectors 
;

\	Get sectors/offset from $i/o
: hdr>s+o		( -- sec offs )
	$i/o 6 + c@				\ eof
	$i/o 2+ @				\ secsused
	over if 1- then			\ # secs, not counting half-filled one
	swap 
\	.s ." <in"
;

\	Write DSR info for binary file
: b>wrhdr		( pab -- ior )
	>r

	\ setup info block
	r@ b>s+o@sz 	cr ." hdr< " 2dup . . ." >"
	s+o>hdr

	1 $i/o 4 + c!			\ flags (program)
	0 $i/o 7 + c!			\ reclen
	0 $i/o 8 + !			\ numrecs
	
	r@ b>fn
	0 (hdr)
	rdrop
;


\	Read DSR info for binary file
: b>rdhdr		( pab -- ior )
	>r

	r@ b>fn
	1 (hdr)
	dup 0= if
		\ setup info block
		hdr>s+o
		r@ b>s+o!sz 
	then

	rdrop
;


\	Update file size from position
: b>updsz		( pab -- ior )
	>r

	r@ b>eof?
	if
		r@ b>s+o@ r@ b>s+o!sz		\ update size to position
		r@ b>wrhdr		
	else
		0
	then

	rdrop
;

\	Write sector
: b>wrsec		( pab -- ior )
	>r
	r@ b>fn
	1			( #sec )
	r@ b>s+o@ drop
	r@ p>buf
	0 (sec)
	rdrop
;

\	Read sector
: b>rdsec		( pab -- ior )
\	." [rdsec" .s
	>r
	r@ b>fn
	1			( #sec )
	r@ b>s+o@ drop
	r@ p>buf
	1 (sec)
	rdrop
\	.s ." ]rdsec"
;

: b>flush?		( pab -- ior )
	\ dirty?
\ ." [flush]"
	>r
	r@ p>bin?						\ binary file?
	r@ p>drt?			 			\ dirty sector?
	and if
		r@ b>wrhdr					\ write header
		?dup 0= if 					( ior )
			r@ b>wrsec				\ write sector
			r@ p>drt0 				\ clear dirty flag
		then
	else
		0
	then
	rdrop
;

\	Set position, write dirty sector and 
\	retrieve containing sector
\	(don't use unless you want disk activity)
\
: b>seek		( dpos pab -- ior )
\ ." b>seek: " .s cr
	>r			\ save pab on rstack

	\ get new and old sectors
	>s+o swap
	r@ b>s+o@ drop		( newpos newsec oldsec )

	\ only work if they are different
	over <> if			( newpos newsec )
	   	\ flush
		r@ b>flush?
		?dup if nip nip rdrop rdrop exit then

		\ set new position
		swap 2dup r@ b>s+o! drop		( newsec )

		\ see if new sector is in the file
		r@ b>s+o@sz if 1+ then			( newsec #secs )
		<= if
			r@ b>rdsec					\ read sector ( ior )
		else
			0
		then
	else
		swap r@ b>s+o!
		0
	then
	rdrop
;

\	Seek ahead
\
: b>seek+				( n pab -- ior )
	>r
	r@ b>pos@ d+
	r> b>seek
;

\ figure how much space is left in the sector
\
: b>spc				( ur pab -- ur secwrt )
	b>s+o@ nip		( ur secoffs )
	$100 over - 	( ur secoffs secmax )
	2 pick			( ur secoffs secmax ur )
	min				( ur secoffs secwrt )
;


\	Read memory from file
\
: b>read		( caddr ur pab -- ur-not ior )
	>r

	begin
		dup		( caddr' ur' )
		r@ b>eof? 0= and
	while

		\ Try to read a sector at a time
		r@ b>spc

	   	swap r@ p>buf +	( caddr ur secwrt vptr )
		3 pick rot		( caddr ur vptr cptr secwrt )
		dup >r			\ save # bytes written
		vcmove			( caddr ur )

		r@ /string		( caddr' ur' )

		r> s>d r@ b>seek+	( caddr ur ior )
		?dup if
			rdrop rot drop exit
		then

	\	.s cr
	repeat

	nip 0
	rdrop
;

\	Write memory to file
\
: b>wrt		( caddr ur pab -- ur-not ior )
	>r

	begin
		dup		( caddr' ur' )
	while

		\ Mark dirty
		r@ p>drt!

		\ Try to write a whole sector at a time

		r@ b>spc
		
	   	swap r@ p>buf +	( caddr ur secwrt vptr )
		3 pick rot		( caddr ur vptr cptr secwrt )
		>r swap r@		\ save # bytes written
		cvmove			( caddr ur )

		r@ /string		( caddr' ur' )

		r> s>d r@ b>seek+	( caddr ur ior )
		?dup if
			rdrop rot drop exit
		then
		r@ b>updsz		\ update file size
		?dup if
			rdrop rot drop exit
		then

\		.s cr
	repeat

	nip 0
	rdrop
;

: rtest
s" dsk1.forth0" r/o var $50 reclen open-file 0= if
include-file
then
;

\	\\\\\\\\\\\\\\\\\\\

\	reclen is needed to get a record buffer;
\	it should be $100 for byte files
: newpab		( caddr u reclen -- vaddr )
	\ use 10 extra chars for fname
	\ to store spaces at end of fname for
	\ internal DSR routines
	over over &10 +

	\ take space from vram
	&10 + + negate pabtos +!

	pabtos @ >r

	\ clear pab
	r@ &10 0 vfill

	\ set record length
	r@ p>len vc!

	\ copy filename
	dup r@ p>fn vc! 	  	\ length
	r@ p>fn 1+ swap cvmove 

	\ make 10 spaces after fname
	r@ p>fn vc@ r@ &10 + +
	&10 bl vfill

	\ set buffer addr
	r@ p>buf r@ p>ad v!

	r> 
;

: frepab		( vaddr -- )
	\ clear opcode to indicate free
	$ff swap vc!			

	\ return memory if we can
	begin
		pabtos @ 
		dup vc@ $ff =	( pabaddr closed? )
	while
		\ get record length... 256 bytes for byte file
		dup p>bin? if 
			$100
		else
			dup p>len vc@
		then
 
		\ pt to buffer
		swap p>buf
		+

		pabtos !	( newaddr -- )
	repeat
	drop	( pabaddr -- )
;


\
\	fam:  	high byte is record length
\			low byte is pab flags
\

0 constant R/W
2 constant W/O
4 constant R/O
6 constant &rw	\ mask to determine which mode is set

\	Note:  we use TI-FORMAT == $80 to conform with
\	ANS in which "R/O" by itself is a byte file;
\	but internally we use $80 to indicate a byte file.
\
: TI-FORMAT $80 OR ;
: VAR $10 OR TI-FORMAT ;
: FIX TI-FORMAT ;
: INT $8 OR TI-FORMAT  ;
: DIS TI-FORMAT ;
: RELATIVE $1 OR TI-FORMAT ;
: SEQUENTIAL TI-FORMAT ;
: RECLEN 8 lshift OR TI-FORMAT ;
: BIN TI-FORMAT INVERT AND ;

: OPEN-FILE		( caddr u fam -- fileid ior )
	b<fmt xor
	dup 
	8 rshift $ff and 
	?dup 0= if				
		b<fmt or >r			\ set bytefile flag
		$100 newpab
		r> swap >r 
		r@ p>fl vc!
		0.0 r@ b>pos!		\ seek to 0
		r@ p>fl vc@ 
		&rw and W/O <> if		\ readable?
			r@ b>rdhdr 			\ get DSR info
			?dup 0= if
				r@ b>rdsec		\ read first sector if not write-only
			then
			dup if r@ frepab then
		else					\ open or create
			r@ b>rdhdr			\ read header ( ior )
			if
				r@ b>wrhdr		\ write blank header
			else
				0
			then
		then
		r> swap				( pab ior )
	else 					( caddr u fam' reclen ) 
		swap >r newpab r>	( pab fam' )
	   	over p>fl vc!		\ set flags 
		dup 8 dsrlnk		\ opcode is OPEN via newpab
		dup if over frepab then
	then
;

: CLOSE-FILE	( fileid -- ior )
	>r
	r@ p>bin? if
		r@ b>flush?
	else
		$1 r@ vc!
		r@ 8 dsrlnk
	then
	r> frepab
;

\	Adjust amount read for record files
\
: p>rdadj	( caddr wanted real -- ior )
	>r
	swap over	( caddr real wanted real )
	>= if 
		r@ p>ad v@		( caddr real vaddr )
		rot rot dup >r vcmove r>
		0
	else
		drop 3 				\ "illegal" to want less than you got ;)
	then
	rdrop
;

\	Read from the file:  ur==0, ior==0 means EOF

: READ-FILE		( caddr ur fileid -- ur ior )
	>r
	r@ p>bin? if
		r@ p>fl vc@ &rw AND W/O = if
			2drop 0 3		\ can't read from write-only file
		else
			r@ 
			over >r 		\ save original ur
			b>read
			swap r> swap -	\ calc bytes not written
			swap
		then
	else
		$2 r@ vc!				\ READ op
		r@ 8 dsrlnk 
		?dup 0= if
			r@ p>fl vc@ $10 and if
				r@ p>cnt vc@ 
			else
				r@ p>len vc@
			then
			r@ p>rdadj
		else
			nip nip
			dup 6 = if
				drop 0 0	\ eof code
			else
				0 swap 
			then
		then
	then
	rdrop
;

: WRITE-FILE		( caddr u fileid -- ior )
	>r
	r@ p>bin? if
		r@ p>fl vc@ &rw AND R/O = if
			2drop 0 3		\ can't write to read-only file
		else
			r@ 
			over >r 		\ save original ur
			b>wrt
			swap r> swap -	\ calc bytes not written
			swap
		then
	else
		$3 r@ vc!				\ WRITE op
		r@ p>ad v@ swap dup >r cvmove r>
		r@ p>cnt vc!
		r@ 8 dsrlnk
	then
	rdrop
;

\	Return codes:
\	u2<=u1, flag=true, ior=0: got a line before u1 chars read
\	u1==u2, flag=true, ior=0: eol not reached
\	u2=0, flag=false, ior=0: at EOF
\	u2?, flag=?, ior<>0: i/o error
\
\	file-position points to next available char
\
: READ-LINE			( caddr u1 fileid -- u2 flag ior )
	dup p>bin? if
		rot	rot	( fileid caddr u1 ) 
		dup >r
		0 do		( fileid caddr )
			\ get a char at a time
			2dup 1 rot read-file	( fileid caddr ur ior )
			?dup if 
\				." error!"
				unloop rdrop exit 			\ failed
			then
			if						( fileid caddr ur )
				dup c@ dup &13 = swap &10 = or			\ newline?
				if
					drop drop i true 0 
					unloop rdrop 
					exit		\ got line
				then
				1+
			else
				\ end of file
\				." eof"
				drop drop i false 0 unloop rdrop exit
			then
		loop
		r> true 0 				\ got line by exhaustion
	else
		READ-FILE
	
		?dup if
			0 swap				\ other error
		else
			?dup if 
				true 0			\ success
			else
				0 0 0			\ eof
			then
		then then
	then
;

: INCLUDE-FILE		( i*x fileid -- j*x )
	<input
	loadfile !
	1 loadline !
	0 blk !
	begin refill while interpret repeat
	\ $8370 ?
	loadfile @ close-file drop
	input>
;

: INCLUDED ( addr c -- )
	r/o open-file ?dup if ." not found: " . cr 
	else
		dup >r include-file
		r> close-file
	then
;

: readfile
	s" dsk1.forth0" R/O $50 RECLEN VAR OPEN-FILE
	?dup 0= IF
		begin
			dup $c000 &80 rot read-file
			?dup 0= if
				?dup if
					$c000 swap type cr
				else
					." eof" close-file drop exit
				then
			else
				." error:" . drop
				close-file exit
			then
		again
	ELSE
		." could not open: " .
	THEN
;

: writefile
	s" dsk1.forth0" R/W $50 RECLEN VAR open-file
	?dup 0= if
		dup s" : test1 2 3 ; "  rot write-file drop
		dup s" : test2 key * test1 * * . ;  " rot write-file drop
		dup s" : test3 10 0 do i test2 loop ; " rot write-file drop
		dup s" cr [char] > emit test3 " rot write-file drop
		close-file
	else
		." could not open: " .
	then
;


: readall
	s" dsk1.dum" r/o open-file 0= if
	>r
	begin
		here 7 r@ read-file	( -- ur ior )
		0= over and
	while
		here swap type
	repeat
	rdrop
	else
	." could not open" 
	then
;

: writeall
	s" dsk1.dum" w/o open-file 0= if
		10 0 do
			dup s" hi there, guacamole hater! " rot write-file . .
		loop
		close-file
		drop
	else
	." could not create"
	then
;

: rdfile
s" dsk1.file" r/o open-file 0= if
	include-file
then
;

: rdfile2
s" dsk1.forth0" r/o var $50 reclen open-file 0= if
	include-file
then
;
