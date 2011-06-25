
#  Read printer output and format it
#

import gtk, os, os.path, sys, pango, gobject

numwindows = 0

def deleted_window(a,b):
	global numwindows
	numwindows -= 1
	if numwindows <= 0:
		gtk.main_quit()

class TiPrinter:
	""" All coordinates are in 1/72 inch.
	 11 inches is 792 dots...
	 Char matrix looks like 12x9.
	 Compresed: 132 --> 7 px/char
	 Expanded: 40: 24x9
	 """
	def __init__(self):
		self.x = 0
		self.y = 0
		#self.pagesize = (720, 720)
		self.vpindistance = 2
		
		self.pagesize = (12*80, self.vpindistance*9*90)
		self.vadvance = self.vpindistance*12  # 1/6"
		self.charwidth = 9
	def parse(self, data):
		self.end_of_page = False
		try:
			while True:
				self.parseone(data)
		except StopIteration, e:
			#print "out of data"
			return

	def parseone(self, data):
		ch = data.next()
		if ch == '\x1b':
			ch = data.next()
			if ch == 'A':    # set line spacing in 1/72 inch
				self.vadvance = self.vpindistance*ord(data.next())
				#self.vadvance = ord(data.next())
			elif ch == 'E':  # turn on emphasized
				self.emph = True
				canvas.set_emphasized(self.emph)
			elif ch == 'F':  # turn off emphasized
				self.emph = False
				canvas.set_emphasized(self.emph)
			elif ch == 'K':  # low density graphics
				cnt = ord(data.next())+ord(data.next())*256
				while cnt > 0:
					self.emitbits(canvas, ord(data.next()), 2)
					cnt-=1
			elif ch == 'L':  # high density graphics
				cnt = ord(data.next())+ord(data.next())*256
				while cnt > 0:
					self.emitbits(canvas, ord(data.next()), 1)
					cnt-=1
			elif ch == '0':  # set to 8 per inch
				self.vadvance = self.vpindistance*72/8
			elif ch == '2':  # set to 6 per inch
				self.vadvance = self.vpindistance*72/6
			elif ch == '8':  # disable paper-end detector
				self.pagesize = (self.pagesize[0], self.vpindistance*9*96)
			elif ch == '9':  # enable paper-end detector
				self.pagesize = (self.pagesize[0], self.vpindistance*9*80)
			else:
				print "ignoring escape "+ch
		elif ch == '\x00':  # terminate tabulation
			pass
		elif ch == '\x07':  # buzzer
			print "!!! BUZZ !!!\n"
		elif ch == '\x08':  # backspace
			self.advancechar(-1)
		elif ch == '\x09':  # tabulation
			self.advancechar(8)  # for now
		elif ch == '\x0A':  # line feed
			self.advanceline()
		elif ch == '\x0B':  # vertical tab
			self.advanceline() # for now
		elif ch == '\x0C':  # form feed
			self.end_of_page = True
			self.x = 0
			self.y = 0
			raise StopIteration()
		elif ch == '\x0D':  # carriage return
			self.x = 0
		elif ch == '\x0E':  # enlarge
			self.charwidth = 24
			canvas.setwidth(self.charwidth)
		elif ch == '\x0F':  # condense
			self.charwidth = 7
			canvas.setwidth(self.charwidth)
		elif ch == '\x12': # turn off condense
			self.charwidth = 12
			canvas.setwidth(self.charwidth)
		elif ch == '\x14': # turn off enlarged
			self.charwidth = 12
			canvas.setwidth(self.charwidth)
		else:
			if ord(ch) < 32 or ord(ch) >= 127:
				ch = ' '
			canvas.plotchar(ch, self.x, self.y)
			self.advancechar(1)
		
	def advancechar(self, cnt):
		xspan = cnt * self.charwidth
		if xspan < 0:
			if self.x + xspan > 0:
				self.x += xspan
			else:
				self.x = 0
		else:
			self.x += xspan
			while self.x > self.pagesize[0]:
				self.x -= self.pagesize[0]
				self.advanceline()
		#print self.x,
		
	def advanceline(self):
		self.y += self.vadvance
		if self.y >= self.pagesize[1]:
			self.y -= self.pagesize[1]
			self.end_of_page = True
			raise StopIteration()
		
	def emitbits(self, canvas, val, adv):
		for i in range(8):
			canvas.plot(self.x, self.y+i*self.vpindistance, ((val<<i)&0x80)!=0)
		self.x += adv

