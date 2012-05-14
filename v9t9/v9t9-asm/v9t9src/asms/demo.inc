; **************************************************
; DEMO.INC include file for demo recording/execution
; **************************************************
; by Edward Swartz  1995
; **************************************************

	include	demoequs.inc


	.data

;
;	Don't change the order of these
;


;	This structure holds information about a buffer of
;	data being collected during demo recording and playback
;	(obviously used to avoid disk thrashing).
;

buffstr	struc
	bufseg	dw	?
	bufpos	dw	?
	bufmax	dw	?
	buflen	dw	?		; for reading only
	ends

;	The below variables are in the form described 
;	by the above "buffstr", only they're not declared
;	as a structure to allow easier naming.  (copout)

fbuffs	dw	?
fbpos	dw	?
fbmax	dw	16384
fblen	dw	?

vbuffs	dw	?			; video buffer segment
vbpos	dw	?			; # bytes currently in buffer
vbmax	dw	8192			; maximum # bytes in buffer
vblen	dw	?			; length of read buffer

sbuffs	dw	?   			; sound
sbpos	dw	?
sbmax	dw	1024
sblen	dw	?

spbuffs	dw	?			; speech
spbpos	dw	?
spbmax	dw	512
spblen	dw	?

givendemo db	80 dup (0)

demopath db	64 dup (0)

wantdemo	db	0		; do we want demoing?

	even


	.code



;	MAKEFULLNAME
;
;	Take a filename, a path, and a destination, and put all the
;	parts together to create a correct filepath.
;
;	Inputs:	BX = path
;		SI = given filename
;		DI = output
;
;	Outputs:DI = full filename, using BX if SI is not absolute.
;

makefullname proc near
	push	ax
	push	bx
	push	cx
	push	si
	push	di

	mov	al,[si]	 		; get first two characters
	mov	ah,[si+1]		; from given filename
	or	ah,ah			; one-char filename?
	jz	mfnrel			; has to be relative
	cmp	ah,':'			; colon (x:)?
	je	mfnabs			; has to be absolute
	cmp	al,'\'			; leading backslash?
	je	mfnabs			; must be absolute
	cmp	al,'.'			; leading period (assume "." or "..")
	je	mfnabs			; most likely absolute
mfnrel:
	xchg	si,bx			; make SI point to path
	mov	cx,64
	call	concatasciiz		; stuff it into the filename
	xchg	si,bx			; restore SI to filename
mfnabs:
	mov	cx,64
	call	concatasciiz		; stuff filename into filename

	xor	al,al
	mov	[di],al			; end the string

	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	DEMOINIT --	Get memory for buffers
;
;	Inputs:		None
;	Outputs:	CY=1 if can't get memory
;

	.data

dmi_err	db	'Not enough memory for demo buffers (26k).',0dh,0ah,0

	.code
demoinit proc	near
	push	ax
	push	bx
	push	es

	mov	ah,48h
	mov	bx,vbmax
	shr	bx,4
	int	21h
	jnc	dmi00
	jmp	dmierr
dmi00:
	mov	vbuffs,ax
	mov	vbpos,0

	mov	ah,48h
	mov	bx,sbmax
	shr	bx,4
	int	21h
	jnc	dmi01
	jmp	dmierr1
dmi01:
	mov	sbuffs,ax
	mov	sbpos,0


	mov	ah,48h
	mov	bx,fbmax
	shr	bx,4
	int	21h
	jnc	dmi02
      	jmp	dmierr2
dmi02:
	mov	fbuffs,ax
	mov	fbpos,0

	mov	ah,48h
	mov	bx,spbmax
	shr	bx,4
	int	21h
	jnc	dmi03
	jmp	dmierr3
dmi03:
	mov	spbuffs,ax
	mov	spbpos,0


	clc
	jmp	dmiout
dmierr3:
	mov	es,fbuffs
	mov	ah,49h
	int	21h
	mov	fbuffs,0
dmierr2:
	mov	es,sbuffs
	mov	ah,49h
	int	21h
	mov	sbuffs,0

dmierr1:
	mov	es,vbuffs
	mov	ah,49h
	int	21h
	mov	vbuffs,0
dmierr:
	lea	dx,dmi_err
	call	setuperror
	stc
dmiout:
	pop	es
	pop	bx
	pop	ax
	ret
	endp


;	DEMOSTART --	Start demo recording
;
;	Inputs:		none
;	Outputs:	CY=1 if can't
;

	.data

dcode	db	'V910'
demohand dw	?			; handle for demo file


	.code

