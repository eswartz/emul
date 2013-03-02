/*
  hardware.h

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
;	HARDWARE.H
;
;	Hardware header file
;==================================================



IFDEF	_HARDWARE_

	.data

	public	currentints
	public	handledints

	public	maxclockhertz
	
	public	usediskdsr
	public	diskromon			; is disk rom selected?
	public	rs232led,diskled,emudiskled


;	RS232

	public	rs1,rs2,pio1,pio2



;---------------------------------------------------------------------------

	.code

	public	hardware_preconfiginit
	public	hardware_postconfiginit
	public	hardware_restart
	public	hardware_restop
	public	hardware_shutdown


	public	readseveralCRU
	public	writeseveralCRU

	public	reset9901
	public	handle9901
	public	reset9901int


;	RS232

	public	rs232isbuffered
	public	setrs232ints,resetrs232ints
	public	handlers232write
	public	handlers232read

	public	isbuffered
	public	rselect
	public	readbuffer

ELSE


	.data

	extrn	currentints:word
	extrn	handledints:word

	extrn	maxclockhertz:word

	extrn	usediskdsr:byte
	extrn	diskromon:byte
	extrn	rs232led:byte,diskled:byte,emudiskled:byte


;	RS232

	extrn	rs1:rsstruc,rs2:rsstruc,pio1:word,pio2:word



;---------------------------------------------------------------------------

	.code

	extrn	hardware_preconfiginit:near
	extrn	hardware_postconfiginit:near
	extrn	hardware_restart:near
	extrn	hardware_restop:near
	extrn	hardware_shutdown:near



	extrn	readseveralCRU:near
	extrn	writeseveralCRU:near

	extrn	reset9901:near
	extrn	handle9901:near
	extrn	reset9901int:near


;	RS232

	extrn	rs232isbuffered:near
	extrn	setrs232ints:near,resetrs232ints:near
	extrn	handlers232write:near
	extrn	handlers232read:near

	extrn	isbuffered:near
	extrn	rselect:near
	extrn	readbuffer:near




ENDIF
