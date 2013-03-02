;   int.inc
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




;==========================================================================
;	The VDP interrupt routine.
;
;	This handles clock, keyboard, etc.
;
;==========================================================================

int	limi	0			; disable interrupts 

	clr	r12			; point to 9901
	tb	2			; VDP interrupt?
	jne	intvdp			; yup.
	jmp	intvdp
       	
	lwpi	>83e0			; they require this...
	lwpi	intws
	jmp	intout			; don't handle device interrupts yet

intvdp:
	sbo	2			; acknowledge VDP interrupt

	inc	@uptime 2+		; time in 1/60 seconds
	jnc	intv00			; overflow?
       	inc	@uptime	 		; more time accuracy

intv00:	
	ab	@h01,@kbdtimer		; inc repeat delay
	bl	@scankbd		; get keyboard, save char

@intpatch:	nop			; PATCH HERE for VDP int

;-----------------------------------

int2:
	inct	@timeout		; blank screen?
	jne	int200

	blwp	@vscreenoff

int200:
	li	0,vcrstimer
	ab	@h01,*0			; cursor timer
	cb	*0,@vcrsblink		; to blink or not to blink?
	jl	int201

	sb	*0,*0			; clear
	mov	@vcursor,0		; get ptr
	blwp	*0			; blink it

int201:
	mov	@userint,0		
	jeq	intout

	bl	*0			; execute user interrupt routine
	lwpi	intws

intout:
	RTWP
