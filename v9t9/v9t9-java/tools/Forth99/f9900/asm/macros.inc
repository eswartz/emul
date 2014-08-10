
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
 
 define XopHandler table [
 	mov 11, 12
 	
 	a 1, 1
 	ci 1, (${table}_end - ${table})
 	jhe $
 	
	li SP, sysstack + sysstacksize
 	mov @${table}(1), 1
 	bl *1
 	
 	b @Int_Return
]

 define BankedIntHandler [
	limi	0
	soc		@F_BANK1, R15		; flag; mark that we're in bank1 on return (SZC in bank 0 code!)
	seto	@BANK1				; switch to ROM bank 
]
 
 define BankedXopHandler [
	mov		*14+, 1
	BankedIntHandler
] 