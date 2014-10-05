/**
 * 
 */
package v9t9.video.v9938;

import static v9t9.common.hardware.VdpTMS9918AConsts.REG_ST;

import java.util.Arrays;
import java.util.BitSet;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.VdpTMS9918AConsts;
import v9t9.common.hardware.VdpV9938Consts;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;
import v9t9.video.tms9918a.VdpTMS9918ACanvasRowRenderer;

/**
 * @author ejs
 *
 */
public class VdpV9938CanvasRowRenderer extends BaseVdpV9938CanvasRenderer {

	private IVdpModeRowRedrawHandler vdpModeRedrawHandler;
	private int currentScanline;
	private boolean scanlineWrapped;
	private int prevScanline;
	private BitSet touchedRows = new BitSet(256);
	
	public VdpV9938CanvasRowRenderer(ISettingsHandler settings,
			IVideoRenderer renderer) {
		super(settings, renderer);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.v9938.BaseVdpV9938CanvasRenderer#getModeRedrawHandler()
	 */
	@Override
	protected
	IVdpModeRedrawHandler getModeRedrawHandler() {
		return vdpModeRedrawHandler;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVdpModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeRowRedrawHandler) redrawHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.v9938.BaseVdpV9938CanvasRenderer#setupBackdrop()
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
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvasRenderer#update()
	 */
	@Override
	public synchronized boolean update() {
		// changes usually handled immediately
		flushVdpChanges(vdpModeRedrawHandler);
		if (!vdpChanges.changed || vdpModeInfo == null)
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
				
				vdpModeRedrawHandler.prepareUpdate();
				if (vdpChanges.fullRedraw) {
//					vdpCanvas.clear();
					vdpChanges.screen.set(0, vdpChanges.screen.size());
					touchedRows.clear();
					touchedRows.set(0, vdpCanvas.getHeight());
				} else {
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
				
				if (scanlineWrapped && prevScanline < vdpCanvas.getHeight()) {
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
	

	private void updateRows(int from, int to) {
		VdpTMS9918ACanvasRowRenderer.updateRows(
				vdpModeRedrawHandler, vdpCanvas, vdpChanges, vdpregs, touchedRows,
				isTextMode(),
					from, to);
	}

	/**
	 * @return
	 */
	protected boolean isTextMode() {
		return modeNumber == VdpV9938Consts.MODE_TEXT2
				|| ((vdpregs[1] & VdpTMS9918AConsts.R1_M1) != 0
					&& modeNumber <= VdpTMS9918AConsts.MODE_LAST_STANDARD); 
	}

}
