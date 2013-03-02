/*
  renderRGBA.c

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "render.h"

#define SWAPPED_RGB 0
#if SWAPPED_RGB

#define COPY(n,s)	do { \
		dest[n*3] = src[s*3+2];	dest[n*3+1] = src[s*3+1];	dest[n*3+2] = src[s*3+0]; } while(0)

#define BLEND(n, s)	do { \
		dest[n*3] = (src[s*3+2]+src[s*3+5])>>1;	\
		dest[n*3+1] = (src[s*3+1]+src[s*3+4])>>1 ;\
		dest[n*3+2] = (src[s*3+0]+src[s*3+3])>>1; \
		} while(0)

#define BLENDL(n, s)	do { \
		dest[n*3] = (src[s*3+2]*3+src[s*3+5])>>2;	\
		dest[n*3+1] = (src[s*3+1]*3+src[s*3+4])>>2;\
		dest[n*3+2] = (src[s*3+0]*3+src[s*3+3])>>2; \
		} while(0)

#define BLENDR(n, s)	do { \
		dest[n*3] = (src[s*3+2]+src[s*3+5]*3)>>2;	\
		dest[n*3+1] = (src[s*3+1]+src[s*3+4]*3)>>2;\
		dest[n*3+2] = (src[s*3+0]+src[s*3+3]*3)>>2; \
		} while(0)

#else

#define COPY(n,s)	do { \
		dest[n*4] = src[s*3];	dest[n*4+1] = src[s*3+1];	dest[n*4+2] = src[s*3+2]; } while(0)

#define BLEND(n, s)	do { \
		dest[n*4] = (src[s*3+0]+src[s*3+3])>>1;	\
		dest[n*4+1] = (src[s*3+1]+src[s*3+4])>>1 ;\
		dest[n*4+2] = (src[s*3+2]+src[s*3+5])>>1; \
		} while(0)

#define BLENDL(n, s)	do { \
		dest[n*4] = (src[s*3+0]*3+src[s*3+3])>>2;	\
		dest[n*4+1] = (src[s*3+1]*3+src[s*3+4])>>2;\
		dest[n*4+2] = (src[s*3+2]*3+src[s*3+5])>>2; \
		} while(0)

#define BLENDR(n, s)	do { \
		dest[n*4] = (src[s*3+0]+src[s*3+3]*3)>>2;	\
		dest[n*4+1] = (src[s*3+1]+src[s*3+4]*3)>>2;\
		dest[n*4+2] = (src[s*3+2]+src[s*3+5]*3)>>2; \
		} while(0)


#endif

static void scaleRow_1_2(unsigned char* dest, const unsigned char* src, int cnt) {
	// .5x scaling: fill 1 dest pixels with 2 src pixels
	while (cnt >= 2) {
		// blend between two src
		BLEND(0, 0);

		src += 6;
		dest += 4;
		cnt -= 2;
	}
}


static void scaleRow_1(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		dest += 4;
		src += 3;
	}
}

static void scaleRow_3_2(unsigned char* dest, const unsigned char* src, int cnt) {
	// 1.5x scaling: fill 3 dest pixels with 2 src pixels
	while (cnt >= 2) {
		// direct copy from src
		COPY(0, 0);
		// blend between two src
		BLEND(1, 0);
		// direct copy from src
		COPY(2, 1);

		src += 6;
		dest += 12;
		cnt -= 2;
	}
	if (cnt > 0)
		COPY(0, 0);
}

static void scaleRow_2(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		dest += 8;
		src += 3;
	}
}

static void scaleRow_5_2(unsigned char* dest, const unsigned char* src, int cnt) {
	// 2.5x scaling: fill 5 dest pixels with 2 src pixels
	while (cnt >= 2) {
		// direct copy from src
		COPY(0, 0);
		// blend between two src
		BLENDL(1, 0);
		BLEND(2, 0);
		BLENDR(3, 0);
		// direct copy from src
		COPY(4, 1);

		dest += 20;
		src += 6;
		cnt -= 2;
	}
	if (cnt > 0)
		COPY(0, 0);
}

static void scaleRow_3(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		dest += 12;
		src += 3;
	}
}

static void scaleRow_7_2(unsigned char* dest, const unsigned char* src, int cnt) {
	// 3.5x scaling: fill 7 dest pixels with 2 src pixels
	while (cnt >= 2) {
		// direct copy from src
		COPY(0, 0);
		COPY(1, 0);
		// blend between two src
		BLENDL(2, 0);
		BLEND(3, 0);
		BLENDR(4, 0);
		// direct copy from src
		COPY(5, 1);
		COPY(6, 1);

		dest += 28;
		src += 6;
		cnt -= 2;
	}
	if (cnt > 0)
		COPY(0, 0);
}

static void scaleRow_4(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		COPY(3, 0);
		dest += 16;
		src += 3;
	}
}


static void scaleRow_9_2(unsigned char* dest, const unsigned char* src, int cnt) {
	// 4.5x scaling: fill 9 dest pixels with 2 src pixels
	while (cnt >= 2) {
		// direct copy from src
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		// blend between two src
		BLENDL(3, 0);
		BLEND(4, 0);
		BLENDR(5, 0);
		// direct copy from src
		COPY(6, 1);
		COPY(7, 1);
		COPY(8, 1);

		dest += 36;
		src += 6;
		cnt -= 2;
	}
	if (cnt > 0)
		COPY(0, 0);
}
static void scaleRow_5(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		COPY(3, 0);
		COPY(4, 0);
		dest += 20;
		src += 3;
	}
}


static void scaleRow_6(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		COPY(3, 0);
		COPY(4, 0);
		COPY(5, 0);
		dest += 24;
		src += 3;
	}
}

static void scaleRow_7(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		COPY(3, 0);
		COPY(4, 0);
		COPY(5, 0);
		COPY(6, 0);
		dest += 28;
		src += 3;
	}
}

static void scaleRow_8(unsigned char* dest, const unsigned char* src, int cnt) {
	while (cnt--) {
		COPY(0, 0);
		COPY(1, 0);
		COPY(2, 0);
		COPY(3, 0);
		COPY(4, 0);
		COPY(5, 0);
		COPY(6, 0);
		COPY(7, 0);
		dest += 32;
		src += 3;
	}
}

static void blendRows(unsigned char *mid, int cnt, int offset) {
	while (cnt--) {
		mid[0] = (mid[-offset] + mid[offset]) >> 1;
		mid++;
	}
}

static void blendRows2(unsigned char *mid, int cnt, int offsetup, int offsetdown) {
	while (cnt--) {
		mid[0] = (mid[offsetup] + mid[offsetdown]) >> 1;
		mid++;
	}
}

static void blendRowsL(unsigned char *mid, int cnt, int offsetup, int offsetdown) {
	while (cnt--) {
		mid[0] = (mid[offsetup] * 3 + mid[offsetdown]) >> 2;
		mid++;
	}
}
static void blendRowsR(unsigned char *mid, int cnt, int offsetup, int offsetdown) {
	while (cnt--) {
		mid[0] = (mid[offsetup] + mid[offsetdown] * 3) >> 2;
		mid++;
	}
}

unsigned char tmp[4096];

static void iterRow_1_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	// scale .5x:  emit 1 dest row for every two src rows

	while (cnt >= 2) {
		// draw first row
		(*scaleRow)(tmp, srcptr, width);
		// draw third row
		(*scaleRow)(tmp + destrowstride, srcptr + rowstride, width);
		// blend them
		int x;
		for (x = 0; x < width * 2; x++) {
			destptr[x] = (tmp[x] + tmp[x + destrowstride]) >> 1;
		}
		destptr += destrowstride;
		srcptr += rowstride * 2;
		cnt -= 2;
	}
}

static void iterRow_1(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		srcptr += rowstride;
	}
}

static void iterRow_3_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	// scale 1.5x:  emit three dest rows for every two src rows
	while (cnt >= 2) {
		// draw first row
		(*scaleRow)(destptr, srcptr, width);
		// draw third row
		(*scaleRow)(destptr + destrowstride * 2, srcptr + rowstride, width);
		// blend them
		blendRows(destptr + destrowstride, destWidth * 4, destrowstride);

		destptr += destrowstride * 3;
		srcptr += rowstride * 2;
		cnt -= 2;
	}
}

static void iterRow_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		srcptr += rowstride;
	}
}

static void iterRow_5_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	// scale 2.5x:  emit five dest rows for every two src rows
	while (cnt >= 2) {
		// draw first row
		(*scaleRow)(destptr, srcptr, width);
		// draw fifth row
		(*scaleRow)(destptr + destrowstride * 4, srcptr + rowstride, width);
		// blend them
		blendRows(destptr + destrowstride * 2, destWidth * 4, destrowstride * 2);		// 2=(0+4)/2
		blendRowsL(destptr + destrowstride, destWidth * 4, -destrowstride, destrowstride);	// 1=(0+2)/2
		blendRowsR(destptr + destrowstride * 3, destWidth * 4, -destrowstride, destrowstride);	// 3=(2+4)/2

		destptr += destrowstride * 5;
		srcptr += rowstride * 2;
		cnt -= 2;
	}
}


static void iterRow_3(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		srcptr += rowstride;
	}
	//printf("out: src=%p, dest=%p\n", srcptr, destptr);

}


static void iterRow_7_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	// scale 3.5x:  emit seven dest rows for every two src rows

	// 0 A
	// 1 A
	// 2 A/B
	// 3 A/B
	// 4 A/B
	// 5 B
	// 6 B
	while (cnt >= 2) {
		// draw first/second row
		(*scaleRow)(destptr, srcptr, width);
		memcpy(destptr + destrowstride, destptr, destWidth * 4);
		// draw fifth/sixth row
		(*scaleRow)(destptr + destrowstride * 5, srcptr + rowstride, width);
		memcpy(destptr + destrowstride * 6, destptr + destrowstride * 5, destWidth * 4);
		// blend them
		blendRows2(destptr + destrowstride * 3, destWidth * 4, -destrowstride * 2, destrowstride * 2);
			// 3=(1+5)/2
		blendRows2(destptr + destrowstride * 2, destWidth * 4, -destrowstride, destrowstride);
			// 2=(1+3)/2
		blendRows2(destptr + destrowstride * 4, destWidth * 4, -destrowstride, destrowstride);
			// 4=(3+5)/2

		destptr += destrowstride * 7;
		srcptr += rowstride * 2;
		cnt -= 2;
	}
}

static void iterRow_4(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;

		srcptr += rowstride;
	}
}


static void iterRow_9_2(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	// scale 4.5x:  emit nine dest rows for every two src rows


	// 0 A
	// 1 A
	// 2 A
	// 3 A/B
	// 4 A/B
	// 5 A/B
	// 6 B
	// 7 B
	// 8 B

	// we cheat a little here
	while (cnt >= 2) {
		// draw first/second/third row
		(*scaleRow)(destptr, srcptr, width);
		memcpy(destptr + destrowstride, destptr, destWidth * 4);
		memcpy(destptr + destrowstride * 2, destptr, destWidth * 4);
		// draw fifth/sixth row
		(*scaleRow)(destptr + destrowstride * 6, srcptr + rowstride, width);
		memcpy(destptr + destrowstride * 7, destptr + destrowstride * 6, destWidth * 4);
		memcpy(destptr + destrowstride * 8, destptr + destrowstride * 6, destWidth * 4);
		// blend them
		blendRows2(destptr + destrowstride * 4, destWidth * 4, -destrowstride * 2, destrowstride * 2);
			// 4=(2+6)/2
		blendRows2(destptr + destrowstride * 3, destWidth * 4, -destrowstride, destrowstride);
			// 3=(2+4)/2
		blendRows2(destptr + destrowstride * 5, destWidth * 4, -destrowstride, destrowstride);
			// 5=(1+5)/2

		destptr += destrowstride * 9;
		srcptr += rowstride * 2;
		cnt -= 2;
	}
}

static void iterRow_5(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;
		memcpy(destptr, destptr - destrowstride, destWidth * 4);
		destptr += destrowstride;

		srcptr += rowstride;
	}
}

static void iterRow_6(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		int c = 5;
		while (c--) {
			memcpy(destptr, destptr - destrowstride, destWidth * 4);
			destptr += destrowstride;
		}
		srcptr += rowstride;
	}
}

static void iterRow_7(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		int c = 6;
		while (c--) {
			memcpy(destptr, destptr - destrowstride, destWidth * 4);
			destptr += destrowstride;
		}

		srcptr += rowstride;
	}
}

static void iterRow_8(unsigned char *destptr, const unsigned char* srcptr, int destrowstride, int rowstride,
		void (*scaleRow)(unsigned char*, const unsigned char *, int), int width, int destWidth, int cnt) {
	while (cnt--) {
		(*scaleRow)(destptr, srcptr, width);
		destptr += destrowstride;
		int c = 7;
		while (c--) {
			memcpy(destptr, destptr - destrowstride, destWidth * 4);
			destptr += destrowstride;
		}
		srcptr += rowstride;
	}
}


_EXPORT
void        scaleImageToRGBA(
		int* dest,
        const char* src, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight) {

	src += offset;
	dest += (upy * destRowstride + upx * 4) / 4;
	//printf("width=%d, destWidth=%d; height=%d, destHeight=%d\n", width, destWidth, height, destHeight);
	//printf("in:  src=%p, dest=%p\n", src, dest);
	//fflush(stdout);

	void (*scaleRow)(unsigned char *, const unsigned char*, int);

	if (width * 8 <= destWidth) {
		scaleRow = scaleRow_8;
	} else if (width * 7 <= destWidth) {
		scaleRow = scaleRow_7;
	} else if (width * 6 <= destWidth) {
		scaleRow = scaleRow_6;
	} else if (width * 5 <= destWidth) {
		scaleRow = scaleRow_5;
	} else if (width * 9 / 2 <= destWidth) {
		scaleRow = scaleRow_9_2;
	} else if (width * 4 <= destWidth) {
		scaleRow = scaleRow_4;
	} else if (width * 7 / 2 <= destWidth) {
		scaleRow = scaleRow_7_2;
	} else if (width * 3 <= destWidth) {
		scaleRow = scaleRow_3;
	} else if (width * 5 / 2 <= destWidth) {
		scaleRow = scaleRow_5_2;
	} else if (width * 2 <= destWidth) {
		scaleRow = scaleRow_2;
	} else if (width * 3 / 2 <= destWidth) {
		scaleRow = scaleRow_3_2;
	} else if (width <= destWidth) {
		scaleRow = scaleRow_1;
	} else {
		// don't overshoot
		destWidth = width / 2;
		scaleRow = scaleRow_1_2;
	}

	void (*iterRow)(unsigned char *, const unsigned char*, int, int,
			void (*)(unsigned char*, const unsigned char *, int), int, int, int);

	if (height * 8 <= destHeight) {
		iterRow = iterRow_8;
	} else if (height * 7 <= destHeight) {
		iterRow = iterRow_7;
	} else if (height * 6 <= destHeight) {
		iterRow = iterRow_6;
	} else if (height * 5 <= destHeight) {
		iterRow = iterRow_5;
	} else if (height * 9 / 2 <= destHeight) {
		iterRow = iterRow_9_2;
	} else if (height * 4 <= destHeight) {
		iterRow = iterRow_4;
	} else if (height * 7 / 2 <= destHeight) {
		iterRow = iterRow_7_2;
	} else if (height * 3 <= destHeight) {
		iterRow = iterRow_3;
	} else if (height * 5 / 2 <= destHeight) {
		iterRow = iterRow_5_2;
	} else if (height * 2 <= destHeight) {
		iterRow = iterRow_2;
	} else if (height * 3 / 2 <= destHeight) {
		iterRow = iterRow_3_2;
	} else if (height <= destHeight) {
		iterRow = iterRow_1;
	} else {
		// don't overshoot
		destHeight = height / 2;
		iterRow = iterRow_1_2;
	}

	const unsigned char* srcptr = (const unsigned char *) src;
	unsigned char* destptr = (unsigned char*) dest;
	iterRow(destptr, srcptr, destRowstride, rowstride, scaleRow, width, destWidth, height);

	//fflush(stdout);
}
