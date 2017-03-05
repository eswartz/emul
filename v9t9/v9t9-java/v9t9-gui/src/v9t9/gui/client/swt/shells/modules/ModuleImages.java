/*
  ModuleImages.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import v9t9.common.files.URIUtils;
import v9t9.common.machine.IMachine;
import v9t9.gui.EmulatorGuiData;

/**
 * @author ejs
 *
 */
public class ModuleImages {
	private static final Logger logger = Logger.getLogger(ModuleImages.class);
	
	private IMachine machine;
	private URI builtinImagesURI;

	private Map<String, Image> loadedImages = new HashMap<String, Image>();

	private Device device;

	public ModuleImages(Device device, IMachine machine) {
		this.device = device;
		this.machine = machine;

		try {
			builtinImagesURI = URIUtils.resolveInsideURI(
					machine.getModel().getDataURL().toURI(), 
					"images/");
			logger.info("builtinImagesURI = " + builtinImagesURI);
		} catch (URISyntaxException e3) {
			logger.error("Failed to load stock module image", e3);
		} 
	}


	public Image loadImage(String string) {
		Image stock = loadedImages.get(string);
		if (stock == null) {
			stock = EmulatorGuiData.loadImage(device, "icons/" + string);
			loadedImages.put(string, stock);
		}
		return stock;
	}

	public Image loadImage(URI imageURI) {
		String key = imageURI.toString();
		Image stock = loadedImages.get(key);
		if (stock == null) {
			try {
				stock = EmulatorGuiData.loadImage(device, imageURI.toURL());
			} catch (MalformedURLException e) {
			}
			loadedImages.put(key, stock);
		}
		return stock;
	}



	/**
	 * @param imagePath
	 * @param imagesURI
	 * @return
	 */
	protected URI getImageURI(String imagePath) {
		URI imagesURI = builtinImagesURI;

		URI imageURI;
		imageURI = machine.getRomPathFileLocator().findFile(imagePath);

		if (imageURI == null) {
			// look inside distribution
			
				imageURI = machine.getRomPathFileLocator().resolveInsideURI(
						imagesURI,
						imagePath);
				if (!machine.getRomPathFileLocator().exists(imageURI))
					imageURI = null;
			
		}
		return imageURI;
	}


	/**
	 * 
	 */
	public void dispose() {
		for (Image image : loadedImages.values())
			image.dispose();
		loadedImages.clear();

	}

}
