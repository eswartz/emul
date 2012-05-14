; ***************************************
; VIDEO.ASM  V9t9 video routines
; ***************************************
; by Edward Swartz  6/4/1993
; ***************************************


_VIDEO_	= 1


	comment	\

อออออออออออออออออออออออออออออฺฤฤฤฤฤฤฤฤฤฤฤฤฟอออออออออออออออออออออออออออออ
                             ณ EGA LAYOUT ณ
อออออออออออออออออออออออออออออภฤฤฤฤฤฤฤฤฤฤฤฤูอออออออออออออออออออออออออออออ

     V9t9 uses EGA video mode 0Dh or 13 (320x200x16) to emulate the
99/4A video screen.  On the EGA, the palette is set up to closely match
16 of the EGA's colors to the 99/4A layout.  On the VGA, the palette is
directly set to the same RGB values the 99/4A uses.

     There are two pages used in the EGA video memory.  The first page
is always visible and is the "correct" screen.  The second page, 256
rows down, is used for scratchwork.  This is where sprites are drawn and
moving sprites' backgrounds are replaced.  The corrected pieces from the
second page are copied directly over the first page.

     A lucky thing about using the 320x200 mode is that sprites do not
have to be clipped when drawn on the second page.  Even the widest
sprite (32 pixels), when drawn on 99/4A column 256, will not wrap around
to obscure the left side of the screen.  (256+32<320.)

     In text mode (40x24), only the first page is used, since there are
no sprites.


อออออออออออออออออออออออออฺฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฟออออออออออออออออออออออออออ
                         ณ DIVISION OF LABOR ณ
อออออออออออออออออออออออออภฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤูออออออออออออออออออออออออออ

     VIDEO.ASM (and its include files) are broken into three main parts:
VDP write handling, graphics updating, and sprite drawing.


ฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤ


     VDP write handling is in "handlevdp" in VIDEO.ASM.  This routine
handles writes to VDP memory which could affect the video screen.  It
also handles VDP write-only register changes.

     The most important thing to understand about the 99/4A video system
is that a write of one byte can change the entire screen, due to the use
of several tables in VDP memory.  For example, the color table, which is
32 bytes long, controls the colors of the characters in 8-byte groups.
So a write to one byte in that table might change the color of the
entire screen, if the screen table is full of the eight affected
characters.

     This is in direct opposition to IBM video cards, which all employ
some sort of bitmap.  What to the 99/4A is a byte change is a radical
full-screen redraw on the IBM.  Therefore, care must be taken to
optimize video handling.


     The obvious way to avoid extra work, is by not making the video
changes right away, but cache an entire set of changes and later update.
Several sets of tables in V9t9 basically emulate the 99/4A's VDP tables,
by storing information about what has changed since the last redraw.
(Also, if a write into VDP memory does not change its contents, it is
ignored completely.)

     To use the example above, a write to the pattern table:  V9t9 will
simply store an >FF somewhere in a 256-byte table to say that that
pattern has changed.  And that's all.  Likewise, a write to the color
table will store eight >FF's in that table, since all of those
characters have effectively changed.

     The process above is all that happens during a VDP byte write.

                                    * * *

     However, writes to the VDP write-only registers poses a different
problem.  Some changes, such as screen color, can be changed very easily
(simply by updating the EGA palette entry for color 0).  But others,
such as switching from graphics into text mode, require a big redraw.
And due to the data-caching above, information about table changes may
not even mean anything in the new graphics mode.  (What does color mean
to text mode?)  And still other situations, like changing sprite size,
cannot be postponed for a long time if the 99/4A program is meaning to
make a point by rapidly alternating sprite size.

     I'm really not sure how I handled this.  I think for mode changes,
I immediately redrew the screen, and for the others, I just blacked out
the change tables to >FF's to force redraws when the next update
happened.


ฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤ


     Graphics updates happen some number of times per second, depending
on the V9t9 variables "VideoUpdateDelay" and "VideoMaxDelay" (I think).
Depending on the current video mode (assumed to be correct by the above
immediate redraw upon mode changes), the change tables are checked and
things are redrawn.

     Everything occurs in 8x8 blocks in V9t9.  If only one byte of a
pattern covering the screen is changed, the whole screen gets redrawn.
Even in bitmap mode, 8x8 blocks are used.  And it's difficult, since the
colors can be different on each row.  Oh well, that's as fast as it can
go.  I've tried.  It's more memory-efficient to use 8x8 blocks, though.

                                    * * *
     
     Something interesting happens in text mode.  Since it only has two
colors, all V9t9 ever writes to the EGA are colors 0 and 15.  The
palette is set up, however, so colors 0 and 15 map to the current
background and foreground.  Therefore, all writes to the text mode
screen are basically as if they were to monochrome bitmaps.

     Also, on the 386 text mode can be optimized.  As you can guess,
drawing individual 6-pixel-wide characters on an effectively 8-byte-wide
matrix is difficult.  On the 386, the redraw is optimized by drawing
four characters (4x6 = 24 pixels) at a time to the screen, storing those
characters' bitmaps in a 32-bit wide register.  It is really very fast,
and maybe even faster than graphics mode now.  ;)

                                 * * *

     There is an undocumented uncompleted define called TEXTONLY which
was supposed to allow you to run V9t9 in text mode.  The earliest
versions of TI Emulator were written in text mode (making bitmap mode
and sprites impossible, of course), but I wasn't able to do that again
with any degree of patience.  The basic fact would be that changes would
immediately be written to the screen in "handlevdp", since it's fast
enough.

                                 * * *

     See "graphicsscreen", "textscreen", "multiscreen", and "bitscreen"
in GRAPHICS.INC for the code.  There's really not much more to say about
it.


ฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤ


     Sprite updates occur with graphics, multicolor, and bitmap modes.
The basic process is this:  The current sprite table is compared to the
last sprite table (stored in memory) for changes.  Changes can include
the fact that sprites get deleted by setting the first byte of a sprite
entry to >D0.

     Whenever a sprite must change, those below and above it must be
updated as well on the bitmap.  Also, the characters in the background
must be updated.  ALSO, changes in the background force all sprites
above them to be updated.

     Somewhere after all the changes are figured out, the background
patterns are copied or drawn to the second page, and the sprites are
drawn over them.  If the user has selected the five-sprites-on-a-line-
bug-mode, then only certain rows of sprites are updated, depending on
where they are.

     After all this, all the updated 8x8 blocks are copied directly to
the visible page, which sometimes can be seen as flicker.  It's a
helluva lot more efficient than double-buffering or page flipping,
however, due to the sprite clipping.

     SPRITES.INC contains all the gooey details.


ออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออ


	\

	include	standard.h
	include	tiemul.h
	include	debug.h
	include special.h		; for DOSPRINT
	include support.h		; for setuperror
	include	speech.h		; for phrase on screen
	include	record.h
	include	log.h

	include	video.h

	include	registers.inc		; for movesprites
	include	memory.inc

	include	sprites.inc
	include	graphics.inc

	.data

