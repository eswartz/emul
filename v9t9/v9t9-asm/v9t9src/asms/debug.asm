; **********************************
; DEBUG.ASM  V9t9 debugger
; **********************************
; by Edward Swartz  5/26/1993
; **********************************


;	DEBUG will be activated by pressing Alt-SysRq.
;
;	Its purpose is to trace code execution.
;

_DEBUG_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	keyboard.h
	include	support.h
	include	debug.h

	include	memory.inc
	include	registers.inc


	.data

;   176 °   177 ±   178 ²   179 ³   180 ´   181 µ   182 ¶   183 ·

;   184 ¸   185 ¹   186 º   187 »   188 ¼   189 ½   190 ¾   191 ¿

;   192 À   193 Á   194 Â   195 Ã   196 Ä   197 Å   198 Æ   199 Ç

;   200 È   201 É   202 Ê   203 Ë   204 Ì   205 Í   206 Î   207 Ï

;   208 Ð   209 Ñ   210 Ò   211 Ó   212 Ô   213 Õ   214 Ö   215 ×

;   216 Ø   217 Ù   218 Ú   219 Û   220 Ü   221 Ý   222 Þ   223 ß

;           	 0         1         2         3         4         5         6         7
;           	 01234567890123456789012345678901234567890123456789012345678901234567890123456789
template db 	'ÚÄ.DISASSEMBLY.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÂÄ.SOURCE.Ä.&.Ä.DESTINATION.ÄÄÄÄÄ¿'  ; 0
	db 	'³                                                   ³                          ³'  ; 1
	db 	'³                                                   ³                          ³'  ; 2
	db 	'³                                                   ³                          ³'  ; 3
	db 	'³                                                   ³                          ³'  ; 4
	db 	'³                                                   ³                          ³'  ; 5
	db 	'³                                                   ³                          ³'  ; 6 
	db 	'³                                                   ³                          ³'  ; 7
	db 	'ÃÄ.REGISTERS.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÂÄ.V9t9.ÄÄÄÄÄÄÂÄ.STATE.ÄÄÄÂÄ.COMMANDS.ÄÄÄÄÄÄÄÄÄÄ´'  ; 8
	db 	'³ R0=      R1=      R2=      R3=     ³ v9t9v9t9v ³ PC=     ³ .Enter.=single step ³'  ; 9
	db 	'³ R4=      R5=      R6=      R7=     ³ 9t9v9t9v9 ³ WP=     ³ .Space.=animate     ³'  ; 10
	db 	'³ R8=      R9=      RA=      RB=     ³ t9v9t9v9t ³ ST=     ³ .Tab.=intermittent  ³'  ; 11
	db 	'³ RC=      RD=      RE=      RF=     ³ 9v9t9v9t9 ³ LAECVPX ³ .ESC.=return        ³'  ; 12
	db 	'ÃÄ.WATCH.Ä.1.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÁÄÄÄÄÄÄÄÄÄÄÄÁÄÄÄÄÄÄÄÄÄ´ .1.=set watch 1     ³'  ; 13
	db 	'³      =                                                   ³ .2.=set watch 2     ³'  ; 14
	db 	'³      =                                                   ³ .O.=tog BASIC offset³'  ; 15
	db 	'³      =                                                   ³ .I.=intermittence   ³'  ; 16
	db 	'³      =                                                   ³ .E.=edit memory     ³'  ; 17
	db 	'ÃÄ.WATCH.Ä.2.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ´ .S.=switch screen   ³'  ; 18
	db 	'³      =                                                   ³ .P.=change PC       ³'  ; 19
	db 	'³      =                                                   ³ .W.=change WP       ³'  ; 20
	db 	'³      =                                                   ³ .U.=change STAT     ³'  ; 21
	db 	'³      =                                                   ³ .B/D.=set/del break ³'  ; 22
	db 	'ÃÄ.VDP.Ä.MEMORY.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÁÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ´'  ; 23
	db 	'³ xxxx = 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10      123456789abcdef0 ³'  ; 24
	db 	'³      =                                                                       ³'  ; 25
	db 	'³      =                                                                       ³'  ; 26
	db 	'³      =                                                                       ³'  ; 27
	db 	'ÃÄ.GROM.Ä.MEMORY.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ´'  ; 28
	db 	'³      =                                                                       ³'  ; 29
	db 	'³      =                                                                       ³'  ; 30
	db 	'³      =                                                                       ³'  ; 31
	db 	'³      =                                                                       ³'  ; 32
	db 	'ÃÄ.PROMPT.ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ´'  ; 33
	db 	'³                                                                              ³'  ; 34
	db 	'³                                                                              ³'  ; 35
	db 	'³                                                                              ³'  ; 36
	db 	'³                                                                              ³'  ; 37
	db 	'³                                                                              ³'  ; 38
	db 	'³                                                                              ³'  ; 39
	db 	'³                                                                              ³'  ; 40
	db 	'³                                                                              ³'  ; 41
	db 	'ÀÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÙ'  ; 42
templatelength	equ $-offset template

debugscreenstart	equ	0
		db	(43*80*2-templatelength) dup (?)

textseg	dw	0b800h

winlx   db 	0
winhx   db 	79
winsx	db	78
winly   db 	0
winhy   db 	42
winsy	db	43

udebugdelay	db	6
debugdelay	db	6
debugcnt	db	0

afterstring	dw	0

noegaerror	db	'*',7,'*',7,' V9t9 requires an EGA or above! ',7,'*',7,'*','$'

dm_singlestep	db	'Single-stepping through memory.',0
dm_continuous	db	'Executing continuously.  Press Alt-SysRq to return to command mode.',0
dm_intermittent	db	'Refreshing intermittently.  Press Alt-SysRq to return to command mode.',0
dm_returning	db	'Returning to nonstop execution.',0
dm_debugging	db	'In debugging command mode...',0

pr_watch1	db	'Enter watch address #1:',0
pr_watch2	db	'Enter watch address #2:',0
pr_inter	db	'Enter new intermittent update delay in 1/60sec (00-FF):',0
pr_edit		db	'Enter the address (hex) to change:',0
pr_num		db	'Enter the number of bytes (hex) you want to change (0 to abort):',0
pr_edty		db	'Press ">" or "C" for CPU, "V" for VDP, or "G" for GROM:',0
pr_ego		db	'For each byte, enter its hex value and press enter.',0

pr_edpc		db	'Enter the new program counter:',0
pr_edst		db	'Enter the new status word (hex):',0
pr_edwp		db	'Enter the new workspace register:',0

pr_break	db	'Enter breakpoint # to set (0-7):',0
pr_braddr	db	'Enter address for breakpoint:',0
pr_dbreak	db	'Enter breakpoint # to delete (0-7):',0

	even

startaddr	dw	0	  	; video mem offset for window

_pc	dw	0		; values of these registers upon entry
_wp	dw	0
_stat	dw	0

watch1	dw	8300h
watch2	dw	83e0h

op	dw	0		; current opcode

e_addr	dw	0
e_num	dw	0
	even


;	This table allows decoding of opcodes for disassembly.
;
;	It's incomplete!  I didn't add the last of my own opcodes
;	here (see STRUCS.INC)


