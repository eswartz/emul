/*
  resources.h

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
#ifndef __XSCREENSAVER_RESOURCES_H__
#define __XSCREENSAVER_RESOURCES_H__

extern char *get_string_resource (Display*,char*,char*);
extern Bool get_boolean_resource (Display*,char*,char*);
extern int get_integer_resource (Display*,char*,char*);
extern double get_float_resource (Display*,char*,char*);
extern unsigned int get_pixel_resource (Display*,Colormap,char*,char*);
extern unsigned int get_minutes_resource (Display*,char*,char*);
extern unsigned int get_seconds_resource (Display*,char*,char*);
extern int parse_time (const char *string, Bool seconds_default_p,
                       Bool silent_p);
extern Pixmap
xscreensaver_logo (Screen *screen, Visual *visual,
                   Drawable drawable, Colormap cmap,
                   unsigned long background_color,
                   unsigned long **pixels_ret, int *npixels_ret,
                   Pixmap *mask_ret,
                   Bool big_p);

#endif /* __XSCREENSAVER_RESOURCES_H__ */
