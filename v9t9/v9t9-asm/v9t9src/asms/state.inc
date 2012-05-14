; **********************************************
; STATE.INC TI Emulator! state-saving routines
; **********************************************
; by Edward Swartz,  4/11/1995
; **********************************************


	comment	^

	include strucs.inc
	include	memory.inc


	.data


statename db	'state.99',0


sscode	db	'SS60'			; state-save file header

statehand dw	0			; handle for state-save file


	.code


;	Save the emulator's state.
;
;
;
savestate proc near
	
	pusha
	push	es

	lea	si,demopath
	lea	di,filename
	mov	cx,64
	call	concatasciiz
	lea	si,statename
	mov	cx,64
	call	concatasciiz
	mov	al,0
	mov	[di],al

	mov	ah,3ch			; else create
	mov	cx,0
	lea	dx,filename
	int	21h
	jnc	ssstart		; can't?
	jmp	ssout			; whoops

ssstart:
	mov	bx,ax
	mov	statehand,ax		; file is open!

	
	mov	ah,40h
	mov	cx,4
	lea	dx,sscode
	int	21h			; write TIEM header
	jnc	ss00

     	jmp	sserr

ss00:
	call	support_save		; TIEMUL.CNF!

	call	tiemul_save		; CPU state, RAM, etc.
	jc	sserr
	call	video_save		; VREGS, etc.
	jc	sserr
	call	special_save
	jc	sserr
	call	int_save
	jc	sserr
	call	hardware_save
	jc	sserr
	call	files_save
	jc	sserr
	call	sound_save
	jc	sserr
	call	keyboard_save
	jc	sserr
	call	debug_save
	jc	sserr
	call	speech_save
	jc	sserr
	call	record_save
	jnc	ssgood

sserr:
	mov	bx,statehand
	mov	ah,3eh
	int	21h			; close file

	lea	dx,filename
	mov	ah,41h
	int	21h			; delete

	stc
	jmp	ssout

ssgood:
	mov	bx,statehand
	mov	ah,3eh
	int	21h			; close file

	clc

ssout:
	pop	es
	popa
	ret
	endp



	.data

sscodever dd	0

givenstate db	64 dup (0)

err_nostate db	'The state-save file % could not be located.',0
err_badstate db	'The file % is not a TI Emulator! v6.0 state-save file.',0

	.code

;	Restore the emulator's state.
;
;
;
restorestate proc near
	
	pusha
	push	es

	lea	si,givenstate
	lea	di,filename
rsfindslash:
	mov	al,[si]
	or	al,al
	jz	rsnoslash
	cmp	al,'\'
	je	rsfoundslash
	inc	si
	jmp	rsfindslash

rsnoslash:
	lea	si,demopath
	mov	cx,64
	call	concatasciiz

rsfoundslash:
	lea	si,givenstate
	mov	cx,64
	call	concatasciiz
	xor	al,al
	mov	[di],al

	lea	dx,filename
	mov	ah,3dh
	mov	al,0
	int	21h
	lea	dx,err_nostate
	jc	rsdout

	mov	statehand,ax

	mov	bx,ax			; get TIEM header 
	mov	cx,4
	lea	dx,sscodever
	mov	ah,3fh
	int	21h

	lea	dx,err_badstate		; and verify
	mov	ax,word ptr sscodever
	cmp	ax,word ptr sscode
	jne	rsdout
	mov	ax,word ptr sscodever+2
	cmp	ax,word ptr sscode+2
	je	rsreadit

rsdout:
	lea	si,filename
	jmp	rserr


rsreadit:
	lea	dx,err_badstate

	call	support_read		; TIEMUL.CNF!

	call	tiemul_read		; CPU state, RAM, etc.
	jc	rserr
	call	video_read		; VREGS, etc.
	jc	rserr
	call	special_read
	jc	rserr
	call	int_read
	jc	rserr
	call	hardware_read
	jc	rserr
	call	files_read
	jc	rserr
	call	sound_read
	jc	rserr
	call	keyboard_read
	jc	rserr
	call	debug_read
	jc	rserr
	call	speech_read
	jc	rserr
	call	record_read
	jc	rserr
	
	
rsgood:
	mov	ah,3eh
	mov	bx,statehand
	int	21h			; close file

	clc
	jmp	rsout

rserr:
	mov	ah,3eh
	mov	bx,statehand
	int	21h			; close file

	call	setuperror		; error message

	stc

rsout:
	pop	es
	popa
	ret
	endp

	^
