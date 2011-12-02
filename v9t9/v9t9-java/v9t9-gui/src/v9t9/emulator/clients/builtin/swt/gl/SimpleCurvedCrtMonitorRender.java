/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.gl;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glMultiTexCoord2f;

/**
 * @author Ed
 *
 */
public class SimpleCurvedCrtMonitorRender extends BaseMonitorEffect {
	public static final SimpleCurvedCrtMonitorRender INSTANCE = new SimpleCurvedCrtMonitorRender();
	
	public SimpleCurvedCrtMonitorRender() {
	}

	@Override
	public void init() {
		
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.gl.IGLMonitorEffect#render()
	 */
	@Override
	public void render() {
		glBegin(GL_QUADS);

		// vertically taper the screen
		drawVerticalCurves(0.5f, 0.5f, 0.5f, 0.5f);
		drawVerticalCurves(0.5f, -0.5f, 0.5f, -0.5f);

		glEnd();
	}

	private final static float CVMAJOR = 0.3f;
	private final static float TVMAJOR = 0.275f;
	private final static float CHMAJOR = 0.2f;
	private final static float THMAJOR = 0.19f;
	
	private final static float EPSILON = 0.02f;
	
	private void drawVerticalCurves(float cy, float cdy, float ty, float tdy) {
		if (Math.abs(cdy) < EPSILON) {
			drawHorizontalCurves(0.5f, -0.5f, cy, cdy,
					0.5f, -0.5f, ty, tdy);
			drawHorizontalCurves(0.5f, 0.5f, cy, cdy,
					0.5f, 0.5f, ty, tdy);
			return;
		}
		// horizontally taper a curve
		drawHorizontalCurves(0.5f, -0.5f, cy, cdy * CVMAJOR,
				0.5f, -0.5f, ty, tdy * TVMAJOR);
		drawHorizontalCurves(0.5f, 0.5f, cy, cdy * CVMAJOR,
				0.5f, 0.5f, ty, tdy * TVMAJOR);
		
		// inner part
		drawVerticalCurves(cy + cdy * CVMAJOR, cdy * (1 - CVMAJOR), 
				ty + tdy * TVMAJOR, tdy * (1 - TVMAJOR));
	}

	private void drawHorizontalCurves(float cx, float cdx, float cy, float cdy,
			float tx, float tdx, float ty, float tdy) {
		if (Math.abs(cdx) < EPSILON) {
			drawQuad(cx, cy, cx + cdx, cy + cdy, tx, ty, tx + tdx, ty + tdy);
			return;
		}
		// inner half
		drawHorizontalCurves(cx, cdx * CHMAJOR, cy, cdy, 
				tx, tdx * THMAJOR, ty, tdy);
		// outer half
		drawHorizontalCurves(cx + cdx * CHMAJOR, cdx * (1 - CHMAJOR), cy, cdy, 
				tx + tdx * THMAJOR, tdx * (1 - THMAJOR), ty, tdy);
	}
	
	protected void drawQuad(float cx, float cy, float cx2, float cy2,
			float tx, float ty, float tx2, float ty2) {
		glMultiTexCoord2f(GL_TEXTURE1, cx, cy);
		glTexCoord2f(tx, ty);		
		glVertex2f(cx, cy);
		glMultiTexCoord2f(GL_TEXTURE1, cx, cy2);
		glTexCoord2f(tx, ty2);		
		glVertex2f(cx, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, cx2, cy2);
		glTexCoord2f(tx2, ty2);		
		glVertex2f(cx2, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, cx2, cy);
		glTexCoord2f(tx2, ty);		
		glVertex2f(cx2, cy);
	}
}
