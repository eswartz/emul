/*
  video_qte.c					-- V9t9 module for Qt/Embedded video interface

  (c) 1994-2002 Edward Swartz

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

/*
 *	Video module for Qt/Embedded
 */
#include <config.h>
#include "v9t9_common.h"

#include "qteloop.h"

#include "video.h"
#include "vdp.h"
#include "memory.h"
#include "9900.h"
#include "v9t9.h"

#include "video_qte.h"
#include <qwidget.h>
#include <qpainter.h>
#include <qpixmap.h>

#define _L LOG_VIDEO

QteVideo *qteVideo;
static int QTE_x_size, QTE_y_size;	// v9t9 video mode size
const int QTE_x_mult=1, QTE_y_mult=1;

// color for each entry
static QColor QTE_palette[17];

//	true if colors 0 and 17 can be changed without redrawing
static bool QTE_paletted=false;

//	mapping from TI color (0=bg, 17=fg, others the same)
//	to v9t9_gdk_color[] entry
static int cmap[17];

static void
video_setpaletteentry(int index, int c)
{
	module_logger(&QTE_Video, _L | L_1, _("Setting index %d to color %d\n"), index, c);

	if (QTE_paletted) {
		/* !!! */
	} else {
		cmap[index] = c == 0 ? vdpbg : c == 16 ? vdpfg : c;
	}
}

static void
video_updatepalette(void)
{
	int         x;

	for (x = 1; x < 16; x++)
		video_setpaletteentry(x, x);
	video_setpaletteentry(0, vdpbg);
	video_setpaletteentry(16, vdpfg);
}


QteVideo::QteVideo(QWidget *parent, const char *name)
	: QWidget(parent, name)
#if BACKING_PIXMAP
	,  m_pix(256, 192, -1, QPixmap::BestOptim)
#endif
	  //m_rotated(false)
{
	setMouseTracking(true);
	setFocusPolicy(StrongFocus);
	setPalette(QPalette(QColor(64,64,64)));
	resize(256, 192);
#if BACKING_PIXMAP
	m_pix.fill();
#endif
//	mainHandler->setCentralWidget(this);
}

QSizePolicy QteVideo::sizePolicy() const
{
	return QSizePolicy(QSizePolicy::Expanding, QSizePolicy::Expanding);
}

void QteVideo::getWindowOffset(int &xoffs, int &yoffs)
{
/*
	if (m_rotated)
	{
		yoffs = (width() - QTE_x_size)/2; if (xoffs<0) xoffs=0;
		xoffs = (height() - QTE_y_size)/2; if (yoffs<0) yoffs=0;
	}
	else
*/
	{
		xoffs = (width() - QTE_x_size)/2; //if (xoffs<0) xoffs=0;
		yoffs = (height() - QTE_y_size)/2; //if (yoffs<0) yoffs=0;
	}
}

void QteVideo::paintEvent(QPaintEvent *e)
{
	QPainter p;

	int xoffs,yoffs;

	getWindowOffset(xoffs, yoffs);

/*
	if (m_rotated)
	{
		p.rotate(90);
		p.translate(0, -192);
	}
*/

	QRect rect(e->rect());

	module_logger(&QTE_Video, _L|L_2, _("paint: offs=%d,%d size=%d,%d, offs=%d,%d\n"),
				  rect.left(),rect.top(),rect.right(),rect.bottom(),xoffs,yoffs);

#if BACKING_PIXMAP
	p.begin(this);
	p.setClipRegion(e->region());

	QRect prect = QRect(rect.left()-xoffs, rect.top()-yoffs, 
						rect.width(), rect.height());
	p.drawPixmap(QPoint(rect.left(), rect.top()), m_pix, prect);
	p.end();
#else
	vdp_redraw_screen(rect.left() - xoffs, rect.top() - yoffs,
					 rect.width(), rect.height());
#endif
}

static      vmResult
QTE_video_detect(void)
{
	return vmOk;
}