demostart proc	near
	pusha
	push	es

	lea	bx,demopath
	lea	si,givendemo
	lea	di,filename
	call	makefullname

	mov	ah,3ch			; else create
	mov	cx,0
	lea	dx,filename
	int	21h
	jnc	dsfnstart		; can't?
	jmp	dmsout			; whoops

dsfnstart:
	mov	bx,ax
	mov	demohand,ax		; file is open!

	
;	The beginning of the demo file contains these thingies:
;
;	The signature, dcode.
;
;	(00=timer, 01=video, 02=sound, 03=speech)
;	Video section:  all the VREGS, all the VDP memory
;	Sound section:  all the bytes for no volume
;

	or	stateflag,demoing	; we're rocking!

	mov	ah,40h
	mov	cx,4
	lea	dx,dcode
	int	21h			; write TIEM header


	mov	bx,vaddr		; save VADDR!

	mov	dvdpst,0
	mov	dvdplen,0		; init VDP buffer
	mov	lastvwaddr,0

	lea	si,vregs
	mov	cx,8
	mov	ah,80h
dmsvregs:
	mov	al,[si]
	mov	vaddr,ax
	call	dvdpreg			; dump VREGS into buffer
	inc	ah
	inc	si
	loop	dmsvregs

	mov	vaddr,4000h		; write at 0

	mov	cx,16384
dmsvdp:
	call	dvdpdata		; dump VDP memory into buffer
	inc	vaddr
;;	mov	ax,vaddr
;;	mov	lastvwaddr,ax
	loop	dmsvdp

	mov	vaddr,bx		; restore VADDR

	mov	al,9fh
	call	dsounddata
	mov	al,0bfh
	call	dsounddata
	mov	al,0dfh
	call	dsounddata
	mov	al,0ffh
	call	dsounddata		; write SOUND stuff into buffer

	mov	ah,TERMINATINGSPEECH
	call	dspeechdata		; initialize speech

	call	demodumpall		; dump these buffers, just because


	clc
dmsout:
	pop	es
	popa
	ret
	endp


;	FILEWRITE --	Write bufferedly to a file
;
;	Inputs:		BX=handle, CX=length, DS:DX=data
;
filewrite proc	near
	push	ax
	push	bx
	push	cx
	push	si
	push	ds
	push	es

	push	ds
	pop	es
	mov	ax,@data
	mov	ds,ax

	mov	si,dx

fw00:
	mov	dx,fbpos
	add	dx,cx
	jc	fw02
	cmp	dx,fbmax
	jbe	fw01			; we won't overfill the buffer

fw02:
	mov	dx,fbmax
	sub	dx,fbpos		; DX = # bytes left

	call	fmove			; move bytes es:si,dx -> fbpos

	call	fwrite			; write buffer

	sub	cx,dx
	jz	fw03
	jmp	fw00

fw01:
	mov	dx,cx
	call	fmove	

fw03:
	pop	es
	pop	ds
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	FMOVE --	Move stuff into file buffer
;
;	Inputs:		es:si=buffer, DX=bytes

fmove	proc	near
	push	ax
	push	cx
	push	di

	mov	di,fbpos
	push	ds
	push	es

	push	es
	mov	es,fbuffs		; es:di = dest
	pop	ds			; ds:si = source

	mov	cx,dx
	shr	cx,1
	rep	movsw
	jnc	fm0
    	movsb
fm0:
	pop	es
	pop	ds

	add	fbpos,dx

	pop	di
	pop	cx
	pop	ax
	ret
	endp


;	FWRITE --	Write buffered stuff to disk
;

fwrite	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	ah,40h
	mov	bx,demohand
	mov	cx,fbpos
	push	ds
	xor	dx,dx
	mov	ds,fbuffs
	int	21h
	pop	ds

	mov	fbpos,0

	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	DEMODUMP --	Dump a buffer to the file
;
;	Inputs:		AH=buffer type, SI=buffer struct pointer
;	Outputs:	none
;
;	This function is usually called when:
;	1)  Buffers are overflowing (only if super-wacky writing
;	    is happening)
;	2)  Timer has ticked

	.data

dheader	db	?			; type
dhlen	dw	?			; length


	.code

demodump proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	dheader,ah		; type
	mov	cx,[si].bufpos
	jcxz	dmdok			; uh, like, not

	mov	dhlen,cx		; length
	mov	cx,3
	lea	dx,dheader
	mov	bx,demohand
	call	filewrite

	mov	bx,demohand
	mov	cx,[si].bufpos
	push	ds
	mov	ds,[si].bufseg
	xor	dx,dx
	call	filewrite
	pop	ds

	mov	[si].bufpos,0		; empty buffer
	jnc	dmdok			; DOS errror?

	and	stateflag,not demoing
	mov	demohand,0

