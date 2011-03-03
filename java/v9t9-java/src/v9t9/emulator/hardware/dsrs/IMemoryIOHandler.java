/**
 * Mar 2, 2011
 */
package v9t9.emulator.hardware.dsrs;

/**
 * @author ejs
 *
 */
public interface IMemoryIOHandler {

	void writeData(int offset, byte val);
	byte readData(int offset);
}
