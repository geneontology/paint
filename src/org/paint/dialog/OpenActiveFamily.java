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

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.util.Constant;
import org.paint.config.PaintConfig;
import org.paint.main.PaintManager;


public class OpenActiveFamily {
	/**
	 * 
	 */
	protected static Logger log = Logger.getLogger(OpenActiveFamily.class);

	private static final String USER_DIR = "user.dir";
	private static final String DATA_DIR = "test_resources";

	private FileDialog chooser;
	private Frame frame;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public OpenActiveFamily(Frame frame) {
		super();
		this.frame = frame;
	}

	public File getSelectedFile(boolean save) {
		chooser = new FileDialog(frame);
		chooser.setMultipleMode(false);
//		chooser.setFilenameFilter(new PAINTFileFilter());
		chooser.setPreferredSize(new Dimension(400,400));
		String suffix = Constant.GAF_SUFFIX;
		Family family = PaintManager.inst().getFamily();

		setCurrentDirectory();
		if (family != null && family.getFamily_name() != null) {
			chooser.setFile(family.getFamily_name() + suffix);
		} else {
			chooser.setFile('*' + suffix);			
		}

		if (save) {
			chooser.setMode(FileDialog.SAVE);
			chooser.setTitle("Save family");
			chooser.setVisible(true);
		}
		else {
			chooser.setMode(FileDialog.LOAD);
			chooser.setTitle("Open family");
			chooser.setVisible(true);
		}
		File gaf_file = null;
		String filename = chooser.getFile();
		if (filename != null) {
			String gaf_dir = chooser.getDirectory();
			gaf_file = new File(gaf_dir, filename);
			PaintConfig.inst().gafdir = gaf_dir;
		}
		chooser.dispose();
		return gaf_file;
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

