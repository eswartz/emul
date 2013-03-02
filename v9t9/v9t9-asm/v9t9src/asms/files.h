/*
  files.h

  (c) 1991-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
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