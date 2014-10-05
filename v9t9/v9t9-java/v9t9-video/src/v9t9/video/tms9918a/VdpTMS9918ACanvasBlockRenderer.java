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

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.video.RedrawBlock;
import v9t9.video.IVdpModeBlockRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;

/**
 * This is a renderer for the TI-99/4A VDP chip which renders to an IVdpCanvas
 * by tracking 8x8 or 6x8 blocks that need to be redrawn.
 * 
 * @author ejs
 */
public class VdpTMS9918ACanvasBlockRenderer extends BaseVdpTMS9918ACanvasRenderer {
	private RedrawBlock[] blocks;
	protected IVdpModeBlockRedrawHandler vdpModeRedrawHandler;

	public VdpTMS9918ACanvasBlockRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		super(settings, renderer);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeBlockRedrawHandler) redrawHandler;
	}
	/** preinitialize the update blocks with the sizes for this mode */
	protected void initUpdateBlocks(int blockWidth) {
		int w = blockWidth;
    	int h = 8;
		if (blocks == null) {
			blocks = new RedrawBlock[getMaxRedrawblocks()];
			for (int i = 0; i < blocks.length; i++) {
				blocks[i] = new RedrawBlock();
 			}
		}
		if (blocks[0].w != blockWidth) {
			for (int i = 0; i < blocks.length; i++) {
				blocks[i].w = w;
				blocks[i].h = h;
			}
		}
	}

	protected final void setVideoMode() {
	}

	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBitmapMode()
	 */
	@Override
	protected void setBitmapMode() {
		super.setBitmapMode();
		initUpdateBlocks(8);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBlankMode()
	 */
	@Override
	protected void setBlankMode() {
		super.setBlankMode();
		initUpdateBlocks(8);
		if (vdpModeRedrawHandler != null)
			vdpModeRedrawHandler.clear();
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setGraphicsMode()
	 */
	@Override
	protected void setGraphicsMode() {
		super.setGraphicsMode();
		initUpdateBlocks(8);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setMultiMode()
	 */
	@Override
	protected void setMultiMode() {
		super.setMultiMode();
		initUpdateBlocks(8);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setTextMode()
	 */
	@Override
	protected void setTextMode() {
		super.setTextMode();
		initUpdateBlocks(6);
	}
	
	protected int getMaxRedrawblocks() {
		return 1024;
	}

	
    public synchronized boolean update() {
		flushVdpChanges(vdpModeRedrawHandler);
		if (!vdpChanges.changed)
			return false;
		//System.out.println(System.currentTimeMillis());
		if (vdpModeRedrawHandler != null) {
			//long start = System.currentTimeMillis();
			
			int count = 0;
			
			// don't let video rendering happen in middle of updating
			synchronized (vdpCanvas) {
				if (colorsChanged) {
					vdpCanvas.syncColors();
					colorsChanged = false;
				}
				
				vdpModeRedrawHandler.prepareUpdate();
				
				if (vdpChanges.fullRedraw) {
					// clear for the actual mode (not blank mode)
					vdpModeRedrawHandler.clear();
					vdpCanvas.markDirty();
				}
				
				if (!isBlank()) {
					if (spriteRedrawHandler != null && drawSprites) {
						byte vdpStatus = (byte) vdpChip.getRegister(REG_ST);
						vdpStatus = spriteRedrawHandler.updateSpriteCoverage(
								vdpStatus, vdpChanges.fullRedraw);
						if (!pauseMachine.getBoolean())
							vdpChip.setRegister(REG_ST, vdpStatus);
					}
					
					if (vdpChanges.fullRedraw) {
						vdpChanges.screen.set(0, getMaxRedrawblocks());
					}
					
					count = vdpModeRedrawHandler.updateCanvas(blocks);
					if (spriteRedrawHandler != null && drawSprites) {
						spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
					}
				}
				

				vdpCanvas.markDirty(blocks, count);
				
				vdpChanges.screen.clear();
				Arrays.fill(vdpChanges.patt, (byte) 0);
				Arrays.fill(vdpChanges.color, (byte) 0);
				
				if (drawSprites) {
					Arrays.fill(vdpChanges.sprpat, (byte) 0);
					vdpChanges.sprite = 0;
				}
				
				vdpChanges.fullRedraw = false;
				vdpChanges.changed = false;
			}

			//System.out.println("elapsed: " + (System.currentTimeMillis() - start));
		}
		
		return true;
	}

}
