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
import java.awt.image.BufferedImage;
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
	private float aspect;

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
						+ "-m MODE: video mode\n"
						+ "  2=256x192x16 (8x1), 3=256x192x16 (8x1+palette),\n"
						+ "  4=256x192x16, 5=512x192x4,\n"
						+ "  6=512x192x16, 7=256x192x256\n"
						+ "  8=64x48x16, 9=256x192x2\n"
						+ "-r WxH: resize image to the given width/height (else use mode)\n"
						+ "-a 0|1: set preserve aspect ratio off or on\n"
						+ "-d none|ordered|fs: select dithering method\n"
						+ "-p std|opt: select palette\n"
						+ "-s 0|1: smooth scaling off or on\n"
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
		getopt = new Getopt(PROGNAME, args, "?o:r:a:d:p:s:m:");

		IVdpCanvas canvas = new ImageDataCanvas24Bit();

		ImageImportDialogOptions opts = new ImageImportDialogOptions(canvas,
				machine.getVdp());

		File outdir = null;
		int width = 256, height = 192;
		float aspect = 1f;

		int opt;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
			case '?':
				help();
				break;
			case 'o':
				outdir = new File(getopt.getOptarg());
				break;
			case 'r': {
				String oa = getopt.getOptarg();
				int idx = oa.indexOf('x');
				if (idx < 0) {
					System.err.println("Unexpected -r WxH argument: " + oa);
					System.exit(1);
				}
				try {
					width = Integer.parseInt(oa.substring(0, idx));
					height = Integer.parseInt(oa.substring(idx + 1));
				} catch (NumberFormatException e) {
					System.err.println("Unexpected -r WxH argument: "
							+ e.getMessage());
					System.exit(1);
				}
				break;
			}
			case 'a':
				opts.setKeepAspect(readFlag(getopt.getOptarg()));
				break;
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
			case 'p': {
				String oa = getopt.getOptarg();
				if (oa.length() > 0 && oa.charAt(0) == 's') {
					opts.setPalette(Palette.STANDARD);
				} else if (oa.length() > 0 && oa.charAt(0) == 'o') {
					opts.setPalette(Palette.OPTIMIZED);
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
				aspect = ((Number) t.get(3)).floatValue();
				break;
			}
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
						in, width, height, aspect, out);
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

	private static Tuple/* VdpFormat, Integer, Integer */readFormat(String mode) {
		try {
			int m = Integer.parseInt(mode);
			switch (m) {
			case 2:
				return new Tuple(VdpFormat.COLOR16_8x1, 256, 192, 1);
			case 3:
				return new Tuple(VdpFormat.COLOR16_8x1_9938, 256, 192, 1);
			case 4:
				return new Tuple(VdpFormat.COLOR16_1x1, 256, 192, 1);
			case 5:
				return new Tuple(VdpFormat.COLOR4_1x1, 512, 192, 2f);
			case 6:
				return new Tuple(VdpFormat.COLOR16_1x1, 512, 192, 2f);
			case 7:
				return new Tuple(VdpFormat.COLOR256_1x1, 256, 192, 1);
			case 8:
				return new Tuple(VdpFormat.COLOR16_4x4, 64, 48, 1);
			case 9:
				return new Tuple(VdpFormat.COLOR16_8x1, 256, 192, 1);
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
			float aspect, File out) throws IOException {
		this.opts = opts;
		this.in = in;
		this.width = width;
		this.height = height;
		this.aspect = aspect;
		this.out = out;
	}

	private void importImage() throws NotifyException, IOException {
		ImageFrame[] frames = ImageUtils.loadImageFromFile(in.getPath());

		opts.setImages(frames);

		VdpColorManager colorMgr = new VdpColorManager();
		ImageImport importer = new ImageImport(colorMgr);

		ImageImportData[] datas = importer.importImage(opts, width, height);

		BufferedImage cvt = datas[0].getConvertedImage();
		if (aspect != 1f) {
			int newHeight = (int) Math.round(height * aspect);
			BufferedImage tmp = new BufferedImage(width, newHeight, cvt.getType());
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(cvt, 0, 0, width, newHeight, null);
			g2.dispose();
			cvt = tmp;
		}

		ImageIO.write(cvt, "png", out);

		Runtime.getRuntime().exec(
				new String[] { "/usr/bin/display", "-sample", "200%x200%",
						out.getPath() });
	}
}