class PrintCanvas:
	def __init__(self, file, printinfo):
		global numwindows
		numwindows += 1
		
		self.data = open(file, 'r').read()
		self.dataptr = 0
		
		self.printinfo = printinfo

		prx = printinfo.pagesize


		self.margins = (int(72*1.5), 36)  # on left, top
		#self.margins = (0, 0)
		self.sizes = (prx[0] + self.margins[0]*2, prx[1] + self.margins[1]*2)

		self.window = gtk.Window()
		self.window.set_title(file)
		self.window.set_default_size(self.sizes[0], self.sizes[1])
		self.window.show_all()

		self.window.connect("delete-event", deleted_window)

		self.notebook = gtk.Notebook()
		self.notebook.set_scrollable(True)
		self.window.add(self.notebook)
		self.notebook.show()

		self.pagenum = 1
		self.new_page()

	def new_page(self):

		self.pixmap = gtk.gdk.Pixmap(self.window.window, self.sizes[0], self.sizes[1])
		self.image = gtk.Image()
		self.image.set_from_pixmap(self.pixmap, None)
		self.notebook.append_page(self.image, gtk.Label("Page "+str(self.pagenum)))
		self.pagenum += 1
		self.image.show()
		
		self.gc = gtk.gdk.GC(self.pixmap)
		self.gc.set_rgb_fg_color(gtk.gdk.color_parse("white"))
		self.pixmap.draw_rectangle(self.gc, True, 0, 0, self.sizes[0], self.sizes[1])
		self.gc.set_rgb_fg_color(gtk.gdk.color_parse("#202020"))
		#self.gc.set_foreground(gtk.gdk.color_parse("purple"))

		self.gc2 = gtk.gdk.GC(self.pixmap)
		self.gc2.set_rgb_fg_color(gtk.gdk.color_parse("#404040"))

		self.desc = pango.FontDescription("Courier New 12")
		self.setwidth(9)
		self.set_emphasized(False)


	def feeder(self):
		while self.dataptr < len(self.data):
			cur = self.data[self.dataptr]
			self.dataptr+=1
			yield cur

	def setwidth(self, w):
		self.fontwidth = w
		if w<9:
			self.desc.set_weight(pango.STRETCH_CONDENSED)
		elif w==9:
			self.desc.set_weight(pango.STRETCH_NORMAL)
		else:
			self.desc.set_weight(pango.STRETCH_EXPANDED)
		self.image.modify_font(self.desc)

	def set_emphasized(self, emph):
		self.emph = emph
		self.desc.set_weight(self.emph and pango.WEIGHT_BOLD or pango.WEIGHT_NORMAL)
		self.image.modify_font(self.desc)
		
	def plot(self, x, y, on=True):
		if on:
			self.pixmap.draw_point(self.gc, self.margins[0]+x, self.margins[1]+y)
			self.pixmap.draw_point(self.gc2, self.margins[0]+x, self.margins[1]+y+1)
			#self.image.queue_draw()

	def plotchar(self, ch, x, y):
		layout = self.image.create_pango_layout(ch)
		self.pixmap.draw_layout(self.gc, int(self.margins[0]+x), \
								int(self.margins[1]+y), layout)
		#self.image.queue_draw()
		
	def render_line(self):
		self.printinfo.parse(self.feeder())
		if self.printinfo.end_of_page:
			if self.pagenum < 20:
				self.new_page()
			else:
				self.dataptr = len(self.data)
		self.image.queue_draw()
		
	def done(self):
		return self.dataptr >= len(self.data)

def printer(canvas):
	canvas.render_line()
	return not canvas.done()

idles = []
for file in sys.argv[1:]:
	printinfo = TiPrinter()
	canvas = PrintCanvas(file, printinfo)
	while printer(canvas):
		pass

gtk.main()

for x in idles:
	gtk.idle_remove(x)
