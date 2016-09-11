/*
  ConvertImage.java

  (c) 2016 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import v9t9.video.ImageDataCanvas24Bit;
import v9t9.video.common.ImageUtils;
import v9t9.video.imageimport.ImageFrame;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportData;
import v9t9.video.imageimport.ImageImportDialogOptions;
import v9t9.video.imageimport.ImageImportOptions.Dither;
import v9t9.video.imageimport.ImageImportOptions.Palette;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Tuple;
import gnu.getopt.Getopt;

/**
 * Access to converting images into renderings for VDP chips
 * 
 * @author ejs
 * 
 */
@Category(Category.OTHER)
public class ConvertImages {
	private static final String PROGNAME = ConvertImages.class.getSimpleName();
	private File out;
	private File in;
	private ImageImportDialogOptions opts;
	private int width;
	private int height;
	private VdpColorManager colorMgr;
	private float stretch;

	private static void help() {
		System.out
				.println("\n"
						+ "V9t9 Image Converter\n"
						+ "\n"
						+ "Convert an image file to a rendering under a VDP mode\n"
						+ "\n"
						+ PROGNAME
						+ " [options] file...\n"
						+ "\n"
						+ "Options:\n"
						+ "-m WxHxC | MODE: video mode; if MODE ends in 'g', assume greyscale\n"
						+ "  single numbers map to 9900 FORTH mode numbers:"
						+ "    2=256x192x16 (8x1), 3=256x192x16 (8x1+palette),\n"
						+ "    4=256x192x16, 5=512x192x4,\n"
						+ "    6=512x192x16, 7=256x192x256\n"
						+ "    8=64x48x16, 9=64x48x16, 10=256x192x2\n"
						+ "-a 0|1: set preserve aspect ratio off or on\n"
						+ "-A aspect: set expected aspect ratio\n"
						+ "-d none|ordered|fs: select dithering method\n"
						+ "-p std|opt[+]: map to standard palette, or optimize (+ = set color 0)n"
						+ "-s 0|1: smooth scaling off or on\n"
						+ "-g: convert to a greyscale image\n"
						+ "-M: dither monochrome\n"
						+ "-b val: modify brightness by the given value (-100 to 100)\n"
						+ "-S 0|1: set smooth scaling off or on (use off for line art)\n"
						+ "-o DIR: write output to the given directory\n" + "");
	}

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();

		if (args.length == 0) {
			help();
			System.exit(0);
		}

		IMachine machine = ToolUtils.createMachine();

		Getopt getopt;
		getopt = new Getopt(PROGNAME, args, "?o:a:A:d:p:s:m:gMb:S:");

		IVdpCanvas canvas = new ImageDataCanvas24Bit();

		ImageImportDialogOptions opts = new ImageImportDialogOptions(canvas,
				machine.getVdp());

		File outdir = null;
		int width = 256, height = 192;
		float stretch = 1f;

