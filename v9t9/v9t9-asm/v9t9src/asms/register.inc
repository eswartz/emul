; ********************************************
; REGISTER.INC include file for register stuff
; ********************************************
; by Edward Swartz  1/9/1995
; ********************************************

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