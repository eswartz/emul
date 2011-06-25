# get symbols

import sys,re

# find lines like:
#
#   sys:6500:mysymbol
#
# and emit
#
#   6500 mysymbol
#
lastname=None
lastaddr=None
for line in sys.stdin.readlines():
    line = line.strip()
    if line.startswith("sym:"):
	if lastaddr:
		print lastaddr, lastname
        addrname = line[4:] 
        idx = addrname.find(':')
	lastaddr = addrname[0:idx]
	lastname = "FORTH="+addrname[idx+1:]
    elif line.startswith("sym!"):
	lastaddr = line[4:]        

if lastaddr:
	print lastaddr, lastname


