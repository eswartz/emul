
%module AnalogTVHack 
%{
#include "analogtv.h"
%}

analogtv *analogtv_allocate(int width, int height);

void analogtv_set_defaults(analogtv *it);
void analogtv_release(analogtv *it);
void analogtv_setup_frame(analogtv *it);
void analogtv_setup_sync(analogtv_input *input, int do_cb, int do_ssavi);
void analogtv_draw(analogtv *it);

//int analogtv_load_ximage(analogtv *it, analogtv_input *input, XImage *pic_im);

analogtv_reception* analogtv_reception_new();
void analogtv_reception_update(analogtv_reception *inp);

void analogtv_init_signal(analogtv *it, double noiselevel);
void analogtv_add_signal(analogtv *it, analogtv_reception *rec);

//void analogtv_setup_teletext(analogtv_input *input);


void analogtv_lcp_to_ntsc(double luma, double chroma, double phase,
                          int ntsc[4]);