opcodes	dw	00000h,'DA','TA',gto,0
	dw	00200h,'LI','  ',gtr,gti
	dw	00220h,'AI','  ',gtr,gti
	dw	00240h,'AN','DI',gtr,gti
	dw	00260h,'OR','I ',gtr,gti
	dw	00280h,'CI','  ',gtr,gti
	dw	002a0h,'ST','WP',gtr,0
	dw	002c0h,'ST','ST',gtr,0
	dw	002e0h,'LW','PI',gti,0
	dw	00300h,'LI','MI',gtn,0
	dw	00320h,'DA','TA',gto,0
	dw	00340h,'ID','LE',0,0
	dw	00360h,'RS','ET',0,0
	dw	00380h,'RT','WP',0,0
	dw	003a0h,'CK','ON',0,0
	dw	003c0h,'CK','OF',0,0
	dw	003e0h,'LR','EX',0,0
	dw	00400h,'BL','WP',gts,0
	dw	00440h,'B ','  ',gts,0
	dw	0045bh,'RT','  ',0,0
	dw	0045ch,'B ','  ',gts,0
	dw	00480h,'X ','  ',gts,0
	dw	004c0h,'CL','R ',gts,0
	dw	00500h,'NE','G ',gts,0
	dw	00540h,'IN','V ',gts,0
	dw	00580h,'IN','C ',gts,0
	dw	005c0h,'IN','CT',gts,0
	dw	00600h,'DE','C ',gts,0
	dw	00640h,'DE','CT',gts,0
	dw	00680h,'BL','  ',gts,0
	dw	006c0h,'SW','PB',gts,0
	dw	00700h,'SE','TO',gts,0
	dw	00740h,'AB','S ',gts,0
	dw	00780h,'??','??',gts,0
	dw	007c0h,'??','??',gts,0
	dw	00800h,'SR','A ',gtr,gtc
	dw	00900h,'SR','L ',gtr,gtc
	dw	00a00h,'SL','A ',gtr,gtc
	dw	00b00h,'SR','C ',gtr,gtc
	dw	00c00h,'DS','R!',0,0
	dw	00c80h,'KE','Y ',0,0
	dw	00ca0h,'SP','RI',0,0
	dw	00cc0h,'TR','NS',0,0
	dw	00ce0h,'IR','ET',0,0
	dw	00d00h,'??','??',0,0
	dw	01000h,'JM','P ',gtj,0
	dw	01100h,'JL','T ',gtj,0
	dw	01200h,'JL','E ',gtj,0
	dw	01300h,'JE','Q ',gtj,0
	dw	01400h,'JH','E ',gtj,0
	dw	01500h,'JG','T ',gtj,0
	dw	01600h,'JN','E ',gtj,0
	dw	01700h,'JN','C ',gtj,0
	dw	01800h,'JO','C ',gtj,0
	dw	01900h,'JN','O ',gtj,0
	dw	01a00h,'JL','  ',gtj,0
	dw	01b00h,'JH','  ',gtj,0
	dw	01c00h,'JO','P ',gtj,0
	dw	01d00h,'SB','O ',gtb,0
	dw	01e00h,'SB','Z ',gtb,0
	dw	01f00h,'TB','  ',gtb,0
	dw	02000h,'CO','C ',gts,gt3		; gt3 = get register
	dw	02400h,'CZ','C ',gts,gt3
	dw	02800h,'XO','R ',gts,gt3
	dw	02c00h,'XO','P ',gts,gt9		; gt9 = get count
	dw	03000h,'LD','CR',gts,gt9
	dw	03400h,'ST','CR',gts,gt9
	dw	03800h,'MP','Y ',gts,gt3
	dw	03c00h,'DI','V ',gts,gt3
	dw	04000h,'SZ','C ',gts,gtd
	dw	05000h,'SZ','CB',gtsb,gtdb
	dw	06000h,'S ','  ',gts,gtd
 	dw	07000h,'SB','  ',gtsb,gtdb
	dw	08000h,'C ','  ',gts,gtd
	dw	09000h,'CB','  ',gtsb,gtdb
	dw	0a000h,'A ','  ',gts,gtd
	dw	0b000h,'AB','  ',gtsb,gtdb
	dw	0c000h,'MO','V ',gts,gtd
	dw	0d000h,'MO','VB',gtsb,gtdb
	dw	0e000h,'SO','C ',gts,gtd
	dw	0f000h,'SO','CB',gtsb,gtdb
opcodeslength	equ $-opcodes
opcodesend	dw	0		


;****************************************************

disline	db	52 dup 	(?)
sdline	db	30 dup	(?)
words	dw	1

temp	db	80 dup (?)

	even

breaks	dw	16 dup (0ffffh)


	.code

ascofs		db	0

;===========================================================================
;	DEBUG:	Pre-config init.
;
;	Nothing, really.  Put in post-config just to avoid the
;	implication that it has to be executed before config.
;===========================================================================

debug_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	DEBUG:	Post-config init.
;
;	þ  Set up the debugger screen by going through "template"
;	and replacing it inline with its CGA-colorized version.
;===========================================================================

debug_postconfiginit proc near
	cld
	lea	di,template+(43*80*2)
	mov	si,offset template+templatelength
	mov     cx,43*80
	mov     dx,030fh        ; DH = current attribute 0A0fh
	mov     bp,80
drawrows:
	dec	si
	mov	al,[si]
	cmp     al,'.'
	jne     notest
	xchg    dh,dl           ; flip attribute from grey/white, v.v.
	jmp	drawrows	; don't change BP!
notest: 
	sub	di,2
	mov	[di],al
	cmp	al,128
	jb	noline
       	mov	al,05h		; 09h
	mov	[di+1],al
	jmp	skip
noline:
	mov     al,dh
	mov	[di+1],al
skip:
	loop	drawrows

	clc
	ret
	endp


;===========================================================================
;	DEBUG:	Restart.
;
;	Nothing.  Debug restart/restop happens through checkstate.
;===========================================================================

debug_restart proc near
	clc
	ret
	endp


;===========================================================================
;	DEBUG:	Restop.
;
;	Nothing.  Debug restart/restop happens through checkstate.
;===========================================================================

debug_restop proc near
	clc
	ret
	endp


;===========================================================================
;	DEBUG:	Shutdown.
;===========================================================================

debug_shutdown proc near
	clc
	ret
	endp



;===========================================================================

;       Set screen mode to use 43 lines
;
set43   proc    near
	push	ax
	push	bx
	push	cx
	push	dx
	push	bp
	push	es

	MOV	DX,03CEH
	MOV	AX,5
	OUT	DX,AX

	mov	ax,0ff08h		
	out	dx,ax	  	; select all bits for clearing

	mov     ax,0500h        ; set page 0 for debugger
	int     10h

	mov     ax,1201h        ; set 350-line operation
	mov     bl,30h
	int     10h

	mov	ax,1201h
	mov	bl,31h
	int	10h		; turn off default palette loading

	mov	ax,83h
	int	10h		; select 80 columns

	call	setdebugbg

	mov	ax,1110h
	mov	bh,8
	mov	bl,0
	mov	cx,256
	mov	dx,0
	push	es
	push	ds
	pop	es
	lea	bp,tifont
	int	10H
	pop	es

	push    es              ; set cursor emulation on
	mov     ax,040h
	mov     es,ax
	or      byte ptr es:[087h],1
	pop     es

	mov     cx,0600h
	mov     ah,1
	int     10h

	push    es              ; set cursor emulation off
	mov     ax,040h
	mov     es,ax
	and     byte ptr es:[087h],0feh  
	pop     es

	pop	es
	pop	bp
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	Set up for the normal text mode.
;
;	AL=default palette load flag

setnormaltext proc near
	push	ax
	push	bx
	
	mov	ah,12h
	mov	bl,31h
	int	10h		; set some sort of palette loading

	mov	ax,1202h	; set 400-line operation
	cmp	vga,0
	jnz	setntvga	
	mov     ax,1201h        ; set 350-line operation
