/*
  demo.c						-- store and playback demo files (unfinished)

  (c) 1994-2001 Edward Swartz

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

#include <stdarg.h>

#include "v9t9_common.h"
#include "vdp.h"
#include "sound.h"
#include "speech.h"
#include "memory.h"
#include "demo.h"
#include "9900.h"
#include "moduledb.h"

#if UNDER_UNIX || defined(__CYGWIN32__)
#include <sys/socket.h>       /*  socket definitions        */
#include <sys/types.h>        /*  socket types              */
#include <arpa/inet.h>        /*  inet (3) funtions         */
#include <sys/time.h>
#include <netinet/tcp.h>
#include <unistd.h>           /*  misc. UNIX functions      */
#include <sys/fcntl.h>
#include <errno.h>
#endif


#define _L LOG_DEMO

/*
 *	These flags tell V9t9 about the state of the demo subsystem.
 *	The stateflag ST_DEMOING is used to initiate recording/playback;
 *	when not set, the demo subsystem is idle.
 */
bool	demo_recording, demo_playing;

/*	Set when reading demo from external port */

bool 	demo_client;	// true: using sockets only
int 	demo_connection;
int		demo_client_last_ticks;
int		demo_client_activity;

bool 	demo_fifo;		// true: using fifo reader (file) for input, fifo for output
int		demo_fifo_read;	// 	input handle 
int		demo_fifo_write;	// output handle

/*	Memory buffer for local file reading */

typedef struct DemoHandle
{
	OSHandle	handle;
	OSSize		sz, pos;	
	u8			*ptr;	
}	DemoHandle;

static OSSpec		demo_spec;
static DemoHandle	demo_handle;

static int			demo_tick_received;

static void demo_stop_record(void);
static void video_buffer_flush(void);

/*
 *	Create a new demo handle
 */
static OSError handle_new(DemoHandle *handle)
{
	return OS_NewHandle(0, &handle->handle);
}

/*
 *	Load a demo handle from disk
 */
static OSError handle_load(DemoHandle *handle, OSSpec *spec)
{
	OSRef ref;
	OSError err;
	OSSize sz;

	err = OS_Open(spec, OSReadOnly, &ref);
	if (err != OS_NOERR) {
		return err;
	}

	/* Get memory for memory handle same size as file */
	if ((err = OS_GetSize(ref, &handle->sz)) != OS_NOERR
	|| (err = OS_NewHandle(handle->sz, &handle->handle)) != OS_NOERR) {
		OS_Close(ref);
		memset(handle, 0, sizeof(DemoHandle));
		return err;
	}

	/* Load all data into a memory handle */
	sz = handle->sz;
	if ((handle->ptr = (u8 *)OS_LockHandle(&handle->handle)) == 0L
	|| (err = OS_Read(ref, handle->ptr, &sz)) != OS_NOERR
	|| sz != handle->sz) {
		if (err == OS_NOERR) err = OS_MEMERR;	/* need truncated file error */
		OS_UnlockHandle(&handle->handle);
		OS_FreeHandle(&handle->handle);
		OS_Close(ref);
		memset(handle, 0, sizeof(DemoHandle));
		return err;
	}

	handle->pos = 0;
	OS_Close(ref);

	return OS_NOERR;
}

/*
 *	Write a demo handle to disk
 */
static OSError handle_store(DemoHandle *handle, OSSpec *spec)
{
	OSError err;
	OSRef ref;
	OSSize sz;

	if ((err = OS_Open(spec, OSWrite, &ref)) == OS_NOERR)
	{
		if ((err = OS_GetHandleSize(&handle->handle, &handle->sz)) == OS_NOERR &&
			(sz = handle->sz) &&
			(handle->ptr = OS_LockHandle(&handle->handle)) != 0L &&
			(err = OS_Write(ref, (void *)handle->ptr, &sz)) == OS_NOERR)
		{
			if (sz != handle->sz) 
				err = OS_MEMERR;	/* oops, need a generic 'disk full' error */
		}
		OS_Close(ref);
	}

	return err;
}

static void handle_free(DemoHandle *handle)
{
	if (handle->ptr) {
		OS_UnlockHandle(&handle->handle);
		OS_FreeHandle(&handle->handle);
		memset(handle, 0, sizeof(DemoHandle));
	}
}

/*
 *	Append data to demo.
 */
static void handle_append(DemoHandle *handle, void *data, OSSize size)
{
	OSError err;

	err = OS_AppendHandle(&handle->handle, data, size);
	handle->sz += size;
	handle->pos += size;
	handle->ptr = 0L;

	if (err != OS_NOERR) {
		logger(_L|LOG_ERROR|LOG_USER, _("Could not add %d bytes to demo (%s)\n"),
			   size, OS_GetErrText(err));
		demo_stop_record();
	}
}

/*
 *	Read data from demo.
 */
