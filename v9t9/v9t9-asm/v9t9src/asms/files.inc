; ************************************
; FILES.INC constant defs include file
; ************************************
; 1/9/1995 by Edward Swartz
; ************************************


;	These constants are used in the emulated DSR ROMs.
;
;	The opcode DSR which fills the >4000->5FFF dataspace 
;	is in some places set to DSR+D_xxxx to signify a
;	specific routine.
;
;	(i.e., the handler code for DSK3 has DSR+D_DSK3 as its
;	only instruction.)
;
;	The code in EMULATE.INC traps the DSR opcode and sends
;	control to "handlefileoperations".



dsroffs	equ	4000h



D_DSK	equ	0
D_DSK1	equ	1
D_DSK2	equ	2
D_DSK3	equ	3
D_DSK4	equ	4
D_DSK5	equ	5

D_INIT	equ	6

D_DSKSUB equ	7

D_10	equ	D_DSKSUB
D_11	equ	D_DSKSUB+1
D_12	equ	D_DSKSUB+2
D_13	equ	D_DSKSUB+3
D_14	equ	D_DSKSUB+4
D_15	equ	D_DSKSUB+5
D_16	equ	D_DSKSUB+6

D_FILES	equ	D_DSKSUB+7



D_RS232		equ	0
D_RS2321	equ	1
D_RS2322	equ	2
D_PIO		equ	3
