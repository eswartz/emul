;=========================================
;	VIDEO.H
;
;	Video header file
;=========================================


IFDEF	_video_

	.data

	public	vga
	public	blank
	public	ontiscreen

	public	changes
	public	spritechanges
	public	screenhandler
	public	everupdate

	public	tifont

	public	textbg

	public	videoredrawlatency	; 1/60 delay in redrawing screen
	public	videoredrawmaxdelay
	public	sinceredrawcounter
	public	vdprestcounter

	public	video16			; 16-bit video?
	public	checkspritecoinc

	public	check5sprites

	public	video_debug

	public	secret

;---------------------------------------------------------------------------

	.code

	public	video_preconfiginit
	public	video_postconfiginit
	public	video_restart
	public	video_restop
	public	video_shutdown


	public	handlevdp
	public	handlevdp1
	public	handlevdpreg

	public	setpalette
	public	invertpalette
	public	setdebugbg

	public	updatevideoscreen

	public	drawphrase
	public	erasephrase

	public	movesprites

	public	drawled

	public	dotransfer
	public	screenfill

	public	completeupdate		; *************** purge me!

;	public	loadlogo

ELSE


	.data

	extrn	vga:byte
	extrn	blank:byte
	extrn	ontiscreen:byte

	extrn	changes:byte
	extrn	spritechanges:byte
	extrn	screenhandler:word
	extrn	everupdate:byte

	extrn	tifont:byte

	extrn	textbg:byte

	extrn	videoredrawlatency:byte	; 1/60 delay in redrawing screen
	extrn	videoredrawmaxdelay:byte
	extrn	sinceredrawcounter:byte
	extrn	vdprestcounter:byte

	extrn	video16:byte			; 16-bit video?
	extrn	checkspritecoinc:byte

	extrn	check5sprites:byte

	extrn	video_debug:byte

	extrn	secret:byte

;---------------------------------------------------------------------------

	.code
	

	extrn	video_preconfiginit:near
	extrn	video_postconfiginit:near
	extrn	video_restart:near
	extrn	video_restop:near
	extrn	video_shutdown:near


	extrn	handlevdp:near
	extrn	handlevdp1:near
	extrn	handlevdpreg:near

	extrn	updatevideoscreen:near

	extrn	setpalette:near
	extrn	invertpalette:near
	extrn	setdebugbg:near

	extrn	drawphrase:near
	extrn	erasephrase:near

	extrn	movesprites:near

	extrn	drawled:near

	extrn	dotransfer:near
	extrn	screenfill:near

	extrn	completeupdate:near		; ********** purge me!

;	extrn	loadlogo:near

ENDIF
