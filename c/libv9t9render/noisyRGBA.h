

static void darken_row(unsigned char *row, int width, int rowstride, int mulfac, int blend) {
	int i;
	// produce a dark line between rows
	unsigned char *next = row + rowstride;
	unsigned char *prev = row - rowstride;
	int mulfac2 = mulfac - 32;
	for (i = 0; i < width * 4; i ++) {
		row[i] = (row[i] * mulfac2)>>8;
		//next[i] = (next[i] * mulfac + row[i] * (256 - mulfac)) >> 8;
		//prev[i] = (prev[i] * mulfac + row[i] * (256 - mulfac)) >> 8;
	}
	/*
	// blend the edges of the pixels with the dark line
	if (blend && mulfac < 248) {
		mulfac /= 2;
		unsigned char *next = row + rowstride;
		for (i = 0; i < width * 3; i++) {
			next[i] = (next[i] * mulfac + row[i] * (256 - mulfac)) / 256;
		}
		unsigned char *prev = row - rowstride;
		for (i = 0; i < width * 3; i++) {
			prev[i] = (prev[i] * mulfac + row[i] * (256 - mulfac)) / 256;
		}
	}
	*/
}

static void blur_row(unsigned char *row, int width, int rowstride, int mulfac, int blend) {
	int i;
	// produce a dark line between rows
	unsigned char *next = row + rowstride;
	unsigned char *prev = row - rowstride;
	for (i = 0; i < width * 4; i ++) {
		next[i] = (next[i] + row[i] ) >> 1;
		prev[i] = (prev[i] + row[i]) >> 1;
	}
}

static void darken_pixels(unsigned char *row, int width, int realWidth, int mulfac) {
	int c;
	// produce dark lines between pixels
	for (c = 0; c < realWidth; c++) {
		int i = c * width * 4 / realWidth;
		row[i] = (row[i] * mulfac) >> 8;
		row[i+1] = (row[i+1] * mulfac) >> 8;
		row[i+2] = (row[i+2] * mulfac) >> 8;
		row[i+3] = (row[i+3] * mulfac) >> 8;
		row[i-4] = (row[i-4] + row[i]) >> 1;
		row[i-3] = (row[i-3] + row[i+1]) >> 1;
		row[i-2] = (row[i-2] + row[i+2]) >> 1;
		row[i-1] = (row[i-1] + row[i+3]) >> 1;
	}
	/*
	// blur edges of pixels together
	for (c = 1; c < realWidth - 1; c++) {
		int i = c * width * 3 / realWidth;
		row[i-3] = (row[i-3] + row[i]) >> 1;
		row[i-2] = (row[i-2] + row[i+1]) >> 1;
		row[i-1] = (row[i-1] + row[i+2]) >> 1;
	}*/
}


#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void addNoiseRGBA(unsigned int *idata, int offset, int width, int height, int rowstride,
		int realWidth, int realHeight) {
	//printf("%p %d %dx%d %d ...\n", idata, offset, width, height, rowstride);

	idata += offset / 4;
	int rr, c, ir;

	unsigned char *data = (unsigned char*)idata;

	if (height < realHeight  || width < realWidth)
		return;

	if (height <= realHeight && width <= realWidth)
		return;

	//printf("rowstep=%d, colstep=%d\n", rowstep, colstep);

	// For drawing into a row, don't use direct rowstride, since the
	// last row is smaller than we expect -- only use the known width.

	int pr = 1;
	int mulfac;
	switch (height / realHeight) {
	case 1:
		mulfac = 248;
		break;
	case 2:
		mulfac = 240;
		break;
	case 3:
		mulfac = 230;
		break;
	case 4:
	default:
		mulfac = 220;
		break;
	}
	//int mulfac = 256 - (int)(pow(1.5, 4+(double)height / realHeight));
	//if (mulfac < 128) mulfac = 128;
	//printf("mulfac=%d\n",mulfac);
	int colmulfac = mulfac + (256 - mulfac) / 2;
	if (colmulfac > 255) colmulfac = 255;

	for (rr = 1; rr < realHeight; rr++) {
		int r = rr * height / realHeight;
		// darken between rows

		/*
		int ir;
		for (ir = -1; ir < 2; ir++) {
			if (r + ir >= 0 && r + ir < height) {
				int nr = (r + ir) * realHeight / height;
				if (1||nr != r) {
					int mulfac = !ir ? 192 : 224;
					darken_row(data + rowstride * (r + ir), width * 3, mulfac);
				}
			}
		}
		for (ir = pr; ir < r; ir++) {
			darken_pixels(data + rowstride * ir, width, realWidth, 224);
		}
		*/

		darken_row(data + rowstride * r, width, rowstride, mulfac, 1); //rr > 0 && rr < realHeight - 1);
		if (height >= realHeight * 4) {
			int altmulfac = 256 - (256 - mulfac) / 4;
			if (r > 2) {
				darken_row(data + rowstride * (r - 1), width, rowstride, altmulfac, 1); //rr > 0 && rr < realHeight - 1);
				//blur_row(data + rowstride * (r - 1), width, rowstride, mulfac, 1); //rr > 0 && rr < realHeight - 1);
			}
			if (0 && r < height - 2) {
				darken_row(data + rowstride * (r + 1), width, rowstride, altmulfac, 1); //rr > 0 && rr < realHeight - 1);
				//blur_row(data + rowstride * (r + 1), width, rowstride, mulfac, 1); //rr > 0 && rr < realHeight - 1);
			}
		}

		if (width > realWidth) {
			for (ir = pr; ir < r; ir++) {
				darken_pixels(data + rowstride * ir, width, realWidth, colmulfac);
			}
		}

		pr = r;
	}

}
