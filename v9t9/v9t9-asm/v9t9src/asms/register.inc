;   register.inc
; 
;   (c) 1991-2012 Edward Swartz
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

;
;	Offsets for registers.
;

R0	equ	0
R1	equ	2
R2	equ	4
R3	equ	6
R4	equ	8
R5	equ	10
R6	equ	12
R7	equ	14
R8	equ	16
R9	equ	18
R10	equ	20
R11	equ	22
R12	equ	24
R13	equ	26
R14	equ	28
R15	equ	30


;
;	Registers used in the emulator.
;

WP	equ	bp
STATHI	equ	DH
STAT	equ	DX
IP	equ	_IP
PC	equ	_IP