		int opt;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
			case '?':
				help();
				break;
			case 'o':
				outdir = new File(getopt.getOptarg());
				break;
			case 'a':
				opts.setKeepAspect(readFlag(getopt.getOptarg()));
				break;
			case 'A': {
				String oa = getopt.getOptarg();
				try {
					opts.setAspect(Float.parseFloat(oa));
				} catch (NumberFormatException e) {
					System.err.println("Unexpected -A argument: " + oa);
					System.exit(1);
				}
				break;
			}
			case 'd': {
				String oa = getopt.getOptarg();
				if (oa.length() > 0 && oa.charAt(0) == 'n') {
					opts.setDitherType(Dither.NONE);
				} else if (oa.length() > 0 && oa.charAt(0) == 'o') {
					opts.setDitherType(Dither.ORDERED);
				} else if (oa.length() > 0 && oa.charAt(0) == 'f') {
					opts.setDitherType(Dither.FS);
				} else {
					System.err.println("Unexpected -d argument: " + oa);
					System.exit(1);
				}
				break;
			}
			case 'M':
				opts.setDitherMono(true);
				break;
			case 'p': {
				String oa = getopt.getOptarg();
				if (oa.length() > 0 && oa.charAt(0) == 's') {
					opts.setPalette(Palette.STANDARD);
				} else if (oa.length() > 0 && oa.charAt(0) == 'o') {
					opts.setPalette(Palette.OPTIMIZED);
					if (oa.charAt(oa.length() - 1) == '+') {
						canvas.getColorMgr().setClearFromPalette(true);
					}
				} else {
					System.err.println("Unexpected -p argument: " + oa);
					System.exit(1);
				}
				break;
			}
			case 's':
				opts.setScaleSmooth(readFlag(getopt.getOptarg()));
				break;
			case 'm': {
				Tuple t = readFormat(getopt.getOptarg());
				opts.setFormat((VdpFormat) t.get(0));
				width = (Integer) t.get(1);
				height = (Integer) t.get(2);
				stretch = ((Number) t.get(3)).floatValue();
				canvas.getColorMgr().setGreyscale((Boolean) t.get(4));
				break;
			}
			case 'g':
				opts.setAsGreyScale(true);
				break;
			case 'b': {
				String oa = getopt.getOptarg();
				try {
					opts.setGamma(Float.parseFloat(getopt.getOptarg()));
				} catch (NumberFormatException e) {
					System.err.println("Unexpected gamma argument: " + oa);
					System.exit(1);			
				}
			}
			case 'S':
				opts.setScaleSmooth(readFlag(getopt.getOptarg()));
				break;
			}
		}

		try {
			// arguments are image files
			int idx = getopt.getOptind();
			if (idx >= args.length) {
				System.err.println("One or more image files expected");
				System.exit(1);
			}
			while (idx < args.length) {
				String name = args[idx++];

				File in = new File(name);

				String outname = getOutputName(in.getName());
				File out = outdir != null ? new File(outdir, outname)
						: new File(in.getParentFile(), outname);

				ConvertImages cvt = new ConvertImages(machine, canvas, opts,
						in, width, height, stretch, out);
				cvt.importImage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotifyException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static boolean readFlag(String flag) {
		if (flag.length() > 0 && flag.charAt(0) == '0')
			return false;
		if (flag.length() > 0 && flag.charAt(0) == '1')
			return true;

		System.err.println("Unexpected flag argument: " + flag);
		System.exit(1);
		return false;
	}

	private static Tuple/* VdpFormat, Integer, Integer, Float, Boolean */readFormat(String mode) {
		boolean isGrey  = false;
		if (mode.endsWith("g")) {
			isGrey = true;
			mode = mode.substring(0, mode.length() - 1);
		}
		
		if (mode.indexOf('x') != 0) {
			// actual resolution + # colors
			int idx = mode.indexOf('x');
			try {
				int w = Integer.parseInt(mode.substring(0, idx));
				int idx2 = mode.indexOf('x', idx+1);
				int h = Integer.parseInt(mode.substring(idx+1, idx2));
				int c = Integer.parseInt(mode.substring(idx2+1));
				
				VdpFormat f = null;
				switch (c) {
				case 2:
					f = VdpFormat.COLOR2_8x1;
					break;
				case 4:
					f = VdpFormat.COLOR4_1x1;
					break;
				case 8:
					f = VdpFormat.COLOR8_1x1;
					break;
				case 16:
					f = VdpFormat.COLOR16_1x1;
					break;
				case 32:
					f = VdpFormat.COLOR32_1x1;
					break;
				case 64:
					f = VdpFormat.COLOR64_1x1;
					break;
				case 128:
					f = VdpFormat.COLOR128_1x1;
					break;
				case 256:
					f = VdpFormat.COLOR256_1x1;
					break;
				default:
					System.err.println("Unexpected -m argument number of colors: " + c);
					System.exit(1);
					return null;
				}
				
				return new Tuple(f, w, h, 1f, isGrey);
				
			} catch (NumberFormatException e) {
				// fall through
			} catch (StringIndexOutOfBoundsException e) {
				// fall through
			}
		}
		try {
			int m = Integer.parseInt(mode);
			switch (m) {
			case 1:
				return new Tuple(VdpFormat.COLOR16_8x8, 256, 192, 1, isGrey);
			case 2:
				return new Tuple(VdpFormat.COLOR16_8x1, 256, 192, 1, isGrey);
			case 3:
				return new Tuple(VdpFormat.COLOR16_8x1_9938, 256, 192, 1, isGrey);
			case 4:
				return new Tuple(VdpFormat.COLOR16_1x1, 256, 192, 1, isGrey);
			case 5:
				return new Tuple(VdpFormat.COLOR4_1x1, 512, 192, 2f, isGrey);
			case 6:
				return new Tuple(VdpFormat.COLOR16_1x1, 512, 192, 2f, isGrey);
			case 7:
				return new Tuple(VdpFormat.COLOR256_1x1, 256, 192, 1, isGrey);
			case 9:
				return new Tuple(VdpFormat.COLOR2_8x1, 256, 192, 1, isGrey);
			case 10:
				return new Tuple(VdpFormat.COLOR16_4x4, 64, 48, 1, isGrey);
			}
		} catch (NumberFormatException e) {
		}
		System.err.println("Unexpected -m argument: " + mode);
		System.exit(1);
		return null;
	}

	private static String getOutputName(String name) {
		String out;
		int idx = name.lastIndexOf('.');
		if (idx > 0)
			out = name.substring(0, idx) + "_vdp";
		else
			out = name + "_vdp";

		return out + ".png";
	}

	public ConvertImages(IMachine machine, IVdpCanvas canvas,
			ImageImportDialogOptions opts, File in, int width, int height,
			float stretch, File out) throws IOException {
		this.stretch = stretch;
		this.colorMgr = canvas.getColorMgr();
		this.opts = opts;
		this.in = in;
		this.width = width;
		this.height = height;
		this.out = out;
	}

	private void importImage() throws NotifyException, IOException {
		ImageFrame[] frames = ImageUtils.loadImageFromFile(in.getPath());

		opts.setImages(frames);

		if (opts.getFormat().isMsx2()) {
			colorMgr.setPalette(VdpColorManager.stockPaletteV9938);
		}
		
		ImageImport importer = new ImageImport(colorMgr);
		
		importer.setTryDirectMapping(false);

		ImageImportData[] datas = importer.importImage(opts, width, height);

		BufferedImage cvt = datas[0].getConvertedImage();
		
		if (stretch != 1f) {
			int newHeight = (int) Math.round(height * stretch);
			BufferedImage tmp = new BufferedImage(width, newHeight, cvt.getType());
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(cvt, 0, 0, width, newHeight, null);
			g2.dispose();
			cvt = tmp;
		}

		if (colorMgr.isGreyscale()) {
			ColorConvertOp op = new ColorConvertOp(cvt.getColorModel().getColorSpace(),
					ColorSpace.getInstance(ColorSpace.CS_GRAY),
					null);
			op.filter(cvt, cvt);
		}
		
		ImageIO.write(cvt, "png", out);

		String[] args = { "/usr/bin/display", 
				"-sample", 
				"200%x200%",
				out.getPath() 
			}; 
		
		if (width >= 512) {
			args[2] = "100%x100%";
		} else if (opts.getFormat() == VdpFormat.COLOR16_4x4) {
			args[2] = "800%x800%";
		}
		
		Runtime.getRuntime().exec(args);
	}
}
