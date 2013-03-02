/*
  log.h

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
;==============================================
;	LOG.H
;
;	Logging header file
;==============================================


IFDEF	_LOG_

	.data

	public	logfilename
	public	islogging



;--------------------------------------------------------------------------

	.code

	public	log_preconfiginit
	public	log_postconfiginit
	public	log_restart
	public	log_restop
	public	log_shutdown

	public	startlogging
	public	stoplogging
	public	logout


ELSE

	.data

	extrn	logfilename:byte
	extrn	islogging:byte



;--------------------------------------------------------------------------

	.code

	extrn	log_preconfiginit:near
	extrn	log_postconfiginit:near
	extrn	log_restart:near
	extrn	log_restop:near
	extrn	log_shutdown:near

	extrn	startlogging:near
	extrn	stoplogging:near
	extrn	logout:near


ENDIF
