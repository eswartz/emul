/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.graphics.Rectangle;
import org.ejs.coffee.core.utils.Pair;

/**
 * This "canvas" contains information about icons that should
 * overlay the video screen and be redrawn or rescaled along with it.  
 * @author ejs
 *
 */
public class IndicatorCanvas {

	public static class Indicator {
		public final ImageIconInfo imageIconInfo;
		public final double xpos, ypos;
		public final double xspan, yspan;
		/**
		 * Create an indicator.
		 * @param imageIconInfo
		 * @param xpos
		 * @param ypos
		 * @param xspan
		 * @param yspan
		 */
		public Indicator(ImageIconInfo imageIconInfo, double xpos, double ypos, double xspan, double yspan) {
			this.imageIconInfo = imageIconInfo;
			this.xpos = xpos;
			this.ypos = ypos;
			this.xspan = xspan;
			this.yspan = yspan;
		}
		
		public Rectangle getBounds(Rectangle full) {
			int cx = (int) (xpos * full.width);
			int cy = (int) (ypos * full.height);
			
			int sx = (int) (xspan * full.width);
			int sy = (int) (yspan * full.height);

			int x = (int) (cx - sx * xpos);
			int y = (int) (cy - sy * xpos);
			
			return new Rectangle(x, y, sx, sy);
		}
	}

	private static final int INDENT = 8;
	
	private boolean dirty;
	private final Timer timer;

	private Map<String, Indicator> indicators;
	private Map<String, TimerTask> indicatorTasks;

	final ISwtVideoRenderer videoRenderer;
	
	public IndicatorCanvas(Timer timer, ISwtVideoRenderer videoRenderer) {
		this.timer = timer;
		this.videoRenderer = videoRenderer;
		dirty = false;
		indicators = Collections.synchronizedMap(new LinkedHashMap<String, IndicatorCanvas.Indicator>());
		indicatorTasks = Collections.synchronizedMap(new HashMap<String, TimerTask>());
	}
	
	public void addIndicator(final String indicatorId, final Indicator indicator, int timeoutMs) {
		TimerTask task = indicatorTasks.get(indicatorId);
		if (task != null)
			task.cancel();
		
		indicators.put(indicatorId, indicator);
		markDirty();

		task = new TimerTask() {

			@Override
			public void run() {
				indicators.remove(indicatorId);
				markDirty();
			} 
			
		};
		indicatorTasks.put(indicatorId, task);
		
		timer.schedule(task, timeoutMs);
	}


	/**
	 * @param indicatorId
	 */
	public void removeIndicator(String indicatorId) {
		TimerTask task = indicatorTasks.remove(indicatorId);
		if (task != null)
			markDirty();
		
		indicators.remove(indicatorId);
	}

	
	/**
	 * Mark all entities dirty due to resize
	 */
	public void markDirty() {
		dirty = true;
		//videoRenderer.reblit();
	}


	/**
	 * Get the bounds and images to render. 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Pair<Rectangle, ImageIconInfo>[] getIndicators(Rectangle full) {
		synchronized (indicators) {
			List<Pair<Rectangle, ImageIconInfo>> list = new ArrayList<Pair<Rectangle,ImageIconInfo>>(indicators.size());
			for (Indicator ind : indicators.values()) {
				Rectangle indB = ind.getBounds(full);
				if (indB == null)
					continue;
				list.add(new Pair<Rectangle, ImageIconInfo>(indB, ind.imageIconInfo));
			}
			return list.toArray(new Pair[list.size()]);
		}
	}
	/**
	 * Report the physical rectangle in the canvas that
	 * needs to be updated 
	 * @return
	 */
	public Rectangle update(Rectangle bounds) {
		if (!dirty)
			return null;

		Rectangle full = new Rectangle(INDENT, INDENT, bounds.width - INDENT, bounds.height - INDENT);
		
		Rectangle affected = null;
		synchronized (indicators) {
			for (Indicator ind : indicators.values()) {
				Rectangle indB = ind.getBounds(full);
				if (indB == null)
					continue;
				if (affected == null)
					affected = indB;
				else
					affected.add(indB);
			}
		}
		return affected;
	}

	public boolean isDirty() {
		return dirty;
	}
	
	/** Test it */
	public static void main(String[] args) {
		Rectangle full = new Rectangle(0, 0, 1024, 768);
		Indicator i;
		ImageIconInfo info = new ImageIconInfo(null);
		i = new Indicator(info, 0, 0, 0.25, 0.25);
		System.out.println(i.getBounds(full));
		i = new Indicator(info, 0.5, 0.5, 0.25, 0.25);
		System.out.println(i.getBounds(full));
		i = new Indicator(info, 1, 1, 0.25, 0.25);
		System.out.println(i.getBounds(full));
	}
}
