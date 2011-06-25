
;
;	testing code
;

_TEST_	dw 	dskws, testpc

dly		dec	10
		jgt  $ 2 -
		rt

testpc	li	12,>0030
		sbo	0		; turn on audio gate
		inct 12		; point to cassette out

		limi 0

		li	1,>0000

testbyt li	2,8
		mov +*1,5

testbit	ldcr 5,1
		src 5,1
	
		li	10,10
		bl	@dly
		dec 2
		jgt testbit

		mov 1,1
		jne testbyt

		rtwp
		

_read_	si	SP,6
		mov	11,@6(SP)
		mov	10,@4(SP)
		mov	12,@2(SP)
		mov 1,*SP

		li 	12,>002c	; point to CS1 motor
		sbo	0

		li	12,>0036	; point to cassette in
		limi 0

readbit	stcr 1,1
		inv 1
		ai	1,>3000
		bl	@printchar

;		li	10,10
;		bl	@dly
		jmp readbit

		mov	+*SP,1
		mov	+*SP,12
		mov +*SP,10
		mov +*SP,11
		rt

		
		
