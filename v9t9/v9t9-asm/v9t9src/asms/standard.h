/*
  standard.h

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
;	STANDARD.H
;
;	Standard defines for all modules
;===========================================

	DOSSEG
	.MODEL  small
	.286

;	The DEMO below means that demonstration recording/playback is
;	enabled.
;
;	LPCSPEECH means that speech data is directly decoded, rather
;	than using a digitized speech file.
;
;	BOTH OF THESE are required now, since things probably depend
;	on demos in an unhealthy manner now, and there is no longer
;	code to handle digitized speech files.
;

        DEMO    = 1                     ; these must ALWAYS be defined
        LPCSPEECH = 1                   ; since leaving them out causes
                                        ; some compile bugs now

	IFDEF	SUPERFAST
		IFDEF	T386
			IFDEF	_TIEMUL_
				SUPER = 1
			ENDIF
		ENDIF
	ENDIF
	


	include	strucs.inc


