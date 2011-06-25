
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