dmdok:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	DWRITEBUFFER --	Write some important data to the buffer
;
;	Inputs:		SI points to buffer variables 
;			(starting with segment address)
;			AL=byte, AH=buffer type
;	Outputs:	none
;
dwritebuffer proc	near
	push	bx
	push	es

	mov	bx,[si].bufpos
	cmp	bx,[si].bufmax
	jb	dwbgo

	call	demodump		; will reset pointers

dwbgo:
	mov	bx,[si].bufpos
	mov	es,[si].bufseg
	mov	es:[bx],al
	inc	bx
	mov	[si].bufpos,bx

	pop	es
	pop	bx
	ret
	endp



;	DVDPREG	--	A VDP reg was changed
;
;	Inputs:		VADDR is current
;	Outputs:	adds stuff to buffer
;
dvdpreg proc near
	push	ax
	push	si

	call	ddumpvdp

	mov	ah,01			; 1 = video
	mov	al,byte ptr vaddr
	lea	si,vbuffs
	call	dwritebuffer

	mov	al,byte ptr vaddr+1
	call	dwritebuffer

	pop	si
	pop	ax
	ret
	endp


;	DDUMPVDP --	Dump VDP subbuffer
;
;	We will store contiguous blocks of VDP data 
;	as pointers into VDP memory, until the block is >255 
;	bytes or until the block moves somewhere else.
;
;
	.data

dvdpst	dw	?			; start addr
dvdplen	db	?			; length

	.code

ddumpvdp proc	near
	push	ax
	push	cx
	push	si
	push	di

	push	es
	mov	es,vbuffs
	mov	di,vbpos

	xor	ah,ah		      	; will this block cause our
	mov	al,dvdplen		; VDP buffer to overflow?
	add	ax,4
	add	ax,di
	cmp	ax,vbmax
	jb	ddvnoov			; no

	mov	ah,01			; yes
	lea	si,vbuffs
	call	demodump

	mov	di,vbpos		; dumped it, start over

ddvnoov:
	cmp	dvdplen,0
	je	ddvno

	mov	ax,dvdpst
	and	ax,3fffh
	mov	es:[di],ax
	mov	al,dvdplen
	mov	es:[di+2],al
	add	di,3

	mov	si,dvdpst
	and	si,3fffh
	xor	ch,ch
	mov	cl,dvdplen
	shr	cx,1
	rep	movsw
	jnc	ddvno
	movsb
ddvno:
	pop	es

	mov	vbpos,di
	mov	ax,vaddr
	and	ax,3fffh
	mov	dvdpst,ax
;;	mov	lastvwaddr,ax
	mov	dvdplen,0

	pop	di
	pop	si
	pop	cx
	pop	ax
	ret
	endp


;	DVDPDATA --	VDP data has been written
;
;	Inputs:		VADDR is current, [VADDR]=data
;	Outputs:	adjusts buffer pointers
;
	.data


lastvwaddr dw	0

	.code

dvdpdata proc	near
	push	ax
	push	bx
	push	si

	mov	al,dvdplen		; get len of contiguous block
	xor	ah,ah			; so far
	add	ax,dvdpst		; add to start addr of block
	mov	bx,vaddr		; compare with current vaddr
	and	bx,3fffh
	and	ax,3fffh
	cmp	ax,bx
	jne	dvddump			; addr changed!

	cmp	dvdplen,255
	jne	dvdgo	 		; not full yet


dvddump:

dvdnobuf:
	call	ddumpvdp		; then, dump the contig block

dvdgo:
	inc	dvdplen

	pop	si
	pop	bx
	pop	ax
	ret
	endp




;	DSOUNDDATA --	SOUND data was written
;
;	Inputs:		AL = byte
;	Outputs:	buffer written

dsounddata proc	near
	push	ax
	push	si
		
	mov	ah,02			; 02=sound
	lea	si,sbuffs
	call	dwritebuffer

	pop	si
	pop	ax
	ret
	endp


;	DSPEECHDATA --	SPEECH data was written
;
;	Inputs:		AL = byte
;	Outputs:	buffer written

dspeechdata proc near
	push	ax
	push	si
    
	cmp	ah,ADDINGBYTE
	je	dsdother

	call	demodumpspeech

dsdother:
	push	ax
	mov	al,ah
	mov	ah,3
	lea	si,spbuffs
	call	dwritebuffer		; push the AH (type) byte
	pop	ax

	mov	ah,03			
	lea	si,spbuffs
	call	dwritebuffer		; push the data byte

