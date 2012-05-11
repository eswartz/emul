/**
 * 
 */
package ejs.base.internal.test;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Test;

import ejs.base.utils.MathUtils;

/**
 * @author ejs
 *
 */
public class TestMathUtils {

	@Test
	public void testRotatePointsCentered() {
		Point2D.Double ans;
		
		ans = MathUtils.rotatePoint(0, 0, 10, 40, 0);
		assertEquals(10., ans.x, 0.01);
		assertEquals(40., ans.y, 0.01);
		
		// rotate "_" counter-clockwise quarter turn -- should point up (screen-wise)
		ans = MathUtils.rotatePoint(0, 0, 40, 0, Math.PI / 2);
		assertEquals(0., ans.x, 0.01);
		assertEquals(40., ans.y, 0.01);
		
		ans = MathUtils.rotatePoint(0, 0, 40, 0, Math.PI);
		assertEquals(-40., ans.x, 0.01);
		assertEquals(0., ans.y, 0.01);
		
		ans = MathUtils.rotatePoint(0, 0, 40, 0, Math.PI * 3 / 2);
		assertEquals(0., ans.x, 0.01);
		assertEquals(-40., ans.y, 0.01);
		
		// turn "/" clockwise one-eighth turn; should be on X axis
		ans = MathUtils.rotatePoint(0, 0, Math.sqrt(2), Math.sqrt(2), -Math.PI / 4);
		assertEquals(2., ans.x, 0.01);
		assertEquals(0., ans.y, 0.01);
	}
	

	@Test
	public void testRotatePointsOffCenter() {
		Point2D.Double ans;
		
		ans = MathUtils.rotatePoint(100, 100, 10, 40, 0);
		assertEquals(110., ans.x, 0.01);
		assertEquals(140., ans.y, 0.01);
		
		// rotate "_" counter-clockwise quarter turn -- should point up (screen-wise)
		
		// given this center, we can assume the bounds is e.g [-50 to 35] x [-5 to 5],
		// so after rotating, 
		ans = MathUtils.rotatePoint(-5, -5, 40, 0, Math.PI / 2);
		assertEquals(-5., ans.x, 0.01);
		assertEquals(35., ans.y, 0.01);
		
		// turn "/" clockwise one-eighth turn; should be on X axis
		ans = MathUtils.rotatePoint(100, 100, Math.sqrt(2), Math.sqrt(2), -Math.PI / 4);
		assertEquals(102., ans.x, 0.01);
		assertEquals(100., ans.y, 0.01);
	}
	

	@Test
	public void testRotateRects() {
		Rectangle2D ans;
		
		// no rot
		ans = MathUtils.rotateRectangle(
				new Rectangle2D.Double(40, 75, 100, 10),
				new Point2D.Double(0, 5),
				0);

		assertEquals(40., ans.getX(), 0.01);
		assertEquals(75., ans.getY(), 0.01);
		assertEquals(100., ans.getWidth(), 0.01);
		assertEquals(10., ans.getHeight(), 0.01);
		
		// rotate 90 degrees left
		ans = MathUtils.rotateRectangle(
				new Rectangle2D.Double(45, 70, 100, 10),
				new Point2D.Double(0, 5),
				Math.PI / 2);

		assertEquals(40., ans.getX(), 0.01);
		assertEquals(75. - 100., ans.getY(), 0.01);
		assertEquals(10., ans.getWidth(), 0.01);
		assertEquals(100., ans.getHeight(), 0.01);
		

		// rotate 180 degrees 
		ans = MathUtils.rotateRectangle(
				new Rectangle2D.Double(45, 70, 100, 10),
				new Point2D.Double(0, 5),
				Math.PI);

		assertEquals(45 - 100., ans.getX(), 0.01);
		assertEquals(70., ans.getY(), 0.01);
		assertEquals(100., ans.getWidth(), 0.01);
		assertEquals(10., ans.getHeight(), 0.01);
	}
	
	@Test
	public void testEllipseCircumference() {
		// a circle
		assertEquals(Math.PI * 10 * 2, MathUtils.ellipseCircumference(10, 10), 0.01);
		
		// degenerate ellipse, a line of length 20, going forward and backward
		assertEquals(40, MathUtils.ellipseCircumference(10, 0), 0.1);
	}
}
