/*
  BaseBitmapModeConverter.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.IPaletteMapper;

import v9t9.common.video.VdpColorManager;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public abstract class BaseBitmapModeConverter implements IModeConverter {

	protected boolean useColorMappedGreyScale;
	protected VdpColorManager colorMgr;
	protected IPaletteMapper mapColor;

	private final int perByte;

	public BaseBitmapModeConverter(
			int perByte,
			VdpColorManager colorMgr, boolean useColorMappedGreyScale, 
			IPaletteMapper mapColor) {
		this.perByte = perByte;
		this.colorMgr = colorMgr;
		this.useColorMappedGreyScale = useColorMappedGreyScale;
		this.mapColor = mapColor;
		
	}
	protected interface IBitmapColorUser {
		/** Called once per each 8x1 block 
		 * 
		 * @param sorted: sorted map of pixel to count
		 * */
		void useColor(int x, int maxx, int y, List<Pair<Integer,Integer>> sorted);
	}

	/**
	 * Only two colors can exist per Nx1 pixels, so find those colors.
	 * If there's a tossup (lots of colors), use information from neighbors
	 * to enhance the odds.
	 * @param img
	 */
	protected void analyzeBitmap(BufferedImage img, int includeSides, IBitmapColorUser colorUser) {
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histograms = new Map[(img.getWidth() + perByte - 1) / perByte];
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histogramSides = new Map[(img.getWidth() + perByte - 1) / perByte];
		Map<Integer, Integer>[] histogramsAbove = null;
		
		int width = img.getWidth();
		for (int y = 0; y < img.getHeight(); y++) {
			// first scan: get histogram for each range
			
			for (int x = 0; x < width; x += perByte) {
				Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
				Map<Integer, Integer> histogramSide = new HashMap<Integer, Integer>();
				
				histograms[x / perByte] = histogram;
				histogramSides[x / perByte] = histogramSide;
				
				int maxx = x + perByte < width ? x + perByte : width;
				
				int scmx;
				int scmn;
				
				// scan outside the perByte8 pixels to get a broader
				// idea of what colors are important
				if (includeSides > 0) {
					scmn = Math.max(0, x - includeSides);
					scmx = Math.min(width, maxx + includeSides);
				} else {
					scmn = x;
					scmx = maxx;
				}
				
				int pixel = 0;
				for (int xd = scmn; xd < scmx; xd++) {
					if (xd < width) {
						pixel = img.getRGB(xd, y) & 0xffffff;
						if (useColorMappedGreyScale) {
							pixel = mapColor.getPixelForGreyscaleMode(pixel);
						}
					}
					
					Map<Integer, Integer> hist = (xd >= x && xd < maxx) ? histogram : histogramSide;
					
					Integer cnt = hist.get(pixel);
					if (cnt == null)
						cnt = 1;
					else
						cnt++;
					
					hist.put(pixel, cnt);
				}
			}
			
			
			for (int x = 0; x < width; x += perByte) {
				Map<Integer, Integer> histogram = histograms[x / perByte];
				Map<Integer, Integer> histogramSide = histogramSides[x / perByte];
				Map<Integer, Integer> histogramAbove = histogramsAbove != null ? histogramsAbove[x / perByte] : null;
				
				int maxx = x + perByte < width ? x + perByte : width;
				
				// get prominent colors, weighing colors that also
				// appear in surrounding pixels higher  
				List<Pair<Integer, Integer>> sorted = new ArrayList<Pair<Integer,Integer>>();
				for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
					Integer c = entry.getKey();
					int cnt = entry.getValue();
					if (includeSides > 0) {
						cnt *= 4;
						
						int minDist = Integer.MAX_VALUE;
						int minCnt = 0;
						for(Map.Entry<Integer, Integer> sideEntry : histogramSide.entrySet()) {
							int dist = ColorMapUtils.getPixelDistance(c, sideEntry.getKey());
							if (dist < minDist && dist < 0x1f*0x1f*3 && sideEntry.getValue() > minCnt) {
								minDist = dist;
								minCnt = sideEntry.getValue();
							}
						}
						if (histogramAbove != null) {
							for(Map.Entry<Integer, Integer> aboveEntry : histogramAbove.entrySet()) {
								int dist = ColorMapUtils.getPixelDistance(c, aboveEntry.getKey());
								if (dist < minDist && dist < 0x1f*0x1f*3 && aboveEntry.getValue() > minCnt) {
									minDist = dist;
									minCnt = aboveEntry.getValue();
								}
							}
						}
						cnt += minCnt;
					}
					sorted.add(new Pair<Integer, Integer>(c, cnt));
				}
				Collections.sort(sorted, new Comparator<Pair<Integer, Integer>>() {
					
					@Override
					public int compare(Pair<Integer, Integer> o1,
							Pair<Integer, Integer> o2) {
						return o2.second - o1.second;
					}
					
				});
				
				colorUser.useColor(x, maxx, y, sorted);
			}
			histogramsAbove = histograms;
		}
	}

}
