package v9t9.engine.cpu;

public interface Status {

	String toString();

	void copyTo(Status copy);

	short flatten();

	void expand(short stat);

	boolean isLT();

	boolean isLE();

	boolean isL();

	boolean isEQ();

	boolean isNE();

	boolean isHE();

	boolean isGT();

	boolean isH();

	boolean isC();

	int getIntMask();

}