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
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.util.DirectoryUtil;
import org.paint.main.PaintManager;


public class OpenActiveFamily {
	/**
	 * 
	 */
	protected static Logger log = Logger.getLogger(OpenActiveFamily.class);

	private static String extension;
	private static final String USER_DIR = "user.dir";
	private static final String DATA_DIR = "test_resources";

	private JFileChooser chooser;
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

	public File getSelectedFile(boolean save, String suffix) {
		chooser = new JFileChooser();
		chooser.setDialogTitle("Choose a family");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Family family = PaintManager.inst().getFamily();
		if (suffix == null || suffix.equals("")) {
			extension = "";
		} else {
			if (suffix.charAt(0) == '.') {
				suffix = suffix.substring(1);
			}
			extension = suffix;
		}

		chooser.setFileFilter(new PAINTFileFilter());

		if (family != null && family.getFamily_name() != null) {
			String filename = family.getFamily_name();
			if (extension.length() > 0)
				filename = filename + extension;
			chooser.setSelectedFile(new File(filename));
		}
		setCurrentDirectory();

		chooser.setVisible(true);
		int returned;
		if (frame == null)
			log.debug("Frame is null");
		if (save) {
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			returned = chooser.showDialog(frame, "Save");
		}
		else {
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			returned = chooser.showDialog(frame, "Open");
		}
		File f = null;
		if (returned == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if (f != null) {
				String dir = f.getAbsolutePath();
				DirectoryUtil.inst().setGafDir(dir.substring(0, dir.lastIndexOf('/')));
			}
		}
		return f;
	}

	private void setCurrentDirectory() {
		if (DirectoryUtil.inst().getGafDir() != null) {
			File gaf_dir = new File(DirectoryUtil.inst().getGafDir());
			chooser.setCurrentDirectory(gaf_dir);
		}
		else {
			StringBuffer  defaultDirectory = new StringBuffer(System.getProperty(USER_DIR));
			defaultDirectory.append(File.separator);
			defaultDirectory.append(DATA_DIR);
			File f = new File(defaultDirectory.toString());
			if (f.exists()) {
				chooser.setCurrentDirectory(f);
			}
		}
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	public class PAINTFileFilter extends FileFilter {

		@Override
		public boolean accept(File f){
			if (f.isDirectory()){
				return true;
			}
			String  suffix = getExtension(f);

			if (suffix != null){
				if (suffix.equals(extension)){
					return true;
				}
				else{
					return false;
				}
			}
			return false;
		}

		@Override
		public String getDescription(){
			return "PAINT FILES";
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

