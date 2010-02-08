;	This is adapted from emudiskdsr.c and 9900.h

DSROP		equ	>0c00
TIDSROP		equ	>0d20

D_DSK 		equ 0		; standard file operation on DSK.XXXX.[YYYY]
D_DSK1 		equ 1		; standard file operation on DSK1.[YYYY]
D_DSK2 		equ 2		; ...
D_DSK3 		equ 3		; ...
D_DSK4 		equ 4		; ...
D_DSK5 		equ 5		; ...

D_INIT 		equ 6		; initialize disk DSR

D_DSKSUB 		equ 7	; subroutines

D_SECRW 		equ 7		; sector read/write	(10)
D_FMTDISK 		equ 8		; format disk			(11)
D_PROT 			equ 9		; file protection		(12)
D_RENAME 		equ 10		; rename file			(13)
D_DINPUT 		equ 11		; direct input file	(14)
D_DOUTPUT 		equ 12		; direct output file	(15)
D_16 			equ 13		; um... I forgot		(16)

D_FILES			equ 14		; setup # files (CALL FILES)

;;;;;;;;;;;;;;;;;

D_RS232			equ	0
D_RS2321		equ	1
D_RS2322		equ	2
D_PIO			equ	3

D_RS232INT		equ	>60
