/*
video_qte.h

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

#ifdef __cplusplus

#include <qwidget.h>
#include <qpixmap.h>

// maintain a backing pixmap?
#define BACKING_PIXMAP	0

class QPixmap;

class QteVideo : public QWidget
{
public:
	QteVideo(QWidget *parent, const char *name);
	QSizePolicy sizePolicy() const;

	// new functions
	//void setRotated(bool x) { m_rotated=x; repaint();}
	//bool rotated(void) { return m_rotated; }
#if BACKING_PIXMAP
	QPixmap *pixmap(void) { return &m_pix; }
#endif

	void getWindowOffset(int &, int &);

protected:
	void paintEvent(QPaintEvent *);

	void mousePressEvent(QMouseEvent *);
	void mouseReleaseEvent(QMouseEvent *);
	void mouseMoveEvent(QMouseEvent *);
	void keyPressEvent(QKeyEvent *);
	void keyReleaseEvent(QKeyEvent *);

private:
#if BACKING_PIXMAP
	QPixmap m_pix;
#endif
//	bool m_rotated;
};

extern QteVideo *qteVideo;

#endif	// __cplusplus