static OSSize handle_read(DemoHandle *handle, void *data, OSSize size)
{
	OSSize len;

	if (demo_client) {
		OSSize red = 0;
		while (size != 0) {
			int pack;
			do {
				errno = 0;
				pack = read(demo_connection, data, size);
				if (pack == 0 || (pack < size && errno != 0)) {
					logger(_L|LOG_USER, _("missing bytes on read (%d of %d, %s)\n"), red+pack, red+size, OS_GetErrText(errno));
					return red + pack;
				}
			} while (pack < 0);
			data = (void*)((char*)data + pack);
			size -= pack;
			red += pack;
		}
		return red;
	}
	else if (demo_fifo) {
		OSSize red = 0;
		while (size != 0) {
			int pack;
			do {
				errno = 0;
				pack = read(demo_fifo_read, data, size);
				if (pack == 0 || (pack < size && errno != 0)) {
					logger(_L|LOG_USER, _("missing bytes on read (%d of %d, %s)\n"), red+pack, red+size, OS_GetErrText(errno));
					return red + pack;
				}
			} while (pack < 0);
			data = (void*)((char*)data + pack);
			size -= pack;
			red += pack;
		}
		return red;
	}

	/* cache the handle ptr */
	if (!handle->ptr) {
		handle->ptr = OS_LockHandle(&handle->handle);
		if (!handle->ptr) return 0;
		OS_GetHandleSize(&handle->handle, &handle->sz);
	}

	/* at EOH yet? */
	if (handle->pos >= handle->sz) {
		return 0;
	}

	/* get only as much as is available */
	len = size;
	if (handle->pos + len > handle->sz) {
		len = handle->sz - handle->pos;
	}

	/* return to user */
	memcpy(data, handle->ptr + handle->pos, len);
	handle->pos += len;
	return len;
}

/*
 *	Write data to live client
 */
static OSSize handle_write(DemoHandle *handle, void *data, OSSize size)
{
	OSSize len;

	if (demo_client) {
		OSSize rit = 0;
		while (size != 0) {
			int pack = write(demo_connection, data, size);
			if (pack < len && errno != EINTR)
				return rit + pack;
			data = (void*)((char*)data + pack);
			size -= pack;
			rit += pack;
		}
		return rit;
	}
	else if (demo_fifo) {
		OSSize rit = 0;
		while (size != 0) {
			int pack = write(demo_fifo_write, data, size);
			if (pack < len && errno != EINTR)
				return rit + pack;
			data = (void*)((char*)data + pack);
			size -= pack;
			rit += pack;
		}
		return rit;
	}
	else {
		logger(_L|LOG_FATAL, _("Invalid use of demo handle\n"));
		return 0;
	}
}


typedef struct
{
	int	type, idx, len, max;
	u8 *data;
}	demo_buffer;

static demo_buffer	buffer_video, buffer_sound, buffer_speech;

static u16 vdp_block_next_addr, vdp_block_addr, vdp_block_len;

/*
 *	Initialize buffer for reading or writing.
 */
static void buffer_init(demo_buffer *buffer, int size, int type)
{
	buffer->type = type;
	buffer->idx = 0;
	buffer->len = 0;
	buffer->max = size;
	if (buffer->data) xfree(buffer->data);
	buffer->data = (u8 *)xmalloc(size);
}

/*
 *	Terminate use of a buffer.
 */
static void buffer_term(demo_buffer *buffer)
{
	if (buffer->data)
		xfree(buffer->data);
	memset(buffer, 0, sizeof(demo_buffer));
}

/*
 *	Set up a buffer for reading or writing.
 */
static void buffer_setup(demo_buffer *buffer)
{
	buffer->idx = buffer->len = 0;
}

/*
 *	Flush a buffer to the file, with a type and length header.
 */
static void buffer_flush(demo_buffer *buffer)
{
	u8 header[4];

	if (!demo_recording) return;
	
	if (buffer->len) {
		// write event type and buffer length
		header[0] = buffer->type;
		header[1] = (buffer->len & 0xff);
		header[2] = (buffer->len >> 8) & 0xff;
		handle_append(&demo_handle, (void *)header, 3);

		// write data
		handle_append(&demo_handle, (void *)buffer->data, buffer->len);
	}

	buffer->idx = buffer->len = 0;
}

/*
 *	Append data to a buffer
 */
static void buffer_write(demo_buffer *buffer, void *data, int len)
{
	if (buffer->idx + len > buffer->max) {
		buffer_flush(buffer);
	}
	memcpy((u8 *)buffer->data + buffer->idx, data, len);
	buffer->idx += len;
	if (buffer->idx > buffer->len)
		buffer->len = buffer->idx;
	
}

/*
 *	Write data into a buffer at a specific position
 */
