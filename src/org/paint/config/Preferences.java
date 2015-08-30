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

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.util.SVGIcon;
import org.paint.util.VersionNumber;

/**
 * Used for reading previous or default user settings from property file and storing current user settings
 */

public class Preferences {
	/**
	 * 
	 */
	//	private static final long serialVersionUID = -5472475387423113108L;

	protected static Logger log = Logger.getLogger("org.panther.paint.config.Preferences");

	private String uploadVersion = "dev_3_panther_upl|UPL 10.0";
	private String pantherURL = " http://paintcuration.usc.edu";

	private boolean useDistances = true;

	private double tree_distance_scaling = 50; 

	private Font font = new Font("Arial", Font.PLAIN, 12); 

	private Map<String, Icon> iconIndex = new HashMap<String, Icon>();
	private Map<String, String> iconURLIndex = new HashMap<String, String>();

	private Color foregroundColor = Color.black;
	private Color backgroundColor = Color.white;
	private Color selectionColor = Color.black; //  = new Color(207,226,245);

	private VersionNumber version;

	private Color expPaintColor = new Color(142, 35, 35);
	private Color curatedPaintColor = new Color(255, 127, 0);
	private Color inferPaintColor = new Color(16, 64, 128);

	public final static int HIGHLIGHT_BP = 1;
	public final static int HIGHLIGHT_CC = 2;
	public final static int HIGHLIGHT_MF = 4;

	private Color mfPaintColor = new Color(232, 248, 232);
	private Color ccPaintColor = new Color(224, 248, 255);
	private Color bpPaintColor = new Color(255, 248, 220);

	private float msa_threshold[] = {
			80, 60, 40
	};

	private float  msa_weighted_threshold[] = {
			90, 75
	};

	private Color  msa_colors[] = {
			new Color(51, 102, 77), new Color(112, 153, 92), new Color(204, 194, 143)
	};

	private Color  msa_weighted_colors[] = {
			new Color(21, 138, 255), new Color(220, 233, 255)
	};

	private static Preferences preferences;

	/*
	 * Get the NCBI taxon ID from their FTP-ed file dump
	 */
	//	private Map<String, String> taxa2IDs;
	//	private Map<String, String> IDs2taxa;