setntvga:
	mov     bl,30h
	int     10h

	pop	bx
	pop	ax
	ret
	endp
	

;       Set 25-line mode
;
;	AL=default palette load flag

set25   proc    near
	push	ax

	call	setnormaltext

	mov	ax,003h		; set 80x25 mode
	int     10h

	pop	ax
	ret
	endp


;       Set the debugger screen from TI mode
;
setdebugfromti	proc    near
	push	es
	push	ax
	push	si
	push	di
	push	cx
	push	dx
	call	set43

	mov	es,textseg

	mov	di,debugscreenstart	; put saved debugger screen on screen
	lea	si,template
	mov     cx,43*80
	cld
	rep	movsw

	mov	dx,3d4h
	mov	al,0dh
	mov	ah,(debugscreenstart/2) and 255
	out	dx,ax
	mov	al,0ch
	mov	ah,debugscreenstart/2/256
	out	dx,ax


	mov	ontiscreen,0
	pop	dx
	pop	cx
	pop	di
	pop	si
	pop	ax
	pop	es
	ret     
	endp


;	Set the debugger screen from text mode
;
setdebugfromtext proc	near
	call	setpalette
	jmp	setdebugfromti
	endp


;	Save debugger screen
;

savedebugscreen	proc	near
	push	ax
	push	es
	push	di
	push	si
	push	cx

	push	ds
	mov	ax,ds
	mov	es,ax
	mov	ds,textseg

	mov	si,debugscreenstart	; save changed debugger screen
	lea	di,template
	mov	cx,160*43/2
	rep	movsw

	pop	ds

	pop	cx
	pop	si
	pop	di
	pop	es
	pop	ax
	ret
	endp


;	Set the TI screen from the debug screen
;
settifromdebug	proc	near
	call	savedebugscreen
	mov	al,1			; don't load palette
	call	setnormaltext
	call	completeupdate		; ***************************
					; move me to video
	mov	ontiscreen,1
	ret
	endp


;	Set the text mode from the debug screen
;
settextfromdebug proc	near
	call	savedebugscreen
	mov	al,0			; default palette load
	call	set25
	mov	ontiscreen,0
	ret
	endp


;==========================================================================


; 	set start address of window on screen using WINxx coordinates
;
setstartaddr	proc	near
	push	ax
	push	bx
	mov	al,160
	mul	winly
	inc	winlx			; forget space in first column
	mov	bl,winlx
	sub	bh,bh
	add	bx,bx
	add	ax,bx	
	add	ax,debugscreenstart
	mov	startaddr,ax
	mov	al,winhx
	sub	al,winlx
	mov	winsx,al
	mov	al,winhy
	sub	al,winly
	inc	al
	mov	winsy,al		; save height of window
	pop	bx
	pop	ax
	ret
	endp


;	Use coordinate in DX and return offset in DI.
;
;	DH=col, DL=row
;
getaddress	proc	near
	push	ax
	push	dx
	mov	di,startaddr
figrow:
	push	dx
	xor	dh,dh
	mov	ax,160
	mul	dx

	add	di,ax
	pop	dx
figcol:
	shr	dx,8
	add	dx,dx
	add	di,dx			; DI contains correct screen addr
	pop	dx
	pop	ax
	ret
	endp


;	display a string on the screen
;	
;	AH = attribute
;	CX = length of string
;	SI = string (DS:)
;	DX = coordinate (DH=col, DL=row)
displaystring	proc	near
	push	es
	push	di
	call	getaddress
	mov	es,textseg
	cld
	xor	ch,ch
	jcxz	exitdisplaystring	; don't even try it with a null string
pushchars:
	movsb
	mov	al,ah
	stosb
	loop	pushchars
exitdisplaystring:
	pop	di
	pop	es
	ret
	endp


;	Print a null-delimited string and scroll.
;
;	DS:SI = string
;
printstring	proc	near
	push	dx
	push	ax
	push	es
	push	di
	call	scroll
	xor	dx,dx
	mov	dl,winsy
	dec	dl
	call	getaddress
	mov	es,textseg
pstrloop:
	mov	al,[si]
	or	al,al
	jz	pstrout
	mov	es:[di],al
	mov	byte ptr es:[di+1],0bh
	add	di,2
	inc	si
	jmp	pstrloop
pstrout:
	add	di,4
	mov	afterstring,di

	pop	di
	pop	es
	pop	ax
	pop	dx
	ret
	endp


;	Print a character in the window.
;
;	This is only to be used after PRINTSTRING.
;
printchar proc	near
	push	es
	push	di

	mov	es,textseg
	mov	di,afterstring
	mov	es:[di],al
	mov	byte ptr es:[di+1],0fh
	pop	di
	pop	es
	ret
	endp


;	scroll window up one line
;
scroll	proc	near
	push	ax
	push	si
	push	di
	push	cx
	push	dx
	push	es
	mov	es,textseg
	mov	di,startaddr		; DI is the top-left corner
	add	di,160			; DI is the correct address to scroll
	mov	dl,winhy
	sub	dl,winly	
	sub	dh,dh			; DX is (# rows-1) to scroll up
	mov	cl,winhx
	sub	cl,winlx
	inc	cl
	sub	ch,ch			; CX is the # columns to scroll
	jcxz	scrollclear
	cmp	dx,0
	jz	scrollclear
scrollrow:
	push	cx
	push	di
scrolltransfer:
	mov	ax,es:[di]
	mov	es:[di-160],ax
	add	di,2
	loop	scrolltransfer	
	pop	di
	add	di,160
	pop	cx
	dec	dx
	jg	scrollrow
	sub	di,160			; address to clear
scrollclear:
	mov	cl,winhx
	sub	cl,winlx
	xor	ch,ch
	mov	ax,0720h
	rep	stosw
exitscroll:	
	pop	es
	pop	dx
	pop	cx
	pop	di
	pop	si
	pop	ax
	ret
scroll	endp

;===========================================================================


;	set full window size
;
fullwindow	proc	near
	mov	winlx,0
	mov	winly,0
	mov	winhx,79
	mov	winhy,42
	call	setstartaddr
	ret
	endp


;	set disassembly window
;
disasmwindow	proc	near
	mov	winlx,1
	mov	winly,1
	mov	winhx,78
	mov	winhy,7
	call	setstartaddr
	ret
	endp


;	set registers window
;
registerwindow	proc	near
	mov	winlx,1
	mov	winly,9
	mov	winhx,36
	mov	winhy,12
	call	setstartaddr
	ret
	endp


;	set memory regs window
;
mmregswindow	proc	near
	mov	winlx,38
	mov	winly,9
	mov	winhx,48
	mov	winhy,12
	call	setstartaddr
	ret
	endp


;	set state window
;
statewindow	proc	near
	mov	winlx,50
	mov	winly,9
	mov	winhx,58
	mov	winhy,12
	call	setstartaddr
	ret
	endp


;	set watch1 window
;
watch1window	proc	near
	mov	winlx,1
	mov	winly,14
	mov	winhx,58
	mov	winhy,17
	call	setstartaddr
	ret
	endp


;	set watch2 window
;
watch2window	proc	near
	mov	winlx,1
	mov	winly,19
	mov	winhx,58
	mov	winhy,22
	call	setstartaddr
	ret
	endp


;	set vdp window
;
vdpwindow	proc	near
	mov	winlx,1
	mov	winly,24
	mov	winhx,78
	mov	winhy,27
	call	setstartaddr
	ret
	endp


;	set grom window
;
gromwindow	proc	near
	mov	winlx,1
	mov	winly,29
	mov	winhx,78
	mov	winhy,32
	call	setstartaddr
	ret
	endp


;	set breakpoint window
;
bpwindow	proc	near
	mov	winlx,1
	mov	winly,34
	mov	winhx,78
	mov	winhy,37
	call	setstartaddr
	ret
	endp


;	set prompt window
;
promptwindow	proc	near
	mov	winlx,1
	mov	winly,34
	mov	winhx,78
	mov	winhy,41
	call	setstartaddr
	ret
	endp


;	Write AX in HEX to BX, CX=# digits
hexval	proc	near
	push	ax
	push	dx
	push	bx
	add	bx,cx
	dec	bx
hexvalloop:
	mov	dl,al
	and	dl,15
	cmp	dl,9
	jbe	nothexalpha
	add	dl,7
nothexalpha:
	add	dl,30h
	mov	[bx],dl
	dec	bx
	shr	ax,4
	loop	hexvalloop
	pop	bx
	add	bx,4
	pop	dx
	pop	ax
	ret
	endp	


;===========================================================================
;
;	[THIS INFO MAY BE OBSELETE]
;
;
;	DEBUGGER ENTRY POINT
;
;	Debugger is only called by:
;
;	1) Alt-Sysrq which halts execution (SYSRQ=1)
;	   Text mode, Keyboard interrupt is "original", user
;	   manually exits debug state or selects to execute
;
;	2) (stateflag&debugrequest)!=0;  current state printed
;	   Text mode permanent while emulator chugs on behind.
;	   Either an interrupt or a sticky bit calls this.
;	   The keyboard interrupt is the TI's.
;	   Alt-sysrq will interrupt and jump to 1).
;
;	The INTDEBUG flag, when set, means the debugger has been selected
;	and is only active when the DEBUGREQUEST flag is set.
;
;	Therefore, it is possible to tell if the debugger screen is up
;	or not by a combination of the intdebug flag and the sysrq var.
;
;	SPECIAL must be responsible for returning to the debugger screen
;	if it was active when it changed the mode.
;
;	DEBUG is responsible for clearing the debug bit.
;
;==========================================================================

