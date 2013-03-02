/*
  debug.h

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
;================================================
;	DEBUG.H
;
;	Debug header file
;================================================


IFDEF	_DEBUG_

	.data

	public	debugdelay
	public	debugcnt


;-------------------------------------------------------------------------

	.code

	public	debug_preconfiginit
	public	debug_postconfiginit
	public	debug_restart
	public	debug_restop
	public	debug_shutdown


	public  debug

	public	setdebugfromtext
	public	setdebugfromti
	public	settifromdebug
	public	settextfromdebug
	public	setnormaltext


ELSE


	.data

	extrn	debugdelay:byte
	extrn	debugcnt:byte


;-------------------------------------------------------------------------

	.code

	extrn	debug_preconfiginit:near
	extrn	debug_postconfiginit:near
	extrn	debug_restart:near
	extrn	debug_restop:near
	extrn	debug_shutdown:near

	extrn  debug:near


	extrn	setdebugfromtext:near
	extrn	setdebugfromti:near
	extrn	settifromdebug:near
	extrn	settextfromdebug:near
	extrn	setnormaltext:near




ENDIF