static      vmResult
QTE_video_init(void)
{
	qteVideo = new QteVideo(mainHandler, "v9t9");
	mainHandler->lay->addWidget(qteVideo);
	mainHandler->setCentralWidget(qteVideo);

	int i;
	for (i = 0; i < 16; i++) {
		QTE_palette[i] = QColor(vdp_palette[i][0], vdp_palette[i][1], vdp_palette[i][2]);
	}
	QTE_x_size = 256; QTE_y_size = 192;

	features |= FE_SHOWVIDEO;

	return vmOk;
}

static      vmResult
QTE_video_enable(void)
{
	qteVideo->show();
	return vmOk;
}

static      vmResult
QTE_video_disable(void)
{
	qteVideo->hide();
	return vmOk;
}

static      vmResult
QTE_video_restart(void)
{
	return vmOk;
}

static      vmResult
QTE_video_restop(void)
{
	return vmOk;
}

static      vmResult
QTE_video_term(void)
{
	delete qteVideo;
	return vmOk;
}

/**************/

template<class T> static T min(T a,T b) { return a<b?a:b; }
template<class T> static T max(T a,T b) { return a>b?a:b; }

static      vmResult
QTE_video_updatelist(struct updateblock *ptr, int num)
{
	if (!num) return vmOk;

	QPainter p;
	QPainter wnd;

	int xoffs, yoffs;

	qteVideo->getWindowOffset(xoffs, yoffs);

	module_logger(&QTE_Video, _L|L_2, _("%d blocks\n"), num);

	wnd.begin(qteVideo);
#if BACKING_PIXMAP
	// we draw directly to window and into pixmap for back buffer.
	p.begin(qteVideo->pixmap());
	QRegion reg;
#endif
	qteVideo->setUpdatesEnabled(false);

	while (num--) 
	{
		const int 	width=8;
		u8			c = 0;
		int			d;
		int			i,j;

		module_logger(&QTE_Video, _L|L_3, _("block %d,%d\n"), ptr->r,ptr->c);

		ptr->r *= QTE_y_mult;
		ptr->c = ptr->c * QTE_x_mult;

		QRect rect(ptr->c, ptr->r, QTE_x_mult*width, QTE_y_mult*8);
#if BACKING_PIXMAP
		reg |= rect;
#endif

		if (video_block_is_solid(ptr, QTE_paletted, &c)) {
#if BACKING_PIXMAP
			p.fillRect(rect, QTE_palette[cmap[c]]);
#endif
			rect.moveBy(xoffs, yoffs);
			wnd.fillRect(rect, QTE_palette[cmap[c]]);
		}
		else
		{

			if (QTE_x_mult > 1 || QTE_y_mult > 1)
			{
				// slow slow!  Maybe use zoom feature
				for (i = 0; i < 8; i++) {
					for (j = 0; j < width; j++) {
						c = ptr->data[i*UPDATEBLOCK_ROW_STRIDE+j];
						QRect rect(ptr->c+j*QTE_x_mult, 
								   ptr->r+i*QTE_y_mult,
								   QTE_x_mult,
								   QTE_y_mult);
#if BACKING_PIXMAP
						p.fillRect(rect, QTE_palette[cmap[c]]);
#endif
						rect.moveBy(xoffs, yoffs);
						wnd.fillRect(rect, QTE_palette[cmap[c]]);
					}
				}
			}
			else
			{
				QCOORD pts[16][8*8*2];
				int ptnum[16]={0};

				for (i = 0; i < 8; i++) {
					for (j = 0; j < width; j++) {
						c = cmap[ptr->data[i*UPDATEBLOCK_ROW_STRIDE+j]];
#if BACKING_PIXMAP
						pts[c][ptnum[c]++] = ptr->c+j;
						pts[c][ptnum[c]++] = ptr->r+i;
#else
						pts[c][ptnum[c]++] = ptr->c+j+xoffs;
						pts[c][ptnum[c]++] = ptr->r+i+yoffs;
#endif
					}
				}
				for (c=0; c<16; c++) if (ptnum[c])
				{
#if BACKING_PIXMAP
					QPointArray arr(ptnum[c]/2, pts[c]);
					p.setPen(QTE_palette[c]);
					p.drawPoints(arr);
					arr.translate(xoffs, yoffs);
					wnd.setPen(QTE_palette[c]);
					wnd.drawPoints(arr);
#else
					QPointArray arr(ptnum[c]/2, pts[c]);
					wnd.setPen(QTE_palette[c]);
					wnd.drawPoints(arr);
#endif
				}
			}				
		}

		ptr++;
	}
#if BACKING_PIXMAP
	p.end();
#endif
	wnd.end();

	qteVideo->setUpdatesEnabled(true);
	return vmOk;
}

