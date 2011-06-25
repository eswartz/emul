;	general.i					-- register and ROM memory equates
;
;	(c) 2010 Edward Swartz
;
;   This program is free software; you can redistribute it and/or modify
;   it under the terms of the GNU General Public License as published by
;   the Free Software Foundation; either version 2 of the License, or
;   (at your option) any later version.
; 
;   This program is distributed in the hope that it will be useful, but
;   WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;   General Public License for more details.
; 
;   You should have received a copy of the GNU General Public License
;   along with this program; if not, write to the Free Software
;   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
;   02111-1307, USA.  
;
;	$Id: general.i,v 1.10 2009-01-10 02:21:52 ejs Exp $

;
;   Memory map
;
VDPRD   equ >80
VDPST   equ >81
VDPWD   equ >82
VDPWA   equ >83
VDPCL   equ >84
VDPWI   equ >85
SOUND   equ >A0   ; +0x20!

; status bits

ST_GIE	equ >8

; memory locations

VRAM	equ >a000


MODE0SCREEN equ VRAM

 define PushA [
    pushn #4, R0
    pushn #4, R4
    pushn #4, R8
    push R12
    push SR
]
 define PopA [
    pop SR
    pop R12
    popn #4, R8
    popn #4, R4
    popn #4, R0
]

