; *********************************************
; TI Emulator! Floppy Drive Controller emulator
; *********************************************
; 1/7/1995 by Edward Swartz
; *********************************************

;	included in FILES.ASM


;
;	This include file contains the code for emulating the FDC chip used
;	in TI controller cards.
;	Disks-on-a-disk and TI floppies will be supported with this.
;
;       Disks-on-a-disk will be simply collections of 256-byte sectors
;	in a block -- nothing else will be inferred.
;	
;       The way a floppy locates sectors is by seeking directly to it.
;
;	There is ambiguity in the size of a disk during formatting.
;	We DO know how many tracks there will be, but the number of sectors
;	in those tracks... hmm...  (1) The MFM double-density format gives 
;	different leading bytes than the FM.  That can help distinguish
;	9 from 18 sectors/track.  
;	
;	PERHAPS we can kludge it almighty, and print an error message
;	when a write-track command is received, and ask the user to
;	supply explicit parameters.
;


	.data


DSKbuffersize	equ	256

DSKnum		db	0		; current drive #
DSKhold		db	0		; holding for data?
DSKlastbyte	db	0		; last byte written to WDDATA
DSKbufoffs	dw	0		; offset into i/o buffer
DSKbuffer	db	DSKbuffersize dup (?)  ; buffered input/output

DSKbuflen	dw	0		; internal use only, max length
DSKbufpos	dw	0		; buffered input pointer
					; DSKbuffer is only a window
DSKwritten	dw	0		; how much written?


DSKhandle	dw	0		; handle for "disk"
DSKdensity	db	0		; density of the "disk", 9 or 18
DSKtracksside	db	0		; how many tracks per side?
DSKtrackoffset	db	0		; last seeked track
DSKtracksize	dw	0		; max # bytes in a track, 
					; set by opendisk
DSKtrackbyteoffset dw	0		; offset into track, bytes


DSKcommand	db	0		; command being executed

DSKtrack	db	0		; current track
DSKsector	db	0		; current sector
DSKside		db	0		; which side?

DSKstatus	db	0		; current status
DSKformatsaving	db	0		; are we saving data in format?
DSKpostponed	db	0		; waiting for input

fdc_seekhome    equ     not 0f5h        ; FDC commands
fdc_seek	equ	not 0e1h
fdc_stepin	equ	not 0a5h

fdc_readsector	equ	not 077h
fdc_writesector	equ	not 057h

fdc_readIDmarker equ	not 03fh

fdc_interrupt	equ	not 02fh

fdc_formattrack	equ	not 00bh

wtcmdtable	db	fdc_seekhome
		dw	seekhome
		db	fdc_seek
		dw	seek
		db	fdc_stepin
		dw	stepin
		db	fdc_readsector
		dw	readsector
		db	fdc_writesector
		dw	writesector
		db	fdc_readIDmarker
		dw	readIDmarker
		db	fdc_interrupt
		dw	interrupt
		db	fdc_formattrack
		dw	formattrack
		db	0

fdc_READY	equ	80h
fdc_WRITEPROT	equ	40h
fdc_BADRECORD	equ	10h
fdc_CRCERR	equ	08h
fdc_LOSTDATA	equ	04h
fdc_TRACK0	equ	04h
fdc_BUSY	equ	01h

fdc_WTDATA	equ	5ffeh		; memory-mapped ports
fdc_RDDATA	equ	5ff6h		; memory-mapped ports

fdc_secrec	struc
	idmark	db	0feh		; ID address mark
	track	db	?		; 0-39, or 0-79
	side	db	?		; 0-1
	sector	db	?		; 0-8, or 0-17
	seclen	db	?		; 128<<seclen == length
	datamark db	0fbh		; Data marker
	ends

DSKcursec fdc_secrec	<>		; current sector we're pointing to


disk_SD	equ	0			; we have 9 sectors/track
disk_DD	equ	1			; we have 18 sectors/track


disk1name	db	'DISK0001.DSK',0	; default names
disk2name	db	'DISK0002.DSK',0	; for emulated disks
disk3name	db	'DISK0003.DSK',0	; when user doesn't say
diskpath	db	64 dup (0)		; path for disk images

	.code

;====================================================================
;	HANDLEFDCREAD --	read a byte from the FDC controller
;
;	Inputs:		DI=addr
;	Outputs:	AL=~value
;
hfrvector dw	rrdstat,rrtaddr,rrsaddr,rrddata,rwtcmd,rwtaddr,rwsaddr,rwtdata

