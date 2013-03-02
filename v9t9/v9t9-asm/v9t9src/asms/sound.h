/*
  sound.h

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
;==========================================
;	SOUND.H
;
;	Sound header file
;==========================================


IFDEF	_SOUND_

	.data

	public	ctvoicedrv
	public	failreason
	public	sbirq
	public	sbdma
	public	sbstat
	public	sync	
	public	sbport
	public	speech
	public	soundcard
	public	playsound
	public	silence
	public	noisetop
	public	toggler


;
;	PC Speaker variables
;

	public	pclasthertz
	public	pcspeakersilence



	.code

	public	sound_preconfiginit
	public	sound_postconfiginit
	public	sound_restart
	public	sound_restop
	public	sound_shutdown


	public	handlesound

	public	updatedevices			; special
	public	restartsound			; special, toggle silence

	public	setsbvoicevol			; speech


;	PC speaker

	public	pcsoundoff


ELSE

	.data

	extrn	ctvoicedrv:word
	extrn	failreason:byte
	extrn	sbirq:byte
	extrn	sbdma:byte
	extrn	sbstat:word
	extrn	sync:word
	extrn	sbport:word
	extrn	speech:word
	extrn	soundcard:byte
	extrn	playsound:byte
	extrn	silence:byte
	extrn	noisetop:byte
	extrn	toggler:word


;
;	PC Speaker variables
;

	extrn	pclasthertz:word
	extrn	pcspeakersilence:byte



	.code

	extrn	sound_preconfiginit:near
	extrn	sound_postconfiginit:near
	extrn	sound_restart:near
	extrn	sound_restop:near
	extrn	sound_shutdown:near


	extrn	handlesound:near
	extrn	restartsound:near


	extrn	updatedevices:near

	extrn	setsbvoicevol:near


;	PC speaker

	extrn	pcsoundoff:near



ENDIF