static void buffer_poke(demo_buffer *buffer, int offset, void *data, int len)
{
	if (offset + len > buffer->len) {
		logger(_L|LOG_ABORT, _("buffer_poke:  overrun (%d+%d > %d)\n"),
			   offset, len, buffer->len);
	}
	memcpy((u8 *)buffer->data + offset, data, len);
}


/*
 *	Read contents of a buffer from the file after reading type byte
 */
static void buffer_cache(demo_buffer *buffer)
{
	u8 header[4];
	int len;

	if (!demo_playing) return;

	/* read buffer length */
	if (handle_read(&demo_handle, (void *)header, 2) == 2) {
		buffer->len = header[0] | (header[1] << 8);
		buffer->idx = 0;
		if (buffer->len > buffer->max) {
			logger(_L|LOG_ABORT, _("Illegal buffer length %d (max = %d), pos = %d\n"),
				   buffer->len, buffer->max, demo_handle.pos);
		}

		/* read buffer data */
		if ((len = handle_read(&demo_handle, buffer->data, buffer->len)) 
			!= buffer->len) {
			logger(_L|LOG_ERROR|LOG_USER, _("Demo file is truncated (expected %d bytes at %d, got %d)\n"),
				   buffer->len, demo_handle.pos, len);
		}
	} else {
		/* no more data */
		buffer->idx = 0;
		buffer->len = 0;
	}
}

/*
 *	Read data from a buffer, return # bytes read
 */
static int buffer_read(demo_buffer *buffer, void *data, int len)
{
	int offs = 0, max;

	while (len) {
		if (buffer->idx >= buffer->len) {
			/* don't recache the buffer */
			return offs;
		}
		max = len;
		if (buffer->idx + max > buffer->len) {
			max = buffer->len - buffer->idx;
		}
		memcpy((u8 *)data + offs, (u8 *)buffer->data + buffer->idx, max);
		offs += max;
		buffer->idx += max;
		len -= max;
	}
	return offs;
}



/*
 *	Start recording a demo to 'spec'.
 *
 *	All the appropriate events in V9t9 that are recorded in a
 *	demo constantly call demo_record_event(); this routine merely
 *	sets up the buffers and process that will store that information.
 */
static int demo_start_record(OSSpec *spec)
{
	OSError err;
	int addr;

	logger(_L|L_1, _("Setting up demo (%s)\n"), OS_SpecToString1(spec));

	/* Create file first */
	demo_spec = *spec;
	err = OS_Create(spec, &OS_TEXTTYPE);
	if (err != OS_NOERR) {
		command_logger(_L|LOG_ERROR|LOG_USER, _("Could not create demo file '%s' (%s)\n"),
			   OS_SpecToString1(spec), OS_GetErrText(err));
		return 0;
	}

	/* Store all data in a memory handle */
	err = handle_new(&demo_handle);
	if (err != OS_NOERR) {
		command_logger(_L|LOG_ERROR|LOG_USER, _("Could not allocate memory for demo (%s)\n"),
			   OS_GetErrText(err));
		return 0;
	}

	/* Clear buffers */
	buffer_setup(&buffer_video);
	buffer_setup(&buffer_sound);
	buffer_setup(&buffer_speech);

	/* Setup global flags */
	demo_recording = true;		/* yes, record demo events */
	stateflag |= ST_DEMOING;	/* yes, demo recording is not paused */
	demo_tick_received = 0;		/* no tick yet */

	/* Magic header */
	handle_append(&demo_handle, DEMO_MAGIC_HEADER, strlen(DEMO_MAGIC_HEADER));

	/* Write VDP regs */
	for (addr = 0; addr < 8; addr++) {
		demo_record_event(demo_type_video, 
					   0x8000 + (addr << 8) + vdpregs[addr]);
	}

	/* Write VDP memory */
	for (addr = 0; addr < 0x4000; addr++) {
		demo_record_event(demo_type_video, 
					   0x4000 + addr, 
					   vdp_mmio_read(addr));
	}
	video_buffer_flush();
	buffer_flush(&buffer_video);

	/* Write sound data */
	for (addr = 0; addr < 4; addr++) {
		u8 base = 0x80 + (addr << 5);

		/* tone/noise bytes */
		demo_record_event(demo_type_sound, 
					   sound_voices[addr].operation[OPERATION_FREQUENCY_LO]);
		demo_record_event(demo_type_sound, 
					   sound_voices[addr].operation[OPERATION_FREQUENCY_HI]);
		demo_record_event(demo_type_sound, 
					   sound_voices[addr].operation[OPERATION_ATTENUATION]);

		/* volume */
		demo_record_event(demo_type_sound, 
					   base + sound_voices[addr].volume);
	}
	buffer_flush(&buffer_sound);

	/* Write speech data */
	demo_record_event(demo_type_speech, demo_speech_terminating);
	buffer_flush(&buffer_speech);
	
	return 1;
}

