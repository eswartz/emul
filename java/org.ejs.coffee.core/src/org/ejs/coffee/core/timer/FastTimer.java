/**
 * 
 */
package org.ejs.coffee.core.timer;

import java.util.ArrayList;

import com.vladium.utils.timing.HRTimer;
import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;


/**
 * Java's system timer sucks on Windows and there's no way to work around it.
 * We'll provide a specific implementation here to use for the CPU and VDP only.
 * @author Ed
 *
 */
public class FastTimer {
	private ITimer timer;
	private ArrayList<RunnableInfo> taskinfos;
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
		taskinfos = new ArrayList<RunnableInfo>();
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
							//long elapsed = now - prev;
							//System.out.print(elapsed + ",");
							
							RunnableInfo[] infoArray;
							synchronized (taskinfos) {
								infoArray = (RunnableInfo[]) taskinfos.toArray(new RunnableInfo[taskinfos
										.size()]);
							}
	
							for (RunnableInfo info : infoArray) {
								try {
									if (now >= info.deadline) {
										//System.out.println("moving from " + info.deadline + " by " + info.delay + " to " + (info.delay + info.deadline));
										 
										info.deadline += info.delay;
										info.task.run();
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
		//timerThread.setPriority(Thread.MAX_PRIORITY);
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
