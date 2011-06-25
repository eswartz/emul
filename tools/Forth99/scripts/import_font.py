import re, sys

def impfont(data, height, cnt):
	print "! XPM2"
	print "128 " + str((cnt / 16) * 8) + " 2 1"
	print "* c #000000"
	print ". c #ffffff"
	for j in range(cnt / 16 * 8):
		ss = ""
		for i in range(16):
			for b in range(8):
				if (ord(data[i*8+(j//8)*128+(j%8)]) & (0x80 >> b)): c = '.' 
				else: c = '*'
				ss += c
		print ss
		
		
if __name__=="__main__":
	if len(sys.argv) < 4:
		print "run as: import_font.py binfile offset height count"
	f = open(sys.argv[1], "rb")
	os, radix = sys.argv[2], 10
	if os[0:2] == "0x":
		os, radix = os[2:], 16
	f.seek(int(os, radix))
	height = int(sys.argv[3])
	cnt = int(sys.argv[4])
	data = f.read(height * cnt)
	impfont(data, height, cnt)
	