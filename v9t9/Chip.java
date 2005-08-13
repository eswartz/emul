/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;

/** The base for any chip that has state, I/O, and an execution model.
 * @author ejs
 */
public class Chip {
    protected Machine machine;
    public Chip(Machine machine) {
        this.machine = machine;
    }
}
