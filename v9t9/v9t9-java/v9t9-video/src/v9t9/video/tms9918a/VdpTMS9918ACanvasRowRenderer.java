/*
  VdpTMS9918ACanvasRenderer.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.tms9918a;

import static v9t9.common.hardware.VdpTMS9918AConsts.REG_ST;

import java.util.Arrays;
import java.util.BitSet;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;

/**
 * This is a renderer for the TI-99/4A VDP chip which renders to an IVdpCanvas
 * in row-by-row fashion.
 * 
 * @author ejs
 */
public class VdpTMS9918ACanvasRowRenderer extends BaseVdpTMS9918ACanvasRenderer {
	protected IVdpModeRowRedrawHandler vdpModeRedrawHandler;
	private int prevScanline;
	private boolean scanlineWrapped;
	private int currentScanline;
	private BitSet touchedRows = new BitSet(256); 
	
	public VdpTMS9918ACanvasRowRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		super(settings, renderer);

	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeRowRedrawHandler) redrawHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setupBackdrop()
	 */
	@Override
	protected void setupBackdrop() {
		update();
		super.setupBackdrop();
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVideoMode()
	 */
	@Override
	protected void setVideoMode() {
		// flush changes from the previous mode
		update();
		synchronized (vdpCanvas) {
			prevScanline = 0;
			scanlineWrapped = false;
			touchedRows.clear();
			touchedRows.set(0, vdpCanvas.getHeight());
		}
	}


	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBitmapMode()
	 */
	@Override
	protected void setBitmapMode() {
		super.setBitmapMode();
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBlankMode()
	 */
	@Override
	protected void setBlankMode() {
		super.setBlankMode();
		vdpModeRedrawHandler = blankModeRedrawHandler;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setGraphicsMode()
	 */
	@Override
	protected void setGraphicsMode() {
		super.setGraphicsMode();
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setMultiMode()
	 */
	@Override
	protected void setMultiMode() {
		super.setMultiMode();
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setTextMode()
	 */
	@Override
	protected void setTextMode() {
		super.setTextMode();
		vdpModeRedrawHandler = new TextModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#onScanline(int)
	 */
	@Override
	protected void onScanline(int scanline) {
		synchronized (vdpCanvas) {
			super.onScanline(scanline);
			currentScanline = scanline;
			if (scanline == 0) {
				scanlineWrapped = true;
			}
		}
	}
	
    public synchronized boolean update() {
		flushVdpChanges(vdpModeRedrawHandler);
		if (!vdpChanges.changed)
			return false;
		//System.out.println(System.currentTimeMillis());
		if (vdpModeRedrawHandler != null) {
			//long start = System.currentTimeMillis();

			// don't let video rendering happen in middle of updating
			synchronized (vdpCanvas) {
				if (colorsChanged) {
					vdpCanvas.syncColors();
					colorsChanged = false;
				}
				
				
				if (vdpChanges.fullRedraw) {
					vdpCanvas.clear();
					vdpChanges.screen.set(0, vdpChanges.screen.size());
					touchedRows.clear();
					touchedRows.set(0, vdpCanvas.getHeight());
				} else {
					vdpModeRedrawHandler.prepareUpdate();
					for (int i = vdpChanges.screen.nextSetBit(0); 
							i >= 0 && i < vdpModeInfo.screen.size; 
							i = vdpChanges.screen.nextSetBit(i+1)) 
					{
						touchedRows.set(i >> 5);
					}	
				}
				
				if (!isBlank()) {
					if (spriteRedrawHandler != null && drawSprites) {
						byte vdpStatus = (byte) vdpChip.getRegister(REG_ST);
						vdpStatus = spriteRedrawHandler.updateSpriteCoverage(
								vdpStatus, vdpChanges.fullRedraw);
						if (!pauseMachine.getBoolean())
							vdpChip.setRegister(REG_ST, vdpStatus);
					}
				}
				
				if (scanlineWrapped) {
					updateRows(prevScanline, vdpCanvas.getHeight());
					prevScanline = 0;
				}
				if (currentScanline > prevScanline) {
					updateRows(prevScanline, currentScanline);
					prevScanline = currentScanline;
				}
				if (spriteRedrawHandler != null && drawSprites) {
					spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
				}
				
				if (scanlineWrapped) {
					Arrays.fill(vdpChanges.patt, (byte) 0);
					Arrays.fill(vdpChanges.color, (byte) 0);
					
					if (drawSprites) {
						Arrays.fill(vdpChanges.sprpat, (byte) 0);
						vdpChanges.sprite = 0;
					}
					
					vdpChanges.fullRedraw = false;
					vdpChanges.changed = ! touchedRows.isEmpty();
					scanlineWrapped = false;
				}
			}

			//System.out.println("elapsed: " + (System.currentTimeMillis() - start));
		}
		
		return true;
	}
	

	/**
	 * @param prevScanline2
	 * @param to
	 */
	private void updateRows(int from, int to) {
		int per = vdpModeRedrawHandler.getCharsPerRow();
		vdpModeRedrawHandler.updateCanvas(from, to);
		
		touchedRows.set(from, to, false);
		vdpCanvas.markDirtyRows(from, to);
		for (int r = (from + 7)  >> 3; r < (to >> 3); r++) {
			vdpChanges.screen.set(r * per, (r + 1) * per, false);
		}
	}

	
}