debug	proc	near

	mov	ax,PC
	mov	_pc,ax
	mov	_wp,wp
	mov	_stat,stat

	mov	cx,8
	lea	bx,breaks

debugbreaks:
	cmp	[bx],ax			; hit brkp?
	jne	debbr0

       	mov	es,cpuseg
	mov	ax,[bx+2]
	mov	bx,[bx]
	cmp	word ptr es:[bx],BREAKOP ; memory changed?
	jne	debdebugging
	mov	es:[bx],ax		; restore instr
	jmp	debdebugging

debbr0:
	add	bx,4
	loop	debugbreaks

	mov	si,_PC
	cmp	es:[si],BREAKOP		; my own breakpoint?
	jne	debnotmine

	add	_PC,2			; not in list... 

debnotmine:
	test	stateflag,intdebug
	jz	debnotdebugging		; MUST be sysrq

	cmp	sysrq,0			; did user press this in middle
					; of debugging?
	jnz	debdebugging		; set up to accept commands

	jmp	debtext			; continue as normal

debnotdebugging:

debdebugging:
	mov	video_debug,1
	call	emustop

	and	stateflag,not intdebug

	cmp	sysrq,0
	jz	debtext
	pusha
	call	promptwindow
	call	scroll
	lea	si,dm_debugging
	call	printstring
	popa
	mov	sysrq,0
debtext:
	call	refresh		  	; update changes since last		
	
	test	stateflag,intdebug
	jnz	debexit

	call	getcommand 		; will handle INTDEBUG
			   		; and DEBUGREQUEST and screen mode
			   		; and keyboard int
	jmp	debout


;	INTDEBUG exits
;
debexit:
	cmp	debugdelay,0	   		; single-step?
	jz	debout

	and	stateflag,not debugrequest	; no, int 8 will re-set this


;	final exit
;	
debout:

	mov    	ax,_PC
	mov	PC,ax
	mov	WP,_WP
	mov	STAT,_STAT

	call	validateWP

	mov	ax,stateflag		; necessary for checkstate


	ret
	endp


;==========================================================================

;	REFRESH
;
;	Go through each window and redraw it.
;	Make word or byte WHITE if changed otherwise make it grey.
;

refresh	proc	near
	pusha
	push	es
	call	rdisasm			; also source & dest
	call	rregs
	call	rstate
	call	rwatch1
	call	rwatch2
	call	rvdp
	call	rgrom
	pop	es
	popa
	ret
	endp


;==========================================================================

;	Heart of the VDP/GROM memory dumping routines.
;	Draws current mem map, hilights changed bytes, current addr in blue
;
;
;	ASSUMES 4 ROWS!
;
;	DS:SI = source data,  
;	ES:BX = screen ASCII
;	CX = # bytes on a line
;	BP = address to MATCH -- different attr
;	DI = start screen address
;	ascofs = value to add to ASCII bytes
;
;	Uses AX,DI as temp
;	     DX as coords

memdump2 proc	near
	push	es
	mov	ax,0b800h
	mov	es,ax
	xor	dx,dx
mdrow:
	push	di			; save start of row
	push	bx			; save ascii row
	push	cx			; save # bytes to draw
	mov	ax,si
	push	bx
	mov	bl,07h			; 0Bh
	call	hex4			; draw address
	pop	bx
	add	di,7*2			; skip to data part
mdbytes:
	mov	ah,0fh			; not right addr?  Then bright white
	mov	al,[si]			; get datum

	cmp	es:[bx],al		; same as before?
	jne	mdbytes2
	mov	ah,0eh			; yup, grey

mdbytes2:
	cmp	si,bp
	jne	mdbytes3
	mov	ah,0bh			; 0eh
mdbytes3:
	sub	al,ascofs
	mov	es:[bx],al		; update ascii
	add	al,ascofs
	mov	es:[bx+1],ah		; update color

	push	bx	      		; save ascii ptr
	mov	bl,ah			
	call	hex2			; draw byte in changed/normal color
	pop	bx
	add	di,3*2			; skip to next byte hex
	add	bx,2			; skip to next byte ascii
	inc	si			; skip to next byte real
	loop	mdbytes			; finish row
	pop	cx			; restore # bytes
	pop	bx			; restore ASCII
	add	bx,160			; next row
	pop	di			; restore addr
	add	di,160			; next row
	inc	dl
	cmp	dl,4			; 4 rows done?
	jb	mdrow
	pop	es
	ret
	endp


;	Heart of the watch window memory dumping routines.
;	Draws current mem map, hilights changed word
;
;	ASSUMES 4 ROWS!
;
;	DS:SI = source data,  
;	ES:BX = screen ASCII
;	CX = # words on a line
;	DI = start screen address
;
;	Uses AX,DI as temp
;	     DX as coords
;

memdump4 proc	near
	push	es
	push	bp

	mov	ax,0b800h
	mov	es,ax
	xor	dx,dx
md4row:
	push	di			; save start of row
	push	bx			; save aSCII row
	push	cx			; save # bytes to draw
	mov	ax,si
	push	bx
	mov	bl,07h			; 0bh
	call	hex4			; draw address
	pop	bx
	add	di,7*2			; skip to data part
