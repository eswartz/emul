#include <stdio.h>
#include <string.h>
#include "noisy.h"



static void darken_row(unsigned char *row, int width, int rowstride, int mulfac, int blend) {
	int i;
	// produce a dark line between rows
	int mulfac2 = mulfac - 32;
	for (i = 0; i < width * 4; i ++) {
		row[i] = (row[i] * mulfac2)>>8;
	}
}

static void darken_pixels(unsigned char *row,int width, int realWidth, int mulfac) {
	int c;
	// produce dark lines between pixels
	for (c = 0; c < realWidth; c++) {
		int i = c * width * 4 / realWidth + 3;
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


void addNoiseRGBA(unsigned char *dststart, unsigned char *srcstart,
		int offset, int end,
		int width, int height, int rowstride,
		int realWidth, int realHeight, int fullHeight) {
	if (!realHeight || height < realHeight  || width < realWidth)
		return;

	//int end = (fullHeight * rowstride);
	if (height == realHeight && width == realWidth) {
		int r;
		for (r = 0; r < height && offset < end; r++, offset += rowstride) {
			memcpy(dststart + offset, srcstart + offset, width * 4);
		}
		return;
	}


	int mult = height / realHeight;
	int mulfac;
	switch (mult) {
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


	int colmulfac = mulfac + (256 - mulfac) / 2;
	if (colmulfac > 255) colmulfac = 255;
	int altmulfac = 256 - (256 - mulfac) / 4;

	int r, ro = 0;
	if (mult > 1)
		ro = (offset / rowstride) % mult;
	for (r = 0; r < height && offset < end; r++, offset += rowstride) {
		memcpy(dststart + offset, srcstart + offset, width * 4);

		//	printf("%d %p-%p %d %d\n", r, dststart + offset, dststart + end, width * 4, realWidth); fflush(stdout);
		if (ro == 0) {
			// darken between rows
			darken_row(dststart + offset, width, rowstride, mulfac, 1);

		}
		if (mult >= 4 && ro == mult - 1) {
			darken_row(dststart + offset,
					width, rowstride, altmulfac, 1);
		}
		if (width > realWidth && offset >= 4) {
			darken_pixels(dststart + offset, width, realWidth, colmulfac);
		}
		if (++ro == mult)
			ro = 0;
	}

}


#if 0
#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif

void addNoiseRGBAxx(int* idata, int offset,
		int width, int height, int rowstride,
		int realWidth, int realHeight, int fullHeight) {
	//printf("%p %d %dx%d %d ...\n", idata, offset, width, height, rowstride);

	if (height < realHeight  || width < realWidth)
		return;

	if (height <= realHeight && width <= realWidth)
		return;

	unsigned char* start = (unsigned char*) idata;
	unsigned char *end = start + (fullHeight * rowstride);

	idata += offset / 4;
	int rr, c, ir;

	unsigned char *data = start + offset;

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

	int colmulfac = mulfac + (256 - mulfac) / 2;
	if (colmulfac > 255) colmulfac = 255;

	for (rr = 0; rr < realHeight; rr++) {
		int r = rr * height / realHeight;
		// darken between rows
		unsigned char* target = data + rowstride * r;

		if (target >= start && target + rowstride < end)
			darken_row(target, width, rowstride, mulfac, 1); //rr > 0 && rr < realHeight - 1);

		if (height >= realHeight * 4) {
			int altmulfac = 256 - (256 - mulfac) / 4;
			if (target - rowstride >= start) {
				darken_row(target - rowstride, width, rowstride, altmulfac, 1);
			}
		}

		if (width > realWidth) {
			for (ir = pr; ir < r; ir++) {
				target = data + rowstride * ir;
				if (target >= start && target + rowstride < end)
					darken_pixels(target, width, realWidth, colmulfac);
			}
		}

		pr = r;
	}

}
#endif