/*
 *	Start playing back a demo.
 *
 *	When demo_playing is set, the ordinary emulator does not run,
 *	but demo_playback_loop() is called instead to update the UI.
 */
static int demo_start_playback_core(void)
{
	OSError err;
	u8 magic[DEMO_MAGIC_HEADER_LENGTH];

	/* Check magic header */
	if (handle_read(&demo_handle, magic, sizeof(magic)) != sizeof(magic)
		|| memcmp(magic, DEMO_MAGIC_HEADER, sizeof(magic)) != 0) {
		logger(_L|LOG_ERROR|LOG_USER, _("Demo file header invalid "
										"(got %02x%02x%02x%02x, expected '%.*s''\n"),
			   magic[0], magic[1], magic[2], magic[3],
			   sizeof(magic), DEMO_MAGIC_HEADER);
		handle_free(&demo_handle);
		return 0;
	}

	/* Clear buffers */
	buffer_setup(&buffer_video);
	buffer_setup(&buffer_sound);
	buffer_setup(&buffer_speech);

	/* Setup global flags */
	demo_playing = true;		/* yes, record demo events */
	stateflag |= ST_DEMOING;	/* yes, demo playback is not paused */

	return 1;
}

/*
 *	Start playing back a demo.
 *
 *	When demo_playing is set, the ordinary emulator does not run,
 *	but demo_playback_loop() is called instead to update the UI.
 */
int demo_start_playback(OSSpec *spec)
{
	OSError err;
	u8 magic[DEMO_MAGIC_HEADER_LENGTH];

	logger(_L|L_1, _("Initiating demo (%s)\n"), OS_SpecToString1(spec));

	/* Open file first */
	demo_spec = *spec;
	err = handle_load(&demo_handle, &demo_spec);
	if (err != OS_NOERR) {
		logger(_L|LOG_ERROR|LOG_USER, _("Could not load demo file %s (%s)\n"),
			   OS_SpecToString1(&demo_spec), OS_GetErrText(err));
		return 0;
	}

	demo_client = false;
	demo_fifo = false;
	demo_connection = 0;
	demo_fifo_read = 0;
	demo_fifo_write = 0;

	return demo_start_playback_core();
}

/*
 *	Start playing back a demo.
 *
 *	When demo_playing is set, the ordinary emulator does not run,
 *	but demo_playback_loop() is called instead to update the UI.
 */
int demo_start_fifo(OSSpec *rspec, OSSpec *wspec)
{
	OSError err;
	u8 magic[DEMO_MAGIC_HEADER_LENGTH];

	logger(_L|L_1, _("Initiating demo (%s / %s)\n"), OS_SpecToString1(rspec), 
		   OS_SpecToString1(wspec));

	/* Open fifo for reading */
	demo_fifo_read = open(OS_SpecToString1(rspec), O_RDONLY);
	if (demo_fifo_read < 0) {
		logger(_L|LOG_ERROR|LOG_USER, _("Could not open demo reader %s (%s)\n"),
			   OS_SpecToString1(rspec), OS_GetErrText(errno));
		return 0;
	}

	/* Open fifo for writing */
	demo_fifo_write = open(OS_SpecToString1(wspec), O_WRONLY);
	if (demo_fifo_write < 0) {
		logger(_L|LOG_ERROR|LOG_USER, _("Could not open demo writer %s (%s)\n"),
			   OS_SpecToString1(wspec), OS_GetErrText(errno));
		return 0;
	}

	demo_fifo = true;
	demo_connection = 0;
	
	return demo_start_playback_core();
}

/*
 *	Start listening for demo data.
 *
 *	When demo_playing is set, the ordinary emulator does not run,
 *	but demo_playback_loop() is called instead to update the UI.
 */
int demo_start_listening(int port)
{
    int       conn_s;                /*  connection socket         */
    struct    sockaddr_in servaddr;  /*  socket address structure  */
	int on = 1;

	logger(_L|L_1|LOG_USER, _("Initiating demo client on port %d\n"), port);

	OS_MakeFileSpec("<External Port>", &demo_spec);

	/* Get a connection */
	if ( (conn_s = socket(AF_INET, SOCK_STREAM, 0)) < 0 ) {
		logger(_L|LOG_ERROR|LOG_USER, _("error creating listening socket (%s)\n"), OS_GetErrText(errno));
		return 0;
    }

	setsockopt(conn_s, IPPROTO_TCP, TCP_NODELAY, &on, sizeof(on));

	/*  Set all bytes in socket address structure to
		zero, and fill in the relevant data members   */


	/*  Set the remote IP address  */

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port        = htons(port);
    if (inet_aton("127.0.0.1", &servaddr.sin_addr) <= 0 ) {
		logger(_L|LOG_ERROR|LOG_USER, "Invalid remote IP address.\n");
		return 0;
    }

	/*  connect() to the remote server  */

    if ( connect(conn_s, (struct sockaddr *) &servaddr, sizeof(servaddr) ) < 0 ) {
		logger(_L|LOG_ERROR|LOG_USER, _("error calling connect() (%s)\n"), OS_GetErrText(errno));
		return 0;
    }
	
	/* Setup global flags */
	demo_client = true;
	demo_connection = conn_s;
	demo_fifo = false;
	demo_fifo_read = 0;
	demo_fifo_write = 0;

	return demo_start_playback_core();
}