md4words:
	mov	dh,0fh			; changed attr
	mov	ax,[si]			; get datum

	cmp	es:[bx],ah		; same as before?
	jne	md4words2
	cmp	es:[bx+2],al
	jne	md4words2
	mov	dh,0eh			; yup, grey 
	mov	es:[bx+1],dh
	mov	es:[bx+3],dh
	mov	es:[di+1],dh
	mov	es:[di+3],dh
	mov	es:[di+5],dh
	mov	es:[di+7],dh
	jmp	md4words3

md4words2:
	mov	es:[bx],ah		; update ascii
	mov	es:[bx+1],dh		; update color
	mov	es:[bx+2],al		; 
	mov	es:[bx+3],dh		;

	push	bx	      		; save ascii ptr
	mov	bl,dh			
	call	hex4			; draw byte in changed/normal color
	pop	bx

md4words3:
	add	di,5*2			; skip to next word hex
	add	bx,2*2			; skip to next word ascii
	add	si,2			; skip to next word real
	loop	md4words	    	; finish row
	pop	cx			; restore # bytes
	pop	bx			; restore ASCII
	add	bx,160			; next row
	pop	di			; restore addr
	add	di,160			; next row
	inc	dl
	cmp	dl,4			; 4 rows done?
	jb	md4row
	pop	bp
	pop	es
	ret
	endp



;	Draw a 2-digit hex digit to the screen
;
;	ES:DI = addr, AL = value, BL = attr
;
;	Destroys AX
;
hex2	proc	near
	push	bx
 	mov	ah,al
	shr	ah,4
	and	al,15
	add	ax,3030h
	cmp	ah,39h
	jbe	hex2a
	add	ah,7
hex2a:
	cmp	al,39h
	jbe	hex2b
	add	al,7
hex2b:
	mov	es:[di],ah
	mov	es:[di+1],bl
	mov	es:[di+2],al
	mov	es:[di+3],bl
	pop	bx
	ret
	endp


;	Draw a 4-digit hex number to the screen
;
;	ES:DI = addr,  AX=value, BL = attr
;
;	Destroys AX
;
hex4	proc	near
	push	bx
	push	cx

	mov	cl,ah		; digits:  CH:CL:AH:AL
	mov	ah,al
	shr	ah,4
	and	al,15
	mov	ch,cl
	shr	ch,4
	and	cl,15

	add	ax,3030h
	add	cx,3030h
	cmp	ah,39h
	jbe	hex4a
	add	ah,7
hex4a:
	cmp	al,39h
	jbe	hex4b
	add	al,7
hex4b:
	cmp	ch,39h
	jbe	hex4c
	add	ch,7
hex4c:
	cmp	cl,39h
	jbe	hex4d
	add	cl,7
hex4d:
	mov	es:[di],ch
	mov	es:[di+1],bl
	mov	es:[di+2],cl
	mov	es:[di+3],bl
	mov	es:[di+4],ah
	mov	es:[di+5],bl
	mov	es:[di+6],al
	mov	es:[di+7],bl

	pop	cx
	pop	bx
	ret
	endp


;	Draw a 2-digit hex digit to the screen, whiten if diff
;
;	DI = addr, AL = value
;
;	SETS ES to screen
;	Destroys AX
;
hex2w	proc	near
	push	bx
	push	es
	mov	bx,0b800h
	mov	es,bx

	mov	bl,0eh			; unchanged attribute 07h
 	mov	ah,al
	shr	ah,4
	and	al,15
	add	ax,3030h
	cmp	ah,39h
	jbe	hex2wa
	add	ah,7
hex2wa:
	cmp	al,39h
	jbe	hex2wb
	add	al,7
hex2wb:
	cmp	es:[di],ah
	jne	hex2wc
      	cmp	es:[di+2],al
	je	hex2wd
hex2wc:
	mov	bl,0fh
hex2wd:
	mov	es:[di],ah
	mov	es:[di+1],bl
	mov	es:[di+2],al
	mov	es:[di+3],bl
	pop	es
	pop	bx
	ret
	endp


;	Draw a 4-digit hex number to the screen, whiten if necc
;
;	DI = addr,  AX=value
;
;	SETS ES to screen
;
;	Destroys AX
;
hex4w	proc	near
	push	bx
	push	cx
	push	es
	mov	bx,0b800h
	mov	es,bx

	mov	bl,0eh		; unchanged attr 07h

	mov	cl,ah		; digits:  CH:CL:AH:AL
	mov	ah,al
	shr	ah,4
	and	al,15
	mov	ch,cl
	shr	ch,4
	and	cl,15

	add	ax,3030h
	add	cx,3030h
	cmp	ah,39h
	jbe	hex4wa
	add	ah,7
hex4wa:
	cmp	al,39h
	jbe	hex4wb
	add	al,7
hex4wb:
	cmp	ch,39h
	jbe	hex4wc
	add	ch,7
hex4wc:
	cmp	cl,39h
	jbe	hex4wd
	add	cl,7
hex4wd:
	cmp	es:[di],ch
	jne	hex4we
	cmp	es:[di+2],cl
	jne	hex4we
	cmp	es:[di+4],ah
	jne	hex4we
	cmp	es:[di+6],al
	je	hex4wf
hex4we:
	mov	bl,03h
hex4wf:
	mov	es:[di],ch
	mov	es:[di+1],bl
	mov	es:[di+2],cl
	mov	es:[di+3],bl
	mov	es:[di+4],ah
	mov	es:[di+5],bl
	mov	es:[di+6],al
	mov	es:[di+7],bl

	pop	es
	pop	cx
	pop	bx
	ret
	endp


;===========================================================================

rregs	proc	near
	call	registerwindow
	mov	dx,0300h		; start coords

	mov	es,cpuseg
	mov	si,_wp


	mov	ch,4			; rows
rregrow:
	mov	cl,4
	call	getaddress
rregcol:


	mov	ax,es:[si]


	call	hex4w
	add	si,2
	add	di,9*2
	dec	cl
	jg	rregcol
	inc	dl
	dec	ch
	jg	rregrow
	ret
	endp


rstate	proc	near
	call	statewindow
	mov	dx,0300h		; start coord
	call	getaddress
	mov	ax,_pc
	call	hex4w
	inc	dl
	call	getaddress
	mov	ax,_wp
	call	hex4w
	inc	dl
	call	getaddress
	mov	ax,_stat
	call	hex4w

	xor	dh,dh
	inc	dl
	call	getaddress

	mov	ax,0b800h
	mov	es,ax

	mov	ax,_stat
	mov	cx,7
rstate1:	       		
	mov	bl,0dh			; draw status register 08h
	add	ax,ax
	jnc	rstate2
	mov	bl,0fh
rstate2:
	mov	es:[di+1],bl
	add	di,2
	loop	rstate1

	ret
	endp


rwatch1	proc	near
	call	watch1window
	mov	si,watch1		; start addr
rwatchenter:
	xor	dl,dl
	mov	dh,winsx
	sub	dh,14
	call	getaddress		; point to ascii part
	mov	bx,di
	xor	dx,dx
	call	getaddress		; point to addr part
	mov	cx,7		      	; # words


	push	ds
	mov	ds,cpuseg

	call	memdump4

	pop	ds

	ret
	endp


rwatch2	proc	near
	call	watch2window
	mov	si,watch2
	jmp	rwatchenter
	endp


rvdp	proc	near
	call	vdpwindow
	mov	bp,vaddr
	mov	si,bp
	and	si,03fc0h		; start address fixed to xxx0
	cmp	si,4000h-(16*4)
	jb	rvdp1
      	mov	si,4000h-(16*4)