maxvdp		equ	3fffh

rowwidth	equ	40

IFDEF		TEXTONLY
screenmode	equ	1
ELSE
screenmode	equ	13
ENDIF

blanktop	equ	0			; blank lines on top of screen
;;blankmemtop	equ	36		  	; blank memory lines
blankspace	equ	(rowwidth-32)/2
nextscreenoffs	equ	rowwidth*260
screen0		equ	blankspace+blanktop*rowwidth
						; top-left corner of screen
speechline	equ	screen0+rowwidth*192
screen1		equ	screen0+nextscreenoffs
screen2		equ	screen1+nextscreenoffs

	IFNDEF	TEXTONLY
blankpage	equ	screen2+nextscreenoffs
	ELSE
blankpage	equ	4000h
	ENDIF

vregsmasks	db	3,0fbh,0fh,0ffh,0ffh,07fh,07h,0ffh
screen		dw	0
patterns	dw	0
colors		dw	0
sprites		dw	0
sprpat		dw	0

colorssize	dw	32
screensize	dw	768
patternssize	dw	2048
spritessize	dw	128
screenwidth	dw	32

segments	dw	32 dup (0ffffh)

IFDEF		TEXTONLY
	textseg	dw	0b800h
ELSE
	graphseg dw	0a000h
ENDIF

;	Handlers for the graphics modes' various functions
;
;	Handlers update CHANGES or SPRITECHANGES. 
;
;	SCREENHANDLER will fix the screen.
;
;	CHANGES contains a 255 if the char needs to be updated,
;	and a 0 if it is okay.
;

IFDEF		TEXTONLY
screenhandler	dw	textonlyscreen
ELSE
screenhandler	dw	graphicsscreen	; update entire screen
ENDIF

charhandler	dw	graphicschar		; update a char
patternhandler	dw	graphicspattern	; update a pattern byte
colorhandler	dw	graphicscolor	; update color
spritehandler	dw	graphicssprite	; update a sprite entry
						; INCLUDES SPRPAT REFS!
sprpathandler	dw	graphicssprpat
colorbasehandler dw	normcolorbase
pattbasehandler	dw	normpattbase

mgraphics	equ	0
mtext		equ	1
mmulticolor	equ	2
mbitmap		equ	3

vga		db	0			; 0 if EGA, <>0 if VGA
mode		db	mgraphics

	IFNDEF	T386

textaddrs	db	2880	dup(?)		; text addresses and offsets
						; for each char position

	ENDIF

changes		db	1056	dup (?)
charchanges	db	768	dup (?)
spritechanges	db	32	dup (?)
spritecharchanges db	256 	dup (?)
spritestodraw	db	32	dup (?)
oldsprites	db	128	dup (?)
olddeleted	dw	0ffffh
;;;spriteposbitmap	db	4*256	dup (?)
spritenums	db	256	dup (?)
fifthsprites	db	256	dup (?)
legalrows	db	256	dup (?)
deleted		dw	0ffffh			; last sprite
magpatt		db	32 dup (?)
obliterated	db	32 dup (?)
check5sprites	db	1
row5		db	?			; the row number to set
						; chars on in CHECK5SPRITEs..
dsccount	db	?
;;patternschanged	db	0ffh			; for DEBUGGING
						; text-mode pattern updates
statechanged	db	0ffh
everupdate	db	0ffh

pal16		db	0,0, 2,18,1,9,4,27,12,36,6,54,16,5,7,63,0
pal256		db	0,0,0,0,0,0
		db	16,44,16,24,48,24
		db	16,16,48,24,24,60
		db	48,16,16,16,60,60
		db	60,16,16,63,32,24
		db	56,48,16,63,56,24
		db	16,32,16,48,16,48
		db	56,56,56,63,63,63

invpalette	db	0,0,16,42,8,9,32,35,32,28,20,38,40,16,56,7,0

magconvert	db	00h,03h,0ch,0fh,30h,33h,3ch,3fh
		db	0c0h,0c3h,0cch,0cfh,0f0h,0f3h,0fch,0ffh
		even
translate	db	512	dup(?)		; colors
						; FG, BG
charoffsets	dw	768	dup(?)

;;ckground	db	0		; background palette
textfg		db	0		; text-mode colors
textbg		db	0
blank		db	0		; is the screen blank?
spritemag	db	1		; 1=1x, 2=2x
spritechars	db	1		; # chars defined by a sprite, 1/4
spritesize	db	8
totspritesize	db	1		; size 1,2,4 in characters on a side
ospritemag	db	1
ospritechars	db	1
ospritesize	db	8
ototspritesize	db	1		; old vals

oldreg		db	0
oldmode		db	0

		even

rowz		dw	0		; # rows to clear w/CLEARSIDES
rowcount	db	256 dup (?)	; # sprites on a line

bitpattmask	dw	0		; mask defined by vreg#4
bitcolormask	dw	0		; mask defined by vreg#3

curx		db	0
cury		db	0

ontiscreen	db	0

videoredrawlatency	db	4	; how often to update screen
videoredrawmaxdelay	db	15	; maximum delay between redraws
sinceredrawcounter	db	0	; time since last redraw
vdprestcounter		db	0	; time since VDP write


rr1	dw	?			; for DOTRANSFER
rr2	dw	?
rr6	dw	?
rr7	dw	?
rr8	dw	?

temploc	db	2 dup (?)		; movesprites
tempmot	db	4 dup (?)		; movesprites

video16	db	0
checkspritecoinc db	0	

video_debug db	0			; emustart/emustop from debug?

	db	'DogSpit'
secret	db	0

	include	tifont.inc


		even



ramsizebit	equ	80h		; VDP Reg 1 masks
blankbit	equ	40h
interruptbit	equ	20h

bitmapbit	macro			;; tests bitmap bit
	test 	byte ptr [vregs],2
	endm

textbit		macro			;; tests text mode bit
	test	byte ptr [vregs+1],10h
	endm

multibit	macro			;; tests multicolor mode bit
	test	byte ptr [vregs+1],8h
	endm

spritemaginfo	macro	reg		;; returns sprite magnification
	mov	reg,byte ptr [vregs+1]
	and	reg,3
	endm

	

	.code

;===========================================================================
;	VIDEO:	Pre-config initialization
;
;	Check the video card.
;===========================================================================

	.data

vpci_msg db	'Detecting video card and monitor...',0dh,0ah,'$'
vpci_err db	'An EGA/VGA monitor and card with 256k+ memory are required.',0dh,0ah,0

	.code

