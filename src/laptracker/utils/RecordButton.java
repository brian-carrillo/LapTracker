package laptracker.utils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JToggleButton;

public class RecordButton extends JToggleButton {
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

		public RecordButton(String string) {
			super(string);

		}

		public void paintComponent(Graphics g) {
			if (this.isSelected()) {
				setForeground(new Color(255,64,64,0));
			} else {
				setForeground(Color.black);
			}
    	    super.paintComponent(g);
    	    if (this.isSelected()) {
    		    int w = getWidth();
    		    int h = getHeight();
    		    Insets i = new Insets(1,1,1,1);
    		    g.setColor(new Color(255,64,64,128)); // selected color
    		    g.fillRect(i.left, i.top, w-i.left-i.right, h-i.bottom-i.top);
    		    g.setColor(Color.black);
    		    g.drawString(getText(), (w - g.getFontMetrics().stringWidth(getText()))/2, (h + g.getFontMetrics().getAscent())/2 - 2);
    		}
    	  }
    	}