rvdp1:
	xor	dl,dl
	mov	dh,winsx
	sub	dh,16
	call	getaddress
	mov	bx,di
	xor	dx,dx
	call	getaddress
	mov	cx,16
	call	memdump2
	ret
	endp


rgrom	proc	near
	call	gromwindow
	mov	bp,gaddr
	mov	si,bp
	and	si,0ffc0h
	xor	dl,dl
	mov	dh,winsx
	sub	dh,16
	call	getaddress
	mov	bx,di
	xor	dx,dx
	call	getaddress
	mov	cx,16
	push	ds
	mov	ds,gplseg
	call	memdump2
	pop	ds
	ret
	endp

;	Disassemble current instruction
;	
;
rdisasm	proc	near
	call	disasmwindow
	call	scroll
	mov	dx,0006h		
	call	getaddress
	mov	ax,0b800h
	mov	es,ax
	mov	byte ptr es:[di+32h*2],'³'

;	Draw hex address

	mov	ax,_pc
	mov	bl,07h			; 0bh
	call	hex4

	mov	byte ptr es:[di+4*2],'='

;	Find the opcode in the "opcodes" table with binary search.
;
;	b.h == op>=opcodes[si].lowerbound


	mov	es,cpuseg
	mov	di,_pc
	mov	bx,es:[di]		
	mov	op,bx			; op,BX=current opcode

	xor	cx,cx			; X:=0;
	mov	dx,opcodeslength/10	; Y:=N;

rdabinsearch:
	mov	ax,cx
	inc	ax
	cmp	ax,dx			; X+1<>Y
	jnz	rdahalve
	jmp	rdabinout
rdahalve:
	mov	ax,cx
	add	ax,dx
	shr	ax,1			; H:=(X+Y)/2;
	push	ax

	add	ax,ax			; get SI=ax*10
	mov	si,ax			; SI=ax*2
	shl	ax,2
	add	si,ax			; SI=(ax*2)+(ax*8)

	pop	ax

	cmp	bx,[si+opcodes]
	jb	rdafalse		; if b.h

	mov	cx,ax			; x:=h;
	jmp	rdabinsearch		; else, fi

rdafalse:
	mov	dx,ax			; y:=h;
	jmp	rdabinsearch		; fi

rdabinout:				
	add	cx,cx
	mov	si,cx			; opcodes[si]=lower bound for opcode
	shl	cx,2
	add	si,cx
	add	si,offset opcodes	; SI points to opcode entry

	lea	di,disline		; put disassembled output
	lea	bx,sdline		; put source & dest values

	mov	words,1			; one word used by instruction

rdadraw:
	mov	ax,[si+2]
	xchg	al,ah
	mov	[di],ax
	mov	ax,[si+4]
	xchg	al,ah
	mov	[di+2],ax		; draw opcode name
	mov	byte ptr [di+4],' '	; space
	add	di,5  			; point to source
	push	_pc			; routines can change it
	mov	ax,[si+6]
	or	ax,ax
	jz	rdadrawdone
	call	ax			; source
	jnc	rdanosource
	call	prvals			; AX=addr
rdanosource:
	mov	ax,[si+8]		
	or	ax,ax
	jz	rdadrawdone

	mov	byte ptr [di],','
	inc	di

	call	ax			; destination
	jnc	rdadrawdone

	mov	byte ptr [bx],','
	inc	bx

	call	prvals

rdadrawdone:
     	pop	_pc
	
	mov	cx,di
	sub	cx,offset disline
	lea	si,disline
	mov	dx,1506h
	mov	ah,07h			; 0bh
	call	displaystring

	mov	cx,bx
	sub	cx,offset sdline
	lea	si,sdline
	mov	dx,3306h
	mov	ah,07h			; 0bh
	call	displaystring

	mov	dx,0606h
	call	getaddress
	mov	ax,0b800h
	mov	es,ax
	mov	cx,words

	mov	si,_pc
	push	ds
	mov	ds,cpuseg


	mov	bl,07h			; 0bh
rdaopwords:
	mov	ax,[si]
	call	hex4
	add	si,2
	add	di,5*2
	loop	rdaopwords

	pop	ds

	ret
	endp


GETWORD	macro	reg,val
	local	gwok,gwout

	cmp	reg,8400h
	jb	GWok
	cmp	reg,0a000h
	jae	GWok
	mov	val,0
	jmp	GWout
gwOk:
	READWORD reg,val
GWOut:
	endm

;--------------------------------------------------------------------

;	Opcode-drawing routines
;
;	DI = ptr to disassembled output
;	BX = ptr to source & dest output
;	ES = cpuseg
;	Don't modify SI!

;	Print a number >xxxx
;
prn	proc	near
	push	cx
	push	bx
	mov	byte ptr [di],'>'
	inc	di
	mov	bx,di
	mov	cx,4
	call	hexval
	pop	bx
	add	di,4
	pop	cx
	ret
	endp

;	Print a register value  Rxx
;
prr	proc	near
	push	ax
	mov	byte ptr [di],'R'
	inc	di
	cmp	ax,9
	jbe	prr1
	mov	byte ptr [di],'1'
	inc	di
	sub	ax,10
prr1:
	add	ax,30h
	mov	byte ptr [di],al
	inc	di
	pop	ax
	ret
	endp

;	Print a byte value decimal xxx
;
prbd	proc	near
	push	ax
	push	cx
	push	dx
	push	bx
	mov	bx,10
	mov	cx,3
	cmp	ax,100
	jae	prbdd
	mov	cx,2
	cmp	ax,10
	jae	prbdd
	mov	cx,1
prbdd:
	add	di,cx
	push	di
prbdl:
	dec	di
	xor	dx,dx
	div	bx
	add	dl,30h
	mov	[di],dl
	loop	prbdl
	pop	di		
	pop	bx
	pop	dx
	pop	cx
	pop	ax
	ret
	endp

;	Print byte hex
;
prbh	proc	near
	push	cx
	push	bx
	mov	byte ptr [di],'>'
	inc	di
	mov	bx,di
	mov	cx,2
	call	hexval
	pop	bx
	add	di,2
	pop	cx
	ret
	endp

;	Print immediate
pri	proc	near
	inc	words

	add	_pc,2
	mov	bp,_pc
	GETWORD bp,ax
	jmp	prn
	endp

;	Print address @>xxxx
;
pra	proc	near
	inc	words

	add	_pc,2
	mov	bp,_pc
	GETWORD bp,ax
	mov	byte ptr [di],'@'
	inc	di
	jmp	prn
	endp

;	Print index  *Rxx
;
prin	proc	near
	mov	byte ptr [di],'*'
	inc	di
	jmp	prr
	endp

;	Print addressed index  (Rxx)
;	AX=reg
prain	proc	near
	push	ax
	call	pra
	pop	ax
	push	ax
	and	ax,15
	jz	prainout
	mov	byte ptr [di],'('
	inc	di
	call	prr
	mov	byte ptr [di],')'
	inc	di
prainout:
	pop	ax
	ret
	endp

;	Print incremented reg *Rxx+
;
princ	proc	near
	call	prin
	mov	byte ptr [di],'+'
	inc	di
	ret
	endp

;	Print value at address
;
prvals	proc	near
	mov	byte ptr [bx],'@'
	inc	bx
	push	di
	mov	di,bx
	and	ax,not 1
	call	prn			; @>addr
	mov	byte ptr [di],'='
	inc	di

	mov	bp,ax
	and	bp,not 1
	GETWORD bp,ax
	call	prn
	mov	bx,di
	pop	di
	ret
	endp

