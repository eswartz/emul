;   general.i
; 
;   (c) 2010-2011 Edward Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

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

