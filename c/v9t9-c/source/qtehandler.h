#ifndef __QTEHANDLER_H__
#define __QTEHANDLER_H__

#ifdef __cplusplus

#include <qmainwindow.h>
#include <qpe/qpeapplication.h>
#include <qwidget.h>
#include <qlabel.h>
#include <qlayout.h>

class QWidget;
//class QTimer;

class QteHandler : public QMainWindow
{
	Q_OBJECT
public:
	QteHandler(QWidget *parent = 0, const char *name = 0, WFlags f = 0);
	void installTimer();
	void uninstallTimer();
	QVBoxLayout *lay;
protected:
	void timerEvent(QTimerEvent *);
private:
	int timerid,idleid;
//	QTimer *timer;
};

#endif

#endif
