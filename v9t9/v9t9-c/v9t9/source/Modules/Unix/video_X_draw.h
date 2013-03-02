/*
  video_X_draw.h				-- some unrolled loops for blitting 8x8 blocks

  (c) 1994-2011 Edward Swartz

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

/*
  $Id$
 */

typedef void (*drawpixelsfunc)(u8 * dst, const u8 * src, int width);

#define SET8(f,x,y) dst[j*f+y*vwxsz+x] = c
#define SET16(f,x,y) ((u16 *)dst)[j*f+y*vwxsz+x] = c
#define SET24(f,x,y) \
    dst[j*3*f+y*vwxsz*3+x*3] = c1; \
    dst[j*3*f+y*vwxsz*3+x*3+1] = c2; \
    dst[j*3*f+y*vwxsz*3+x*3+2] = c3
#define SET32(f,x,y) ((u32 *)dst)[j*f+y*vwxsz+x] = c

static void
drawpixels_8_1x1(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u8          c = cmapping(src[j]);

		SET8(1, 0, 0);
	}
}
static void
drawpixels_16_1x1(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u16         c = cmapping(src[j]);

		SET16(1, 0, 0);
	}
}
static void
drawpixels_24_1x1(u8 * dst, const u8 * src, int width)
{
	int         j;

	if (x11_24_order == MSBFirst)
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = (c >> 16) & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = c & 0xff;
		SET24(1, 0, 0);
	} 
	else 
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = c & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = (c >> 16) & 0xff;
		SET24(1, 0, 0);
	}
}
static void
drawpixels_32_1x1(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u32         c = cmapping(src[j]);

		SET32(1, 0, 0);
	}
}

static void
drawpixels_8_2x2(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u8          c = cmapping(src[j]);

		SET8(2, 0, 0);
		SET8(2, 0, 1);
		SET8(2, 1, 0);
		SET8(2, 1, 1);
	}
}
static void
drawpixels_16_2x2(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u16         c = cmapping(src[j]);

		SET16(2, 0, 0);
		SET16(2, 0, 1);
		SET16(2, 1, 0);
		SET16(2, 1, 1);
	}
}
static void
drawpixels_24_2x2(u8 * dst, const u8 * src, int width)
{
	int         j;

	if (x11_24_order == MSBFirst)
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = (c >> 16) & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = c & 0xff;
		SET24(2, 0, 0);
		SET24(2, 0, 1);
		SET24(2, 1, 0);
		SET24(2, 1, 1);
	}
	else
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = c & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = (c >> 16) & 0xff;
		SET24(2, 0, 0);
		SET24(2, 0, 1);
		SET24(2, 1, 0);
		SET24(2, 1, 1);
	}
}
static void
drawpixels_32_2x2(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u32         c = cmapping(src[j]);

		SET32(2, 0, 0);
		SET32(2, 0, 1);
		SET32(2, 1, 0);
		SET32(2, 1, 1);
	}
}

static void
drawpixels_8_3x3(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u8          c = cmapping(src[j]);

		SET8(3, 0, 0);
		SET8(3, 0, 1);
		SET8(3, 0, 2);
		SET8(3, 1, 0);
		SET8(3, 1, 1);
		SET8(3, 1, 2);
		SET8(3, 2, 0);
		SET8(3, 2, 1);
		SET8(3, 2, 2);
	}
}
static void
drawpixels_16_3x3(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u16         c = cmapping(src[j]);

		SET16(3, 0, 0);
		SET16(3, 0, 1);
		SET16(3, 0, 2);
		SET16(3, 1, 0);
		SET16(3, 1, 1);
		SET16(3, 1, 2);
		SET16(3, 2, 0);
		SET16(3, 2, 1);
		SET16(3, 2, 2);
	}
}
static void
drawpixels_24_3x3(u8 * dst, const u8 * src, int width)
{
	int         j;
   
	if (x11_24_order == MSBFirst)
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = (c >> 16) & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = c & 0xff;
		SET24(3, 0, 0);
		SET24(3, 0, 1);
		SET24(3, 0, 2);
		SET24(3, 1, 0);
		SET24(3, 1, 1);
		SET24(3, 1, 2);
		SET24(3, 2, 0);
		SET24(3, 2, 1);
		SET24(3, 2, 2);
	}
	else
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = c & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = (c >> 16) & 0xff;
		SET24(3, 0, 0);
		SET24(3, 0, 1);
		SET24(3, 0, 2);
		SET24(3, 1, 0);
		SET24(3, 1, 1);
		SET24(3, 1, 2);
		SET24(3, 2, 0);
		SET24(3, 2, 1);
		SET24(3, 2, 2);
	}
}
static void
drawpixels_32_3x3(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u32         c = cmapping(src[j]);

		SET32(3, 0, 0);
		SET32(3, 0, 1);
		SET32(3, 0, 2);
		SET32(3, 1, 0);
		SET32(3, 1, 1);
		SET32(3, 1, 2);
		SET32(3, 2, 0);
		SET32(3, 2, 1);
		SET32(3, 2, 2);
	}
}


