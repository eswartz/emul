/*
  render.h

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
