/*
 * render.h
 *
 *  Created on: Nov 28, 2008
 *      Author: ejs
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
__declspec(dllexport) __stdcall
#endif
struct AnalogTV* allocateAnalogTv(int width, int height);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void freeAnalogTv(struct AnalogTV* );


#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
struct analogtv_s* getAnalogTvData(AnalogTV *analog);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void 		analogizeImageData(
		AnalogTV* analog,
		char* byteArray, int srcoffset, int width, int height, int rowstride);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void        scaleImage(
		char* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void        scaleImageToRGBA(
		int* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void        scaleImageAndAddNoiseToRGBA(
		int* dest,
        const char* from, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);


#endif /* RENDER_H_ */
