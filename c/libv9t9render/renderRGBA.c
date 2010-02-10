
/**
 * Provides faster rendering support for V9t9j.
 *
 * The Java code manages an ImageData struct with RGB data.  We take that,
 * manipulate it to make it look like a monitor, then return that
 * for SDL to render.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "render.h"

#include "noisyRGBA.h"
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
		for (x = 0; x < destWidth; x++) {
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
		blendRows(destptr + destrowstride, destWidth, destrowstride);

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
		blendRows(destptr + destrowstride * 2, destWidth, destrowstride * 2);		// 2=(0+4)/2
		blendRowsL(destptr + destrowstride, destWidth, -destrowstride, destrowstride);	// 1=(0+2)/2
		blendRowsR(destptr + destrowstride * 3, destWidth, -destrowstride, destrowstride);	// 3=(2+4)/2

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

	// we cheat a little here
	while (cnt >= 2) {
		// draw first/second row
		(*scaleRow)(destptr, srcptr, width);
		memcpy(destptr + destrowstride, destptr, destWidth * 4);
		// draw fifth/sixth row
		(*scaleRow)(destptr + destrowstride * 5, srcptr + rowstride, width);
		memcpy(destptr + destrowstride * 6, destptr + destrowstride * 5, destWidth * 4);
		// blend them
		blendRowsL(destptr + destrowstride * 2, destWidth, -destrowstride, destrowstride * 3);
			// 2=(1+5)/2
		blendRows(destptr + destrowstride * 3, destWidth, destrowstride * 3);
			// 3=(0+6)/2
		blendRowsR(destptr + destrowstride * 4, destWidth, -destrowstride * 3, destrowstride);
			// 4=(1+5)/2

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


#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
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