static void demo_stop_record(void)
{
	OSError err;

	logger(_L|L_1, _("Closing demo\n"));
	if (demo_recording) {
		logger(_L|L_1, _("Closing demo (%s)\n"), OS_SpecToString1(&demo_spec));

		video_buffer_flush();
		buffer_flush(&buffer_video);
		buffer_flush(&buffer_sound);
		buffer_flush(&buffer_speech);

		demo_recording = false;

		err = handle_store(&demo_handle, &demo_spec);
		if (err != OS_NOERR) {
			command_logger(_L|LOG_USER|LOG_ERROR, _("Could not write demo file '%s' (%s)\n"),
				   OS_SpecToString1(&demo_spec), OS_GetErrText(err));
		}
		handle_free(&demo_handle);

		stateflag &= ~ST_DEMOING;
	}
}

static void demo_stop_playback(void)
{
	logger(_L|LOG_USER, _("Demo playback stopped.\n"));
	if (demo_playing) {
		if (demo_client) {
			close(demo_connection);
			demo_connection = 0;
			demo_client = false;
		}
		else if (demo_fifo) {
			close(demo_fifo_write);
			close(demo_fifo_read);
			demo_fifo = false;
			demo_fifo_write = 0;
			demo_fifo_read = 0;
		}
		else
			handle_free(&demo_handle);

		demo_playing = false;

		stateflag &= ~ST_DEMOING;
	}
}

static void video_buffer_flush(void)
{
	u8 data[4];

	if (!(vdp_block_addr & 0x8000)) {
		if (vdp_block_len) {
			/* write old subblock header */
			data[0] = vdp_block_addr & 0xff;
			data[1] = (vdp_block_addr >> 8) & 0xff;
			data[2] = vdp_block_len;
			buffer_write(&buffer_video, data, 3);

			/* write subblock data */
			if (!(vdp_block_addr & 0x8000)) {
				buffer_write(&buffer_video, 
							 FLAT_MEMORY_PTR(md_video, vdp_block_addr & 0x3fff), 
							 vdp_block_len);
			}
		}
	} else {
		/* write register */
		data[0] = vdp_block_addr & 0xff;
		data[1] = (vdp_block_addr >> 8) & 0xff;
		buffer_write(&buffer_video, data, 2);
	}

	/* start new subblock */
	vdp_block_addr = 0;
	vdp_block_len = 0;
	vdp_block_next_addr = 0;
}

/*
 *	Main interface to demo saving engine
 */
int demo_record_event(demo_type type, ...)
{
	va_list va;
	int arg;
	u8 data[8];

	if (type == demo_type_tick)
		demo_tick_received = 1;

	if (!demo_recording || !(stateflag & ST_DEMOING)) return 1;

	va_start(va, type);
	switch (type)
	{
	case demo_type_tick:
		/* flush all current data */
		logger(_L|L_3, _("Tick\n"));
		data[0] = type;
		handle_append(&demo_handle, (void *)data, 1);
		video_buffer_flush();
		buffer_flush(&buffer_video);
		buffer_flush(&buffer_sound);
		buffer_flush(&buffer_speech);
		break;

		/* for these events, buffer */

	case demo_type_video:
		/* gather contiguously modified data into a subblock; when the
		 * VDP addr changes, a VDP reg is written or 255 bytes were
		 * received, write subblock to buffer */

		arg = va_arg(va, int);

		if ((arg & 0x8000)				// video register
		|| 	arg != vdp_block_next_addr 	// not contiguous?
		|| 	vdp_block_len == 255) 		// full
		{
			logger(_L|L_2, "(new video block, arg=0x%4x, vdp_block_next_addr=0x%04x, vdp_block_len=%d)\n", 
				   arg, vdp_block_next_addr, vdp_block_len);

			video_buffer_flush();
			vdp_block_addr = vdp_block_next_addr = arg;
		}

		logger(_L|L_3, _("Video >%04X, PC=>%04X "), arg, pc);

		/* write data byte if not a register set */
		if (!(arg & 0x8000)) {
			vdp_block_len++;
			vdp_block_next_addr = arg + 1;
		}
		logger(_L|L_3, "\n");
		break;

	case demo_type_sound:
		/* just send it */
		data[0] = va_arg(va, int);
		logger(_L|L_2, _("Sound >%02X\n"), data[0]);
		buffer_write(&buffer_sound, data, 1);
		break;

	case demo_type_speech:
		/* In order to demo speech when no speech ROM
		   is present, demos record direct data for 
		   vocabulary phrases as well, and speech commands
		   are not recorded.  Instead there is a layer
		   of abstraction that divides sequences of speech
		   data in start, data, and stop events. */
		data[0] = va_arg(va, int);

		logger(_L|L_2, _("Speech %d"), data[0]);

		/* write event byte */
		buffer_write(&buffer_speech, data, 1);

		switch (data[0]) 
		{
		case demo_speech_starting:
			break;

		case demo_speech_adding_byte:	
			/* this event has one byte of info */
			data[0] = va_arg(va, int);
			logger(_L|L_2, " (>%02X)", data[0]);
			buffer_write(&buffer_speech, data, 1);
			break;

		case demo_speech_terminating:
		case demo_speech_stopping:
		case demo_speech_interrupt:
			break;
		}
		logger(_L|L_2, "\n");
		break;

	default:
		logger(LOG_ABORT, _("Unknown event type passed to demo_event (%d)\n"),
			   type);
		break;
	}

	va_end(va);
	return 1;
}

