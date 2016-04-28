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
import org.bbop.phylo.util.Constant;
import org.paint.dialog.SelectFamily;
import org.paint.dialog.OpenNewFamily;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.main.PaintManager;
import org.paint.util.InternetChecker;
import org.paint.util.LoginUtil;

public class FileMenu extends JMenu implements AnnotationChangeListener { // DynamicMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(FileMenu.class);

	private JMenuItem newFamItem;
	private JMenuItem openFamItem;
	private JMenuItem saveFamItem;

	private static final String new_fam = "New ... ";
	private static final String open_fam = "Open ... ";
	private static final String save_annots = "Save ... ";

	private static List<FileMenu> instances = new ArrayList<FileMenu>();

	public FileMenu() {
		super("Annotate");
		this.setMnemonic('f');

		newFamItem = new JMenuItem(new_fam);
		newFamItem.addActionListener(new NewFamilyActionListener());
		this.add(newFamItem);

		this.addSeparator();

		openFamItem = new JMenuItem(open_fam);
		openFamItem.addActionListener(new OpenFamilyActionListener());
		this.add(openFamItem);

		this.addSeparator();

		saveFamItem = new JMenuItem(save_annots);
		saveFamItem.addActionListener(new SaveToFileActionListener());
		this.add(saveFamItem);

		this.addSeparator();

		updateMenu();

		EventManager.inst().registerGeneAnnotationChangeListener(this);

		instances.add(this);
	}

	public void updateMenu() {
		newFamItem.setEnabled(InternetChecker.getInstance().isConnectionPresent(true));

		boolean family_loaded = DirtyIndicator.inst().familyLoaded();
		saveFamItem.setEnabled(family_loaded);
	}

	private static class SaveToFileActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			SelectFamily dlg = new SelectFamily(GUIManager.getManager().getFrame());

			String f = dlg.getSelectedDirectory(true);
			if (f != null) {
				PaintManager.inst().saveFamily();
			}
		}
	}

	/** Opens book from database
	 * 
	 */
	private class NewFamilyActionListener implements ActionListener{
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
							PaintManager.inst().openNewFamily(familyID);
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
	private class OpenFamilyActionListener implements ActionListener{
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
						SelectFamily dlg = new SelectFamily(GUIManager.getManager().getFrame());
						String full_file_name = dlg.getSelectedDirectory(false);
						if (full_file_name != null) {
							if (full_file_name.charAt(full_file_name.length() - 1) == '/') {
								full_file_name = full_file_name.substring(0, full_file_name.length() - 1);
							}
							String familyID = full_file_name.substring(full_file_name.lastIndexOf('/') + 1);
							PaintManager.inst().openActiveFamily(familyID);
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

