package v9t9.tools.asm;

/**
 * @author Ed
 *
 */
public interface ITokenListener {
	void tokenRead(int pos, boolean followsSpace, String image);
}
