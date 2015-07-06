;   xutil.inc
; 
;   (c) 2014 Ed Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 
	
;==========================================================================
;	Utilitiy services XOP entry
;
;	R12: pointer to operand
;	*R14+: command (word)
;==========================================================================

xop_util_tbl
	dw		xu_init, xu_seed, xu_rand1, xu_rand2, xu_noise
xop_util_tbl_end equ	$

xop_util_ent
	XopHandler xop_util_tbl 

xu_init
	li		0, >ACE1
	mov		0, @randseed1
	clr		@randseed2
	clr		@randnoise
	rt

xu_seed
	mov		*12, 0
	jne		$0+
	li		0, >ACE1
$0:
	mov		0, @randseed1
	mov		*12, @randseed2
	rt

xu_rand1
	mov		@randseed1, 0
	mov		0, 1
	srl		0, 1
	andi	1, 1
	neg		1
	andi	1, >B400
	xor		1, 0
	mov		0, @randseed1
	mov		0, *12
	rt

xu_rand2
	li		0, >6fe5
	mpy		@randseed2, 0
	ai		1, >7ab9
	mov		1, @randseed2

	mov		1, *12
	rt

xu_noise
	mov		*12, 0

	a		@uptime, 0
	src		5, 0
	xor		5, 0
	xor		@randnoise, 0
	
	mov		0, *12
	rt