;	GET things
;

;	Get opcode
;
gto	proc	near
	mov	ax,op
	call	prn
	clc
	ret
	endp


;	Get register
;
gtr	proc	near
	mov	ax,op
	and	ax,15
	call	prr
	add	ax,ax
	add	ax,_wp
	stc
	ret
	endp


;	Get immediate
;
gti	proc	near
	call	pri
	clc
	ret
	endp


;	Get interrupt
;
gtn	proc	near
	inc	words

	add	_pc,2
	mov	bp,_pc
	GETWORD bp,ax
	and	ax,15
	call	prn
	clc
	ret
	endp


;	Get count for shift
;
gtc	proc	near
	mov	ax,op
	and	ax,00f0h
	shr	ax,4
	call	prbd
	clc
	ret
	endp


;	Get jump operand
;
gtj	proc	near
	mov	ax,op
	shl	ax,8
	sar	ax,7
	add	ax,_pc
	add	ax,2
	call	prn
	clc
	ret
	endp


;	Get bit count
;
gtb	proc	near
	mov	ax,op
	and	ax,255
	call	prbh
	clc
	ret
	endp


;	Get register from type III
;
gt3	proc	near
	mov	ax,op
	and	ax,03c0h
	shr	ax,6
	call	prr
	add	ax,ax
	add	ax,_wp
	stc
	ret
	endp


;	Get count from type IX
;
gt9	proc	near
	mov	ax,op
	and	ax,03c0h
	shr	ax,6
	call	prbd
	clc
	ret
	endp

;	Get complex thing.  Called by gts,gtd,gtsb,gtsd
;	DL=type, AX=reg
;
;	Returns AX=addr
gtcomp	proc	near
	or	dl,dl
	jnz	gtcomp1
	call	prr
	add	ax,ax
	add	ax,_wp
	ret
gtcomp1:
	cmp	dl,1
	jne	gtcomp2
	call	prin

	add	ax,ax
	add	ax,_wp
	mov	bp,ax
	GETWORD bp,ax
	ret
gtcomp2:
	cmp	dl,2
	jne	gtcomp3
	call	prain
	add	ax,ax
	jz	gtcomp21

	add	ax,_wp
	mov	bp,ax
	GETWORD bp,ax
gtcomp21:
	mov	bp,_pc

	push	bx
	GETWORD bp,bx
	add	ax,bx
	pop	bx

	ret
gtcomp3:
	call	princ
	add	ax,ax
	add	ax,_wp

	mov	bp,ax
	GETWORD bp,ax
	ret
	endp


;	Get source operand
;	
gts	proc	near
	mov	dx,op
       	and	dx,30h
	shr	dx,4
	mov	ax,op
	and	ax,15
	call	gtcomp
	stc		
	ret
	endp


;	Get destination operand
;
gtd	proc	near
	mov	dx,op
	and	dx,0c00h
	shr	dx,10
	mov	ax,op
	shr	ax,6
	and	ax,15
	call	gtcomp
	stc
	ret
	endp


;	Get source operand, byte
;
gtsb	proc	near
	call	gts
	ret
	endp


;	Get dest operand, byte
;
gtdb	proc	near
	call	gtd
	ret
	endp



;=========================================================================

;	GET COMMAND during "captive" mode.
;	An Alt-Sysrq, or just a debug call w/o INTDEBUG set, will call this.
;
;	When called, INTDEBUG==0 and DEBUGREQUEST==1
;
;
getcommand	proc	near
	pusha
	push	es
getcommloop:
	call	getkey
	cmp	ax,13			; enter == step once
	jne	gc_1

;	Set up to execute one instruction then come back.
;	Since DEBUGREQUEST is still set, the routine will come back.
;	Seeing as how INTDEBUG is unset, it will assume an interruption
;	by sysrq.
;
	mov	ontiscreen,0
	lea	si,dm_singlestep
	jmp	gcdebugout

gc_1:
	cmp	ax,' '			; space == animate
	jne	gc_2

;	Set up to execute continuously.  
;	Set INTDEBUG and DEBUGDELAY to 0, and reset DEBUGREQUEST.
;

	and	stateflag,not debugrequest
	or	stateflag,intdebug
	
	mov	debugdelay,0
	mov	ontiscreen,0
	lea	si,dm_continuous
	jmp	gcdebugout

gc_2:
	cmp	ax,9			; tab == interrupt 10 times/sec
	jne	gc_3

;	Set up an intermittent interrupt.
;	Set INTDEBUG, DEBUGDELAY to 6, reset DEBUGREQUEST
;
	and	stateflag,not debugrequest
	or	stateflag,intdebug
	mov	al,udebugdelay
	mov	debugdelay,al
	mov	ontiscreen,0
	lea	si,dm_intermittent
	jmp	gcdebugout

gc_3:
	cmp	ax,27			; esc == go back
	jne	gc_4

;	Stop debugging
;
	and	stateflag,not debugrequest
	mov	debugdelay,255
	mov	ontiscreen,1
	lea	si,dm_returning
	jmp	gctiout

gc_4:
	cmp	ax,'1'
	jne	gc_5
	
	call	promptwindow		; 1 = edit watch 1
	lea	si,pr_watch1
	call	printstring
	lea	si,watch1
	mov	cx,4
	call	inputhex
	call	rwatch1
	jmp	getcommloop

gc_5:
	cmp	ax,'2'
	jne	gc_6

	call	promptwindow		; 2 = edit watch 2
	lea	si,pr_watch2
	call	printstring
	lea	si,watch2
	mov	cx,4
	call	inputhex
	call	rwatch2
	jmp	getcommloop

gc_6:
	cmp	ax,'O'			; O = BASIC toggle
	jne	gc_7

gc_60:
	cmp	ascofs,60h
	jne	gc_61
	mov	ascofs,0h
	call	rvdp
	call	rgrom
	jmp	getcommloop
gc_61:
	mov	ascofs,60h
	call	rvdp
	call	rgrom
	jmp	getcommloop

gc_7:
	cmp	ax,'I'		  	; I = intermittence
	jne	gc_75

	call	promptwindow
	lea	si,pr_inter
	call	printstring
	lea	si,udebugdelay
	mov	cx,2
	call	inputhex
	mov	debugcnt,0
	jmp	getcommloop


gc_75:
	cmp	ax,'S'			; S = swap screen
	jne	gc_8

	call	settifromdebug

	call	getkey

	call	setdebugfromti

	jmp	getcommloop


gc_8:

	cmp	ax,69			; E)dit... need another key
	je	gc_80
      	jmp	gc_9

gc_80:
	call	promptwindow
	lea	si,pr_edty
	call	printstring
	call	getkeye
	cmp	ax,62			; >
	je	gc_82
	cmp	ax,67			; C
	je	gc_82
	cmp	ax,71			; G
	je	gc_82
	cmp	ax,86			; V
	je	gc_82
	jmp	gc_9

gc_82:
	push	ax
	lea	si,pr_edit
	call	printstring
	lea	si,e_addr
	mov	cx,4
	call	inputhex
	lea	si,pr_num
	call	printstring
	lea	si,e_num
	mov	cx,2
	call	inputhex
	and	e_num,0ffh
	pop	ax

	cmp	ax,62
	je	gc_8c
	cmp	ax,67
	je	gc_8c
	cmp	ax,71
	je	gc_8g
	cmp	ax,86
	je	gc_8v