video_preconfiginit proc near

	lea	dx,vpci_msg
	call	dosprint

	mov	vga,0				; don't know yet	

	mov	ah,12h
	mov	bl,10h
	int	10h				; get information

	cmp	bh,0
	jnz	vpcierr				; color or monochrome?
        or      bl,bl
        jz      vpciskipok                      ; VMWARE says this...
	cmp	bl,3
	jb	vpcierr				; 256k+?
vpciskipok:
	and	cl,15
	cmp	cl,6
	jb	vpcierr				; EGA/VGA+ monitor?

	mov	ah,1ah
	mov	al,0
	int	10h				; get display combo code
	cmp	al,1ah
	jne	vpciskip			; not supported (okay)

	cmp	bl,0ch 				; MCGA range.
	ja	vpcierr
	cmp	bl,0ah
	jae	vpcierr

	cmp	bl,4			
	jb	vpcierr

	cmp	bl,7
	jb	vpcigood			; EGA.

	mov	vga,1

	jmp	vpcigood

vpciskip:
	mov	ax,0dh
	int	10h
	mov	ah,0fh
	int	10h
	cmp	al,0dh
	jne	vpcierr				; whoops!  Nope.

	mov	ax,13h
	int	10h
	mov	ah,0fh
	int	10h
	cmp	al,13h
	jne	vpcireset			; EGA.

	mov	vga,1				; VGA.

vpcireset:
	mov	ax,3
	int	10h				; stay in text mode.

vpcigood:	
	clc					; looks like it.
	jmp	vpciout

vpcierr:
	lea	dx,vpci_err
	call	setuperror
	stc

vpciout:
	mov	ontiscreen,0

	ret
	endp


;===========================================================================
;	VIDEO:	Post-configuration init.
;
;	þ  Set up stupid buffers and tables.
;===========================================================================

video_postconfiginit proc near
	IFNDEF	T386
	call	settextaddrs
	ENDIF

	call	makecharoffsets
	call	setuptranslate

	mov	video_debug,0

	test	stateflag,intdebug		; starting out debugging?
	jnz	vpocidebug			; yup...

	mov	ontiscreen,1			; no.  normal
	jmp	vpociout

vpocidebug:
	mov	ontiscreen,0			; yes

vpociout:
	clc
	ret
	endp


;===========================================================================
;	VIDEO:	Restart.
;
;	þ  Set video mode, clearsides, etc IF NOT IN DEBUG MODE!
;	
;===========================================================================

video_restart proc near
	push	ax

	cmp	video_debug,0			; exiting from debugger?
	jz	vrtograph			; no

	mov	video_debug,0

	cmp	ontiscreen,0			; debugging somehow?
	jz	vrout

	call	settifromdebug			; nope
	jmp	vrout


vrtograph:
	cmp	ontiscreen,0			; we only get here from
	jz	vrdeb				; DOS shell

	call	completeupdate			; redraw screen
	jmp	vrout

vrdeb:
	call	setdebugfromtext		; redraw debugger screen

vrout:
	clc
	pop	ax
	ret
	endp


;===========================================================================
;	VIDEO:	Restop.
;
;	þ  Set text mode.
;	þ  If we're in intermittent debug mode, save that screen.
;	
;===========================================================================

video_restop proc near
	cmp	video_debug,0		; stopping as debug interrupt?
	jz	vrograph		; no... set text mode

	mov	video_debug,0

	cmp	ontiscreen,0		; debug screen still up
	jz	vroout

	call	setdebugfromti		; set up debugging scren
	jmp	vroout
	
vrograph:
	cmp	ontiscreen,0		; debugging?
	jnz	vrotext

	call	settextfromdebug	; set 400 lines, if necc.
	jmp	vroout

vrotext:
	mov	ax,1200h
	mov	bl,31h	
	int	10h			; turn on default palette loading

	mov	ax,3
	int	10h

vroout:
	clc
	ret
	endp


;===========================================================================
;	VIDEO:	Shutdown.
;===========================================================================

video_shutdown proc near
	mov	ax,1202h
	mov	bl,30h
	int	10h			; set 400 lines

	test	stateflag,intdebug	; debugger mode?
	jnz	vsdnoskip		

	mov	ah,0fh			; check current video mode
	int	10h
	cmp	al,3			; already text?
	je	vsdskip

vsdnoskip:				; we in debugger mode, so have to
	mov	ax,3			; do mode set to force 400 lines
	int	10h
	mov	ah,10h
	mov	al,03h
	mov	bl,1
	int	10h

	mov	ah,01
	mov	ch,67h
	int	10h
vsdskip:

	clc
	ret
	endp


	IFNDEF	T386

;	Set up TEXTADDRS for text mode
;
;	Textaddrs contains the EGA address for the character
;	at each row/column, as well as the bit offset within
;	the byte where the pattern starts.
;
settextaddrs	proc	near
	pusha
	lea	bx,textaddrs
	mov	si,SCREEN0+1
					; SI=offset of first char
	mov	bp,8			; divisor
	mov	di,24			; DI=row counter
starows:
	xor	cx,cx			; col pixel #
stacols:
	mov	ax,cx			
	xor	dx,dx
	div	bp			; AX=# cols, DX=offset
	add	ax,si
	mov	[bx],ax
	mov	[bx+2],dl
	add	bx,3			; next rec
	add	cx,6			; next col pixel
	cmp	cx,240
	jl	stacols
	add	si,8*rowwidth
	dec	di
	jg	starows
	popa
	ret
	endp
	
	ENDIF


;	Make character offsets
;
makecharoffsets	proc	near
;	Also sets up CHAROFFSETS

	lea	di,charoffsets
	mov	dx,24
	xor	ax,ax
sutrows:
	mov	cx,32
sutcol:
	mov	[di],ax
	add	di,2
	inc	ax
	loop	sutcol

	add	ax,-32+rowwidth*8
	dec	dx
	jg	sutrows

	ret
	endp


;	 Set up TRANSLATE, which has switched-nybble colors.
;
;	This is used to translate 99/4A colors into EGA colors,
;	notably in the case where the 99/4A color is 0 (clear),
;	which must be set to the EGA color for the background.
;
setuptranslate	proc	near
	lea	bx,translate
	mov	dh,byte ptr vregs+7	; DH = screen color
	and	dh,15
	mov	dl,0
svhighloop:
	mov	cl,0
svlowloop:
	mov	[bx],cl			; no, use CL as color

svlclback:
	mov	[bx+1],dl
svlsiback:
	add	bx,2

	inc	cl
	cmp	cl,16
	jl	svlowloop
	inc	dl
	cmp	dl,16
	jl	svhighloop

	ret
setuptranslate	endp


;	Set border color
;
setborder	proc	near
	mov	ax,1001h
	mov	bh,pal16
	int	10h

	mov	ax,1000h
	mov	bh,pal16
	mov	bl,0
	int	10h
	ret
setborder	endp





