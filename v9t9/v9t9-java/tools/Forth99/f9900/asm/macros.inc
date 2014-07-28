
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
 	limi 0
 	
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
	seto	@BANK1				; switch to ROM bank 
	soc		#F_BANK1, R15		; flag; mark that we're in bank1 on return
]
 
 define BankedXopHandler [
	mov		*14+, 1
	BankedIntHandler
] 