handlefdcread	proc	near
	push	di
	sub	di,5ff0h
	and	di,not 1
	mov	ax,[cs:word ptr hfrvector+di]
	call	ax			; returns AX=value
	not	al
	pop	di
	ret
	endp


;======================================================================
;	HANDLEFDCWRITE --	Write a byte to the FDC controller
;
;	Inputs:		DI=addr, AL=value
;
hfwvector dw	wrdstat,wrtaddr,wrsaddr,wrddata,wwtcmd,wwtaddr,wwsaddr,wwtdata

handlefdcwrite proc	near
	push	di
	not	al
	sub	di,5ff0h
	and	di,not 1
	mov	di,[cs:word ptr hfwvector+di]
	call	di
	pop	di
	ret
	endp

;==================================================================

wrdstat:
wrtaddr:
wrsaddr:
wrddata:
rwtcmd:
rwtaddr:
rwsaddr:
rwtdata:
	mov	al,0ffh
	ret


rrdstat	proc	near
	mov	al,DSKstatus
	xor	al,80h
	ret
	endp


rrtaddr	proc	near
	mov	al,DSKtrack
	ret
	endp

rrsaddr proc	near
	mov	al,DSKsector
	ret
	endp


rrddata	proc	near
	push	bx
	push	cx
	cmp	DSKhold,0
	jz	rrdgarbage		; ??? whaddaya want?

	mov	al,DSKcommand

	cmp	al,fdc_readsector
	je	rrdbuff			; sectors are buffered
	cmp	al,fdc_readIDmarker
	je	rrdbuff			; ID address is buffered

	jmp	rrdgarbage		; others are unsupported


rrdbuff:
	mov	bx,DSKbufoffs
	mov	cx,DSKbufpos
	mov	al,[bx+DSKbuffer]
	inc	cx
	cmp	cx,DSKbuflen
	jb	rrdnotexhausted
	mov	cx,0			; reset if overreading
	xor	bx,bx
	jmp	rrddone
rrdnotexhausted:

	inc	bx
	cmp	bx,DSKbuffersize
	jb	rrddone

	call	fillbuffer		; DSKcommand will say if we need to
rrddone:
	mov	DSKbufoffs,bx
	mov	DSKbufpos,cx
	jmp	rrdout

rrdgarbage:
	mov	al,0			; ???

rrdout:
	pop	cx
	pop	bx

	ret
	endp


;	FILLBUFFER --	To conserve memory, DSKbuffer doesn't have as many
;			bytes as it may need (like with 1024-byte sectors)
;			so we use a buffered approach when reading.
;
;	Inputs:		BX=DSKbufoffs, CX=DSKbufpos
;	Outputs:	BX=DSKbufoffs, CX=DSKbufpos
;
fillbuffer proc near
	push	ax
	push	bx
	push	dx
	push	cx

	mov	cx,DSKbuflen
	sub	cx,DSKbufpos		; CX=# bytes left
	cmp	cx,DSKbuffersize
	jb	flblower
	mov	cx,DSKbuffersize
flblower:
	jcxz	flbout

	mov	bx,DSKhandle
	or	bx,bx
	jz	flbclear		; sigh...
	mov	ah,3fh
	lea	dx,DSKbuffer
	int	21h			; read it, baby!
	jnc	flbtestoverrun

	or	DSKstatus,fdc_CRCERR	; assume the worst

flbtestoverrun:
	cmp	ax,cx
	je	flbclear

	or	DSKstatus,fdc_LOSTDATA

flbclear:
	xor	bx,bx			; clear out pointer
flbout:
	pop	cx
	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;=====================================================================

wwtaddr proc	near
	mov	DSKtrack,al
	ret
	endp


wwsaddr	proc	near
	mov	DSKsector,al
	ret
	endp





wwtdata proc	near
	push	ax
	push	bx
	push	cx

	cmp	DSKhold,0
	jnz	wwdvalid
	jmp	wwdkeep

wwdvalid:
	cmp	DSKcommand,fdc_writesector
	je	wwdbuff
	cmp	DSKcommand,fdc_formattrack
	je	wwdtrack

	jmp	wwdkeep

;
;	FORMAT TRACK...  we must ignore a ton of crap
;
;
wwdtrack:
	cmp	DSKformatsaving,0	; are we weeding out or saving?
	jnz	wwdbuff			; we're saving something...

	cmp	al,0a1h			; DD code (?)
	je	wwdfgotdd
	cmp	al,0feh			; ID address mark follows
	je	wwdignore
	cmp	al,0fbh			; Sector data follows
	je	wwdfgetsec
	jmp	wwdignore		; what IS this crap?

