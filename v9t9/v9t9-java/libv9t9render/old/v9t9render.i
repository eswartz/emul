
%module V9t9Render
%include "various.i";
%array_functions(char, byteArray);

%{
//typedef unsigned char* byteArray;
#include "render.h"

%}

void        renderGdkPixbufFromImageData(
        const char *BYTE, int width, int height, int rowstride, 
        int destWidth, int destHeight,
        int upx, int upy, int upheight, int upwidth,
        long long gdkWindow_);
        

struct AnalogTV* allocateAnalogTv(int width, int height);
void freeAnalogTv(struct AnalogTV* );
        
void        renderAnalogGdkPixbufFromImageData(
        AnalogTV* analog,
        const char* BYTE, int width, int height, int rowstride,
        int destWidth, int destHeight,
        long long gdkWindow_);

void        renderNoisyGdkPixbufFromImageData(
        const char* BYTE, int width, int height, int rowstride,
        int destWidth, int destHeight,
        int upx, int upy, int upwidth, int upheight,
        long long gdkWindow_);
        
struct OpenGL;
struct OpenGL* allocateOpenGL(int nblocks);
void realizeOpenGL(struct OpenGL *, long long gtkWidget_);
void freeOpenGL(struct OpenGL *);

void        renderOpenGLFromImageData(struct OpenGL* ogl,
       const  char* BYTE, int width, int height, int rowstride,
        int destWidth, int destHeight,
        int upx, int upy, int upwidth, int upheight);

///

void    updateBlockTexture(struct OpenGL* ogl, int blockidx, int width, int height, char *BYTE, int offset);

void        renderOpenGLFromBlocks(struct OpenGL* ogl,
        int width, int height,
        int destWidth, int destHeight,
        int upx, int upy, int upwidth, int upheight);
