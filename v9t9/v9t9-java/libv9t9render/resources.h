/*
  resources.h

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