bool stDemoTickWaiting;

static int demo_runner(void) 
{
	u8 data[8];
	demo_type type;

	/* get type byte for buffer */
	while (1) {
#if 1
		if (demo_client || demo_fifo) {
			int curticks = TM_GetTicks();
//			logger(_L|LOG_INFO|L_4, _("ticks: %d, act=%d\n"), curticks, demo_client_activity);
			if (curticks > demo_client_last_ticks + 60 && demo_client_activity) {
//				logger(_L|LOG_INFO|L_4, _("client slow\n"));
				demo_client_last_ticks = curticks;
				demo_client_activity = 0;
				return em_TooFast;
			}
		}
#endif

		if (stDemoTickWaiting) {
			stDemoTickWaiting = false;
			data[0] = demo_type_tick;
		} else if (handle_read(&demo_handle, data, 1) == 1) {
		}
		else
			break;

		type = data[0];
		switch (type)
		{
		case demo_type_tick:
			/* pause until next timer tick (vdp_interrupt) */
			if (!demo_tick_received)
			{
				stDemoTickWaiting = true;
				logger(_L|L_4, _("."));
				return em_TooFast;
			}
			else
			{
				logger(_L|L_4, _("tick\n"));
				demo_tick_received = 0;
			}
			break;

		/* these events are buffered */

		case demo_type_video:
			buffer_cache(&buffer_video);
			logger(_L|L_2, _("Video %d\n"), buffer_video.len);

			/* read address */
			while (buffer_read(&buffer_video, data, 2) == 2) {
				u16 addr = data[0] | (data[1] << 8);
				
				vdp_mmio_set_addr(addr);

				/* if not a register, read the data too */
				if (!(addr & 0x8000)) {
					addr &= 0x3fff;
					if (buffer_read(&buffer_video, data, 1) == 1) {
						u8 len = data[0];

						logger(_L|L_3, _("(video >%04x, %d bytes)\n"),
							   addr, len);

						if (buffer_read(&buffer_video, 
										FLAT_MEMORY_PTR(md_video, addr), 
										len) == len)
						{
							while (len--) vdp_touch(addr++);
						}
					}
					else
						goto corrupt;
				}
				else
					logger(_L|L_3, _("(video reg >%04x)\n"),
						   addr);

			}
			break;

		case demo_type_sound:
			/* just send it */
			buffer_cache(&buffer_sound);
			logger(_L|L_2, _("Sound %d\n"), buffer_sound.len);
			while (buffer_read(&buffer_sound, data, 1) == 1) {
				logger(_L|L_2, _("(sound %02x)\n"), data[0]);
				sound_mmio_write(data[0]);
			}
			break;

		case demo_type_speech:
			buffer_cache(&buffer_speech);
			logger(_L|L_2, _("Speech %d\n"), buffer_speech.len);

			while (buffer_read(&buffer_speech, data, 1) == 1) {
				switch (data[0]) 
				{
				case demo_speech_starting:
					speech_demo_init();
					break;

				case demo_speech_adding_byte:	
					if (buffer_read(&buffer_speech, data, 1) != 1)
						goto corrupt;

					logger(_L|L_2, _("(speech %02x)\n"), data[0]);
					speech_demo_push(data[0]);
					break;

				case demo_speech_terminating:
					speech_demo_term();
					break;

				case demo_speech_stopping:
					speech_demo_stop();
					break;

				case demo_speech_interrupt:
					//speech_intr();
					break;
				}
			}
			break;

		case demo_type_cru_write:
		{
			//logger(_L|L_4, _("CRU write "));
			if (handle_read(&demo_handle, data, 5) != 5)
				break;
			int addr = data[0] | (data[1] << 8);
			int bits = data[2];
			int value = data[3] | (data[4] << 8);
			//logger(_L|L_4, _(">%04X[%d] = >%04X\n"), addr, bits, value);
			cruwrite(addr, value, bits);
			break;
		}

		case demo_type_cru_read:
		{
			//logger(_L|L_4, _("CRU read "));
			if (handle_read(&demo_handle, data+1, 3) != 3)
				break;
			int addr = data[1] | (data[2] << 8);
			int bits = data[3];
			int value = cruread(addr, bits);
			//logger(_L|L_4, _(">%04X[%d] = >%04X\n"), addr, bits, value);
			data[4] = value & 0xff;
			data[5] = (value >> 8) & 0xff;
			data[0] = demo_type_cru_write;
			handle_write(&demo_handle, data, 6);
			break;
		}

		default:
			logger(_L|LOG_ERROR, _("(type %d)\n"), data[0]);

		corrupt:
			logger(_L|LOG_ERROR|LOG_USER, _("Demo appears to be corrupt at %d\n"),
				   demo_handle.pos);
			break;
		}

		if (demo_client || demo_fifo) {
//			if (demo_client_activity++ >= 1024)
			//			return em_TooFast;
		}
	}

	return em_Quitting;
}

