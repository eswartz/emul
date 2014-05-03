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
	private int charColumn;
	private int charRow;
	/** positions in 1/72 in */
	private int posX, posY;
	
	private int leftPixel, rightPixel;
	private int pixelWidth;
	private int pixelHeight;
	private int topPixel;
	private int bottomPixel;
//	private int charPixelHeight;
//	private int charPixelWidth;
	private int charMatrixHeight;
	private int charMatrixWidth;
	private double paperHeightInches;
	private double paperWidthInches;
	/** character size in 1/72 in */
	private int paperWidthDots;
	private int paperHeightDots;
	/** character size in 1/72 in */
	private double charWidthDots = 5; //8. * 72 / 80;
	private int charAdvanceDots = 7;
	private int charsPerLine = 80;
	/** character size in 1/72 in */
	private int lineHeightDots = 72 / 6;
	private Graphics2D g2d;
	/**
	 * 
	 */
	public EpsonPrinterImageEngine(int horizDpi, int vertDpi) {
		setPaperSize(8.5, 11.0);
		setDpi(100, 100);
		
		
		try {
			loadFont(EmulatorMachinesData.getResourceDataURL("printers/rx80_font.txt"), 9, 9);
		} catch (IOException e) {
			e.printStackTrace();
		}
		initPage();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageEngine#setDpi(int, int)
	 */
	@Override
	public void setDpi(int horizDpi, int vertDpi) {
		this.horizDpi = horizDpi;
		this.vertDpi = vertDpi;
		initPage();
	}
	/**
	 * @param dataURL
	 * @throws IOException 
	 */
	private void loadFont(URL dataURL, int width, int height) throws IOException {
		charMatrixWidth = width;
		charMatrixHeight = height;
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
		if (g2d != null)
			g2d.dispose();
		g2d = image.createGraphics();
		listeners.fire(new ListenerList.IFire<IPrinterImageListener>() {

			@Override
			public void fire(IPrinterImageListener listener) {
				listener.updated();
			}
		});
	}
	
	public void setPaperSize(double widthInches, double heightInches){
		this.paperWidthInches = widthInches;
		this.paperHeightInches = heightInches;
		paperHeightDots = (int) (paperHeightInches * 72);
		paperWidthDots = (int) (paperWidthInches * 72);
	}
	/**
	 * 
	 */
	protected void initPage() {
		pixelWidth = (int) (paperWidthInches * horizDpi);
		pixelHeight = (int)(paperHeightInches * vertDpi);
		
		image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_BYTE_GRAY);
		
		g2d = image.createGraphics();
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
		g2d.dispose();
		
		leftPixel = (int) (0.25 * horizDpi);
		rightPixel = pixelWidth - leftPixel;
		topPixel  = (int) (0.5 * vertDpi);
		bottomPixel  = pixelHeight - topPixel;
		
//		charPixelWidth = (int) ((rightPixel - leftPixel) / 80);
//		charPixelHeight = (int) ((bottomPixel - topPixel) / 66);
		
		posY = topPixel * paperHeightDots / pixelHeight;
		
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
			int space = (int) ((tabSize - charColumn % tabSize) * charWidthDots);
			if (space + posX >= rightPixel) {
				carriageReturn();
				newLine();
			} else {
				posX += space; 
			}
			break;
		}
		case 27: {
			atEsc = true;
			break;
		}
			
		default:
			drawChar(ch);
			break;
		}
	}
	/**
	 * @param ch
	 */
	private void drawChar(char ch) {
		CharacterMatrix matrix = font.get(ch);
		if (matrix != null) {
			for (int cx = 0; cx < charMatrixWidth; cx++) {
				double x = mapX(posX + cx * charWidthDots / charMatrixWidth);
				for (int cy = 0; cy < charMatrixHeight; cy++) {
					int y = mapY(posY + cy);
					boolean s = matrix.isSet(cy, cx);
					if (s) {
						dot(x, y);
					}
				}
			}
			pageDirty = true;
		}
		advanceChar();
		firePageUpdated();
	}
	/**
	 * @param x
	 * @param y
	 */
	private void dot(double x, double y) {
		int pixel = 0;
		if ((horizDpi | vertDpi) < 200) {
			if (x - (int) x < 0.5 && y - (int) y < 0.5)
				pixel = 0;
			else
				pixel = 0x808080;
			image.setRGB((int) x, (int) y, pixel);
		}
		else {
			g2d.setColor(Color.black);
			g2d.fillOval((int) x, (int) y, horizDpi/75, vertDpi/75);
		}
		
	}

	private double mapX(double pos72) {
		return (pos72 * pixelWidth / paperWidthDots);
	}
	private int mapY(double pos72) {
		return (int) (pos72 * pixelHeight / paperHeightDots);
	}
	/**
	 * 
	 */
	private void advanceChar() {
		charColumn++;
		if (charColumn >= charsPerLine) {
			carriageReturn();
			newLine();
			return;
		}
		posX += charAdvanceDots;
		if (posX + charWidthDots >= paperWidthDots) {
			carriageReturn();
			newLine();
		}
				
	}
	protected void newLine() {
		posY += lineHeightDots;
		if (posY + lineHeightDots >= paperHeightDots) {
			newPage();
		}
	}
	private void handleEsc(char ch) {
		switch (ch) {
		
		}
	}
	private void carriageReturn() {
		charColumn = 0;
		posX = leftPixel * paperWidthDots / pixelWidth;
		firePageUpdated();
	}

}
