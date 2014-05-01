/**
 * 
 */
package v9t9.machine.printer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageListener;
import v9t9.machine.EmulatorMachinesData;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class EpsonPrinterImageEngine implements IPrinterImageEngine {
	
	private ListenerList<IPrinterImageListener> listeners = new ListenerList<IPrinterImageListener>();
	private boolean firstPage = true;
	private boolean pageDirty = false;
	private int tabSize = 8;
	private boolean atEsc;
	private BufferedImage image;
	private int horizDpi;
	private int vertDpi;

	private Map<Character, CharacterMatrix> font = new HashMap<Character, CharacterMatrix>();
	private int column;
	private int row;
	private int px, py;
	
	private int leftPixel, rightPixel;
	private int pixelWidth;
	private int pixelHeight;
	private int topPixel;
	private int bottomPixel;
	private int rowPixelHeight;
	private int charPixelWidth;
	/**
	 * 
	 */
	public EpsonPrinterImageEngine(int horizDpi, int vertDpi) {
		this.horizDpi = horizDpi;
		this.vertDpi = vertDpi;
		try {
			loadFont(EmulatorMachinesData.getResourceDataURL("printers/rx80_font.txt"), 9, 9);
		} catch (IOException e) {
			e.printStackTrace();
		}
		initPage();
	}
	/**
	 * @param dataURL
	 * @throws IOException 
	 */
	private void loadFont(URL dataURL, int width, int height) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataURL.openStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 3 && line.charAt(0) == '\'' && line.charAt(2) == '\'') {
				char ch = line.charAt(1);
				CharacterMatrix matrix = new CharacterMatrix(ch, width, height);
				for (int row = 0; row < height; row++) {
					line = reader.readLine();
					if (line == null)
						throw new IOException("missing row " + row + " for char " + ch);
					if (line.length() > width)
						throw new IOException("unexpected length in " + row + " for char " + ch);
					for (int col = 0; col < width; col++) {
						char c = col < line.length() ? line.charAt(col) : ' ';
						matrix.set(row, col, (c != '.' && c != ' '));
					}
				}
				font.put(ch, matrix);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#addListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	@Override
	public void addListener(IPrinterImageListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#removeListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	@Override
	public void removeListener(IPrinterImageListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageEngine#flushPage()
	 */
	@Override
	public void flushPage() {
		if (firstPage || pageDirty) {
			newPage();
		}
	}
	/**
	 * 
	 */
	public void newPage() {
		if (pageDirty) {
			termPage();
			firePageUpdated();
		}
		firstPage = false;
		initPage();
		fireNewPage();
	}
	/**
	 * 
	 */
	protected void fireNewPage() {
		pageDirty = false;
		listeners.fire(new ListenerList.IFire<IPrinterImageListener>() {
			
			@Override
			public void fire(IPrinterImageListener listener) {
				listener.newPage(image);
			}
		});
	}
	/**
	 * 
	 */
	protected void firePageUpdated() {
		listeners.fire(new ListenerList.IFire<IPrinterImageListener>() {

			@Override
			public void fire(IPrinterImageListener listener) {
				listener.updated();
			}
		});
	}
	
	/**
	 * 
	 */
	protected void initPage() {
		pixelWidth = (int) (8.5 * horizDpi);
		pixelHeight = (int)(11 * vertDpi);
		
		image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = image.createGraphics();
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
		
		leftPixel = (int) (0.5 * horizDpi);
		rightPixel = pixelWidth - leftPixel;
		topPixel  = (int) (0.5 * vertDpi);
		bottomPixel  = pixelHeight - topPixel;
		
		rowPixelHeight = 9;
		charPixelWidth = 9;
		
		py = topPixel;
		
		carriageReturn();
		pageDirty = false;
	}
	protected void termPage() {
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageEngine#print(java.lang.String)
	 */
	@Override
	public void print(String text) {
		for (char ch : text.toCharArray())
			print(ch);
	}
	
	public void print(char ch) {
		
		if (atEsc) {
			handleEsc(ch);
			return;
		}

		if (ch == 0) {
			return;
		}

		switch (ch) {
		case '\r':
			carriageReturn();
			break;
		case '\n':
			newLine();
			break;
		case '\t':  {
			int space = (tabSize - column % tabSize) * charPixelWidth;
			if (space + px >= rightPixel) {
				carriageReturn();
				newLine();
			} else {
				px += space; 
			}
			break;
		}
		case 27: {
			atEsc = true;
			break;
		}
			
		default:
			drawChar(ch);
			column++;
			break;
		}
	}
	/**
	 * @param ch
	 */
	private void drawChar(char ch) {
		CharacterMatrix matrix = font.get(ch);
		if (matrix != null) {
			for (int x = 0; x < charPixelWidth; x++) {
				for (int y = 0; y < rowPixelHeight; y++) {
					boolean s = matrix.isSet(y, x);
					if (s) {
						if (x % 2 == 0)
							image.setRGB(px + x / 2, py + y, 0x000000);
						else
							image.setRGB(px + x / 2, py + y, 0xff808080);
					}
				}
			}
			pageDirty = true;
		}
		advanceChar();
		firePageUpdated();
	}
	/**
	 * 
	 */
	private void advanceChar() {
		px += charPixelWidth;
		if (px >= rightPixel) {
			carriageReturn();
			newLine();
		}
				
	}
	/**
	 * 
	 */
	protected void newLine() {
		py += rowPixelHeight;
		if (py >= bottomPixel) {
			newPage();
		}
	}
	/**
	 * @param ch
	 */
	private void handleEsc(char ch) {
		switch (ch) {
		
		}
	}
	/**
	 * 
	 */
	private void carriageReturn() {
		column = 0;
		px = leftPixel;
		firePageUpdated();
	}

}
