/*
  render-ogl.c

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
#include <gdk/gdk.h>
#include <gtk/gtkwindow.h>
#include <gdk-pixbuf/gdk-pixbuf.h>

#include <stdlib.h>
#include <math.h>
#include "render.h"

#include <GL/gl.h>
#include <gtk/gtkgl.h>
#include <gdk/gdkgl.h>

#define VERTEX_ARRAYS 1

struct OpenGL {
	GdkGLConfig* config;
	GdkGLContext* context;
	GdkGLDrawable* drawable;
	GtkWidget* widget;

	int screentexid;
	int screentexloaded;
	int displaylist;

	// block mode
	int nblocks;
	GLuint* blocktextureids;

	// vertex array mode
	unsigned short* indices; // 49152 * 4
	unsigned short* vertices; // X*Y
};

typedef struct OpenGL OpenGL;
static void configure_window(OpenGL *ogl, int width, int height) {
	GdkGLContext* context = ogl->context;
	gdk_gl_drawable_gl_begin(ogl->drawable, context);
	glViewport(0, 0, width, height);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(0, width, height, 0, -1, 1);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	if (!ogl->screentexid) {
		glGenTextures(1, &ogl->screentexid);
	}
	glBindTexture(GL_TEXTURE_2D, ogl->screentexid);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

	gdk_gl_drawable_gl_end(ogl->drawable);

}

static int initialized;
struct OpenGL* allocateOpenGL(int nblocks) {
	if (!initialized) {
		int fakeargc = 1;
		char *fakeargv[] = { "", NULL };
		if (!gdk_gl_init_check(&fakeargc, &fakeargv)) {
			fprintf(stderr, "Cannot init GDKGL");
			return NULL;
		}
		initialized = 1;

	}

	OpenGL* ogl = (OpenGL*) calloc(sizeof(OpenGL), 1);
	//printf("ogl alloc=%p\n", ogl);
	ogl->nblocks = nblocks;

	ogl->indices
			= (unsigned short *) malloc(sizeof(unsigned short) * 49152 * 4);
	ogl->vertices = (unsigned short *) malloc(sizeof(unsigned short) * 49152
			* 2);
	int r, c;
	int idx = 0;
	for (r = 0; r < 192; r++) {
		int nr = r < 191 ? r + 1 : r;
		for (c = 0; c < 256; c++) {
			ogl->indices[idx++] = r * 256 + c;
			ogl->indices[idx++] = nr * 256 + c;
			ogl->indices[idx++] = nr * 256 + c + 1;
			ogl->indices[idx++] = r * 256 + c + 1;
			ogl->vertices[(r * 256 + c) * 2] = c;
			ogl->vertices[(r * 256 + c) * 2 + 1] = r;
		}
	}
	return ogl;
}
void realizeOpenGL(struct OpenGL* ogl, long long gtkWindow_) {
	GtkWidget* window = GTK_WIDGET((void*) gtkWindow_);

	GdkGLConfig
			* config = gdk_gl_config_new_by_mode_for_screen(
					gdk_screen_get_default(), GDK_GL_MODE_RGB
							| GDK_GL_MODE_SINGLE/*| GDK_GL_MODE_DOUBLE*/);
	if (config == NULL)
		return;

	//printf("ogl=%p\n", ogl);
	ogl->config = config;

	gtk_widget_unrealize(window);
	if (!gtk_widget_set_gl_capability(window, ogl->config, NULL, FALSE,
			GDK_GL_RGBA_TYPE)) {
		fprintf(stderr, "Cannot enable GDKGL on window");
		return;
	}

	gtk_widget_realize(window);
	GdkGLDrawable* drawable = gtk_widget_get_gl_drawable(GTK_WIDGET(window));

	ogl->drawable = drawable;
	ogl->widget = GTK_WIDGET(window);
	ogl->context = gtk_widget_get_gl_context(ogl->widget);


	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_COLOR_ARRAY);
	glVertexPointer(2, GL_SHORT, 0, ogl->vertices);
	//gdk_gl_drawable_gl_begin(ogl->drawable, ogl->context);

}
void freeOpenGL(struct OpenGL* ogl) {
	//g_object_unref(ogl->drawable);
	//g_object_unref(ogl->config);
	free(ogl);
}