wwdfgotdd:
	mov	DSKdensity,18		; got a DD code??
	jmp	wwdignore

wwdfgetid:
	push	ax
	xor	ax,ax

	jmp	wwdfsettosave

wwdfgetsec:

	push	ax
	mov	bx,DSKbufpos	 	
	mov	cl,1
	mov	ax,128
	shl	ax,cl			; # bytes == 128<<sectorsizecode

wwdfsettosave:
	mov	DSKbuflen,ax
	mov	DSKwritten,0
	xor	bx,bx
	xor	cx,cx
	mov	DSKformatsaving,1	; and, we'll save the current byte
	pop	ax
	jmp	wwdout


wwdbuff:
	mov	bx,DSKbufoffs
	mov	cx,DSKbufpos
	mov	[bx+DSKbuffer],al
	inc	bx
	inc	cx
	cmp	cx,DSKbuflen		; # wanted
	jb	wwdnotfull

	call	flushbuffer		; we have all the bytes
	jmp	wwdout

wwdnotfull:
	cmp	bx,DSKbuffersize
	jb	wwdout

	call	flushbuffer		; we filled the buffer

wwdout:
	mov	DSKbufoffs,bx
	mov	DSKbufpos,cx
	jmp	wwdignore

wwdkeep:
	mov	DSKlastbyte,al		; save last byte (for seek)
wwdignore:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	FLUSHBUFFER --	We're using buffers to store data sent into the FDC.
;			To save memory, there isn't the maximum space stored.
;			This function flushes the buffer.
;
;	Inputs:		BX=DSKbufoffs
;	Outputs:	BX=DSKbufoffs
;
flushbuffer proc near
	push	ax
	push	cx
	push	dx


	mov	cx,bx			; CX=# bytes to write
	jcxz	fub00

	add	DSKwritten,cx
	mov	ax,DSKwritten
	cmp	ax,DSKbuflen
	jb	fubnotall

	mov	DSKformatsaving,0	; we filled the request

fubnotall:
	mov	bx,DSKhandle
	or	bx,bx
	jz	fub00			; no disk in drive... sigh

	mov	ah,40h
	lea	dx,DSKbuffer
	int	21h			; write it!
	jnc	fubcheckoverrun

	or	DSKstatus,fdc_BADRECORD	; too far out, dude

fubcheckoverrun:
	cmp	ax,cx
	je	fub00

	or	DSKstatus,fdc_LOSTDATA

fub00:
	xor	bx,bx			; buffer be empty

	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	FLUSHBUFFEROUT --	External-call of flushbuffer
;
;
flushbufferout proc	near
	push	bx

	mov	bx,DSKbufoffs
	call	flushbuffer
	mov	DSKbufoffs,BX

	pop	bx
	ret
	endp



;	WWTCMD --	Write command to FDC
;			
wwtcmd	proc	near
	push	bx

	mov	DSKbufoffs,0		; start over
	mov	DSKbufpos,0		; nothing in buffer
	mov	DSKstatus,0		; clear errors
	mov	DSKpostponed,0		; are we waiting for data?

	lea	bx,wtcmdtable
wwc:
	cmp	byte ptr [bx],0
	je	wwcerr
	cmp	[bx],al
	je	wwcmatch
	add	bx,3
	jmp	wwc
wwcerr:
	jmp	wwcout
wwcmatch:
	mov	DSKcommand,al		; save current command
	mov	bx,[bx+1]
	call	bx
wwcout:
	pop	bx
	ret
	endp

;******************************************************

;	GETFILESIZE --	Returns file size of the DSKhandle file
;			without changing seek ptr.
;
;	Outputs:	DX:AX
;
getfilesize proc near
	push	bx
	push	cx
	push	si
	push	di
	
	xor	si,si			; filesize is 0
	xor	di,di

	mov	bx,DSKhandle
	or	bx,bx
	jz	gfsno			; no file open?

	mov	ah,42h
	mov	al,01h
	xor	cx,cx
	xor	dx,dx
	int	21h			; get current pointer
	jc	gfsno			; big err means no disk

	push	dx			; MSW
	push	ax			; LSW

	mov	ah,42h
	mov	al,02h
	xor	cx,cx
	xor	dx,dx
	int	21h			; seek to end, get size

	mov	si,ax			
	mov	di,dx			; save file length

	pop	dx
	pop	cx			; restore pointerr

	mov	ah,42h
	mov	al,00h
	int	21h			; point back to original pointer

