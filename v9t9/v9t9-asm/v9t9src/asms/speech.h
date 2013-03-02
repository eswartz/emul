/*
  speech.h

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
;=================================================
;	SPEECH.H
;
;	Speech header file
;=================================================


IFDEF	_SPEECH_

	.data

	public	sphrase
	public	sdelay

	public	speechexcite
	public	speechpitches
	public	uselpc
;;;;;;	public	speechavail

	public	cmmnd
	public	queueing



;--------------------------------------------------------------------------

	.code

	public	speech_preconfiginit
	public	speech_postconfiginit
	public	speech_restart
	public	speech_restop
	public	speech_shutdown


	public	handlespeechwrite
	public	handlespeechread


	public	startspeech
	public	stopspeech
	public	addspeech
	public	terminatespeech



ELSE

	.data

	extrn	sphrase:byte
	extrn	sdelay:word

	extrn	speechexcite:byte
	extrn	speechpitches:byte
	extrn	uselpc:byte
	extrn	speechavail:byte

	extrn	cmmnd:byte
	extrn	queueing:byte



;--------------------------------------------------------------------------

	.code

	extrn	speech_preconfiginit:near
	extrn	speech_postconfiginit:near
	extrn	speech_restart:near
	extrn	speech_restop:near
	extrn	speech_shutdown:near


	extrn	handlespeechwrite:near
	extrn	handlespeechread:near

	extrn	startspeech:near
	extrn	stopspeech:near
	extrn	addspeech:near
	extrn	terminatespeech:near




ENDIF