/*
 *	Called in lieu of execute().
 *	Send a buffer's worth of demo data to the UI, 
 *	keeping pace with the clock. 
 *
 *	return em_XXXX flag 
 */
int demo_playback_loop(void)
{
	int ret;

	/* shouldn't be here otherwise */
	if (!demo_playing)
		return em_KeepGoing;

	/* check for things not checked */
	if (stateflag & ST_TERMINATE) {
		return em_Quitting;
	}

	/* demo is paused? */
	if (!(stateflag & ST_DEMOING)) 
		return em_TooFast;

	ret = demo_runner();
	
	if (ret != em_Quitting)
		return ret;

	demo_stop_playback();

	/* turn off sound */
	sound_silence();
	speech_demo_stop();

	return em_KeepGoing;
}

static void demo_end_current(void)
{
	if (demo_recording) {
		logger(_L|LOG_USER, _("Ending currently recording demo.\n"));
		demo_stop_record();
	} else if (demo_playing) {
		logger(_L|LOG_USER, _("Stopping currently playing demo.\n"));
		demo_stop_playback();
	}
}

static DECL_SYMBOL_ACTION(demo_record_demo)
{
	if (task == csa_WRITE) {
		OSSpec spec;
		char *str;
		OSError err;

		demo_end_current();
		command_arg_get_string(SYM_ARG_1st, &str);

		if (!data_create_file(demospath, str, &spec, &OS_TEXTTYPE)) {
			if ((err = OS_MakeFileSpec(str, &spec)) != OS_NOERR) {
				logger(_L|LOG_USER|LOG_ERROR, _("Invalid demo filename '%s' (%s)\n"),
					   str, OS_GetErrText(err));
				return 0;
			}
		}
		return demo_start_record(&spec);
	}
	return 1;
}

static DECL_SYMBOL_ACTION(demo_play_demo)
{
	if (task == csa_WRITE) {
		OSSpec spec;
		char *str;
		OSError err;

		demo_end_current();
		command_arg_get_string(SYM_ARG_1st, &str);
		if (!data_find_file(demospath, str, &spec)) {
			if ((err = OS_MakeFileSpec(str, &spec)) != OS_NOERR
				|| (err = OS_Status(&spec)) != OS_NOERR) {
				logger(_L|LOG_USER|LOG_ERROR, _("Can't load demo '%s' (%s)\n"),
					   str, OS_GetErrText(err));
				return 0;
			}
		}
		if (demo_start_playback(&spec)) {
			/* so the end of the demo won't 
			   reboot or do something nasty */
			execution_pause(true);
			return 1;
		}
		return 0;
	}
	return 1;
}

static DECL_SYMBOL_ACTION(demo_listen_demo)
{
	if (task == csa_WRITE) {
		OSError err;
		int port;

		demo_end_current();
		command_arg_get_num(SYM_ARG_1st, &port);

		if (demo_start_listening(port)) {
			/* so the end of the demo won't 
			   reboot or do something nasty */
			execution_pause(true);
			return 1;
		}
		return 0;
	}
	return 1;
}

