;   general.inc
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
;	Register equates
;

R0	equ	0
R1	equ	1
R2	equ	2
R3	equ	3
R4	equ	4
R5	equ	5
R6	equ	6
R7	equ	7
R8	equ	8
R9	equ	9
R10	equ	10
R11	equ	11
R12	equ	12
R13	equ	13
R14	equ	14
R15	equ	15

;
;	Masks for stack push/pop
;

SR0	equ	1
SR1	equ	2
SR2	equ	4
SR3	equ	8
SR4	equ	16
SR5	equ	32
SR6	equ	64
SR7	equ	128
SR8	equ	256
SR9	equ	512
SR10	equ	1024
SR11	equ	2048
SR12	equ	4096
SR13	equ	8192
SR14	equ	16384
SR15	equ	32768

;	
;	Masks in status word.
;

ST_L	equ	1
ST_A	equ	2
ST_E	equ	4
ST_C	equ	8
ST_OV	equ	16
ST_OP	equ	32
ST_X	equ	64


;
;	FORTH GROM offsets
;

grom_kbdlist		equ	>0000
grom_fonts		equ	>0130


;
;	Low RAM setup.
;

@TIB	equ	>2000				; TIB  = >2000
@S0	equ	>2800				; stack= >2000 - >2800
@R0	equ	>2FF0				; rstak= >2800 - >2FF0

@First	equ	>2FF0				; 4 buffers (1k + 4 bytes)
@Limit	equ	>4000