gfsno:
	mov	ax,si			; return size
	mov	dx,di

	pop	di
	pop	si
	pop	cx
	pop	bx
	ret
	endp



;	CHECKDISK --	Check to see if the "disk is in the drive"
;			1)  Is file open?
;
;	Outputs:	Set FDC_READY if so
;
checkdisk proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	bx,DSKhandle
	or	bx,bx
	jz	chkdskno

	or	DSKstatus,fdc_READY

chkdskno:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;=========================================================================
;	SEEKTOTRACK --	According to DSKtrackoffset, seek in the "disk"
;			to sector 0.
;
;
;	(I know this isn't anything like actual drives, where the sector
;	changes constantly as the motor spins.)
;
seektotrack proc near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	bx,DSKhandle
	or	bx,bx
	jz	sttfail

;	Each track will emulate the actual density on a track.
;	For single density disk, we have 256-byte sectors*9,
;	and double, it's 256-byte sectors*18.
;	(Set by OPENDISK)
;	This, of course, prevents goofy-ass things like
;	mixed single/double-density disks...

	mov	al,DSKtrackoffset
	mov	DSKtrack,al		; update it
	xor	ah,ah
	cmp	DSKside,0
	jz	sttnotside2
	xor	cx,cx
	mov	cl,DSKtracksside
	sub	ax,cx
	not	ax
sttnotside2:
	mul	DSKtracksize		; # bytes per track

	push	ax
	push	dx

	mov	cx,dx			; CX:DX=new position
	mov	dx,ax
	mov	ah,42h			; seek
	mov	al,00			; absolute
	int	21h
      	
	call	getfilesize
	pop	bx
	pop	cx
	cmp	bx,dx
	jb	sttok
	ja	stterr
	cmp	cx,ax
	jb	sttok
stterr:
	stc				; aack!  Disk isn't this large
	jmp	sttfail
sttok:
	clc
sttfail:
	mov	DSKtrackbyteoffset,0	; offset into track

	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;=====================================================================

;	OPENDISK --	Open a disk, in DISKxNAME, with DiskPath.
;			Create, if necessary.
;
opendisk proc	near
	pusha

	cmp	usediskdsr,0		; are we really in the right DSR?
	jnz	oddoit

	jmp	odnodisk

oddoit:
	mov	ax,DSKhandle
	or	ax,ax
	jz	odclosed
	
	call	closedisk		; close the udder one first

odclosed:
	mov	al,13
	mul	DSKnum
	sub	ax,13			; DSKnum=0 means no disk
	jge	odlegal
	jmp	odnodisk

odlegal:
	lea	bx,disk1name		; create filename
	add	bx,ax
	lea	si,diskpath

	lea	di,DSKbuffer		; for disk file
	mov	cx,50
	call	concatasciiz
	mov	si,bx
	mov	cx,0
	call	concatasciiz
	mov	byte ptr [di],0

	mov	ax,3d02h		; open with read/write access
	lea	dx,DSKbuffer
	int	21h
	jc	odcreatefile

	mov	DSKhandle,ax
	call	getfilesize
	mov	bx,ax
	or	bx,dx
	jnz	odgotsomething

	call	closedisk		; geez... empty... we'll kill it

odcreatefile:
	mov	ah,3ch
	mov	cx,0			; create a new file
	lea	dx,DSKbuffer		; filename
	int	21h
	mov	DSKhandle,0
	jc	odnodisk		; couldn't create

	mov	bx,ax
	mov	DSKdensity,9		; assume SD at first...
	jmp	odgotit

odgotsomething:
	mov	cx,20			; read info from sector 0
	lea	dx,DSKbuffer
	mov	bx,DSKhandle
	mov	ah,3fh
	int	21h	    		

	mov	al,[DSKbuffer+17]
	mov	DSKtracksside,al	; # tracks per side
	mov	al,[DSKbuffer+12]	
	mov	DSKdensity,al		; # sectors per track

odgotit:
	mov	DSKhandle,bx		; not repetive, for ODCREATEFILE
	mov	ax,256			; FIXED at 256!
	mov	cl,DSKdensity
	xor	ch,ch
	mul	cx			; AX=track size
	mov	DSKtracksize,ax

odnodisk:
	popa
	ret
	endp


;=========================================================================
;	CLOSEDISK --	Close "disk"
;
;
closedisk proc near
	push	ax
	push	bx

	cmp	usediskdsr,0		; are we really in DSR?
	jnz	cddoit

	jmp	cdout

