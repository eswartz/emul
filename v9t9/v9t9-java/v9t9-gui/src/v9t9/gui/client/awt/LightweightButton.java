/**
 * 
 */
package v9t9.gui.client.awt;
import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

public class LightweightButton extends JComponent {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5322193433431683136L;
    private boolean pressed = false;
    ActionListener actionListener;     // Post action events to listeners
    private Dimension size;
    
    public LightweightButton(String tooltip, Dimension size) {
        super();
		setToolTipText(tooltip);
		this.size = size;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);       
    }
    /**
     * Adds the specified action listener to receive action events
     * from this button.
     * @param listener the action listener
     */
    public void addActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.add(actionListener, listener);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }
    
    /**
     * Removes the specified action listener so it no longer receives
     * action events from this button.
     * @param listener the action listener
     */
    public void removeActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.remove(actionListener, listener);
    }
    
    
    /**
     * Paints the button and sends an action event to all listeners.
     */
    public void processMouseEvent(MouseEvent e) {
        switch(e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                pressed = true;

                repaint();
                break;
            case MouseEvent.MOUSE_RELEASED:
                if(actionListener != null) {
                    actionListener.actionPerformed(new ActionEvent(
                    this, ActionEvent.ACTION_PERFORMED, ""));
                }
                if(pressed == true) {
                    pressed = false;

                    repaint();
                }
                break;
            case MouseEvent.MOUSE_ENTERED:
                break;
            case MouseEvent.MOUSE_EXITED:
                if(pressed == true) {
                    // Cancel! Don't send action event.
                    pressed = false;

                    repaint();
 
                }
                break;
        }
        super.processMouseEvent(e);
    }
    
    @Override
    public Dimension getMinimumSize() {
    	return size;
    }
    
    public Dimension getPreferredSize() {
        return size;
    }
    
    public void update(Graphics g) {
        paint(g);
    }
}