void renderOpenGLFromImageData(OpenGL* ogl, char* pixels, int width,
		int height, int rowstride, int destWidth, int destHeight, int upx,
		int upy, int upwidth, int upheight) {
	if (ogl == NULL) {
		printf("null OGL\n");
		return;
	}

	//return;

	configure_window(ogl, destWidth, destHeight);
	gdk_gl_drawable_gl_begin(ogl->drawable, ogl->context);

	if (1) {
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glBindTexture(GL_TEXTURE_2D, ogl->screentexid);
		if (1||!ogl->screentexloaded) {
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, rowstride / 3, height, 0,
					GL_RGB, GL_UNSIGNED_BYTE, pixels);
			ogl->screentexloaded = 1;
		} else {
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, height-upy, rowstride / 3,  upheight,
					GL_RGB, GL_UNSIGNED_BYTE, pixels);

		}

		/*
		 glPushMatrix();
		 glScalef((double)destWidth / (rowstride/3), (double)destHeight / height, 1);

		 glBegin(GL_QUADS);
		 glTexCoord2i((double)upx / (rowstride/3), (double)upy / height);
		 glVertex2f(upx, upy);

		 glTexCoord2i((double)(upx + upwidth) / (rowstride/3), (double)upy / height);
		 glVertex2f(upx + upwidth, upy);

		 glTexCoord2i((double)(upx + upwidth) / (rowstride/3), (double)(upy + upheight) / height);
		 glVertex2f(upx + upwidth, upy + upheight);

		 glTexCoord2i((double)upx / (rowstride/3), (double)(upy + upheight) / height);
		 glVertex2f(upx, upy + upheight);
		 */


		if (0 && ogl->displaylist) {
			glPushMatrix();
			glScalef((double)destWidth/width, (double)destHeight/height, 1);
			glCallList(ogl->displaylist);
			glPopMatrix();
		} else {
			//int list = glGenLists(1);
			//	ogl->displaylist = list;

			//glNewList(ogl->displaylist, GL_COMPILE_AND_EXECUTE);

			glPushMatrix();
			glScalef((double)destWidth, (double)destHeight, 1);
			glClearColor(0, 0, 0, 1);
			//glClear(GL_COLOR_BUFFER_BIT);
			glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
			glEnable(GL_TEXTURE_2D);

			glBegin(GL_QUADS);
			glTexCoord2i(0, 0);
			glVertex2f(0, 0);

			glTexCoord2i(1, 0);
			glVertex2f(1, 0);

			glTexCoord2i(1, 1);
			glVertex2f(1, 1);

			glTexCoord2i(0, 1);
			glVertex2f(0, 1);

			glEnd();
			glPopMatrix();
			//glEndList();

		}

	} else if (0) {
		glPixelZoom((double) destWidth / width, (double) -destHeight / height);
		glClearColor(0, 0, 0, 1);
		//glClear(GL_COLOR_BUFFER_BIT);
		//glRasterPos2i(0, 0);
		//glDrawPixels(width, height, GL_RGB, GL_UNSIGNED_BYTE, pixels);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, upy);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, upx);
		glRasterPos2i((double) upx * destWidth / height, (double) upy
				* destHeight / height);
		glDrawPixels(width, height, GL_RGB, GL_UNSIGNED_BYTE, pixels);
	} else if (0) {
		int r, c;
		//glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_TEXTURE_2D);
		float scalex = (double) destWidth / width;
		float scaley = (double) destHeight / height;
		glPointSize((scalex + scaley) / 2);
		glBegin(GL_POINTS);
		glColor3b(0, 0, 0);
		int last = 0;
		for (r = 0; r < height; r++) {
			unsigned char* ptr = (unsigned char *) pixels + (r * rowstride);
			for (c = 0; c < width; c++) {
				int color = (ptr[0] << 16) | (ptr[1] << 8) | (ptr[2]);
				if (color != last)
					glColor3b(ptr[0], ptr[1], ptr[2]);
				glVertex3f(c * scalex, r * scaley, 0.0);
				ptr += 3;
				last = color;
			}
		}
		glEnd();

	} else if (1) {
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glVertexPointer(2, GL_SHORT, 0, ogl->vertices);
		glColorPointer(3, GL_UNSIGNED_BYTE, 0, pixels);
		glPushMatrix();
		glScalef((double)destWidth / width, (double)destHeight / height, 1);
		glDrawElements(GL_QUADS, 49152 * 4, GL_UNSIGNED_SHORT, ogl->indices);
		glPopMatrix();
	}
	//if (gdk_gl_drawable_is_double_buffered (ogl->drawable))
	gdk_gl_drawable_swap_buffers(ogl->drawable);

	gdk_gl_drawable_gl_end(ogl->drawable);

}

void updateBlockTexture(struct OpenGL* ogl, int blockidx, int width,
		int height, char *data, int offset) {

	gdk_gl_drawable_gl_begin(ogl->drawable, ogl->context);
	//
	if (!ogl->blocktextureids) {
		ogl->blocktextureids = (GLuint *) calloc(ogl->nblocks, sizeof(GLuint));
		printf("allocating %d textures into %p\n", ogl->nblocks,
				ogl->blocktextureids);
		glGenTextures(ogl->nblocks, ogl->blocktextureids);
	}
	int texid = ogl->blocktextureids[blockidx];
	printf("updating %d [%d] @ %dx%d in %p + %d\n", blockidx, texid, width,
			height, data, offset);
	fflush(stdout);
	fflush(stderr);
	glBindTexture(GL_TEXTURE_2D, texid);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB,
			GL_UNSIGNED_BYTE, data + offset);

	gdk_gl_drawable_gl_end(ogl->drawable);
}

void renderOpenGLFromBlocks(struct OpenGL* ogl, int width, int height,
		int destWidth, int destHeight, int upx, int upy, int upwidth,
		int upheight) {

	if (!ogl->blocktextureids)
		return;

	printf("updating blocks\n");
	configure_window(ogl, destWidth, destHeight);
	gdk_gl_drawable_gl_begin(ogl->drawable, ogl->context);

	int x, y;
	int bw = width / 32;
	int bh = 8;
	glEnable(GL_TEXTURE_2D);
	for (y = 0; y < height / bh; y++) {
		for (x = 0; x < width / bw; x++) {
			int block = y * 32 + x;
			if (block >= ogl->nblocks)
				printf("bad block %d %d -> %d\n", x, y, block);
			else
				glBindTexture(GL_TEXTURE_2D, ogl->blocktextureids[block]);

			glBegin(GL_QUADS);
			glTexCoord2i(0, 0);
			glVertex2f(x * bw, y * bh);

			glTexCoord2i(1, 0);
			glVertex2f(x * bw + bw, y * bh);

			glTexCoord2i(1, 1);
			glVertex2f(x * bw + bw, y * bh + bh);

			glTexCoord2i(0, 1);
			glVertex2f(x * bw, y * bh + bh);
			glEnd();

		}
	}

	gdk_gl_drawable_swap_buffers(ogl->drawable);

	gdk_gl_drawable_gl_end(ogl->drawable);

}

