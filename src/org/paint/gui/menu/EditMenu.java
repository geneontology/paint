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
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.tracking.LogAction;
import org.bbop.phylo.tracking.LogEntry;
import org.bbop.swing.DynamicMenu;
import org.paint.dialog.find.FindDialog;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.ChallengeEvent;
import org.paint.gui.event.ChallengeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.main.PaintManager;

public class EditMenu extends DynamicMenu implements 
FamilyChangeListener, 
AnnotationChangeListener,
ChallengeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(EditMenu.class.getName());
	protected JMenuItem undoItem ;
	protected JMenuItem redoItem ;
	protected JMenuItem searchItem ;
	protected FindDialog     findDialog;

	private static final String undo = "Undo";
	private static final String redo = "Redo";
	private static final String find = "Find...";
//	private static final String curation_status_color = "Color selection...";

	public EditMenu() {
		super("Edit");
		this.setMnemonic('e');

		undoItem = new JMenuItem(undo);
		undoItem.setMnemonic(KeyEvent.VK_Z);
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.META_MASK));
		undoItem.addActionListener(new undoActionListener());
		add(undoItem);
		undoItem.setEnabled(false);
		
		redoItem = new JMenuItem(redo);
		redoItem.setMnemonic(KeyEvent.VK_Y);
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.META_MASK));
		redoItem.addActionListener(new redoActionListener());
		add(redoItem);
		redoItem.setEnabled(false);
		
		// Separator line
		this.addSeparator();

		searchItem = new JMenuItem(find);
		searchItem.setMnemonic(KeyEvent.VK_F);
		//Setting the accelerator:
		searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.META_MASK));
		searchItem.addActionListener(new SearchActionListener());
		this.add(searchItem);
		searchItem.setEnabled(PaintManager.inst().getFamily() != null);

		// Separator line
		this.addSeparator();

//		JMenuItem curationStatusColor = new JMenuItem(curation_status_color);
//		curationStatusColor.addActionListener(new CurationStatusColorListener());
//		this.add(curationStatusColor);

		/* So we can hide and show this menu based on what data is available */
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
		EventManager.inst().registerChallengeListener(this);
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class undoActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			Family family = PaintManager.inst().getFamily();
			LogEntry entry = LogAction.inst().undo(family);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(entry.getNode()));
			if (entry.getAction() == LogEntry.LOG_ENTRY_TYPE.PRUNE) {
				NodeReorderEvent event = new NodeReorderEvent(this);
				event.setNodes(family.getTree().getTerminusNodes());
				EventManager.inst().fireNodeReorderEvent(event);
			} else if ((entry.getAction() == LogEntry.LOG_ENTRY_TYPE.CHALLENGE) ||
					(entry.getAction() == LogEntry.LOG_ENTRY_TYPE.NOT && entry.getRemovedAssociations() != null)) {
				GeneAnnotation restore_annot = entry.getLoggedAssociation();
				String aspect_name = AspectSelector.inst().getAspectName4Code(restore_annot.getAspect());
				ChallengeEvent challenge_event = new ChallengeEvent(aspect_name);
				EventManager.inst().fireChallengeEvent(challenge_event);
			}
		}
	}

	private class redoActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			Family family = PaintManager.inst().getFamily();
			LogEntry entry = LogAction.inst().redo(family);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(entry.getNode()));
			if (entry.getAction() == LogEntry.LOG_ENTRY_TYPE.PRUNE) {
				NodeReorderEvent event = new NodeReorderEvent(this);
				event.setNodes(family.getTree().getTerminusNodes());
				EventManager.inst().fireNodeReorderEvent(event);
			} else if ((entry.getAction() == LogEntry.LOG_ENTRY_TYPE.CHALLENGE) ||
					(entry.getAction() == LogEntry.LOG_ENTRY_TYPE.NOT && entry.getRemovedAssociations() != null)) {
				/* 
				 * Important to log the challenge first, otherwise it
				 * is unavailable for display in the evidence/log panel.
				 */
				String aspect_name = AspectSelector.inst().getAspectName4Code(entry.getLoggedAssociation().getAspect());
				ChallengeEvent challenge_event = new ChallengeEvent(aspect_name);
				EventManager.inst().fireChallengeEvent(challenge_event);

			}
		}
	}

	private class SearchActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if (null == findDialog){
				findDialog = new FindDialog(GUIManager.getManager().getFrame(), "Find");
			} else 
				findDialog.setVisible(true);
		}
	}

	public void newFamilyData(FamilyChangeEvent e) {
		searchItem.setEnabled(true);
		updateLogItems();
	}

//	private class CurationStatusColorListener implements ActionListener {
//		public void actionPerformed(ActionEvent e) {
//			new CurationStatusColorDialog(GUIManager.getManager().getFrame());
//		}
//	}
//
	private void updateLogItems() {
		String item_label;
		item_label = LogAction.inst().doneString();
		if (item_label != null) {
			undoItem.setText(undo + ' ' + item_label);
			undoItem.setEnabled(true);
		}
		else {
			undoItem.setText(undo);
			undoItem.setEnabled(false);
		}
		item_label = LogAction.inst().undoneString();
		if (item_label != null) {
			redoItem.setText(redo + ' ' + item_label);
			redoItem.setEnabled(true);
		}
		else {
			redoItem.setText(redo);
			redoItem.setEnabled(false);	
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		updateLogItems();
	}

	public void handleChallengeEvent(ChallengeEvent event) {
		updateLogItems();		
	}
}
