/*
  special.h

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
;===========================================
;	SPECIAL.H
;
;	Header file
;===========================================

IFDEF	_SPECIAL_

	.data

	public	modinfoseg
	public	nummodulesinlist
	public	nummodulesselected
	public	moduleslist

	public	selected
	public	defaultmodule
	public	modextension


;--------------------------------------------------------------------------

	.code

	public	special_preconfiginit
	public	special_postconfiginit
	public	special_restart
	public	special_restop
	public	special_shutdown

	public	handlespecialfunctions
	public	pickamodule
       	public	print
	public	dosprint

	public	loadparts			; of a module

	IFDEF	BETA
	public	betago
	ENDIF

ELSE

	.data

	extrn	modinfoseg:word
	extrn	nummodulesinlist:byte
	extrn	nummodulesselected:byte
	extrn	moduleslist:byte

	extrn	selected:byte
	extrn	defaultmodule:byte
	extrn	modextension:byte


;--------------------------------------------------------------------------

	.code

	extrn	special_preconfiginit:near
	extrn	special_postconfiginit:near
	extrn	special_restart:near
	extrn	special_restop:near
	extrn	special_shutdown:near


	extrn	handlespecialfunctions:near
	extrn	pickamodule:near
       	extrn	print:near
	extrn	dosprint:near

	extrn	loadparts:near

	IFDEF	BETA
	extrn	betago:near
	ENDIF



ENDIF
