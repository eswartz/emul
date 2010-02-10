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

struct AnalogTV* allocateAnalogTv(int width, int height);
void freeAnalogTv(struct AnalogTV* );

void 		renderGdkPixbufFromImageData(
		char *byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		int upx, int upy, int upwidth, int upehight,
		long long gdkWindow_);

void 		renderNoisyGdkPixbufFromImageData(
		char* byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		int upx, int upy, int upwidth, int upehight,
		long long gdkWindow_);

void 		renderAnalogGdkPixbufFromImageData(
		struct AnalogTV* analog,
		char* byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		long long gdkWindow_);


struct OpenGL* allocateOpenGL(int nblocks);
void realizeOpenGL(struct OpenGL *, long long gtkWidget_);
void freeOpenGL(struct OpenGL *);

void        renderOpenGLFromImageData(struct OpenGL* ogl,
        char* BYTE, int width, int height, int rowstride,
        int destWidth, int destHeight,
        int upx, int upy, int upwidth, int upheight);

///

void    updateBlockTexture(struct OpenGL* ogl, int blockidx, int width, int height, char *BYTE, int offset);

void        renderOpenGLFromBlocks(struct OpenGL* ogl,
		int width, int height,
        int destWidth, int destHeight,
        int upx, int upy, int upwidth, int upheight);

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
void add_noise(unsigned char *data, int offset,
		int width, int height, int rowstride,
		int realWidth, int realHeight);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void add_noise_rgba(unsigned int *data, int offset,
			int width, int height, int rowstride,
		int realWidth, int realHeight);
#endif /* RENDER_H_ */
