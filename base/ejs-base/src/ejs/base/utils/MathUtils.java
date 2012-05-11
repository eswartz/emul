/**
 * 
 */
package ejs.base.utils;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * @author ejs
 *
 */
public class MathUtils {

	/**
	 * Get a modulo of q % d which is always positive.
	 * @param q
	 * @param d
	 * @return
	 */
	public static double positiveModulo(double q, double d) {
		double m = q % d;
		while (m < 0) {
			m += d;
		}
		return m;
	}
	
	/**
	 * @param rect rectangle to rotate
	 * @param center of rotation, relative to rect
	 * @param angle in radians, 0 is right on X axis
	 * @return
	 */
	public static Rectangle2D rotateRectangleOnEllipse(
			Rectangle2D rect,
			Point2D center,
			double angle,
			double a, double b) {
		
		double ellipseScale = b > 0 ? a / b : 0;
		AffineTransform trans = AffineTransform.getRotateInstance(-angle, 
				rect.getX() + center.getX(), (rect.getY() + center.getY()) * ellipseScale);
		trans.scale(1.0, ellipseScale > 0 ? 1.0 / ellipseScale : 0);
		
		Point2D upperLeft = trans.transform(new Point2D.Double(rect.getMinX(), rect.getMinY() * ellipseScale), null);
		Point2D upperRight = trans.transform(new Point2D.Double(rect.getMaxX(), rect.getMinY() * ellipseScale), null);
		Point2D lowerLeft = trans.transform(new Point2D.Double(rect.getMinX(), rect.getMaxY() * ellipseScale), null);
		Point2D lowerRight = trans.transform(new Point2D.Double(rect.getMaxX(), rect.getMaxY() * ellipseScale), null);

		double minX = Math.min(upperLeft.getX(), Math.min(upperRight.getX(),
				Math.min(lowerLeft.getX(), lowerRight.getX())));
		double minY = Math.min(upperLeft.getY(), Math.min(upperRight.getY(),
				Math.min(lowerLeft.getY(), lowerRight.getY())));
		double maxX = Math.max(upperLeft.getX(), Math.max(upperRight.getX(),
				Math.max(lowerLeft.getX(), lowerRight.getX())));
		double maxY = Math.max(upperLeft.getY(), Math.max(upperRight.getY(),
				Math.max(lowerLeft.getY(), lowerRight.getY())));
		
		return new Rectangle2D.Double(minX /*+ center.getX()*/, 
				minY /*+ center.getY()*/, 
				maxX - minX, maxY - minY);
	}


	/**
	 * @param rect rectangle to rotate
	 * @param center of rotation, relative to rect
	 * @param angle in radians, 0 is right on X axis
	 * @return
	 */
	public static Rectangle2D rotateRectangle(
			Rectangle2D rect,
			Point2D center,
			double angle) {
		
		return rotateRectangleOnEllipse(rect, center, angle, 1.0, 1.0);
	}

	/**
	 * Rotate a point relative to screen coordinates, where (0,0) is upper-left.
	 * @param xoffs point to rotate, relative to 0,0 
	 * @param yoffs point to rotate, relative to 0,0
	 * @param cosA cos(angle) to rotate, where 0 is right along X axis
	 * @param sinA cos(angle) to rotate, where 0 is right along X axis
	 * @return
	 */
	public static Point2D.Double rotatePoint(
			double xoffs, double yoffs,
			double cosA, double sinA) {
		double xp = (cosA * (xoffs)) + (-sinA * (yoffs));
		double yp = (sinA * (xoffs)) + (cosA * (yoffs));
		return new Point2D.Double(xp, yp);
	}

	/**
	 * Rotate a point relative to screen coordinates, where (0,0) is upper-left.
	 * 
	 * @param x0 center of rotation
	 * @param y0 center of rotation
	 * @param x point to rotate
	 * @param y point to rotate
	 * @param angle angle in radians to rotate, where 0 is right along X axis
	 * @return
	 */
	public static Point2D.Double rotatePoint(double x0, double y0, 
			double x, double y,
			double angle) {
		Point2D.Double pt = rotatePoint(x, y, Math.cos(angle), Math.sin(angle));
		pt.x += x0;
		pt.y += y0;
		return pt;
	}
	
	/**
	 * Estimate the circumference of an ellipse.
	 * 
	 * @from http://en.wikipedia.org/wiki/Ellipse#Circumference
	 * @param a
	 * @param b
	 * @return
	 */
	public static double ellipseCircumference(double a, double b) {
		if (a + b == 0)
			return 0;
		double aDb = a - b;
		double aPb = a + b;
		double diffSumQuotSq3 = 3 * (aDb / aPb) * (aDb / aPb);
		double dividend1 = diffSumQuotSq3;
		double divisor1 = 10 + Math.sqrt(4 - diffSumQuotSq3);
		return Math.PI * (a + b) * (1 + dividend1 / divisor1);
	}
}