dsdout:
	pop	si
	pop	ax
	ret
	endp


;	DTIMERINT --	TIMER int occurred, dump everything
;

	.data

dtimerbyte db	0

	.code

dtimerint proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	call	demodumpall

	mov	bx,demohand
	mov	cx,1
	lea	dx,dtimerbyte
	call	filewrite

	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	DEMODUMPALL -- 	Dump all the buffers
;
;
demodumpall proc near
	push	ax
	push	si

	call	ddumpvdp		; empty VDP stuff

	mov	ah,1			; VDP
	lea	si,vbuffs
	call	demodump

	mov	ah,2			; sound
	lea	si,sbuffs
	call	demodump

	pop	si
	pop	ax
	ret
	endp


demodumpspeech proc near
	push	ax
	push	si
	mov	ah,3
	lea	si,spbuffs
	call	demodump
	pop	si
	pop	ax
	ret
	endp



;	DEMOSTOP --	Stop the demo
;
;
demostop proc	near
	call	demodumpall
	call	demodumpspeech
	call	democlose
	and	stateflag,not demoing

	ret
	endp


;	DEMOCLOSE --	Close the demo file
;
democlose proc	near
	push	ax
	push	bx

	test	features,FE_emulating		; EXECUTING a demo?
	jz	dmcnowrite

	call	fwrite

dmcnowrite:

	mov	ah,3eh
	mov	bx,demohand
	or	bx,bx
	jz	dmcnot
	int	21h
	mov	demohand,0
dmcnot:

	pop	bx
	pop	ax
	ret
	endp





;*************************************************************************
;
;	RUNDEMO --	Run the demo file
;
;	Inputs:		nothing
;	Outputs:	CY=1 if found a command-line param and executed
;			the demo, or if there was an error
;			and DX=message
;			ELSE
;			CY=0 if no demo executed
;
;	DEMOINIT better have already been called.
;
;*************************************************************************

	.data

dcodever	db	4 dup (?)

err_nodemo db	'Could not find demo file %.',0dh,0ah,'$'
err_badfile db	'The file % is not a V9t9 v6.0 demo file.',0dh,0ah,'$'
err_badtype db	'Illegal block type detected in demo file.',0dh,0ah,'$'
err_demodone db	'$'
	db	0dh,'Demo completed.',0dh,0ah,'$'


	.code

rundemo proc	near
	pusha
	push	es

	lea	di,filename
	lea	si,givendemo
	mov	cx,64
	call	concatasciiz
	xor	al,al
	mov	[di],al


	lea	dx,filename
	mov	ah,3dh
	mov	al,0
	int	21h
	lea	dx,err_nodemo
	jc	rndout

	mov	demohand,ax

	mov	bx,ax			; get TIEM header 
	mov	cx,4
	lea	dx,dcodever
	mov	ah,3fh
	int	21h

	lea	dx,err_badfile		; and verify
	mov	ax,word ptr dcodever
	cmp	ax,word ptr dcode
	jne	rndcout
	mov	ax,word ptr dcodever+2
	cmp	ax,word ptr dcode+2
	jne	rndcout

	pop	es
	popa
	jmp	execdemo

rndcout:
	call	democlose

rndout:
	lea	si,filename
	call	setuperror

	pop	es
	popa
	ret
	endp


;	EXECDEMO --	Execute the demo file
;
;	Inputs:		DEMOHAND is set up
;	Outputs:	none
;
	.data

edtype	db	?
	even	
edleng	dw	?

	.code

execdemo proc	near
	mov	ax,0
	call	setcurrentspeed

exdloop:
	mov	bx,demohand		; read type field
	mov	cx,1
	lea	dx,edtype
	mov	ah,3fh
	int	21h	      		
	jnc	exdcont
	jmp	exdout
exdcont:
	cmp	ax,cx
	je	exdcont2



	mov	cx,60*3
exdwaitasec0:
	and	stateflag,not titick
exdwaitasec1:
	test	stateflag,titick+ctrlbreakpressed
	jz	exdwaitasec1
	test	stateflag,ctrlbreakpressed
	jnz	exdinterr
	loop	exdwaitasec0

exdinterr:
	or	stateflag,happymessage
	lea	dx,err_demodone
	jmp	exdout

exdcont2:
	mov	al,edtype
	cmp	al,0			; timer tick marker?
	jne	exdcont3

	and	stateflag,not titick