gc_8c:
	mov	es,cpuseg
	mov	di,1 			; XOR mask
	jmp	gc_8r
gc_8g:
	mov	es,gplseg
	xor	di,di
	jmp	gc_8r
gc_8v:
	push	ds
	pop	es
	xor	di,di
	and	e_addr,3fffh
	mov	ax,e_addr
	add	ax,e_num
	cmp	ax,4000h
	jbe	gc_8r
	sub	ax,4000h
	sub	e_num,ax

gc_8r:
	lea	si,pr_ego
	call	printstring
gc_8l:
	cmp	e_num,0
	je	gc_8o
	mov	cx,2
	mov	si,e_addr
	xor	si,di
	push	di
	push	es
	call	inputhex_es
	call	rregs
	call	rwatch1
	call	rwatch2
	call	rvdp
	call	rgrom
	call	promptwindow
	pop	es
	pop	di
	inc	e_addr
	dec	e_num
	jmp	gc_8l
gc_8o:
	jmp	getcommloop

gc_9:
	cmp	ax,'W'	      		; W = change WP addr
	jne	gc_10

	call	promptwindow
	lea	si,pr_edwp
	call	printstring
	lea	si,_WP
	mov	cx,4
	call	inputhex
	call	rstate
	call	rregs
	jmp	getcommloop

gc_10:
	cmp	ax,'P'	      		; P = change PC
	jne	gc_11

	call	promptwindow
	lea	si,pr_edpc
	call	printstring
	lea	si,_PC
	mov	cx,4
	call	inputhex
	call	rstate
	call	rdisasm
	jmp	getcommloop

gc_11:
	cmp	ax,'U'			; change STAT
	jne	gc_12

	call	promptwindow
	lea	si,pr_edst
	call	printstring
	lea	si,_STAT
	mov	cx,4
	call	inputhex
	call	rstate
	jmp	getcommloop

gc_12:
	cmp	ax,'B'			; add a breakpoint
	jne	gc_13

	call	promptwindow
	lea	si,pr_break
	call	printstring
	call	getbptr				; rets BX=brk vec
	jc	gc_120
	call	brdelete			; delete old one
	lea	si,pr_braddr
	call	printstring
	mov	si,bx				; addr of bp
	mov	cx,4
	call	inputhex
	and	word ptr [bx],0fffeh		; fix PC
	mov	si,[bx]				; get addr
	mov	es,cpuseg
	mov	ax,es:[si]			; get old inst
	mov	[bx+2],ax			; save
	mov	word ptr es:[si],BREAKOP	; replace
gc_120:	jmp	gc_upd

gc_13:
	cmp	ax,'D'			; delete a breakpoint
	jne	gc_14

	call	promptwindow
	lea	si,pr_dbreak
	call	printstring
	call	getbptr
	jc	gc_130
	call	brdelete
gc_130:	jmp	gc_upd
gc_upd:	call	rwatch1
	call	rwatch2
	call	rdisasm
	jmp	getcommloop

gc_14:
	mov	ah,2
	mov	dl,7
	int	21h
	jmp	getcommloop

gctiout:

gcdebugout:
	call	promptwindow
	call	printstring

	mov	video_debug,1
	call	emustart
	pop	es
	popa
	ret
	endp


;----------------------------------------------------------------

;	Get breakpoint pointer.
;
;	Accept input from keyboard.
;	Return C=1 if error.
;	[BX]-> breakpoint vector

getbptr proc 	near
	call	getkeye
	cmp	ax,'0'
	jb	getberr
	cmp	ax,'7'
	ja	getberr

	mov	bx,ax
	sub	bx,'0'
	add	bx,bx
	add	bx,offset breaks
	clc
	jmp	getbout

getberr:
	stc
getbout:
	ret
	endp


;
;	Delete a breakpoint.
;
;	[BX] = breakpoint struc
;	ES:  = cpuseg

brdelete proc	near
	push	ax
	push	bx
	push	si

	cmp	word ptr [bx],0ffffh
	je	brd0

	mov	si,[bx]
	mov	ax,[bx+2]
	cmp	word ptr es:[si],BREAKOP	; changed?
	jne	brd1
	mov	es:[si],ax

brd1:
	mov	word ptr [bx],0ffffh

brd0:
	pop	si
	pop	bx
	pop	ax
	ret
	endp


;=======================================================================

;	Get a key using BIOS
;	
;	Return in AX -- AH=00h if normal key, AH=0ffh if extended
;
getkey	proc	near
	mov	ah,10h
	int	16h
	mov	ah,00h
	or	al,al
	jz	gkext
	cmp	al,'a'
	jb	gkout
	cmp	al,'z'
	ja	gkout
	sub	al,20h			; uppercase
	jmp	gkout
gkext:
	mov	ah,10h
	int	16h
	mov	ah,0ffh
gkout:
	ret
	endp


;	Get a key and echo it.
;
;	See GETKEY.

getkeye proc	near
	call	getkey
	call	printchar
	ret
	endp


;	Input a string using int 21h.
;	SI=dest loc, CX=length
;
;	Returns null-terminated string, SI=where
;
input	proc	near
	push	dx
	push	cx
	push	bx
	push	ax
	call	scroll
	mov	dh,0
	mov	dl,winsy
	dec	dl
	call	movecursor

	inc	cx
	mov	[si],cl
	mov	ah,0ah
	mov	dx,si
	int	21h
	
	mov	bl,[si+1]
	xor	bh,bh
	mov	byte ptr [si+bx+2],0
	add	si,2

	call	losecursor
	pop	ax
	pop	bx
	pop	cx
	pop	dx
	ret
	endp

;	Move cursor to coords DH,DL
;	
;
movecursor	proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	xchg	dl,dh
	add	dl,winlx
	add	dh,winly
	mov	ah,2
	mov	bh,0
	int	10h
	mov	ah,1
	mov	cx,0607h
	int	10h
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	Lose cursor
;
losecursor	proc	near
	push	ax
	push	cx
	mov	ah,1
	mov	ch,20h
	int	10h
	pop	cx
	pop	ax
	ret
	endp


;	Input a hexadecimal number
;
;	[SI]=word dest
;	CX=# digits
;
inputhex	proc	near
	push	es
	push	ds
	pop	es
	call	inputhex_es
	pop	es
	ret
	endp


;	Input a hexadecimal number to ES:
;
;	ES:[SI]=word dest
;	CX=# digits
;
inputhex_es	proc	near
	push	ax
	push	dx
	push	si
	push	cx
iheloop:
	lea	si,temp
	call	input

	xor	dx,dx			; result word
iheencode:
	mov	al,[si]
	or	al,al
	jz	ihedone
	cmp	al,'f'
	ja	iheerr
	cmp	al,'a'
	jb	iheenc1
       	and	al,not 20h
iheenc1:
	cmp	al,'0'
	jb	iheerr
	cmp	al,'F'
	ja	iheerr
	cmp	al,'9'
	jbe	iheok
	cmp	al,'A'
	jae	iheok

iheerr:
	mov	ah,2
	mov	dl,7
	int	21h
	jmp	iheloop

iheok:
	sub	al,30h
	cmp	al,9
	jbe	iheok2
	sub	al,7
iheok2:
	shl	dx,4
	or	dl,al
	inc	si
	jmp	iheencode

ihedone:
	pop	cx
	pop	si
	cmp	cx,2
	ja	iheword

      	mov	es:[si],dl
	jmp	iheout
iheword:
	mov	es:[si],dx
iheout:
	pop	dx
	pop	ax
	ret

	endp



	end
