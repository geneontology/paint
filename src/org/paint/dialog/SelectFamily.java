/* Copyright (C) 2008 LBNL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the BSD license
 * as published by the Free Software Foundation
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.paint.dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.util.Constant;
import org.paint.config.PaintConfig;
import org.paint.main.PaintManager;


public class SelectFamily {
	/**
	 * 
	 */
	protected static Logger log = Logger.getLogger(SelectFamily.class);

	private static final String USER_DIR = "user.dir";
	private static final String DATA_DIR = "test_resources";

	private FileDialog chooser;
	private Frame frame;
	private Pattern pthr;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public SelectFamily(Frame frame) {
		super();
		this.frame = frame;
		pthr = Pattern.compile("PTHR\\d{5}");
	}

	public String getSelectedDirectory(boolean save) {
		chooser = new FileDialog(frame);
		chooser.setMultipleMode(false);
		chooser.setPreferredSize(new Dimension(400,400));
		String family_dir = null;
		setCurrentDirectory();
		if (save) {
			Family family = PaintManager.inst().getFamily();
			if (family != null && family.getFamily_name() != null) {
				chooser.setFile(family.getFamily_name());
				chooser.setMode(FileDialog.SAVE);
				chooser.setTitle("Save family");
				chooser.setVisible(true);
				if (chooser.getDirectory() != null ) {
					String chosen = chooser.getDirectory();
					Matcher matcher = pthr.matcher(chosen);
					if (!matcher.find()) {
						File dir = new File(new File(chosen), family.getFamily_name());
						dir.mkdir();
						try {
							family_dir = dir.getCanonicalPath();
						} catch (IOException e) {
							log.error("Couldn't get directory path for " + family.getFamily_name());
						}
					} else {
						family_dir = chosen;
					}
				}
			}
		}
		else {
			chooser.setMode(FileDialog.LOAD);
			chooser.setTitle("Open family");
			chooser.setVisible(true);
			String chosen = chooser.getDirectory();
			if (chosen != null) {
				Matcher matcher = pthr.matcher(chosen);
				if (matcher.find()) {
					family_dir = chosen;
				}
			}
		}
		chooser.dispose();
		if (family_dir != null) {
			PaintConfig.inst().gafdir = family_dir;
		}
		return family_dir;
	}

	private void setCurrentDirectory() {
		if (PaintConfig.inst().gafdir != null) {
			chooser.setDirectory(PaintConfig.inst().gafdir);
		}
		else {
			StringBuffer  defaultDirectory = new StringBuffer(System.getProperty(USER_DIR));
			defaultDirectory.append(File.separator);
			defaultDirectory.append(DATA_DIR);
			chooser.setDirectory(defaultDirectory.toString());
		}
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	public class PAINTFileFilter implements FilenameFilter {

		public boolean accept(File dir, String name){
			if (dir.isDirectory()){
				return true;
			}
			String  suffix = getExtension(new File(name));

			if (suffix != null){
				if (suffix.equals(Constant.GAF_SUFFIX)){
					return true;
				}
				else{
					return false;
				}
			}
			return false;
		}

		public String getDescription(){
			return "PAINT GAF FILE";
		}

		public String getExtension(File f){
			String  ext = null;
			String  s = f.getName();
			int     i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1){
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

	}
}

