;==================================================
;	FILES.H
;
;	Files header file
;==================================================

IFDEF	_FILES_

	.data

	public	dsrseg_realdisk			; segment for disk dsr
	public	dsrseg_realrs232    		; segment for rs232 dsr
	public	dsrseg_emudisk			; segment for emulated disk
	public	dsrseg_emurs232			; segment for emu rs232

	public	diskdsrname
	public	emudiskdsrname
	public	shareddiskdsrname
	public	rs232dsrname
	public	emurs232dsrname


;	FDC.INC

	public	diskpath
	public	disk1name
	public	disk2name
	public	disk3name

	public	DSKhold
	public	DSKbufoffs
	public	DSKnum
	public	DSKside


	
;--------------------------------------------------------------------------

	.code

	public	files_preconfiginit
	public	files_postconfiginit
	public	files_restart
	public	files_restop
	public	files_shutdown


	public	handlefileinterrupt
	public	handlefileoperations
	public	handlecriticalerror

	public	closeallfiles
	public	VDPUpdate

	public	allocatedsrs			; get DSR ROM memory
	public	loaddsrs			; load DSR ROM images


;	FDC

	public	handlefdcread
	public	handlefdcwrite
	public	opendisk
	public	closedisk
	public	seektotrack
	public	DSKholdoff

	public	DSKoptwrite
	public	DSKoptread



ELSE
	.data

	extrn	dsrseg_realdisk:word			; segment for disk dsr
	extrn	dsrseg_realrs232:word			; segment for rs232 dsr
	extrn	dsrseg_emurs232:word			; segment for rs232 dsr
	extrn	dsrseg_emudisk:word			; segment for emulated disk

	extrn	diskdsrname:byte
	extrn	emudiskdsrname:byte
	extrn	shareddiskdsrname:byte
	extrn	rs232dsrname:byte
	extrn	emurs232dsrname:byte


;	FDC.INC

	extrn	diskpath:byte
	extrn	disk1name:byte
	extrn	disk2name:byte
	extrn	disk3name:byte

	extrn	DSKhold:byte
	extrn	DSKbufoffs:word
	extrn	DSKnum:byte
	extrn	DSKside:byte


	
;--------------------------------------------------------------------------

	.code

	extrn	files_preconfiginit:near
	extrn	files_postconfiginit:near
	extrn	files_restart:near
	extrn	files_restop:near
	extrn	files_shutdown:near

	extrn	handlefileinterrupt:near
	extrn	handlefileoperations:near
	extrn	handlecriticalerror:near

	extrn	closeallfiles:near
	extrn	VDPUpdate:near

	extrn	allocatedsrs:near
	extrn	loaddsrs:near


;	FDC

	extrn	handlefdcread:near
	extrn	handlefdcwrite:near
	extrn	opendisk:near
	extrn	closedisk:near
	extrn	seektotrack:near
	extrn	DSKholdoff:near

	extrn	DSKoptwrite:near
	extrn	DSKoptread:near



ENDIF
