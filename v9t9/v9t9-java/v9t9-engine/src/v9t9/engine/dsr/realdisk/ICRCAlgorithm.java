/**
 * 
 */
package v9t9.engine.dsr.realdisk;

public interface ICRCAlgorithm {
	void reset();
	void setPoly(int poly);
	int getPoly();
	short feed(byte b);
	short read();
}