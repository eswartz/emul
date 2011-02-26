
#!/usr/bin/env python

import sys, traceback, time, string
from gimpfu import *

def produce_char(frgn, xx, yy, charwidth, charheight):
	st = "\tdb "
	for y in range(yy, yy+charheight):
		if y - yy >= charheight:
			st += ", >0"
		else:
			if y != yy:
				st += ", "
			byte = 0
			mask = 0x80
			for x in range(xx, xx+charwidth):
				#print ">>",frgn[x,y]
				val = ord(frgn[x,y][0])
				if val != 0:
					byte |= mask
				mask >>= 1
			st += ">"+hex(byte)[2:]
	print st
	
def fontmap(image, drawable, charwidth, charheight):
	# get the desired data into a new image
	width = drawable.width
	height = drawable.height

	print "Table:\n\n"
	try:
		gimp.tile_cache_ntiles(2 * width)
		frgn = drawable.get_pixel_rgn(0, 0, width, height, False, False)
		for y in range(0, height, charheight):
			for x in range(0, width, charwidth):
				produce_char(frgn, x, y, charwidth, charheight)

	except:
		print "!!! FAILED !!!"
		traceback.print_exc()
		pdb.gimp_image_undo_group_end(image)
		return

	return

# Register with The Gimp
register(
	"plug_in_ti_font_map",
	"TI Font Map",
	"Create a TI font map from an image.",
	"Ed Swartz",
	"(c) 2008, Ed Swartz",
	"2006-10-20",
	"<Image>/Filters/Map/TI Font Map",
	"INDEXED*",
		 [
			(PF_INT, "charwidth", "Char width", 5 ),
			(PF_INT, "charheight", "Char height", 6 ),
		  ],#controls
		 [],
			fontmap)  # how

main()



