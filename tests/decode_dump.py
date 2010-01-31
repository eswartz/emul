import re, sys

patt = re.compile("([0-9A-F]+)\s([0-9A-F]+).*")

def hex4(num):
	str = hex(num)[2:].upper()
	if len(str) == 4:
		return str
	return "0000"[0:4-len(str)] + str

class Decoder:
	"""
Try to find loops in a dump.  

	"""
	def __init__(self):
		self.insts = []
		self.loop = []
		self.loopidx = 0
		self.loopcnt = 0
		self.lastpc = 0
		pass

	def add(self, pc, wp):
		#print hex4(pc),
		if self.lastpc > pc:
			# back branch
			foundloop = False
			if len(self.loop) > 0:
				# check current loop
				#print "match:",self.loop
				instidx = len(self.insts)
				minidx = max(0, instidx - 512)
				while instidx > minidx:
					instidx -= 1
					if self.insts[instidx] == pc:
						# see if the loop matches
						if self.insts[instidx:instidx+len(self.loop)] == self.loop:
							#print "{",hex4(pc),"}"
							self.loopcnt += 1
							self.loopidx = 1
							foundloop = True
							break
						# keep trying
			
				if not foundloop:
					self.dump()
					self.loop = []
					self.loopcnt = 0

			if not foundloop:
				if not pc in self.loop:
					# might be part of larger loop
					self.loop += [pc]
					self.loopidx += 1
				else:
					self.loop = [pc]
					self.loopidx = 1
					self.loopcnt = 0

			#print "[",self.loop,"]"
		elif self.loopidx < len(self.loop):
			#print "test inst",pc,"with",self.loop,"at",self.loopidx
			if pc == self.loop[self.loopidx]:
				# good
				self.loopidx += 1
			else:
				# out of loop
				self.dump_serial(self.loop)
				self.loop = [pc]
				self.loopidx = 1
				self.loopcnt = 0
		else:
			# add to possible loop
			self.loop += [pc]
			self.loopidx += 1
			#print "+[",self.loop,"]"

		self.insts += [pc]
		self.lastpc = pc
				
	def dump_serial(self, insts):
		print "Serial code: ",
		for inst in insts:
			print hex4(inst),
		print ""

	def dump(self):
		if self.loopcnt > 0:
			print "Loop:",map(hex4, self.loop),";",self.loopcnt,"iterations"
		else:
			self.dump_serial(self.loop)
	def decode(self, file):
		while True:
			line = file.readline()
			if not len(line):
				break
			match = patt.match(line)
			#print line,
			if match:
				pc = int(match.group(1), 16)
				wp = int(match.group(2), 16)
				self.add(pc, wp)		
		self.dump()

if __name__=="__main__":
	decoder = Decoder()
	decoder.decode(sys.stdin)
