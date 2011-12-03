/**
 * 
 */
package v9t9.base.internal.test;

import v9t9.base.timer.PthreadLibrary;
import v9t9.base.timer.RTLibrary;
import v9t9.base.timer.RTLibrary.RT;

import com.sun.jna.Native;

/**
 * @author ejs
 *
 */
public class TestRT {
	static long thread() { return  (((long)PthreadLibrary.INSTANCE.pthread_self()) & 0xffffffff); };
	
	static int hit;
	static void foo(RT rt, long current_time) {
		hit++;
		System.out.printf("* called at %d from %d\n", current_time,  thread());
		if (hit >= 10) {
			RTLibrary.INSTANCE.rt_periodic(rt, 0, 0, null);
		}
		
	}
	
	public static void main(String[] args) {
		Native.setProtected(true);
		System.out.println(System.getenv("LD_PRELOAD"));	// ${system_property:java.home}/lib/${system_property:os.arch}/libjsig.so or /usr/lib/jvm/java-6-sun-1.6.0.20/jre/lib/i386/libjsig.so
		RTLibrary.INSTANCE.rt_init();
		
		Thread main = new Thread("main") {
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				System.out.printf("Thread is %d\n", thread());

				System.out.printf("Current time: %d\n", RTLibrary.INSTANCE.rt_get_time());
				System.out.printf("Current time: %d\n", RTLibrary.INSTANCE.rt_get_time());
				System.out.printf("Current time: %d\n", RTLibrary.INSTANCE.rt_get_time());
				System.out.printf("Current time: %d\n", RTLibrary.INSTANCE.rt_get_time());
				
				RT rt = RTLibrary.INSTANCE.rt_new();

				RTLibrary.TimerCallFunc fooCb = new RTLibrary.TimerCallFunc() {
					
					public void invoke(RT rt, long currentTime) {
						foo(rt, currentTime);
					}
				};
				RTLibrary.INSTANCE.rt_oneshot(rt, RTLibrary.INSTANCE.rt_get_time() + 1000000000L, 
						fooCb);

				try {
					sleep(2000);
				} catch (InterruptedException e) {
					return;
				}

				assert(hit > 0);

				hit = 0;
				RTLibrary.INSTANCE.rt_periodic(rt, RTLibrary.INSTANCE.rt_get_time(), 1000000000L / 100000, 
						fooCb);

				try {
					sleep(5000);
				} catch (InterruptedException e) {
					return;
				}

				assert(hit == 10);

				RTLibrary.INSTANCE.rt_free(rt);
			}
		};
		
		main.start();
		
		try {
			main.join();
		} catch (InterruptedException e) {
		}
	}
}