static void QTE_clear_sides(int total, int inside)
{
	QPainter p;

	if (inside < total)
	{
		int strip = (total - inside) * QTE_x_mult / 2;

#if BACKING_PIXMAP
		p.begin(qteVideo->pixmap());
		// clear sides
		p.fillRect(0, 0, strip, QTE_y_size*QTE_y_mult, 
				   QTE_palette[vdpbg]);
		p.fillRect(total*QTE_x_mult-strip, 0, 
				   strip, QTE_y_size*QTE_y_mult, 
				   QTE_palette[vdpbg]);
		p.end();
#else

		int xoffs, yoffs;
		qteVideo->getWindowOffset(xoffs, yoffs);

		p.begin(qteVideo);

		// clear sides
		p.fillRect(xoffs, yoffs, strip, QTE_y_size*QTE_y_mult, 
				   QTE_palette[vdpbg]);
		p.fillRect(xoffs+total*QTE_x_mult-strip, yoffs, strip, QTE_y_size*QTE_y_mult, 
				   QTE_palette[vdpbg]);
		p.end();
#endif
	}
}

static      vmResult
QTE_video_resize(u32 newxsize, u32 newysize)
{
	module_logger(&QTE_Video, _L | L_1, _("Resizing to %d,%d\n"), newxsize, newysize);
	QTE_clear_sides(QTE_x_size, newxsize);
	QTE_x_size = newxsize;
	QTE_y_size = newysize;
	//qteVideo->repaint();
	return vmOk;
}

static      vmResult
QTE_video_setfgbg(u8 fg, u8 bg)
{
	video_setpaletteentry(0, bg);
	video_setpaletteentry(16, fg);
	if (!QTE_paletted)
	{
		QTE_clear_sides(256, QTE_x_size);
		//vdpcompleteredraw();
		vdp_redraw_screen(0, 0, 256, 192);
	}
	return vmOk;
}

static      vmResult
QTE_video_setblank(u8 bg)
{
	int         x;

	for (x = 0; x <= 16; x++)
		video_setpaletteentry(x, bg);
	if (!QTE_paletted)
	{
		//vdpcompleteredraw();
		vdp_redraw_screen(0, 0, 256, 192);
	}
	//qteVideo->repaint();
	return vmOk;
}

static      vmResult
QTE_video_resetfromblank(void)
{
	video_updatepalette();
	if (!QTE_paletted)
	{
		//vdpcompleteredraw();
		vdp_redraw_screen(0, 0, 256, 192);
	}
	//qteVideo->repaint();
	return vmOk;
}

/***********************************************************/

static vmVideoModule QTE_video_videoModule = {
	3,
	QTE_video_updatelist,
	QTE_video_resize,
	QTE_video_setfgbg,
	QTE_video_setblank,
	QTE_video_resetfromblank
};

vmModule    QTE_Video = {
	3,
	"QT/Embedded video",
	"vidQTE",

	vmTypeVideo,
	vmFlagsExclusive,

	QTE_video_detect,
	QTE_video_init,
	QTE_video_term,
	QTE_video_enable,
	QTE_video_disable,
	QTE_video_restart,
	QTE_video_restop,
	{(vmGenericModule *) & QTE_video_videoModule}
};
