
#!/usr/bin/env python

import sys, traceback, time, string
from gimpfu import *

def produce_char(f, frgn, xx, yy, charwidth, charheight):
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
	f.write(st+'\n')
	

def fontmap_save(image, drawable, filename, raw_filename, xx, charwidth, charheight):
	# get the desired data into a new image
	print image,drawable,filename,raw_filename,charwidth,charheight
	width = drawable.width
	height = drawable.height

        f = open(filename, "w")
	f.write("Table:\n\n")
	try:
		gimp.tile_cache_ntiles(2 * width)
		frgn = drawable.get_pixel_rgn(0, 0, width, height, False, False)
		ch = 0
		for y in range(0, height, charheight):
			for x in range(0, width, charwidth):
			        if ch % 8 == 0:
			                f.write("; char " + str(ch) + "\n")
				produce_char(f, frgn, x, y, charwidth, charheight)
				ch += 1
                f.close()
	except:
		print "!!! FAILED !!!"
		f.close()
		traceback.print_exc()
		pdb.gimp_image_undo_group_end(image)
		return

	return



def fontmap_query():
	pdb.gimp_register_save_handler("file_forth99_font_save", "fnt", "")



# Register with The Gimp
register(
	"file_forth99_font_save",
	"Forth99 Font Save",
	"Save a picture to a Forth99 font",
	"Ed Swartz",
	"(c) 2011, Ed Swartz",
	"2011-03-12",
	"<Save>/Forth99",
	"INDEXED",
		 [
		  (PF_RADIO, "sizing0", "Image scaling", 0, (("stretch",0),("scale",1)) ),
			(PF_INT, "charwidth", "Char width", 5 ),
			(PF_INT, "charheight", "Char height", 6 ),
		  ],#controls
	[], #output
	fontmap_save,
	on_query = fontmap_query)

main()





