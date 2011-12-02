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
public class StandardMonitorRender extends BaseMonitorEffect {
	public static final StandardMonitorRender INSTANCE = new StandardMonitorRender();
	
	public StandardMonitorRender() {
	}
	
	@Override
	public void init() {
		
	}
	
	@Override
	public void render() {
		glBegin(GL_QUADS);
		
		drawQuad(0, 0, 1, 1, 0, 0, 1, 1);
		
		glEnd();
	}


	protected void drawQuad(float cx, float cy, float cx2, float cy2,
			float tx, float ty, float tx2, float ty2) {
		glMultiTexCoord2f(GL_TEXTURE1, tx, ty);
		glTexCoord2f(tx, ty);		
		glVertex2f(cx, cy);
		glMultiTexCoord2f(GL_TEXTURE1, tx, ty2);
		glTexCoord2f(tx, ty2);		
		glVertex2f(cx, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, tx2, ty2);
		glTexCoord2f(tx2, ty2);		
		glVertex2f(cx2, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, tx2, ty);
		glTexCoord2f(tx2, ty);		
		glVertex2f(cx2, cy);
	}
}
