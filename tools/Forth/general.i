;	general.inc					-- register and ROM memory equates
;
;	(c) 1996-2001 Edward Swartz
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
    #if ENHANCED_MEMORY
VDPRD   equ >FF80
VDPST   equ >FF82
VDPWD   equ >FF88
VDPWA   equ >FF8A
VDPCL   equ >FF8C
VDPWI   equ >FF8E
GPLRD   equ >FF90
GPLRA   equ >FF92
GPLWD   equ >FF94
GPLWA   equ >FF96
SPCHRD  equ >FF98
SPCHWT  equ >FF9A
SOUND   equ >FFA0   ; 0x20!
ROMBANK equ >FFC0
FTHBANK equ >FFC2
    #else
VDPWA   equ >8C02
VDPWD   equ >8C00
VDPST   equ >8802
VDPRD   equ >8800
GPLRD   equ >9800
GPLRA   equ >9802
GPLWD   equ >9C00
GPLWA   equ >9C02
SPCHRD  equ >9000
SPCHWT  equ >9400
SOUND   equ >8400
ROMBANK equ >1000
FTHBANK equ >1002
    #fi

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
grom_font8x8		equ	>0130
grom_font5x6		equ >0930

;	stack pointer
SP	equ		10

 define Push SP, ... [
    ai SP, -${#}*2
    foreach REG, IDX {
        mov ${REG}, @(${#}-${IDX}-1)*2(SP)
    }
]
 define Pop SP, ... [
    foreach - REG, IDX {
        mov *${SP}+, ${REG}
    }
]

 define Vector label, ws [
${label}  data ${ws}, ${label}_entry
${label}_entry:    
 ]