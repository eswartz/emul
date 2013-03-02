/*
  v9t9-analogtv.c

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
#include <stdlib.h>
#include <math.h>
#include "render.h"
#include "analogtv.h"

_EXPORT
struct AnalogTV* allocateAnalogTv(int width, int height) {
	AnalogTV* tv = (AnalogTV*) calloc(sizeof(AnalogTV), 1);
	tv->tv = analogtv_allocate(width, height);
	analogtv_set_defaults(tv->tv);
	tv->input= analogtv_input_allocate();

	tv->tv->tint_control = 0;// 180;
	tv->tv->color_control = .85;
	tv->tv->width_control = 1.0;

	analogtv_setup_sync(tv->input, 1, 0);
	tv->rec = analogtv_reception_new();
	tv->rec->level = 1.0;
	tv->rec->input = tv->input;
	return tv;
}

_EXPORT
void freeAnalogTv(struct AnalogTV* tv) {
	analogtv_release(tv->tv);
	free(tv->rec);
	free(tv->input);
}

_EXPORT
struct analogtv_s* getAnalogTvData(struct AnalogTV *analog) {
	return analog->tv;
}


_EXPORT
void 		analogizeImageData(
		AnalogTV* analog,
		char* byteArray, int srcoffset, int width, int height, int rowstride) {

	analogtv_setup_sync(analog->input, 1, 0);
	analogtv_setup_frame(analog->tv);
	analogtv_load_rgb24(analog->tv, analog->rec->input, byteArray + srcoffset, width, height, rowstride);
	analogtv_init_signal(analog->tv, 0.0);
	analogtv_reception_update(analog->rec);
	analogtv_add_signal(analog->tv, analog->rec);
	analogtv_draw(analog->tv);
}


