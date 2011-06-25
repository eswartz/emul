
# dump the info from a *.trk file
import os, os.path, sys, struct

def calc_crc(crc, val):
	pass

def findID(data, offs, endoffs):
	""" Find a sector ID marker """
	try:
		while offs < endoffs:
			#while data[offs] != '\xff':
			#	offs+=1
			#while data[offs] == '\xff':
			#	offs+=1
			try:
				while data[offs] != '\xfe':
					offs+=1
				offs+=1		# skip 0xfe
				(trackid, sideid, sectorid, sizeid) = data[offs:offs+4]
				offs += 4
				crcid = data[offs]*256+data[offs+1]
				offs += 2

				# find data marker
				cnt = 8 + 12 + 8 + 16
				while cnt > 0 and data[offs] != '\xfb':
					if data[offs] == '\xfe':
						raise "do over"
					offs += 1
					cnt -= 1
				if cnt > 0 and ord(sizeid) > 0 and ord(sizeid) < 5 and ord(sideid) in [0,1]:
					offs += 1  # skip 0xfb
					return (ord(trackid), ord(sideid), ord(sectorid), ord(sizeid), offs)
			except:
				pass
		
	except IndexError, e:
		pass
	# fallthrough for inner overruns
	return (0, 0, 0, 0, endoffs)

def printable(x):
	if (ord(x)<32 or ord(x)>127):
		return '.'
	else:
		return x
	
def dumpsector(data, offs, size):
	""" Dump data for one sector"""
	idx = 0
	shex = ""
	sasc = ""
	while idx < size:
		if idx % 16 == 0:
			shex = (">%03X " % (idx,))
			sasc = ""
		shex += ("%02X " % (ord(data[offs+idx]),))
		sasc += printable(data[offs+idx])
		if idx % 16 == 15:
			print "\t"+shex+" "+sasc
		idx+=1
			
def dumptrack(data, offs, endoffs):
	""" Dump data for one track """
	while offs < endoffs:
		(trackid, sideid, sectorid, sizeid, offs) = findID(data, offs, endoffs)
		ssize = 128 << sizeid
		if offs != endoffs and offs + ssize <= endoffs:
			print "--- Sector ",sectorid," (Track",trackid," Side",sideid," Size",sizeid,")",("  (>%06X)" % (offs,))
			dumpsector(data, offs, ssize)			
	
def dump(file):
	data = open(file, 'r').read()
	hdr = data[0:12]
	(magic, version, tracks, sides, unused, tracksize, track0offs) =\
		struct.unpack("4sBBBBHH", hdr)
	tracksize = ((tracksize&0xff)<<8)|((tracksize>>8)&0xff)
	track0offs = ((track0offs&0xff)<<8)|((track0offs>>8)&0xff)
	if magic != "trak":
		print file,"is not a 'trak' file"
		sys.exit(1)
	print "track size: ",tracksize
	for trk in xrange(tracks*sides):
		offs = track0offs + (trk * tracksize)
		print "=== Track",trk,(" (>%06X of >%06X)" % (offs, len(data)))
		dumptrack(data, offs, offs + tracksize)

if __name__ == "__main__":
	dump(sys.argv[1])