;	Set foreground color (color #15)
;	
;	Only used in text mode.  In text mode the EGA colors
;	that make up the screen are 0 and 15, and their palette
;	entries are adjusted to provide the text-mode colors.
;
setfg	proc	near
	mov	ax,1000h
	mov	bl,textfg
	textbit
	jnz	setfgtext
	mov	bl,15
setfgtext:
	xor	bh,bh
	mov	bh,[bx+pal16]
	mov	bl,0fh
	int	10h
	ret
	endp



;	Set the TI-colored palette
;
setpalette	proc	near
	mov	ah,12h
	mov	al,01h
	mov	bl,31h
	int	10h			; turn off default palette loading

	mov	ah,10h
	mov	al,02h
	push	ds
	pop	es

	lea	dx,pal16
	int	10h

	cmp	vga,0
	jz	spnovga

	mov	cl,0			; shift right
	call	setpalette256
spnovga:
	call	setborder
	call	setfg
	ret
	endp


;	Set the inverted palette
;	
;	Not really "inverted" but the "flashed" palette --
;	used when performing special functions that flash the
;	screen.
;
invertpalette	proc	near
	mov	ah,12h
	mov	al,01h
	mov	bl,31h
	int	10h			; turn off default palette loading

	cmp	vga,0
	je	ipega

	mov	cl,1			; shift right -- make dim
	call	setpalette256
	jmp	ipout

ipega:
	mov	ah,10h
	mov	al,02h
	push	ds
	pop	es
	mov	dx,offset invpalette
	int	10h
ipout:
	ret
	endp


setpalette256	proc	near
	push	di
	push	si
	push	cx
	push	dx
	push	bx
	push	ax

	lea	si,pal16
	lea	di,pal256
	mov	ch,16

sp256loop:
	mov	dx,3c8h
	mov	al,[si]
	out	dx,al
	inc	dx
	mov	al,[di]
	shr	al,cl
	out	dx,al
	mov	al,[di+1]
	shr	al,cl
	out	dx,al
	mov	al,[di+2]
	shr	al,cl
	out	dx,al
	add	di,3
	inc	si
	dec	ch
	jg	sp256loop

	pop	ax
	pop	bx
	pop	dx
	pop	cx
	pop	si
	pop	di
	ret
	endp


;	Set background colors 
;
;	Used in DEBUG so the colors are always the same.
;
setdebugbg	proc	near
	mov	ax,1001h
	mov	bh,0
	int	10h			; set overscan to black

	mov	ax,1000h
	mov	bh,0
	mov	bl,0		
	int	10h			; set color 0 to black

	mov	ax,1000h
	mov	bh,63			; set color 15 to white
	mov	bl,15
	int	10h
sdbout:
	ret
	endp



;	Set start address of screen
;
dstart	proc	near
	push	ax
	push	dx
	cli
	mov 	ax,0
	mov 	dx,03d4h
	mov 	ah,bh
	mov 	al,0ch
	out 	dx,ax
	mov 	ah,bl
	inc 	al
	out 	dx,ax
	sti
	pop	dx
	pop	ax
	ret
	endp



IFDEF	TEXTONLY

settiscreenmode proc	near
	call	setpalette
	mov	ax,screenmode
	int	10h
	call	setpalette

	mov	ah,10h
	mov	al,03h
	mov	bl,0
	int	10h			; 

	mov	ah,01
	mov	ch,20h
	int	10h			; no cursor

	mov	dx,3c4h
	mov	al,01h
	out	dx,al
	inc	dx
	in	al,dx
	or	al,1
	out	dx,al			; 8-columns

	mov	dx,3dah
	in	al,dx
	mov	dx,3c0h
	mov	al,10h
	out	dx,al
	inc	dx
	in	al,dx
	or	al,4
	dec	dx
	out	dx,al

	mov	ah,2
	mov	bh,0
	mov	dh,25
	mov	dl,0
	int	10h

	ret
	endp
ELSE


;	Set 320x200x16 mode for color TI emulation
;
settiscreenmode	proc	near
	call	setpalette
	mov	ax,screenmode
	int	10h		; set 320x200x16, with all 3 pages clear			
	mov	bx,blanktop
	call	dstart
	mov	ax,205h
	mov	dx,3ceh
	out	dx,ax		; select write mode 2

	call	setpalette

	ret
	endp
ENDIF


IFNDEF	TEXTONLY

;	Clear the extra pixels on both sides of the screen.  (EGA screen
;	is 320 pixels wide, but the 99/4A screen is only 256.)
;
;	Also used for switch into text mode.  
;	(Graphics mode is 256 pixels wide, but text is only 240 pixels.)
;

clearsides	proc	near
	push	bp
	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax			; select write mode 2
	mov	ax,0ff08h		
	out	dx,ax			; select all bits for clearing

	xor	al,al

	mov	es,graphseg
	sub	di,di

	mov	cx,(screen0/rowwidth)*rowwidth
	jcxz	cstnotop
cstsolid:				; clear solid lines at top
	mov	ah,es:[di]
	mov	es:[di],al
	inc	di
	loop	cstsolid

cstnotop:
	mov	cx,(260-192)*rowwidth
	mov	di,(screen0-blankspace)+192*rowwidth
csbsolid:				; clear solid lines at bottom
	mov	ah,es:[di]
	mov	es:[di],al
	inc	di
	loop	csbsolid


	mov	di,screen0-blankspace
	mov	dx,192
	mov	bp,36
	mov	cx,4

	textbit
	jz	cssnotext
	
	mov	bp,35
	mov	cx,5
cssnotext:				; clear sides
cssrloop:
	push	cx
	push	di
cssloop:
	mov	ah,es:[di]
	mov	es:[di],al
	mov	es:[di+bp],al
	inc	di
	loop	cssloop
	pop	di
	add	di,rowwidth
	pop	cx
	dec	dx
	jg	cssrloop

	pop	bp
	ret
	endp
ENDIF

IFDEF	TEXTONLY

cleartextsides 	proc near
	mov	es,textseg
	xor	di,di
	mov	dx,24
	xor	ax,ax
ctsrow:
	mov	cx,4
ctsrow1:
	mov	es:[di],ax
	add	di,2
	loop	ctsrow1
	mov	cx,4
	add	di,32*2
ctsrow2:
	mov	es:[di],ax
	add	di,2
	loop	ctsrow2
	dec	dx
	jg	ctsrow

	ret
	endp


clearsides proc near
	textbit
	jz	csnottextout
	call	cleartextsides
csnottextout:
	ret
	endp


clearscreen 	proc near
	mov	es,textseg
	mov	di,blankpage
	mov	cx,40*25*2
	xor	ax,ax
	rep	stosw
	ret
	endp

ELSE

cleartextsides	proc	near
	push	bp
	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax			; select write mode 2
	mov	ax,0ff08h		
	out	dx,ax			; select all bits for clearing
	xor	al,al

	mov	es,graphseg

	mov	di,screen0-blankspace
	mov	dx,192
	mov	bp,36
	mov	cx,4

	textbit
	jz	ctssnotext
	
	mov	bp,35
	mov	cx,5
ctssnotext:				; clear sides

ctssrloop:
	push	cx
	push	di
ctssloop:
	mov	ah,es:[di]
	mov	es:[di],al
	mov	es:[di+bp],al
	inc	di
	loop	ctssloop
	pop	di
	add	di,rowwidth
	pop	cx
	dec	dx
	jg	ctssrloop

	pop	bp
	ret
	endp


clearscreen	proc	near
	mov	dx,3c4h
	mov	ax,0f02h
	out	dx,ax

	mov	dx,3ceh
	mov	ax,005h
	out	dx,ax			; select write mode 2
	mov	ax,0ff08h		
	out	dx,ax			; select all bits for clearing

	xor	ax,ax
	mov	es,graphseg
	mov	di,blankpage

	mov	cx,200*rowwidth
	shr	cx,1

	rep	stosw

	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax

	ret
clearscreen	endp


ENDIF

;	Initialize video
;
;	Call only once
;
initvideo	proc	near
	pusha

	call	setuptranslate
	call	makecharoffsets
	mov	ax,1200h
	mov	bl,31h	
	int	10h			; turn on default palette loading
	mov	ax,3
	int	10h
	call	completeupdate
	popa
	ret
	endp


;	Set up variables and interpretations of VREGS
;	for each mode
;	(Call from HANDLEVDPREG)
;	VREGS=registers
;	Changes colors,patterns and handlers
;	
;	Only changes to registers 0,1,3,4 will cause
;	these routines to be called


IFNDEF	TEXTONLY

handlebitmap	proc	near

	mov	screensize,768			
	mov	colorssize,6144
	mov	patternssize,6144
	mov	spritessize,128
	mov	screenwidth,32
	mov	charhandler,offset bitchar
	mov	patternhandler,offset bitpattern	
	mov	colorhandler,offset bitcolor
	mov	spritehandler,offset graphicssprite
	mov	screenhandler,offset bitscreen
	mov	sprpathandler,offset graphicssprpat
	mov	colorbasehandler,offset bitcolorbase
	mov	pattbasehandler,offset bitpattbase
	mov	mode,mbitmap		

	ret
	endp


;
handlegraphics	proc	near

	mov	screensize,768
	mov	colorssize,32
	mov	patternssize,2048
	mov	spritessize,128
	mov	charhandler,offset graphicschar
	mov	patternhandler,offset graphicspattern	
	mov	colorhandler,offset graphicscolor
	mov	spritehandler,offset graphicssprite
	mov	screenhandler,offset graphicsscreen
	mov	sprpathandler,offset graphicssprpat
	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase
	mov	mode,mgraphics

	ret
	endp


;
handletext	proc	near

	mov	screensize,960
	mov	colorssize,0ffffh
	mov	patternssize,2048
	mov	spritessize,0ffffh
	mov	screenwidth,40
	mov	charhandler,offset textchar
	mov	patternhandler,offset textpattern	
	mov	colorhandler,offset textcolor
	mov	spritehandler,offset textsprite		; must be NULL!
	mov	screenhandler,offset textscreen
	mov	sprpathandler,offset textsprpat
	mov	mode,mtext

	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase

	ret
	endp



;
handlemulti	proc	near

	mov	screensize,768
	mov	colorssize,0ffffh
	mov	patternssize,1536
	mov	spritessize,128
	mov	screenwidth,32
	mov	charhandler,offset multichar
	mov	patternhandler,offset multipattern	
	mov	colorhandler,offset multicolor
	mov	spritehandler,offset graphicssprite
	mov	screenhandler,offset multiscreen
	mov	sprpathandler,offset graphicssprpat
	mov	mode,mmulticolor

	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase

	ret
	endp

ELSE

handlebitmap	proc	near
	mov	screensize,768			
	mov	colorssize,0ffffh
	mov	patternssize,0ffffh
	mov	spritessize,0ffffh
	mov	screenwidth,32
	mov	charhandler,offset textonlyhandler
	mov	patternhandler,offset textonlyhandler
	mov	colorhandler,offset textonlyhandler
	mov	spritehandler,offset textonlyhandler
	mov	screenhandler,offset textonlyscreen
	mov	sprpathandler,offset textonlyhandler
	mov	colorbasehandler,offset textonlyhandler
	mov	pattbasehandler,offset textonlyhandler
	mov	mode,mbitmap		

	ret
	endp


;
handlegraphics	proc	near

	mov	screensize,768
	mov	colorssize,32
	mov	patternssize,2048
	mov	spritessize,128
	mov	screenwidth,32
	mov	charhandler,offset graphicschar
	mov	patternhandler,offset graphicspattern	
	mov	colorhandler,offset graphicscolor
	mov	spritehandler,offset textonlyhandler
	mov	screenhandler,offset textgraphicsscreen
	mov	sprpathandler,offset textonlyhandler

	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase
	mov	mode,mgraphics

	ret
	endp


;
handletext	proc	near

	mov	screensize,960
	mov	colorssize,0ffffh
	mov	patternssize,2048
	mov	spritessize,0ffffh
	mov	screenwidth,40
	mov	charhandler,offset textchar
	mov	patternhandler,offset textpattern	
	mov	colorhandler,offset textonlyhandler
	mov	spritehandler,offset textonlyhandler	; must be NULL!
	mov	screenhandler,offset textonlyscreen
	mov	sprpathandler,offset textonlyhandler
	mov	mode,mtext

	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase

	ret
	endp



;
handlemulti	proc	near

	mov	screensize,768
	mov	colorssize,0ffffh
	mov	patternssize,0ffffh
	mov	spritessize,0ffffh
	mov	screenwidth,32
	mov	charhandler,offset textonlyhandler
	mov	patternhandler,offset textonlyhandler
	mov	colorhandler,offset textonlyhandler
	mov	spritehandler,offset textonlyhandler
	mov	screenhandler,offset textonlyhandler
	mov	sprpathandler,offset textonlyhandler
	mov	mode,mmulticolor

	mov	colorbasehandler,offset normcolorbase
	mov	pattbasehandler,offset normpattbase

	ret
	endp








ENDIF


;	Figure out the base address of the color and pattern tables
;	in "normal" modes.
;
normcolorbase	proc	near
	push	ax
	mov	al,vregs+3
	xor	ah,ah
	shl	ax,6
	and	ax,maxvdp
	mov	colors,ax
	pop	ax
	ret
	endp


normpattbase	proc	near
	push	ax
	mov	al,vregs+4
	xor	ah,ah
	shl	ax,11
	and	ax,maxvdp
	mov	patterns,ax
	pop	ax
	ret
	endp


;	Figure out the base address of the color and pattern tables
;	in bitmap modes.
;
bitcolorbase	proc	near
	push	ax
	mov	al,byte ptr vregs+3
	mov	colors,0
	test	al,80h
	jz	bcb00
	mov	colors,2000h
bcb00:
	and	al,not 80h
	xor	ah,ah
	shl	ax,6
	or	ax,3fh
	mov	bitcolormask,ax
	pop	ax
	ret
	endp


bitpattbase	proc	near
	push	ax
	push	bx
	mov	al,vregs+4			; patterns
	mov	patterns,0
	test	al,4
	jz	bpb10
	mov	patterns,2000h
bpb10:
	and	al,not 4
	xor	ah,ah
	shl	ax,11
	MOV	BX,BITCOLORMASK
	AND	BX,7FFH
	OR	AX,BX
	mov	bitpattmask,ax
	pop	bx
	pop	ax
	ret
	endp


;==========================================================================
;	Update a VDP write-only register
;	VADDR=value
;==========================================================================

hvr_sprites	equ	1		; means DO redraw sprites
hvr_mode	equ	2		; means IS a mode change
hvr_redraw	equ	4		; means MUST redraw screen

handlevdpreg	proc	near
	pusha
	push	es
	mov	al,byte ptr vaddr	; AL = reg value
	sub	bh,bh
	mov	bl,byte ptr vaddr+1	; BL = reg#
	and	bl,15
	and	al,[vregsmasks+bx]
	cmp	al,[vregs+bx]
	jne	hvrgchange
	jmp	hvrgnochange		; it hasn't even changed!

hvrgchange:

IFDEF	DEMO
	test	stateflag,demoing
	jz	hvrgnodemo
	call	dvdpreg
hvrgnodemo:

ENDIF

	mov	ah,mode
	mov	oldmode,ah
	mov	ah,[vregs+bx]
	mov	oldreg,ah
	mov	[vregs+bx],al		; update value
	add	bx,bx
	xor	ah,ah
	call	[word ptr cs:vdpregs+bx] ; but this does the work

	push	ax
	test	al,hvr_mode+hvr_redraw
	jnz	hvrgmode
	jmp	hvrgtestsprites

;	Figure what mode is represented and call the handlers for
;	each mode.
;
hvrgmode:
	bitmapbit
	jz	hvrgtesttext
	jmp	hvrgsetbitmap
hvrgtesttext:
	textbit
	jz	hvrgtestmulti
	jmp	hvrgsettext
hvrgtestmulti:
	multibit
	jz	hvrgsetgraphics
	jmp	hvrgsetmulti

hvrgsetgraphics:
	call	handlegraphics
	jmp	hvrgsetredraw
hvrgsetbitmap:
	call	handlebitmap
	jmp	hvrgsetredraw
hvrgsettext:
	call	handletext
	jmp	hvrgsetredraw
hvrgsetmulti:
	call	handlemulti

hvrgsetredraw:
	call	colorbasehandler
	call	pattbasehandler

	mov	statechanged,0ffh
	mov	videodatachanged,0ffh

	cmp	ontiscreen,0
	jz	hvrgtestsprites		; don't redraw while debugging!

	cmp	mode,mtext
	jne	hvrgnosides
	call	cleartextsides

hvrgnosides:
	cmp	blank,0
	jnz	hvrgblankit
	
	call	setcompleteupdate
	call	setfg
	call	screenhandler
	mov	bx,0
	jmp	hvrgredraw

hvrgblankit:
	mov	bx,blankpage

hvrgredraw:
	call	dstart
	pop	ax
	jmp	hvrgsegments		; sprites/redraw already done by mode


hvrgtestsprites:
	pop	ax
	test	al,hvr_sprites
	jz	hvrgsegments

	call	setupdatesprites
	
hvrgsegments:
;	This will set up the SEGMENTS list which will be used in
;	determining what graphics part an address is in.

	lea	bx,segments
	mov	ax,screen
	mov	[bx],ax
	add	ax,screensize
	mov	[bx+2],ax
	mov	ax,charhandler
	mov	[bx+4],ax
	add	bx,6

	mov	ax,patterns
	mov	[bx],ax
	add	ax,patternssize
	mov	[bx+2],ax
	mov	ax,patternhandler
	mov	[bx+4],ax
	add	bx,6

	cmp	colorssize,0ffffh
	je	hvro3

	mov	ax,colors
	mov	[bx],ax
	add	ax,colorssize
	mov	[bx+2],ax
	mov	ax,colorhandler
	mov	[bx+4],ax
	add	bx,6

hvro3:
	cmp	spritessize,0ffffh
	je	hvro4

	mov	ax,sprites
	mov	[bx],ax
	add	ax,spritessize
	mov	[bx+2],ax
	mov	ax,spritehandler
	mov	[bx+4],ax
	add	bx,6

hvro4:
	cmp	spritessize,0ffffh
	je	hvro5

	mov	ax,sprpat
	mov	[bx],ax
	add	ax,2048
	mov	[bx+2],ax
	mov	ax,sprpathandler
	mov	[bx+4],ax
	add	bx,6

hvro5:
	mov	word ptr [bx],0ffffh
	call	sortseglist

	mov	videodatachanged,0ffh	; this stuff changed!

hvrgnochange:
	pop	es
	popa
	ret
	endp


;	Sort the SEGMENTS list by address ASCENDING
;
;	Input:		BX points to the end of the list
;
sortseglist 	proc	near
	push	ax
	push	bx
	push	si
	push	di
	push	bp
	mov	bp,bx

	lea	si,segments

sslfirst:
	cmp	si,bp
	jae	sslout

	mov	bx,si			; BX points to current entry
	mov	di,si			; DI points to "min" entry
sslfindmax:
	cmp	bx,bp
	jae	sslswap

	mov	ax,[bx]
	cmp	ax,[di]
	ja	sslless
	mov	di,bx			; entry at BX is higher

sslless:
	add	bx,6
	jmp	sslfindmax

sslswap:
	mov	ax,[si]			; swap entries
	xchg	[di],ax
	mov	[si],ax

	mov	ax,[si+2]
	xchg	[di+2],ax
	mov	[si+2],ax

	mov	ax,[si+4]
	xchg	[di+4],ax
	mov	[si+4],ax

	add	si,6
	jmp	sslfirst

sslout:
	pop	bp
	pop	di
	pop	si
	pop	bx
	pop	ax
	ret
	endp


;==========================================================================
;	Handle changes to VDP write-only registers.
;
;==========================================================================

vdpregs	dw	reg0,reg1,reg2,reg3,reg4,reg5,reg6,reg7
	dw	regn,regn,regn,regn,regn,regn,regn,regn

;	Register 0
;
;	This register controls BITMAP and VIDEO IN.
;	Only emulate BITMAP bit.
;
reg0	proc	near
	mov	al,oldreg
	and	al,2			; bitmap bit
	mov	ah,vregs
	and	ah,2
	cmp	al,ah
	mov	al,0
	je	reg0no
	mov	al,hvr_mode
reg0no:
	ret
	endp


;	Register 1
;
;	This register controls VIDEO SIZE, BLANK, VIDEO INTERRUPT,
;	TEXT MODE, MULTICOLOR MODE, and SPRITE SIZES.
;
;	Ignore VIDEO SIZE and VIDEO INTERRUPT.
;
reg1	proc	near
	mov	vdpramsize,3fffh

	mov	statechanged,0
	mov	ah,blank
	mov	blank,0			; NOT blank
	test	al,blankbit
	jnz	reg1notblank
	mov	blank,1			; blank

reg1notblank:
	xor	ah,blank
	jz	reg1nostatechange
	mov	statechanged,hvr_redraw

reg1nostatechange:
	test	al,80h			; memory size bit
	jnz	reg1_16k
	mov	vdpramsize,0fffh

reg1_16k:    				; ignore interrupt bit
	mov	ah,spritemag
	mov	ospritemag,ah
	mov	ah,spritechars
	mov	ospritechars,ah
	mov	ah,spritesize
	mov	ospritesize,ah
	mov	ah,totspritesize
	mov	ototspritesize,ah	; save old values for redraw

	and	al,3			; mode bits handled at updatemode
	call	getspritesizes
	
	mov	al,oldreg
	and	al,3
	mov	ah,vregs+1
	and	ah,3
	cmp	ah,al			; compare old/new sprite parts

	mov	al,statechanged
	je	reg1nosprites
;;	or	al,hvr_sprites
	or	al,hvr_redraw		; KLUDGE until we figger out why
					; changing sizes AND moving sprites
					; causes goofups

reg1nosprites:

	mov	bl,oldreg
	and	bl,10h+8h
	mov	ah,vregs+1
	and	ah,10h+8h
	
	cmp	ah,bl
	je	reg1nomode
	or	al,hvr_mode

reg1nomode:
	ret
	endp


;	Register 2
;
;	Register*>400 = screen start address
;
reg2	proc	near
	shl	ax,10
	and	ax,maxvdp
	mov	screen,ax
	mov	al,hvr_redraw
	ret
	endp


;	Register 3
;
;	Register*>40 = color table base OR
;	more complex thing in bitmap mode.
;
reg3	proc	near
	call	colorbasehandler
	mov	al,hvr_redraw
	ret
	endp


;	Register 4
;
;	Register*>800 = pattern base table OR
;	more complex thing in bitmap mode.
;
reg4	proc	near
	call	pattbasehandler
	mov	al,hvr_redraw
	ret
	endp


;	Register 5
;
;	Register*>80 = sprite table base 
;
reg5	proc	near
	shl	ax,7	
	and	ax,maxvdp
	mov	sprites,ax
	mov	al,hvr_sprites
	ret
	endp


;	Register 6
;
;	Register*>800 = sprite pattern table
;
reg6	proc	near
	shl	ax,11
	and	ax,maxvdp
	mov	sprpat,ax
	mov	al,hvr_sprites
	ret
	endp


;	Register 7
;
;	Register = colors for foreground (text mode) and background
;
reg7	proc	near
	mov	bl,al
	shr	al,4
	mov	textfg,al		; AL= FG
	and	bl,15			; BG= BG
	jnz	reg7noblack
	mov	bl,1			; BG=0 -> black

reg7noblack:
	cmp	textfg,0		; foreground blank?
	jnz	reg7nofgbg
	mov	textfg,bl 		; make it background color

reg7nofgbg:
	xor	bh,bh
	mov	al,[pal16+bx]
	mov	pal16,al

	cmp	ontiscreen,0
	jz	reg7noup

	call	setborder
	call	setfg
	call	clearscreen

reg7noup:
	mov	al,0
	ret
	endp


;	Invalid register
;
;
regn	proc	near
	mov	al,0
	ret
	endp


;	Figure new sprite size variables.
;	AL=low bits of VREG #1
;
getspritesizes	proc	near
	or	al,al
	jnz	gss01

;	Mag=0, Size=0

	mov	spritemag,1
	mov	spritechars,1
	mov	spritesize,8
	mov	totspritesize,1
	jmp	gssout

gss01:
	cmp	al,1
	jne	gss02

;	Mag=1, Size=0

	mov	spritemag,2
	mov	spritechars,1
	mov	spritesize,8
	mov	totspritesize,2
	jmp	gssout

gss02:
	cmp	al,2
	jne	gss03

;	Mag=0, Size=1

	mov	spritemag,1
	mov	spritechars,4
	mov	spritesize,16
	mov	totspritesize,2
	jmp	gssout

gss03:

;	Mag=1, Size=1

	mov	spritemag,2
	mov	spritechars,4
	mov	spritesize,16
	mov	totspritesize,4

gssout:
	ret
	endp


;=========================================================================
;
;	Update a byte in VDP RAM
;	VADDR=address
;	AL=value
;
;=========================================================================

;	Accessory entry point, used to FORCE an update.
;

handlevdp1 proc	near
	push	ax
	push	bx
	push	cx
	MOV	BX,VADDR
	jmp	hvchanged
	endp

;	Ordinary entry point.
;
;	If the byte hasn't changed, don't do anything.
;

handlevdp	proc	near
	push	ax
	push	bx
	push	cx

	mov	bx,vaddr
	cmp	[bx],al
	jne	hvchanged

	jmp	handlevdpout
hvchanged:
	mov	[bx],al

IFDEF	DEMO
	test	stateflag,demoing
	jz	hvnowrite
     	call	dvdpdata
hvnowrite:

ENDIF

	cmp	blank,0
	jne	handlevdpout		; yes, it is blank, <> 0.


	lea	bx,segments		; sorted forwards
	mov	ax,vaddr
nextsegment:
	mov	cx,[bx]
	cmp	cx,0ffffh
	je	endofsegment

	cmp	ax,cx
	jb	endofsegment
	cmp	ax,[bx+2]
	jae	notthissegment

	mov	vdprestcounter,0	
	push	bx
	call	[bx+4]			; update this stuff
	mov	videodatachanged,0ffh
	pop	bx			; in case routines changed it
notthissegment:
	add	bx,6
	jmp	nextsegment
endofsegment:

handlevdpout:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	********************************************************
;	********************************** VDP HANDLING ROUTINES
;	********************************************************

;
;	These may only change BX indiscriminately; others must
;	be saved.
;

;	Update char in CHANGES
;
graphicschar	proc	near
	mov	bx,vaddr
	sub	bx,screen
	mov	byte ptr [changes+bx],0ffh
	ret
	endp

			
;	Update color in CHANGES
;
graphicscolor	proc	near
	mov	bx,vaddr
	sub	bx,colors
	shl	bx,3			; BX=low char to change

	mov	word ptr [charchanges+bx],0ffffh
	mov	word ptr [charchanges+bx+2],0ffffh
	mov	word ptr [charchanges+bx+4],0ffffh
	mov	word ptr [charchanges+bx+6],0ffffh

	ret
	endp	


;	Update pattern in CHANGES
;
graphicspattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BL=char # that changed
	mov	[charchanges+bx],0ffh

	ret
	endp


;	Update sprite in SPRITECHANGES
;
graphicssprite	proc	near
	mov	bx,vaddr
	sub	bx,sprites		; sprite entry changed
	shr	bx,2
	mov	byte ptr [spritechanges+bx],0ffh
	ret
	endp


;	Update sprite pattern in SPRITECHARCHANGES
;
graphicssprpat	proc	near
	mov	bx,vaddr
	sub	bx,sprpat
	shr	bx,3
	mov	byte ptr [spritecharchanges+bx],0ffh
	ret
	endp

;--------------------------------------------------

;	Update text char in CHANGES
;
textchar	proc	near
	mov	bx,vaddr
	sub	bx,screen
	mov	byte ptr [changes+bx],0ffh
	ret
	endp

			
;	Update text color???
;
textcolor	proc	near
	ret
	endp	


;	Update text pattern in CHANGES
;
textpattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BL=char # that changed
	MOV	BYTE PTR [charCHANGES+BX],0FFH
	ret
	endp


;	Update text sprite???
;
textsprite	proc	near
	ret
	endp


;	Update text sprite pattern???
;
textsprpat	proc	near
	ret
	endp

;-----------------------------------------------------------

;	Update bitmap char in CHANGES
;
bitchar	proc	near
	mov	bx,vaddr
	sub	bx,screen
	mov	byte ptr [changes+bx],0ffh
	ret
	endp


;	Update bitmap color in CHANGES
;
bitcolor	proc	near
	mov	bx,vaddr
	sub	bx,colors
	shr	bx,3			; BL = # char
	mov	byte ptr [charchanges+bx],0ffh
	ret	
	endp	


;	Update pattern in CHANGES
;
bitpattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BH = screen section, BL = # char
	mov	byte ptr [charchanges+bx],0ffh
	ret
	endp

;---------------------------------------------------------------

;	Update multicolor char in CHANGES
;
multichar	proc	near
	mov	bx,vaddr
	sub	bx,screen
	mov	byte ptr [changes+bx],0ffh
	ret
	endp


;	Update multicolor mode color??
;
multicolor	proc	near
	ret
	endp

			
;	Update pattern (COLOR) in CHANGES
;
multipattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BL=char # that changed

	mov	[charchanges+bx],0ffh
	ret
	endp


;=========================================================================
;	Optimization of 99/4A ROM memory-move routine.
;
;=========================================================================


;	DOTRANSFER --	Optimized GPL block-move instruction
;
;
dotransfer	proc	near
	;	Read=BX
	;	Write=SI
	nop				; so we can breakpoint here

	READREG	R1,ax
	mov	rr1,ax
	READREG R2,ax
	mov	rr2,ax
	READREG	R6,ax
	mov	rr6,ax
	READREG	R7,ax
	mov	rr7,ax
	READREG	R8,ax
	mov	rr8,ax

dtwl:
	cmp	rr8,0
	jnz	dtwhile
	jmp	dtout
dtwhile:
	mov	bx,rr1
	push	bx
	cmp	rr6,664h
	jnz	dtnotrvdp
	mov	vaddr,bx
	mov	bx,8800h
	jmp	dtoutr
dtnotrvdp:
	cmp	rr6,672h
	jnz	dtnotrgpl
	mov	gaddr,bx
	mov	bx,9800h
	jmp	dtoutr
dtnotrgpl:
	cmp	rr6,660h
	jne	dtnotrcpu
	jmp	dtoutr
dtnotrcpu:
	pop	bx
	jmp	dtexit	
dtoutr:
	READBYTE bx,al

	WRITEREGBYTE R11,al

	pop	bx
	inc	bx
	mov	rr1,bx


	mov	si,rr2
	push	si

	READREGBYTE R11,al

	cmp	rr7,682h
	jne	dtnotwcpu

	WRITEBYTE si,al

	jmp	dtwout
dtnotwcpu:
	cmp	rr7,686h
	jne	dtnotwgpl
	mov	gaddr,si
	mov	si,9c00h
	WRITEBYTE si,al

	jmp	dtwout
dtnotwgpl:
	cmp	rr7,698h
	je	dtwvdpreg2
	jmp	dtnotwvdpreg
dtwvdpreg2:	; 0698
	push	bx
	mov	bx,si		; BX=R2

	READREGBYTE R14,ah
	cmp	ah,bl

	pop	bx
	jne	dtwvdp		; 69c
	push	bx
	mov	bl,al		; BL=R11

	READREGBYTE R14,ah
	test	ah,8

	jz	dtwvdpreg	; 6a2
	or	bl,80h		; 06a4
dtwvdpreg:     	
	WRITEBYTE 83D4h,bl

	pop	bx
dtwvdp:	 	; 6AC
	mov	vwrite,0
	WRITEBYTE 8C02h,al

	mov	ax,si
	or	al,80h		; 6ae
	WRITEBYTE 8C02h,al

	jmp	dtwout
dtnotwvdpreg:
	cmp	rr7,6bah
	jne	dtnotwvdp
	or	si,4000h
	mov	vaddr,si
	mov	vwriteoffset,0   	; ding!

	WRITEBYTE 8c00h,al

	jmp	dtwout
dtnotwvdp:
	pop	si
	jmp	dtexit
dtwout:
	pop	si
	inc	si
	mov	rr2,si
	
	dec	rr8
	jmp	dtwl

dtout:
	or	stat,20h
	mov	IP,83eh
	jmp	dtexitnow
dtexit:
	mov	IP,65eh
dtexitnow:

	mov	ax,rr1
	WRITEREG R1,ax
	mov	ax,rr2
	WRITEREG R2,ax
	mov	ax,rr6
	WRITEREG R6,ax
	mov	ax,rr7
	WRITEREG R7,ax
	mov	ax,rr8
	WRITEREG R8,ax

	ret
	endp


;========================================================================
;	ROM routine at >5AC
;
;	Fill screen with char
;
;	R5 = char
;	R7 = count
;========================================================================


screenfill proc	near
	mov	vaddr,0
	READREGBYTE R5,al
	READREG	R7,cx
sfloop:
	WRITEBYTE 8c00h,al
	loop	sfloop

	WRITEREG R7,0
	mov	IP,5B4H			; skip rest of loop

	ret
	endp



IFDEF	TEXTONLY
	include	grtext.inc
ENDIF


	end
