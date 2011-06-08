/**
 * 
 */
package org.ejs.coffee.core.timer;

import org.eclipse.core.runtime.ListenerList;

import com.vladium.utils.timing.HRTimer;
import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;


/**
 * This is a timer thread which makes use of a high-resolution system timer
 * to provide predictable numbers of interrupts for a given task.  
 * In a consistently loaded system, the timing should be predictable as
 * well. 
 * @author Ed
 *
 */
public class FastTimer {
	private ITimer timer;
	private ListenerList taskinfos;
	private Object controlLock;
	private Thread timerThread;
	private static int gCnt;
	
	class RunnableInfo {
		public RunnableInfo(Runnable task, long delay) {
			super();
			this.task = task;
			this.delay = delay;
			this.deadline = timer.getTimeNs() + this.delay;
		}
		Runnable task;
		/** ns */
		long deadline;
		/** ns */
		long delay;
	}
	public FastTimer() {
		taskinfos = new ListenerList();
		timer = TimerFactory.newTimer();
		controlLock = new Object();
	}
	
	/**
	 * Schedule a task to occur the given number of times per second.
	 * @param task
	 * @param perSecond
	 */
	public void scheduleTask(Runnable task, long perSecond) {
		synchronized (controlLock) {
			RunnableInfo info = new RunnableInfo(task, 1000000000L / perSecond);
			taskinfos.add(info);
			
			System.out.println("Adding task @ " + info.delay + " ns");
			if (taskinfos.size() > 0) {
				if (timerThread == null)
					startTimer();
			}
		}
	}

	private void startTimer() {
		timerThread = new Thread("FastTimer-"+gCnt++) {
			@Override
			public void run() {
				if (timer instanceof HRTimer) {
					System.out.println("Minimum resolution is: " + HRTimer.getMinimumTimerResolution());
					HRTimer.timeBeginPeriod(10);
				}
				try {
					//long prev = timer.getTimeNs();
					//double prevd = timer.getTimeDouble();
					while (true) {
						if (timerThread == null)
							break;
						try {
							Thread.sleep(0, 50000);
						} catch (InterruptedException e) {
							return;
						}
						synchronized (controlLock) {
							long now = timer.getTimeNs();
							//System.out.println(now);
							//long elapsed = now - prev;
							//System.out.print(elapsed + ",");
	
							for (Object o : taskinfos.getListeners()) {
								RunnableInfo info = (RunnableInfo) o;
								try {
									if (now >= info.deadline) {
										//System.out.println("moving from " + info.deadline + " by " + info.delay + " to " + (info.delay + info.deadline));
										 
										while (now >= info.deadline) {
											info.deadline += info.delay;
											info.task.run();
										}
									}
									else if ((now ^ info.deadline) < 0) {
										// clock overflowed
										info.deadline = now + info.delay;
										info.task.run();
									}
								} catch (Throwable t) {
									t.printStackTrace();
									info.deadline = -1;
								}
							}
							//prev = now;
						}
					}
				} finally {
					if (timer instanceof HRTimer) {
						HRTimer.timeEndPeriod(10);
					}
				}
			}
		};
		timerThread.setPriority(Thread.MAX_PRIORITY);
		timerThread.start();		
	}
	
	public void cancel() {
		synchronized (controlLock) {
			if (timerThread != null) {
				timerThread.interrupt();
				taskinfos.clear();
				timerThread = null;
			}
		}
	}
}