cddoit:
	cmp	DSKpostponed,0		; were we in the middle of a write?
	jz	cdnosave

	call	flushbufferout

cdnosave:
	mov	bx,DSKhandle
	or	bx,bx
	jz	cdout

	mov	ah,3eh			; close file
      	int	21h

cdout:
	mov	DSKhandle,0
	pop	bx
	pop	ax
	ret
	endp

;===================================================================

;	SEEKHOME --	Position the heads to track 0.
;
seekhome proc	near
	mov	DSKtrackoffset,0
	or	DSKstatus,fdc_TRACK0
	call	checkdisk
	call	seektotrack
	ret
	endp


;	SEEK --		Seek to some track.
;
seek	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

;	Only the TRACK NUMBER matters here, in real life.
;	This routine ONLY needs to set DSKtrackoffset.

	mov	al,DSKlastbyte		; get last byte written to me
	mov	DSKtrackoffset,al

	or	al,al
	jnz	seeknot0
	or	DSKstatus,fdc_TRACK0

seeknot0:
	cmp	al,80
	jb	seeknodisk
	or	DSKstatus,fdc_BADRECORD	; 80 tracks max (a side!)
seeknodisk:
	call	checkdisk
	call	seektotrack
	
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;==================================================================
;	STEPIN --	Step in one track
;
;
stepin 	proc	near
	push	ax
	mov	al,DSKtrackoffset
	inc	al
	cmp	al,80
	jb	stepinout

	or	DSKstatus,fdc_BADRECORD		; boing!!  kill the drive!

stepinout:
	mov	DSKtrackoffset,al

	call	checkdisk
	call	seektotrack
	pop	ax
	ret
	endp


;=======================================================================

;	The routines below all use buffering, to save memory and time.
;
;	WRITE routines will set DSKbuflen to be the maximum # of bytes
;	expected for a write operation.  WTCMD already cleared the
;	other buffer pointers.  The routine below should seek in the 
;	file to the proper position.
;	The DSKpostponed flag will be set to say
;	that we are expecting data (which fills up the DSKbuffer and
;	is flushed in WTDATA when enough bytes are received).
;	*  If a command is sent during a write (short buffer), or the
;	disk is deselected, then FLUSHBUFFER is called to empty it.
;	
;	DSKcommandaddr is not used.
;
;	READ routines will set DSKbuflen to the # of bytes expected to
;	be read.  If more are read, the buffer starts over.  If less
;	are read, it doesn't matter.
;




;	SKIPSEEK --	Skip AX bytes in file
;			Assumes BX=handle.  Call from FINDIDMARKER only!
;
skipseek proc	near
	push	ax
	push	cx
	push	dx

	add	DSKtrackbyteoffset,ax

	mov	dx,ax
	xor	cx,cx			; CX:DX = offset from current ptr
	mov	ax,4201h
	int	21h

	pop	dx
	pop	cx
	pop	ax
	ret
	endp



;	FINDIDMARKER --	Uses DSKtrack/DSKsector
;
;			Just skip DSKsector*256 bytes, return success
;
findidmarker proc near
	push	ax
	push	bx

	call	seektotrack
	jnc	fimgo

	or	DSKstatus,fdc_BADRECORD
	stc
	jmp	fimout

fimgo:
	mov	ah,DSKsector
	xor	al,al
	mov	bx,DSKhandle
	call	skipseek

	clc
fimout:
	pop	bx
	pop	ax
	ret
	endp


;	READIDMARKER --	Read ID marker from current position.
;			Return 4 bytes at DSKbuffer.
;			Leaves pointer at beginning of sector.
;
;	This is a command.
;
readidmarker proc	near
	push	ax

	mov	al,DSKtrack
	mov	DSKbuffer.track,al
	mov	al,DSKsector
	mov	DSKbuffer.sector,al
	mov	al,2
	mov	DSKbuffer.seclen,al
	mov	al,DSKside
	mov	DSKbuffer.side,al

	mov	ax,size fdc_secrec+1	; set up buffer
	mov	DSKbuflen,ax		; one more byte is >FE
	mov	DSKbufoffs,1		; start one ahead

	call	checkdisk
	pop	ax
	ret
	endp



