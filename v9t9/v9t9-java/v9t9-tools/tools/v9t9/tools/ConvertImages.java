/*
  ConvertImage.java

  (c) 2016 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import static v9t9.common.video.VdpColorManager.fromRGB8;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.common.video.VdpFormat.Layout;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import v9t9.video.ImageDataCanvas24Bit;
import v9t9.video.common.ImageUtils;
import v9t9.video.imageimport.GifSequenceWriter;
import v9t9.video.imageimport.ImageFrame;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportData;
import v9t9.video.imageimport.ImageImportDialogOptions;
import v9t9.video.imageimport.ImageImportOptions.Dither;
import v9t9.video.imageimport.ImageImportOptions.PaletteOption;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Pair;
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

	static class CubePaletteMaker {
		byte[][] pal;
		
		CubePaletteMaker(int num, int rs, int gs, int bs, boolean pure) {
			pal = new byte[num][];
			int idx = 0;
			
			if (pure) {
				// span 0 to 255 in each channel
				for (int r = 0; r < rs; r++) {
					for (int g = 0; g < gs; g++) {
						for (int b = 0; b < bs; b++) {
							pal[idx] = new byte[] { 
									(byte) (r*255/(rs-1)), 
									(byte) (g*255/(gs-1)),
									(byte) (b*255/(bs-1)) 
							};
							idx++;
						}
					}
				}
			} else {
				// always have non-pure colors, by drawing a line
				// through each color axis that starts 1/2 a step
				// above 0 and ends 1/2 a step below 255,
				// where a step is the distance between successive 
				// increments
				for (int r = 0; r < rs; r++) {
					for (int g = 0; g < gs; g++) {
						for (int b = 0; b < bs; b++) {
							pal[idx] = new byte[] { 
									(byte) ((r*255/rs)+128/rs), 
									(byte) ((g*255/gs)+128/gs),
									(byte) ((b*255/bs)+128/bs) 
							};
							idx++;
						}
					}
				}
			}

			// when pure, black and white are generated
			int greys = num - idx + (pure ? 1 : -1);
			int g = pure ? 1 : 0;
			while (idx < num) {
				byte gr = (byte) (g*255/greys);
				pal[idx] = new byte[] { gr, gr, gr };
				g++;
				idx++;
			}
		}
		
		byte[][] getPalette() {
			return pal;
		}
	}

	static Map<String,byte[][]> stockPalettes = new HashMap<String, byte[][]>();
	
	/* colors from http://fornaxvoid.com/colorpalettes/ */

	static {
		
	/** VIC-I (6561) colors */
	stockPalettes.put("vic", new byte[][] { 
		/* 0: black */ fromRGB8("000000"), 
		/* 1: white */ fromRGB8("ffffff"),
		/* 2: red */ fromRGB8("782922"), 
		/* 3: cyan */ fromRGB8("87d6dd"),
		/* 4: purple */ fromRGB8("aa5fb6"), 
		/* 5: green */ fromRGB8("55a049"),
		/* 6: blue */ fromRGB8("40318d"), 
		/* 7: yellow */ fromRGB8("bfce72"),
		
		/*  8: orange */ fromRGB8("aa7449"), 
		/*  9: light orange */ fromRGB8("eab489"),
		/* 10: pink */ fromRGB8("b86962"), 
		/* 11: light cyan */ fromRGB8("c7ffff"),
		/* 12: light purple */ fromRGB8("ea9ff6"), 
		/* 13: light green */ fromRGB8("94e089"),
		/* 14: light blue */ fromRGB8("8071cc"), 
		/* 15: light yellow */ fromRGB8("ffffb2"),
	});
	
	stockPalettes.put("c64", new byte[][] { 
		/* 0: black */ fromRGB8("000000"), 
		/* 1: white */ fromRGB8("ffffff"),
		/* 2: red */ fromRGB8("883932"), 
		/* 3: cyan */ fromRGB8("67b6bd"),
		/* 4: purple */ fromRGB8("8b3f96"), 
		/* 5: green */ fromRGB8("55a049"),
		/* 6: blue */ fromRGB8("40318d"), 
		/* 7: yellow */ fromRGB8("bfce72"),
		
		/*  8: orange */ fromRGB8("8b5429"), 
		/*  9: brown */ fromRGB8("574200"),
		/* 10: pink */ fromRGB8("b86962"), 
		/* 11: dark grey */ fromRGB8("505050"),
		/* 12: grey */ fromRGB8("787878"), 
		/* 13: light green */ fromRGB8("94e089"),
		/* 14: light blue */ fromRGB8("7869c4"), 
		/* 15: light grey */ fromRGB8("9f9f9f"),
	});
	
	stockPalettes.put("zx", new byte[][] { 
		/* 0: black */ fromRGB8("000000"), 
		/* 1: blue */ fromRGB8("0000c0"),
		/* 2: red */ fromRGB8("c00000"), 
		/* 3: purple */ fromRGB8("c000c0"),
		/* 4: black */ fromRGB8("000000"), 
		/* 5: light blue */ fromRGB8("0000ff"),
		/* 6: light red */ fromRGB8("ff0000"), 
		/* 7: light purple */ fromRGB8("ff00ff"),
		
		/*  8: green */ fromRGB8("00c000"), 
		/*  9: cyan */ fromRGB8("00c0c0"),
		/* 10: yellow */ fromRGB8("c0c000"), 
		/* 11: grey */ fromRGB8("c0c0c0"),
		/* 12: light green */ fromRGB8("00ff00"), 
		/* 13: light cyan */ fromRGB8("00ffff"),
		/* 14: light yellow */ fromRGB8("ffff00"), 
		/* 15: white */ fromRGB8("ffffff"),
	});
	
	stockPalettes.put("apple2", new byte[][] { 
		/* 0: black */ fromRGB8("000000"), 
		/* 1: magenta */ fromRGB8("6c2940"),
		/* 2: dark blue */ fromRGB8("403578"), 
		/* 3: purple */ fromRGB8("d93cf0"),
		/* 4: dark green */ fromRGB8("135740"), 
		/* 5: grey 1 */ fromRGB8("808080"),
		/* 6: medium blue */ fromRGB8("2697f0"), 
		/* 7: light blue */ fromRGB8("bfb4f8"),
		
		/*  8: brown */ fromRGB8("404b07"), 
		/*  9: orange */ fromRGB8("d9680f"),
		/* 10: grey 2 */ fromRGB8("808080"),  // yes, same as grey 1
		/* 11: pink */ fromRGB8("eca8bf"),
		/* 12: light green */ fromRGB8("26c30f"), 
		/* 13: yellow */ fromRGB8("bfca87"),
		/* 14: aquamarine */ fromRGB8("93d6bf"), 
		/* 15: white */ fromRGB8("ffffff"),
	});

	// from Graphics Gems, Frame Buffer Techniques (Mapping RGB Triples onto Four Bits)
	stockPalettes.put("rgb14", new byte[][] { 
		/* 0: black */  fromRGB8("000000"), 
		/* 1: olive*/   fromRGB8("808000"),
		/* 2: purple */ fromRGB8("800080"), 
		/* 3: red */    fromRGB8("ff0000"),
		/* 4: aqua */   fromRGB8("008080"), 
		/* 5: green */  fromRGB8("00ff00"),
		/* 6: blue */   fromRGB8("0000ff"), 
		/* 7: grey 1 */ fromRGB8("404040"),
		/* 8: grey 2 */ fromRGB8("c0c0c0"), 
		/* 9: yellow */ fromRGB8("ffff00"),
		/* 10: magenta*/fromRGB8("ff00ff"),
		/* 11: pink */  fromRGB8("ff8080"),
		/* 12: cyan */  fromRGB8("00ffff"), 
		/* 13: lime */  fromRGB8("80ff80"),
		/* 14: sky*/    fromRGB8("8080ff"), 
		/* 15: white */ fromRGB8("ffffff"),
	});
	
	// more subdued version of the above
	stockPalettes.put("rgb14m", new byte[][] { 
		/* 0: black */  fromRGB8("000000"), 
		/* 1: olive*/   fromRGB8("808040"),
		/* 2: purple */ fromRGB8("804080"), 
		/* 3: red */    fromRGB8("bf4040"),
		/* 4: aqua */   fromRGB8("408080"), 
		/* 5: green */  fromRGB8("40bf40"),
		/* 6: blue */   fromRGB8("4040bf"), 
		/* 7: grey 1 */ fromRGB8("404040"),
		/* 8: grey 2 */ fromRGB8("c0c0c0"), 
		/* 9: yellow */ fromRGB8("bfbf40"),
		/* 10: magenta*/fromRGB8("bf40bf"),
		/* 11: pink */  fromRGB8("bf8080"),
		/* 12: cyan */  fromRGB8("40bfbf"), 
		/* 13: lime */  fromRGB8("80bf80"),
		/* 14: sky*/    fromRGB8("8080bf"), 
		/* 15: white */ fromRGB8("ffffff"),
	});

	stockPalettes.put("cube666", new CubePaletteMaker(256, 6, 6, 6, false).getPalette());
	stockPalettes.put("cube865", new CubePaletteMaker(256, 8, 6, 5, false).getPalette());
	stockPalettes.put("cube766", new CubePaletteMaker(256, 7, 6, 6, false).getPalette());
	stockPalettes.put("cube974", new CubePaletteMaker(256, 9, 7, 4, false).getPalette());
	
	stockPalettes.put("cube444", new CubePaletteMaker(64, 4, 4, 4, true).getPalette());
	stockPalettes.put("ega", new CubePaletteMaker(64, 4, 4, 4, true).getPalette());
	stockPalettes.put("cube444m", new CubePaletteMaker(64, 4, 4, 4, false).getPalette());
	stockPalettes.put("cube543", new CubePaletteMaker(64, 5, 4, 3, true).getPalette());
	stockPalettes.put("cube543m", new CubePaletteMaker(64, 5, 4, 3, false).getPalette());
	stockPalettes.put("cube633", new CubePaletteMaker(64, 6, 3, 3, true).getPalette());
	stockPalettes.put("cube633m", new CubePaletteMaker(64, 6, 3, 3, false).getPalette());
	
	stockPalettes.put("cube322", new CubePaletteMaker(16, 3, 2, 2, true).getPalette());
	stockPalettes.put("cube422", new CubePaletteMaker(16, 4, 2, 2, true).getPalette());
	stockPalettes.put("cube322m", new CubePaletteMaker(16, 3, 2, 2, false).getPalette());
	stockPalettes.put("cube422m", new CubePaletteMaker(16, 4, 2, 2, false).getPalette());
	
	} // static
	
	
	private ImageImportDialogOptions opts;
	private int width;
	private int height;
	private VdpColorManager colorMgr;
	private float stretch;
	private boolean flattenGreyscale;

	private boolean test;

	private boolean clip;

	private boolean showPalette;


	/** test 8-color mode */
	static VdpFormat COLOR8_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 8, true);
	/** test 8-color mode */
	static VdpFormat COLOR8_DIRECT_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 8, true, true);
	/** test 16-color mode */
	static VdpFormat COLOR16_DIRECT_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 16, true, true);
	/** test 32-color mode */
	static VdpFormat COLOR32_DIRECT_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 32, true, true);
	/** test 64-color mode */
	static VdpFormat COLOR64_DIRECT_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 64, true, true);
	/** test 128-color mode */
	static VdpFormat COLOR128_DIRECT_1x1 = new VdpFormat(VdpFormat.Layout.BITMAP, 128, true, true);
	
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
						+ "  single numbers map to 9900 FORTH mode numbers:\n"
						+ "    2=256x192x16 (8x1), 3=256x192x16 (8x1+palette),\n"
						+ "    4=256x192x16, 5=512x192x4,\n"
						+ "    6=512x192x16, 7=256x192x256\n"
						+ "    8=64x48x16, 9=256x192x2, 10=64x48x16\n"
						+ "-s 0|1: smooth scaling off or on\n"
						+ "-a 0|1: set preserve aspect ratio off or on\n"
						+ "-A aspect: set expected aspect ratio\n"
						+ "-d none|ordered|fs|fsr: select dithering method\n"
						+ "-p std|18|38|20|vic|c64|zx|a2|opt[+]: map to palette\n"
						+ "   std|18 = TMS19918A, 38 = V9938, vic = 6560 (VIC), c64 = 6567 (VIC-II), \n"
						+ "   zx = ZX Spectrum, a2 = Apple ][\n"
						+ "   opt: optimize palette (+ = set color 0)\n"
						+ "-O: use octree to quantize colors instead of median cut\n"
						+ "-g: convert to a greyscale image\n"
						+ "-M: dither monochrome\n"
						+ "-b val: modify brightness by the given value (-100 to 100)\n"
						+ "-S 0|1: set smooth scaling off or on (use off for line art)\n"
						+ "-o DIR: write output to the given directory\n" 
						+ "-c: regardless of the mode, clip the output to actual side of the image\n"
						+ "-t: test that reimporting the generated output is identical\n"
						+ "-P: write and display the palette\n"
						+ ""
					);
	}

	public static void main(String[] args) {
		
		ConvertImages cvt = new ConvertImages();
		cvt.run(args);
		
	}
	
	private void run(String[] args) {
		LoggingUtils.setupNullLogging();

		if (args.length == 0) {
			help();
			System.exit(0);
		}

		IMachine machine = ToolUtils.createMachine();

		Getopt getopt;
		getopt = new Getopt(PROGNAME, args, "?o:m:s:a:A:d:p:OgMb:S:F:ctP");

		IVdpCanvas canvas = new ImageDataCanvas24Bit();
		
		colorMgr = canvas.getColorMgr();

		opts = new ImageImportDialogOptions(canvas,
				machine.getVdp());
		opts.setPaletteUsage(PaletteOption.FIXED);

		File outdir = null;
		width = 256;
		height = 192;
		stretch = 1f;
		
		flattenGreyscale = true;
		
		test = false;
		clip = false;
		
		showPalette = false;

		int opt;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
			case '?':
				help();
				break;
			case 'o':
				outdir = new File(getopt.getOptarg());
				break;
			case 'm': {
				Tuple t = readFormat(getopt.getOptarg());
				opts.setFormat((VdpFormat) t.get(0));
				width = (Integer) t.get(1);
				height = (Integer) t.get(2);
				stretch = ((Number) t.get(3)).floatValue();
				opts.setAspect((float) width / height);
				colorMgr.setGreyscale((Boolean) t.get(4));
				break;
			}
			case 's':
				opts.setScaleSmooth(readFlag(getopt.getOptarg()));
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
			case 'p': {
				String oa = getopt.getOptarg();
				boolean good = false;
				if (oa.length() > 0) {
					good = true;
					char ch = oa.charAt(0);
					if (ch == 's' || "18".equals(oa)) {
						opts.setPaletteUsage(PaletteOption.CURRENT);
						colorMgr.setPalette(VdpColorManager.stockPalette);
					} else if (oa.length() >= 3 && "opt".equals(oa.substring(0, 3))) {
						opts.setPaletteUsage(PaletteOption.OPTIMIZED);
						boolean useColor0 = (oa.charAt(oa.length() - 1) == '+');
						canvas.getColorMgr().setClearFromPalette(useColor0);
					} else {
						byte[][] pal = stockPalettes.get(oa);
						if (pal != null) {
							opts.setPaletteUsage(PaletteOption.CURRENT);
							colorMgr.setPalette(pal);
						} else {
							good = false;
						}
					}
				}
				if (!good) {
					System.err.println("Unexpected -p argument: " + oa);
					System.exit(1);
				}
				break;
			}
			case 'O':
				opts.setUseOctree(true);
				break;
			case 'd': {
				String oa = getopt.getOptarg();
				if (oa.length() > 0 && oa.charAt(0) == 'n') {
					opts.setDitherType(Dither.NONE);
				} else if (oa.length() > 0 && oa.charAt(0) == 'o') {
					opts.setDitherType(Dither.ORDERED);
				} else if (oa.length() > 0 && oa.charAt(0) == 'f') {
					if (oa.charAt(oa.length() - 1) == 'r')
						opts.setDitherType(Dither.FSR);
					else
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
			case 'g':
				opts.setAsGreyScale(true);
				break;
			case 'F':
				flattenGreyscale = readFlag(getopt.getOptarg());
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
			case 't':
				test = true;
				break;
			case 'c':
				clip = true;
				break;
			case 'P':
				showPalette = true;
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

				String outBaseName = getOutputBaseName(in.getName());
				File outBase = outdir != null ? new File(outdir, outBaseName)
						: new File(in.getParentFile(), outBaseName);

				Pair<Integer, Integer> size = importImage(in, outBase);
				
				if (test) {
					File outBase2 = outdir != null ? new File(outdir, outBaseName+"_test")
						: new File(in.getParentFile(), outBaseName + "_test");
					
					// force no resize on next import
					clip = false;
					opts.setScaleSmooth(false);
					opts.setKeepAspect(false);
					width = size.first; height = size.second; 
					importImage(new File(outBase.getPath() +".png"), outBase2);
					
//					File out3 = outdir != null ? new File(outdir, outname+"_test2")
//						: new File(in.getParentFile(), outname + "_test2");
//					importImage(out2, out3);
				}
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
					f = COLOR8_DIRECT_1x1;
					break;
				case 16:
					f = COLOR16_DIRECT_1x1;
					break;
				case 32:
					f = COLOR32_DIRECT_1x1;
					break;
				case 64:
					f = COLOR64_DIRECT_1x1;
					break;
				case 128:
					f = COLOR128_DIRECT_1x1;
					break;
				case 256:
					f = VdpFormat.COLOR256_1x1;
					break;
				case 512:
					f = new VdpFormat(Layout.BITMAP, 512, true, true);
					break;
				case 4096:
					f = new VdpFormat(Layout.BITMAP, 4096, true, true);
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
			case 22:
				return new Tuple(new VdpFormat(Layout.APPLE2_HIRES, 16, false), 280, 192, 1, isGrey);
				
			}
		} catch (NumberFormatException e) {
		}
		System.err.println("Unexpected -m argument: " + mode);
		System.exit(1);
		return null;
	}

	private static String getOutputBaseName(String name) {
		String out;
		int idx = name.lastIndexOf('.');
		if (idx > 0)
			out = name.substring(0, idx) + "_vdp";
		else
			out = name + "_vdp";

//		if (name.toLowerCase().endsWith(".gif"))
//			return out + ".gif";
//		
//		return out + ".png";
		return out;
	}

	private Pair<Integer, Integer> importImage(File in, File outBase) throws NotifyException, IOException {
		ImageFrame[] frames = ImageUtils.loadImageFromFile(in.getPath());

		opts.setImages(frames);

		if (opts.getPaletteUsage() == PaletteOption.FIXED) {
			if (opts.getFormat().isMsx2()) {
				colorMgr.setPalette(VdpColorManager.stockPaletteV9938);
			} else {
				colorMgr.setPalette(VdpColorManager.stockPalette);
			}
		}
		
		ImageImport importer = new ImageImport(colorMgr);
		
		importer.setTryDirectMapping(false);
		importer.setFlattenGreyscale(flattenGreyscale);
		importer.setClip(clip);

		ImageImportData[] datas = importer.importImage(opts, width, height);

		saveImage(datas, outBase);
		
		return new Pair<Integer, Integer>(datas[0].getConvertedImage().getWidth(), datas[0].getConvertedImage().getHeight());
	}

	/**
	 * @param datas
	 * @param out
	 * @throws IOException 
	 */
	private void saveImage(ImageImportData[] datas, File outBase) throws IOException {

		if (showPalette) {
			for (ImageImportData data : datas) {
				byte[][] palette = data.getThePalette();
				System.out.println("Palette:\n" + dumpPalette(palette));
				
				BufferedImage palImg = new BufferedImage(palette.length, 1, BufferedImage.TYPE_3BYTE_BGR);
				for (int i = 0; i < palImg.getWidth(); i++) {
					int rgb = (palette[i][2] & 0xff) 
							| ((palette[i][1] << 8) & 0xff00)
							| ((palette[i][0] << 16) & 0xff0000)
							;
					palImg.setRGB(i, 0, rgb);
				}
				
				String out = outBase.getPath() + "_pal.png";
				ImageIO.write(palImg, "png", new File(out));
				
				String[] args = { "/usr/bin/display", 
						"-sample", 
						"1600%x1600%",
						out  
					}; 
				
				Runtime.getRuntime().exec(args);
			}
			
		}
		
		if (datas.length == 1) {
			BufferedImage cvt = datas[0].getConvertedImage();
			
			if (opts.getPaletteUsage() == PaletteOption.OPTIMIZED) {
				// flatten pixels, to account for loss of palette precision
				
				int w = cvt.getWidth();
				int h = cvt.getHeight();
				int[] rgb = { 0, 0, 0 };
				int[] rgbs = new int[w];
				for (int y = 0; y < h; y++) {
					cvt.getRGB(0, y, w, 1, rgbs, 0, w);
					for (int x = 0; x < w; x++) {
						rgb[0] = (rgbs[x] & 0xff0000) >> 16;
						rgb[1] = (rgbs[x] & 0xff00) >> 8;
						rgb[2] = (rgbs[x] & 0xff);
						
						rgb[0] = (rgb[0] * 0xff / 0xdf);
						rgb[1] = (rgb[1] * 0xff / 0xdf);
						rgb[2] = (rgb[2] * 0xff / 0xdf);
						
						rgbs[x] = (Math.min(255, rgb[0]) << 16)
								| (Math.min(255, rgb[1]) << 8)
								| Math.min(255, rgb[2]);

					}
					//converted.setRGB(0, y, w, 1, rgbs, 0, w);
				}
			}
			
			
			cvt = stretchImage(cvt);
	
			String out = outBase.getPath() + ".png";
			ImageIO.write(cvt, "png", new File(out));
	
			String[] args = { "/usr/bin/display", 
					"-sample", 
					"200%x200%",
					out
				}; 
			
			if (width >= 512) {
				args[2] = "100%x100%";
			} else if (opts.getFormat() == VdpFormat.COLOR16_4x4 || width <= 64 || height <= 48) {
				args[2] = "800%x800%";
			} else if (width <= 128 || height <= 96) {
				args[2] = "400%x400%";
			}
			
			Runtime.getRuntime().exec(args);
			return;
		}
		
		// animated GIF?
		String out = outBase.getPath() + ".gif";
		ImageOutputStream outs = new FileImageOutputStream(new File(out));
		GifSequenceWriter writer = new GifSequenceWriter(outs, 
				datas[0].getConvertedImage().getType(), 
				datas[0].delayMs,
				true);
				
		for (int i = 0; i < datas.length; i++) {
			BufferedImage frame = stretchImage(datas[i].getConvertedImage());
			writer.writeToSequence(frame);
		}
		
		writer.close();
		outs.close();

		String[] args = { "/usr/bin/eog", 
				out
			}; 
		
		Runtime.getRuntime().exec(args);
	}

	/**
	 * @param cvt
	 * @return
	 */
	protected BufferedImage stretchImage(BufferedImage cvt) {
		if (stretch != 1f) {
			int newHeight = (int) Math.round(height * stretch);
			BufferedImage tmp = new BufferedImage(width, newHeight, cvt.getType());
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(cvt, 0, 0, width, newHeight, null);
			g2.dispose();
			cvt = tmp;
		}
		return cvt;
	}

	private String dumpPalette(byte[][] thePalette) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < thePalette.length; i++) {
			sb.append(Integer.toHexString(i)).append(": ");
			sb.append(Integer.toHexString(thePalette[i][0] & 0xff)).append(" ");
			sb.append(Integer.toHexString(thePalette[i][1] & 0xff)).append(" ");
			sb.append(Integer.toHexString(thePalette[i][2] & 0xff)).append(" ");
			sb.append('\n');
		}
		return sb.toString();
	}
}
