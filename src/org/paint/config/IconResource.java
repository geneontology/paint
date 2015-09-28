/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.config;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.paint.util.SVGIcon;

/**
 * Used for reading previous or default user settings from property file and storing current user settings
 */

public class IconResource { // extends DirectoryUtil {
	/**
	 * 
	 */

	protected static Logger log = Logger.getLogger("org.panther.paint.config.Preferences");

	private Map<String, Icon> iconIndex = new HashMap<String, Icon>();
	private Map<String, String> iconURLIndex = new HashMap<String, String>();

	private static IconResource resource;

	/**
	 * Constructor declaration
	 * @throws Exception 
	 *
	 *
	 * @see
	 */
	public IconResource() { //throws Exception {
		// For now use this font, however, this should be loaded from the users system; 
		// just in case the font is unavailable in the users machine.
		iconURLIndex.put("trash", "resource:trash.png");
		iconURLIndex.put("paint", "resource:direct_annot.png");
		iconURLIndex.put("arrowDown", "resource:arrowDown.png");
		iconURLIndex.put("block", "resource:Emblem-question.svg");
		iconURLIndex.put("not", "resource:round-stop.png");
		iconURLIndex.put("exp", "resource:Bkchem.png");
		iconURLIndex.put("inherited", "resource:inherited_annot.png");		
		iconURLIndex.put("colocate", "resource:colocate.png");
		iconURLIndex.put("contribute", "resource:contribute.svg");
	}
	
	public static IconResource inst() {
		if (resource == null) {
			resource = new IconResource();
		}
		return resource;
	}

	protected static ClassLoader getExtensionLoader() {
		return IconResource.class.getClassLoader();
	}

	protected Icon loadLibraryIconLocal(String name) {
		String dir = "org/paint/resources/";
		URL url = getExtensionLoader().getResource(
				dir + name);
		if (url == null) {
			url = getExtensionLoader().getResource(
					"org/paint/resources/icons" + name);
		}
		if (url == null)
			log.debug("Oops, could not find icon " + name);
		return getIconForURL(url);
	}

	public static Icon getIconForURL(URL url) {
		if (url == null)
			return null;

		try {
			String urlStr = url.toString();
			if (urlStr.endsWith("svg"))
				return new SVGIcon(urlStr);
		} catch (Exception e) {
			log.info("WARNING: Exception getting icon for " + url + ": " + e); // DEL
		}
		return new ImageIcon(url);
	}

	public static Image loadLibraryImage(String name) {
		URL url = getExtensionLoader().getResource(
				"org/paint/gui/resources/" + name);
		return Toolkit.getDefaultToolkit().createImage(url);
	}

	public Icon loadLibraryIcon(String name) {
		return loadLibraryIconLocal(name);
	}

	public Icon getIconByName(String id) {
		Icon out = (Icon) iconIndex.get(id);
		if (out == null) {
			String iconURL = iconURLIndex.get(id);
			if (iconURL != null) {
				if (iconURL.startsWith("resource:")) {
					out = loadLibraryIcon(iconURL.substring(9));
				} else {
					try {
						out = getIconForURL(new URL(iconURL));
					} catch (MalformedURLException e) {
						File file = new File(iconURL);
						if (file.exists())
							try {
								out = getIconForURL(file.toURI().toURL());
							} catch (MalformedURLException e1) {
							}
					}
				}
			}
			if (out != null) {
				iconIndex.put(id, out);
			}
		}
		return out;
	}

}