exdtimeout:
	test	stateflag,checkable
	jz	exdtimeout
	mov	bx,stateflag		; save it
	call	checksoftstate
	and	stateflag,ctrlbreakpressed

					; all others will screw us over
	test	bx,titick
	jnz	exdloop
	jmp	exdtimeout

exdcont3:
	push	ax
	mov	bx,demohand		; read length field
	mov	cx,2
	lea	dx,edleng
	mov	ah,3fh
	int	21h
	jc	exdbadout
	cmp	ax,cx
	pop	ax
	jne	exdbadout		
	
	cmp	al,1			; video marker
	jne	exdcont4

	call	execvideo
	jmp	exdloop

exdcont4:
	cmp	al,2			; sound marker
	jne	exdcont5

	call	execsound
	jmp	exdloop

exdcont5:
	cmp	al,3			; speech marker
	jne	exdcont6

	call	execspeech
	jmp	exdloop

exdcont6:

exdbadout:
	lea	dx,err_badtype
exdout:
	call	setuperror
	ret
	endp


;	EXECREAD --	Read a buffer from the file
;
;	Inputs:		AH=buffer type, SI=buffer struct pointer
;	Outputs:	none
;

execread proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	test	features,FE_emulating
	jnz	exrok			; be sure it's still valid

	mov	ax,0
	mov	bx,demohand
	mov	cx,edleng
	jcxz	erskip
	mov	ah,3fh
	push	ds
	mov	ds,[si].bufseg
	xor	dx,dx
	int	21h			; read data
	pop	ds
erskip:
	mov	[si].bufpos,0		; empty buffer
	mov	[si].buflen,ax
	jnc	exrok

	call	democlose
	stc
exrok:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp





;	EXECVIDEO --	Execute a video block
;
;	Inputs:		EDLENG is set up
;

execvideo proc	near
	push	ax
	push	bx
	push	cx
	push	si
	push	di
	push	es

	mov	ah,1
	lea	si,vbuffs
	call	execread
	jc	exvout

	mov	es,vbuffs
	xor	di,di
exvloop:
	cmp	di,vblen
	jae	exvout
	mov	ax,es:[di]		; vaddr
	add	di,2
	mov	vaddr,ax		; set VADDR
	mov	bx,ax
	and	bx,3fffh
	test	ax,8000h
	jz	exvnotvreg
	call	handlevdpreg		; if it's a register...
	jmp	exvloop
exvnotvreg:
	mov	al,es:[di]		; supposed-to-be length
	inc	di
	or	al,al
	jz	exvloop			; no data 
	mov	cl,al
	xor	ch,ch			; make length

exvdloop:
	mov	vaddr,bx
	mov	al,es:[di]
	call	handlevdp		; write it!
	inc	di
	inc	bx
	and	bx,3fffh
	loop	exvdloop
	jmp	exvloop

exvout:
	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	EXECSOUND --	Execute sound block
;
;	Inputs:		EDLENG is set up
;

execsound proc	near
	push	ax
	push	si
	push	es

	mov	ah,2
	lea	si,sbuffs
	call	execread
	jc	exsout

	mov	es,sbuffs
	xor	di,di
	mov	cx,sblen
	jcxz	exsout
exsloop:
	mov	al,es:[di]
	call	handlesound
	inc	di
	loop	exsloop

exsout:
	pop	es
	pop	si
	pop	ax
	ret
	endp


;	EXECSPEECH --	Execute speech block
;
;	Inputs:		EDLENG is set up
;

execspeech proc	near
	push	ax
	push	si
	push	es

	mov	ah,3
	lea	si,spbuffs
	call	execread
	jc	exspout

	mov	es,spbuffs
	xor	di,di
	mov	cx,spblen

exsploop:
	jcxz	exspout

	mov	al,es:[di]
	inc	di
	dec	cx
	cmp	al,SPEECHSTARTING
	jne	exsp0

	call	startspeech

	inc	di				; 'data' is useless
	dec	cx
	jmp	exsploop

exsp0:
	cmp	al,SPEECHSTOPPING
	jne	exsp1

	call	stopspeech

	inc	di
	dec	cx				; no 'data'
	jmp	exsploop

exsp1:
	cmp	al,ADDINGBYTE
	jne	exsp2

	mov	al,es:[di]
	inc	di				; get byte
	dec	cx

	call	addspeech
	jmp	exsploop

exsp2:
	cmp	al,TERMINATINGSPEECH
	jne	exsp3

	call	terminatespeech

	inc	di				; no 'data'
	dec	cx
	jmp	exsploop
	
exsp3:
	jmp	exsploop

exspout:
	pop	es
	pop	si
	pop	ax
	ret
	endp