	/**
	 * Constructor declaration
	 * @throws Exception 
	 *
	 *
	 * @see
	 */
	public Preferences() { //throws Exception {
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

	public static Preferences inst() {
		if (preferences == null) {
			XMLDecoder d;
			try {
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(
						Preferences.getPrefsXMLFile())));
				Preferences p = (Preferences) d.readObject();
				preferences = (Preferences) p;
				d.close();
			} catch (Exception e) {
				log.info("Could not read preferences file from "
						+ Preferences.getPrefsXMLFile());
			}
			if (preferences == null)
				preferences = new Preferences();

//			GUIManager.addShutdownHook(new Runnable() {
//				public void run() {
//					writePreferences(inst());
//				}
//			});
		}
		return preferences;
	}

	protected static File getPrefsXMLFile() {
		return new File(getPaintPrefsDir(), "preferences.xml");
	}

	protected static File getPaintPrefsDir() {
		File f = new File("config");
		f.mkdirs();
		return f;
	}

	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();

	}

	public static void writePreferences(Preferences preferences) {
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
					new FileOutputStream(getPrefsXMLFile())));
			log.info("Writing preferences to " + getPrefsXMLFile());
			encoder.setPersistenceDelegate(Font.class,
					new DefaultPersistenceDelegate(
							new String[]{ "name",
									"style",
							"size" }) );
			encoder.setPersistenceDelegate(Color.class,
					new DefaultPersistenceDelegate(
							new String[]{ "red",
									"green",
							"blue" }) );
			encoder.writeObject(preferences);
			encoder.close();

		} catch (IOException ex) {
			log.info("Could not write verification settings!");
			ex.printStackTrace();
		}
	}

	public VersionNumber getVersion() {
		if (version == null) {
			try {
				URL url = getExtensionLoader().getResource(
						"org/paint/resources/VERSION");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				version = new VersionNumber(reader.readLine());
				reader.close();
			} catch (Exception e) {
				try {
					version = new VersionNumber("1.0");
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		}
		return version;
	}

	protected static ClassLoader getExtensionLoader() {
		return Preferences.class.getClassLoader();
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
		return inst().loadLibraryIconLocal(name);
	}

	public Font getFont(){
		return font;
	}

	public void setFont(Font f) {
		font = f;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color c) {
		foregroundColor = c;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color c) {
		backgroundColor = c;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color c) {
		selectionColor = c;
	}

	public double getTree_distance_scaling() {
		return tree_distance_scaling;
	}

	public void setTree_distance_scaling(double scale) {
		tree_distance_scaling = scale;
	}

	public boolean isUseDistances(){
		return useDistances;
	}

	public boolean getUseDistances() {
		return isUseDistances();
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param distance
	 *
	 * @see
	 */
	public void setUseDistances(boolean distance){
		useDistances = distance;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public void toggleUseDistances(){
		setUseDistances(!useDistances);
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

	public String getPantherURL() {
		return pantherURL;
	}

	public void setPantherURL(String url) {
		if (url != null && url.length() > 0)
			pantherURL = url;
	}

	public String getUploadVersion() {
		return uploadVersion;
	}

	public void setUploadVersion(String v) {
		uploadVersion = v;
	}

	public Color getAspectColor(int aspect) {
		Color color = getBackgroundColor();
		if (aspect > 0) {
			switch (aspect) {
			case Preferences.HIGHLIGHT_MF:
				color = mfPaintColor;
				break;
			case Preferences.HIGHLIGHT_CC:
				color = ccPaintColor;
				break;
			case Preferences.HIGHLIGHT_BP:
				color = bpPaintColor;
				break;
			}
		}
		return color;
	}

	public void setAspectColor(int aspect, Color color) {
		if (aspect > 0) {
			switch (aspect) {
			case Preferences.HIGHLIGHT_MF:
				mfPaintColor = color;
				break;
			case Preferences.HIGHLIGHT_CC:
				ccPaintColor = color;
				break;
			case Preferences.HIGHLIGHT_BP:
				bpPaintColor = color;
				break;
			}
		}
	}

	public Color getMfPaintColor() {
		return mfPaintColor;
	}

	public void setMfPaintColor(Color mfColor) {
		mfPaintColor = mfColor;
	}

	public Color getCcPaintColor() {
		return ccPaintColor;
	}

	public void setCcPaintColor(Color ccColor) {
		ccPaintColor = ccColor;
	}

	public Color getBpPaintColor() {
		return bpPaintColor;
	}

	public void setBpPaintColor(Color bpColor) {
		bpPaintColor = bpColor;
	}

	public Color getExpPaintColor() {
		return expPaintColor;
	}

	public void setExpPaintColor(Color c) {
		expPaintColor = c;
	}

	public Color getCuratedPaintColor() {
		return curatedPaintColor;
	}

	public void setCuratedPaintColor(Color c) {
		curatedPaintColor = c;
	}

	public Color getInferPaintColor() {
		return inferPaintColor;
	}

	public void setInferPaintColor(Color c) {
		inferPaintColor = c;
	}

	public float[] getMSAThresholds(boolean weighted) {
		if (weighted)
			return msa_weighted_threshold;
		else
			return msa_threshold;
	}

	public Color[] getMSAColors(boolean weighted) {
		if (weighted)
			return msa_weighted_colors;
		else
			return msa_colors;
	}

	public void setMSAThresholds(boolean weighted, float[] thresholds) {
		if (weighted) 
			msa_weighted_threshold = thresholds;
		else
			msa_threshold = thresholds;
	}

	public void setMSAColors(boolean weighted, Color[] colors) {
		if (weighted) 
			msa_weighted_colors = colors;
		else
			msa_colors = colors;
	}

	public float[] getMsa_threshold() {
		return msa_threshold;
	}

	public void setMsa_threshold(float[] thresholds) {
		msa_threshold = thresholds;
	}

	public float[] getMsa_weighted_threshold() {
		return msa_weighted_threshold;
	}

	public void setMsa_weighted_threshold(float[] thresholds) {
		msa_weighted_threshold = thresholds;
	}

	public Color[] getMsa_colors() {
		return msa_colors;
	}

	public void setMsa_colors(Color[] colors) {
		msa_colors = colors;
	}

	public Color[] getMsa_weighted_colors() {
		return msa_weighted_colors;
	}

	public void setMsa_weighted_colors(Color[] colors) {
		msa_weighted_colors = colors;
	}

}
