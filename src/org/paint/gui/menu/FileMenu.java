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

package org.paint.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.dialog.OpenActiveFamily;
import org.paint.dialog.OpenNewFamily;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.main.PaintManager;
import org.paint.util.InternetChecker;
import org.paint.util.LoginUtil;

import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;

public class FileMenu extends JMenu implements AnnotationChangeListener { // DynamicMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(FileMenu.class);

	private JMenuItem openDBItem;
	private JMenuItem openLocalFileItem;
	private JMenuItem saveFileLocalItem;

	private static final String open_db = "New ... ";
	private static final String open_file = "Open ... ";
	private static final String save_annots = "Save ... ";

	private static List<FileMenu> instances = new ArrayList<FileMenu>();

	public FileMenu() {
		super("Annotate");
		this.setMnemonic('f');

		openDBItem = new JMenuItem(open_db);
		openDBItem.addActionListener(new OpenFromDBActionListener());
		this.add(openDBItem);

		this.addSeparator();

		openLocalFileItem = new JMenuItem(open_file);
		openLocalFileItem.addActionListener(new OpenFromFileActionListener());
		this.add(openLocalFileItem);

		this.addSeparator();

		saveFileLocalItem = new JMenuItem(save_annots);
		saveFileLocalItem.addActionListener(new SaveToFileActionListener());
		this.add(saveFileLocalItem);

		this.addSeparator();

		updateMenu();

		EventManager.inst().registerGeneAnnotationChangeListener(this);

		instances.add(this);
	}

	public void updateMenu() {
		openDBItem.setEnabled(InternetChecker.getInstance().isConnectionPresent(true));

		boolean family_loaded = DirtyIndicator.inst().familyLoaded();
		saveFileLocalItem.setEnabled(family_loaded);
	}

	private static class SaveToFileActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			OpenActiveFamily dlg = new OpenActiveFamily(GUIManager.getManager().getFrame());

			File f = dlg.getSelectedFile(true, null);
			if (f != null) {
				PaintManager.inst().getFamily().save();
			}
		}
	}

	/** Opens book from database
	 * 
	 */
	private class OpenFromDBActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				protected Void doInBackground() throws Exception {
					// If document has been updated, attempt to save before opening/locking another
					boolean proceed = true;
					if (DirtyIndicator.inst().genesAreDirty())
						proceed = DirtyIndicator.inst().runDirtyDialog("opening a new family?");
					if (proceed) {
						if (!LoginUtil.getLoggedIn()) {
							if (!LoginUtil.login()) {
								return null;
							}
						}

						OpenNewFamily dlg = new OpenNewFamily(GUIManager.getManager().getFrame());
						String familyID = dlg.display();
						if (familyID != null) {
							PaintManager.inst().openNewFamily(familyID, null);
							updateMenu();
						}
					}
					return null;
				}
			};
			worker.execute();
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		updateMenu();
	}


	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class OpenFromFileActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				protected Void doInBackground() throws Exception {
					// If document has been updated, attempt to save before opening/locking another
					boolean proceed = true;
					boolean dirty = DirtyIndicator.inst().genesAreDirty();
					if (dirty)
						proceed = DirtyIndicator.inst().runDirtyDialog("opening a family?");
					if (proceed) {
						if (!LoginUtil.getLoggedIn()) {
							LoginUtil.login();
						}
						OpenActiveFamily dlg = new OpenActiveFamily(GUIManager.getManager().getFrame());
						File f = dlg.getSelectedFile(false, FileNameGenerator.PAINT_SUFFIX);
						if ((null != f) && (f.isFile())){
							String full_file_name = "";
							try {
								full_file_name = f.getCanonicalPath();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String familyID = full_file_name.substring(full_file_name.lastIndexOf('/') + 1, full_file_name.lastIndexOf('.'));

							PaintManager.inst().openNewFamily(familyID, full_file_name);
							updateMenu();
						}
					}
					return null;
				}
			};
			worker.execute();
		};
	}
}

