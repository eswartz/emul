/*
  render.h

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
#ifndef RENDER_H_
#define RENDER_H_

/*
long long 	newGdkPixbuf(int width, int height);
int			gdkPixbufHeight(long long pixbuf_);
int			gdkPixbufWidth(long long pixbuf_);
int			gdkPixbufBytesPerLine(long long pixbuf_);
void		copyToGdkPixbuf(long long pixbuf_, unsigned char* data);
void 		renderGdkPixbuf(long long pixbuf_, long long gdkWindow_);
*/

struct analogtv_s;
struct analogtv_input_s;
struct analogtv_reception_s;

struct AnalogTV {
	struct analogtv_s* tv;
	struct analogtv_input_s* input;
	struct analogtv_reception_s* rec;
};

typedef struct AnalogTV AnalogTV;

#ifdef _WIN32
#define _EXPORT __declspec(dllexport) __cdecl
#else
#define _EXPORT
#endif


_EXPORT struct AnalogTV* allocateAnalogTv(int width, int height);

_EXPORT void freeAnalogTv(struct AnalogTV* );


_EXPORT struct analogtv_s* getAnalogTvData(AnalogTV *analog);

_EXPORT void 		analogizeImageData(
		AnalogTV* analog,
		char* byteArray, int srcoffset, int width, int height, int rowstride);

_EXPORT void        scaleImage(
		char* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

_EXPORT void        scaleImageToRGBA(
		int* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

_EXPORT void        scaleImageAndAddNoiseToRGBA(
		int* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);


#endif /* RENDER_H_ */
