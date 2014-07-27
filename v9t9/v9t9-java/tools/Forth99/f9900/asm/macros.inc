
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