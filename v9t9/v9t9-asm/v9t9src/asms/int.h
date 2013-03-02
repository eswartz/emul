/*
  int.h

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
;=========================================
;	INT.H
;
;	Interrupt header file
;=========================================


IFDEF	_INT_

	.data

	public	videodatachanges
	public	debugint

	public	spchcntr

	public	currentfunc

	public	elapsed
	public	timer0interval


;--------------------------------------------------------------------------

	.code


	public	int_preconfiginit
	public	int_postconfiginit
	public	int_restart
	public	int_restop
	public	int_shutdown


	public	installctrlc
	public	resetctrlc

	public	setspeed

	public	setcurrentspeed
	public	nullinterrupter


ELSE
	.data

	extrn	videodatachanges:byte
	extrn	debugint:byte

	extrn	spchcntr:byte

	extrn	currentfunc:word

	extrn	elapsed:word
	extrn	timer0interval:word


;--------------------------------------------------------------------------

	.code

	extrn	int_preconfiginit:near
	extrn	int_postconfiginit:near
	extrn	int_restart:near
	extrn	int_restop:near
	extrn	int_shutdown:near


	extrn	installctrlc:near
	extrn	resetctrlc:near

	extrn	setspeed:near

	extrn	setcurrentspeed:near
	extrn	nullinterrupter:near



ENDIF
