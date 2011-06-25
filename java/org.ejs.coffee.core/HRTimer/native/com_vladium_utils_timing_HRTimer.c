/* ------------------------------------------------------------------------- */
/*
 * A win32 implementation of JNI methods in com.vladium.utils.timing.HRTimer
 * class. The author compiled it using Microsoft Visual C++ but the code
 * should be easy to use with any compiler for win32 platform.
 *
 * For simplicity, this implementaion assumes JNI 1.2+ and omits error handling.
 *
 * (C) 2002, Vladimir Roubtsov [vroubtsov@illinoisalumni.org]
 */
/* ------------------------------------------------------------------------- */

#if !defined NDEBUG
#include <stdio.h>
#endif // NDEBUG

#include <windows.h>

#include "com_vladium_utils_timing_HRTimer.h"

// scale factor for converting a performancce counter reading into milliseconds:
static jdouble s_scaleFactor;
static LARGE_INTEGER s_counterFrequency;
static TIMECAPS s_timecaps;

/* ------------------------------------------------------------------------- */

/*
 * This method was added in JNI 1.2. It is executed once before any other
 * methods are called and is ostensibly for negotiating JNI spec versions, but
 * can also be conveniently used for initializing variables that will not
 * change throughout the lifetime of this process.
 */
JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void * reserved)
{

    QueryPerformanceFrequency (& s_counterFrequency);

    // NOTE: counterFrequency will be zero for a machine that does not have
    // support for a high-resolution counter. This is only likely for very
    // old hardware but for a robust implementation you should handle this
    // case.

#if !defined NDEBUG
    printf ("PCFrequency called: %I64d\n", s_counterFrequency.QuadPart);
#endif

    s_scaleFactor = s_counterFrequency.QuadPart / 1000.0;


    return JNI_VERSION_1_2;
}
/* ......................................................................... */

/*
 * Class:     com_vladium_utils_timing_HRTimer
 * Method:    get
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_com_vladium_utils_timing_HRTimer_getTime (JNIEnv * e, jclass cls)
{
    LARGE_INTEGER counterReading;

    QueryPerformanceCounter (& counterReading);

    return counterReading.QuadPart / s_scaleFactor;
}

JNIEXPORT jlong JNICALL
Java_com_vladium_utils_timing_HRTimer_getTimerFrequency (JNIEnv * e, jclass cls)
{
    return s_counterFrequency.QuadPart;
}

JNIEXPORT jlong JNICALL
Java_com_vladium_utils_timing_HRTimer_getTimerReading (JNIEnv * e, jclass cls)
{
    LARGE_INTEGER counterReading;

    QueryPerformanceCounter (& counterReading);

    return counterReading.QuadPart;
}

JNIEXPORT jint JNICALL
Java_com_vladium_utils_timing_HRTimer_getMinimumTimerResolution (JNIEnv * e, jclass cls)
{
	MMRESULT result = timeGetDevCaps(&s_timecaps, sizeof(s_timecaps));
    return result == TIMERR_NOERROR ? s_timecaps.wPeriodMin : 50;
}

JNIEXPORT jboolean JNICALL
Java_com_vladium_utils_timing_HRTimer_timeBeginPeriod (JNIEnv * e, jclass cls, jint ms)
{
	MMRESULT result = timeBeginPeriod(ms);
    return result == TIMERR_NOERROR;
}

JNIEXPORT jboolean JNICALL
Java_com_vladium_utils_timing_HRTimer_timeEndPeriod (JNIEnv * e, jclass cls, jint ms)
{
	MMRESULT result = timeEndPeriod(ms);
    return result == TIMERR_NOERROR;
}

/* ------------------------------------------------------------------------- */
/* end of file */
