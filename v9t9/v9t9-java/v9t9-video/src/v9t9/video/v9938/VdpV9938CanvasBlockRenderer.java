/*
  VdpV9938CanvasRenderer.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;


import static v9t9.common.hardware.VdpTMS9918AConsts.REG_ST;

import java.util.Arrays;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.RedrawBlock;
import v9t9.video.IVdpModeBlockRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;

/**
 * This is a renderer for the V9938 video chip which renders to an IVdpCanvas.  
 * 
 * This functions as a superset of the TMS9918A renderer.
 * <p>
 * Mode bits:
 * <p>
 * R0:	M3 @ 1, M4 @ 2, M5 @ 3
 * R1:	M1 @ 4, M2 @ 3 
 * <p>
 * <pre>
 *                   M1  M2  M3  M4  M5     Mode #
 * Text 1 mode:      1   0   0   0   0		= 1		>81F0, >8000
 * Text 2 mode:      1   0   0   1   0		= 9		>81F0, >8004
 * Multicolor:       0   1   0   0   0		= 2		>81C0, >8000
 * Graphics 1 mode:  0   0   0   0   0		= 0		>81E0, >8000
 * Graphics 2 mode:  0   0   1   0   0		= 4		>81E0, >8002
 * Graphics 3 mode:  0   0   0   1   0		= 8		>81E0, >8004 (bitmap + new sprites)
 * Graphics 4 mode:  0   0   1   1   0		= 12	>81E0, >8006 (bitmap 256x192x16)
 * Graphics 5 mode:  0   0   0   0   1		= 16	>81E0, >8008 (bitmap 512x192x4)
 * Graphics 6 mode:  0   0   1   0   1		= 20	>81E0, >800A (bitmap 512x192x16)
 * Graphics 7 mode:  0   0   1   1   1		= 28	>81E0, >800E (bitmap 256x192x256)
 * </pre>
 * TODO: toying with row masking
 * @author ejs  
 *
 */
public class VdpV9938CanvasBlockRenderer extends BaseVdpV9938CanvasRenderer implements IVdpCanvasRenderer {
	private RedrawBlock[] blocks;
	protected IVdpModeBlockRedrawHandler vdpModeRedrawHandler;
	
	public VdpV9938CanvasBlockRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		super(settings, renderer);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.v9938.BaseVdpV9938CanvasRenderer#getModeRedrawHandler()
	 */
	@Override
	protected IVdpModeRedrawHandler getModeRedrawHandler() {
		return vdpModeRedrawHandler;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVdpModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeBlockRedrawHandler) redrawHandler;
	}
	
	protected int getMaxRedrawblocks() {
		return 80 * 27;
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
	
	protected void setText2Mode() {
		super.setText2Mode();
		initUpdateBlocks(6);
	}
	
	protected void setGraphics4Mode() {
		super.setGraphics4Mode();
		initUpdateBlocks(8);
	}

	
	protected void setGraphics5Mode() {
		super.setGraphics5Mode();
		initUpdateBlocks(8);
	}

	protected void setGraphics6Mode() {
		super.setGraphics6Mode();
		initUpdateBlocks(8);
	}
	
	protected void setGraphics7Mode() {
		super.setGraphics7Mode();
		initUpdateBlocks(8);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVideoMode()
	 */
	@Override
	protected void setVideoMode() {
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