static DECL_SYMBOL_ACTION(demo_listen_demo_2)
{
	if (task == csa_WRITE) {
		OSError err;
		char *rstr, *wstr;
		OSSpec rspec, wspec;

		demo_end_current();
		command_arg_get_string(SYM_ARG_1st, &rstr);
		command_arg_get_string(SYM_ARG_2nd, &wstr);

		if (!data_find_file(demospath, rstr, &rspec)) {
			if ((err = OS_MakeFileSpec(rstr, &rspec)) != OS_NOERR
				|| (err = OS_Status(&rspec)) != OS_NOERR) {
				logger(_L|LOG_USER|LOG_ERROR, _("Can't load demo reader '%s' (%s)\n"),
					   rstr, OS_GetErrText(err));
				return 0;
			}
		}
		if (!data_find_file(demospath, wstr, &wspec)) {
			if ((err = OS_MakeFileSpec(wstr, &wspec)) != OS_NOERR
				|| (err = OS_Status(&wspec)) != OS_NOERR) {
				logger(_L|LOG_USER|LOG_ERROR, _("Can't load demo writer '%s' (%s)\n"),
					   wstr, OS_GetErrText(err));
				return 0;
			}
		}

		if (demo_start_fifo(&rspec, &wspec)) {
			/* so the end of the demo won't 
			   reboot or do something nasty */
			execution_pause(true);
			return 1;
		}
		return 0;
	}
	return 1;
}

static DECL_SYMBOL_ACTION(demo_stop_demo)
{
	if (task == csa_WRITE) {
		demo_end_current();
	}
	return 1;
}

void demo_init(void)
{
	command_symbol_table *democommands =
		command_symbol_table_new(_("Demo Options"),
								 _("These commands control recording and playback of V9t9 demos, "
								   "which do not require ROMs to view."),

		  command_symbol_new("DemosPath",
							 _("Set directory list to search for demo files; "
							 "first entry of the list is used to save demos"),
							 c_STATIC|c_CONFIG_ONLY,
							 NULL /* action*/,
							 RET_FIRST_ARG,
							 command_arg_new_string
							 (_("path"),
							  _("list of directories "
								"separated by one of these characters: '"
								OS_ENVSEPLIST
								"'"),
							  NULL	/* action */,
							  NEW_ARG_STRBUF(&demospath),
							  NULL /* next */ )
							 ,

		 command_symbol_new("RecordDemo",
							_("Start recording a new demo"),
							c_STATIC|c_DONT_SAVE,
							demo_record_demo,
							NULL /*return*/,
							command_arg_new_string
							("filename",
							 _("demo file to create"),
							 NULL /* action */ ,
							 NEW_ARG_NEW_STRBUF,
							 NULL /* next */ )
							,

		 command_symbol_new("PlayDemo",
							_("Start playing an existing demo"),
							c_STATIC|c_DONT_SAVE,
							demo_play_demo,
							NULL /*return*/,
							command_arg_new_string
							("filename",
							 _("demo file to play"),
							 NULL /* action */ ,
							 NEW_ARG_NEW_STRBUF,
							 NULL /* next */ )
							,

		 command_symbol_new("ListenDemoFifo",
							_("Start listening for demo data on fifos"),
							c_STATIC|c_DONT_SAVE,
							demo_listen_demo_2,
							NULL /*return*/,
							command_arg_new_string
							("read filename",
							 _("demo read file"),
							 NULL /* action */ ,
							 NEW_ARG_NEW_STRBUF,
							command_arg_new_string
							("write filename",
							 _("demo write file"),
							 NULL /* action */ ,
							 NEW_ARG_NEW_STRBUF,

							 NULL /* next */ ))
							,

		 command_symbol_new("ListenDemoPort",
							_("Start listening for demo data on port"),
							c_STATIC|c_DONT_SAVE,
							demo_listen_demo,
							NULL /*return*/,
							command_arg_new_num
							("port",
							 _("port to listen on"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(int),
							 NULL /* next */ )
							,

		 command_symbol_new("PauseDemo",
							_("Pause recording or playback of demo"),
							c_STATIC|c_DONT_SAVE,
							NULL /*action*/,
							RET_FIRST_ARG /*return*/,
							command_arg_new_toggle
							("off|on",
							 _("off: continues, on: pauses"),
							 NULL /* action */ ,
							 ARG_NUM(stateflag),
							 ST_DEMOING,
							 NULL /* next */ )
							,

		 command_symbol_new("StopDemo",
							_("Stop recording or playback of demo"),
							c_STATIC|c_DONT_SAVE,
							demo_stop_demo,
							NULL /*return*/,
							NULL /* args */
							,
		NULL /* next */ ))))))),
		NULL /* sub */,
        NULL /* next */
    );

	command_symbol_table_add_subtable(universe, democommands);

	// these constants were hardcoded in v9t9 6.0 and
	// should probably be kept this way
	buffer_init(&buffer_video, 8192, demo_type_video);
	buffer_init(&buffer_sound, 1024, demo_type_sound);
	buffer_init(&buffer_speech, 512, demo_type_speech);
}

void demo_term(void)
{
	buffer_term(&buffer_video);
	buffer_term(&buffer_sound);
	buffer_term(&buffer_speech);
}