;==================================================================
;	READSECTOR --	Read a sector
;
;
readsector proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	si
	push	di

	mov	bx,DSKhandle
	or	bx,bx
	jz	rdstrout

	call	findIDmarker
	jc	rdstrseek	; couldn't find the correct sec/track

	mov	ax,256

	mov	cx,ax		; # bytes to
	mov	ah,3fh		; read
	lea	dx,DSKbuffer	; into DSKbuffer
	int	21h
	jnc	rdstrout

	cmp	ax,15h		; drive not ready
	je	rdstrdnr
	cmp	ax,17h		; data error
	je	rdstrcrc
	cmp	ax,19h		; seek error
	je	rdstrseek
	jmp	rdstrcrc	; just say CRC

rdstrdnr:
	jmp	rdstrnodisk
rdstrcrc:
	or	DSKstatus,fdc_CRCERR
	jmp	rdstrout
rdstrseek:
	or	DSKstatus,fdc_BADRECORD
	jmp	rdstrout
rdstrout:
	mov	DSKbufoffs,0	; start at beginning
	mov	DSKbuflen,cx	; # bytes we can read
	call	checkdisk
rdstrnodisk:
	pop	di
	pop	si
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;==============================================================
;	WRITESECTOR --	Write a sector
;
;
writesector proc near
	push	ax
	push	bx
	push	cx
	push	dx

	call	findIDmarker
	jc	wtstrseek

	mov	DSKpostponed,1	; we'll just wait until we *GET* the data
	jmp	wtstrgood

wtstrseek:
	or	DSKstatus,fdc_BADRECORD
	mov	ax,0
	jmp	wtstrskip
wtstrgood:
	mov	ax,256
wtstrskip:
	mov	DSKwritten,0	; none received
	mov	DSKbufoffs,0	; start at beginning
	mov	DSKbufpos,0	; start at beginning
	mov	DSKbuflen,ax	; how many bytes to get

	call	checkdisk
wtstrnodisk:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;=========================================================================

;	FORMATTRACK --	format a track.
;
;	Format: 
;	We expect the first track to be created with the correct size
;	(single/double density), and the rest of the disk must follow
;	this formula.
;
;	So, the first byte in the file is 9/18 saying single/double
;	density.
;
;	From this, DSKTRACKSIZE is calculated.
;
;	Then follow any number of tracks (<40) consisting of DSKdensity
;	copies of this:
;
;	>FE (id marker)
;	track, side, sector, sectorsize (all bytes)
;	>FB (data marker)
;	data (# bytes == 128<<sectorsize)
;
formattrack proc near
	push	ax

	mov	DSKformatsaving,0
	mov	ax,DSKtracksize
	mov	DSKbuflen,ax	

	call	checkdisk
	pop	ax
	ret
	endp

;=================================================================

;	INTERRUPT --	Interrupt current operation
;
;
interrupt proc	near
	push	ax

	cmp	DSKpostponed,0
	jz	intpout

	call	flushbufferout
	mov	DSKpostponed,0

intpout:
	or	DSKstatus,fdc_READY
	pop	ax
	ret
	endp


;===============================================================

;	DSKholdoff -- 	The ready/hold line has been cleared.
;			If an operation (write sector, for instance)
;			is waiting, then execute it now.
;
DSKholdoff proc	near

	cmp	DSKpostponed,0
	jz	DSKhonope

	call	flushbufferout		; flush the buffer
	mov	DSKpostponed,0

DSKhonope:
	ret
	endp



;****************************************************************
;
;	Optimization routines for the TI DSR
;
;*****************************************************************
; 	For each, read or write to/from VDP memory, count in R6
;***************************************************************


;	Write inverted bytes to WTDATA.
;
;
DSKoptwrite proc near
	push	ax
	push	bx
	push	cx
	push	si
	push	di
	push	es

	mov	es,cpuseg
	mov	cx,es:[WP+R6]		; count
DSKwoloop:
	mov	di,8800h
	READBYTE di,al		; get byte from VDP
	not	al 		; invert it
	mov	di,fdc_WTDATA
	WRITEBYTE di,al
	loop	DSKwoloop

	mov	word ptr es:[WP+R6],0
	or	stat,st_e

	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	Read inverted bytes from RRDDATA.
;
;
DSKoptread proc near
	push	ax
	push	bx
	push	cx
	push	si
	push	di
	push	es

	mov	es,cpuseg
	mov	cx,es:[WP+R6]		; count
DSKroloop:
	mov	di,fdc_RDDATA
	READBYTE di,al
	not	al 			; invert it	
	mov	di,8C00h
	WRITEBYTE di,al
	loop	DSKroloop

	mov	word ptr es:[WP+R6],0
	or	stat,st_e

	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