static void
drawpixels_8_4x4(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u8          c = cmapping(src[j]);

		SET8(4, 0, 0);
		SET8(4, 0, 1);
		SET8(4, 0, 2);
		SET8(4, 0, 3);
		SET8(4, 1, 0);
		SET8(4, 1, 1);
		SET8(4, 1, 2);
		SET8(4, 1, 3);
		SET8(4, 2, 0);
		SET8(4, 2, 1);
		SET8(4, 2, 2);
		SET8(4, 2, 3);
		SET8(4, 3, 0);
		SET8(4, 3, 1);
		SET8(4, 3, 2);
		SET8(4, 3, 3);
	}
}
static void
drawpixels_16_4x4(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u16         c = cmapping(src[j]);

		SET16(4, 0, 0);
		SET16(4, 0, 1);
		SET16(4, 0, 2);
		SET16(4, 0, 3);
		SET16(4, 1, 0);
		SET16(4, 1, 1);
		SET16(4, 1, 2);
		SET16(4, 1, 3);
		SET16(4, 2, 0);
		SET16(4, 2, 1);
		SET16(4, 2, 2);
		SET16(4, 2, 3);
		SET16(4, 3, 0);
		SET16(4, 3, 1);
		SET16(4, 3, 2);
		SET16(4, 3, 3);
	}
}
static void
drawpixels_24_4x4(u8 * dst, const u8 * src, int width)
{
	int         j;

	if (x11_24_order == MSBFirst)
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = (c >> 16) & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = c & 0xff;
		SET24(4, 0, 0);
		SET24(4, 0, 1);
		SET24(4, 0, 2);
		SET24(4, 0, 3);
		SET24(4, 1, 0);
		SET24(4, 1, 1);
		SET24(4, 1, 2);
		SET24(4, 1, 3);
		SET24(4, 2, 0);
		SET24(4, 2, 1);
		SET24(4, 2, 2);
		SET24(4, 2, 3);
		SET24(4, 3, 0);
		SET24(4, 3, 1);
		SET24(4, 3, 2);
		SET24(4, 3, 3);
	}
	else
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = c & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = (c >> 16) & 0xff;
		SET24(4, 0, 0);
		SET24(4, 0, 1);
		SET24(4, 0, 2);
		SET24(4, 0, 3);
		SET24(4, 1, 0);
		SET24(4, 1, 1);
		SET24(4, 1, 2);
		SET24(4, 1, 3);
		SET24(4, 2, 0);
		SET24(4, 2, 1);
		SET24(4, 2, 2);
		SET24(4, 2, 3);
		SET24(4, 3, 0);
		SET24(4, 3, 1);
		SET24(4, 3, 2);
		SET24(4, 3, 3);
	}
}
static void
drawpixels_32_4x4(u8 * dst, const u8 * src, int width)
{
	int         j;

	for (j = 0; j < width; j++) {
		u32         c = cmapping(src[j]);

		SET32(4, 0, 0);
		SET32(4, 0, 1);
		SET32(4, 0, 2);
		SET32(4, 0, 3);
		SET32(4, 1, 0);
		SET32(4, 1, 1);
		SET32(4, 1, 2);
		SET32(4, 1, 3);
		SET32(4, 2, 0);
		SET32(4, 2, 1);
		SET32(4, 2, 2);
		SET32(4, 2, 3);
		SET32(4, 3, 0);
		SET32(4, 3, 1);
		SET32(4, 3, 2);
		SET32(4, 3, 3);
	}
}

static void
drawpixels_8_XxY(u8 * dst, const u8 * src, int width)
{
	int         i, j, k;

	for (j = 0; j < width; j++) {
		u8          c = cmapping(src[j]);

		for (k = 0; k < vwym; k++) 
			for (i = 0; i < vwxm; i++)
				dst[j * vwxm + k * vwxsz + i] = c;
	}
}
static void
drawpixels_16_XxY(u8 * dst, const u8 * src, int width)
{
	int         j, i, k;

	for (j = 0; j < width; j++) {
		u16         c = cmapping(src[j]);

		for (k = 0; k < vwym; k++)
			for (i = 0; i < vwxm; i++)
				((u16 *) dst)[j * vwxm + k * vwxsz + i] = c;
	}
}
static void
drawpixels_24_XxY(u8 * dst, const u8 * src, int width)
{
	int         j, i, k;

	if (x11_24_order == MSBFirst)
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = (c >> 16) & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = c & 0xff;
		for (k = 0; k < vwym; k++)
			for (i = 0; i < vwxm; i++) {
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3] = c1;
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3 + 1] = c2;
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3 + 2] = c3;
			}
	}
	for (j = 0; j < width; j++) {
		unsigned long c = cmapping(src[j]);
		u8          c1, c2, c3;

		c1 = c & 0xff;
		c2 = (c >> 8) & 0xff;
		c3 = (c >> 16) & 0xff;
		for (k = 0; k < vwym; k++) 
			for (i = 0; i < vwxm; i++) {
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3] = c1;
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3 + 1] = c2;
				dst[j * 3 * vwxm + k * vwxsz * 3 + i * 3 + 2] = c3;
			}
	}
}
static void
drawpixels_32_XxY(u8 * dst, const u8 * src, int width)
{
	int         j, i, k;

	for (j = 0; j < width; j++) {
		u32         c = cmapping(src[j]);

		for (k = 0; k < vwym; k++)
			for (i = 0; i < vwxm; i++)
				((u32 *) dst)[j * vwxm + k * vwxsz + i] = c;
	}
}

static drawpixelsfunc drawpixels_NxN[4][4] = {
	{drawpixels_8_1x1, drawpixels_16_1x1, drawpixels_24_1x1, drawpixels_32_1x1},
	{drawpixels_8_2x2, drawpixels_16_2x2, drawpixels_24_2x2, drawpixels_32_2x2},
	{drawpixels_8_3x3, drawpixels_16_3x3, drawpixels_24_3x3, drawpixels_32_3x3},
	{drawpixels_8_4x4, drawpixels_16_4x4, drawpixels_24_4x4, drawpixels_32_4x4}
};

static drawpixelsfunc drawpixels_XxY[] = {
	drawpixels_8_XxY,
	drawpixels_16_XxY,
	drawpixels_24_XxY,
	drawpixels_32_XxY
};

