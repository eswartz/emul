#ifndef __QTELOOP_H__
#define __QTELOOP_H__

#include "qtehandler.h"

#ifdef __cplusplus

extern QteHandler *mainHandler;

class QKeyEvent;
class QMouseEvent;

extern int QTE_handle_key(QKeyEvent *e, int down);
extern int QTE_handle_mouse(QMouseEvent *e, int pressrelease, int move);

#endif	// __cplusplus

#include "centry.h"

int QTE_system_init(void);
int QTE_system_loop(void);

void QTE_start_timer(void);
void QTE_stop_timer(void);

#include "cexit.h"

#endif
