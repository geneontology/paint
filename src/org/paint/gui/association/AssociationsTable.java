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

package org.paint.gui.association;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.phylo.annotate.PaintAction;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.tracking.LogAction;
import org.bbop.phylo.util.Constant;
import org.bbop.swing.HyperlinkLabel;
import org.paint.dialog.ChallengeDialog;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.ChallengeEvent;
import org.paint.gui.event.ChallengeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.tree.TreePanel;
import org.paint.main.PaintManager;
import org.paint.util.GuiConstant;
import org.paint.util.HTMLUtil;

public class AssociationsTable extends JTable
implements GeneSelectListener, 
MouseListener, 
FamilyChangeListener,
TermSelectionListener, 
AnnotationChangeListener,
AspectChangeListener,
ChallengeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AssociationsTableModel assoc_model;

	private Bioentity node;
	private boolean widths_initialized;

	private static Logger log = Logger.getLogger(AssociationsTable.class);

	public enum PHYLO_ACTION {
		REMOVE, 
		RESTORE,
		CHALLENGE,
		LOST, 
		REGAIN,
		DEPENDENCIES;

		public String toString() {
			return super.toString().toUpperCase();
		}
	}

	public AssociationsTable() {
		super();	

		assoc_model = new AssociationsTableModel();
		setModel(assoc_model);

		setBackground(GuiConstant.BACKGROUND_COLOR);
		setSelectionBackground(GuiConstant.SELECTION_COLOR);

		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);

		setDefaultRenderer(GeneAnnotation.class, new GOTermRenderer());
		setDefaultRenderer(HyperlinkLabel.class, new HyperlinkCellRenderer());
		WithCellRenderer with_renderer = new WithCellRenderer();
		setDefaultRenderer(WithCellModel.class, with_renderer);
		setDefaultRenderer(String.class, new ECOCellRenderer());
		setDefaultRenderer(PHYLO_ACTION.class, new TrashCellRenderer());

		TableColumn withcol = getColumn(AssociationsTableModel.WITH_COL_NAME);
		WithCellController edit = new WithCellController();
		withcol.setCellEditor(edit);
		setEditingColumn(AssociationsTableModel.WITH_COLUMN);

		setShowGrid(false);
		setIntercellSpacing(new Dimension(1, 1));

		addMouseListener(this);
		EventManager.inst().registerGeneListener(this);
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerTermListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
		EventManager.inst().registerAspectChangeListener(this);
		EventManager.inst().registerChallengeListener(this);

		widths_initialized = false;
	}

	public void newFamilyData(FamilyChangeEvent e) {
		TreePanel tree = PaintManager.inst().getTree();
		if (tree != null) {
			Bioentity root = tree.getRoot();
			setAnnotations(tree.getTopLeafNode(root));
		} else {
			setAnnotations(null);
		}
	}

	private void setAnnotations(Bioentity node) {
		this.node = node;
		assoc_model.setNode(node);
		setColumnWidths();
		revalidate();
		repaint();
	}

	public boolean isCellEditable(int row, int column) {
		return assoc_model.isCellEditable(row, column);
	}

	private void setColumnWidths() {
		if (!widths_initialized) {
			Insets insets = new DefaultTableCellRenderer().getInsets();
			int col_count = getColumnCount();
			TableColumnModel col_model = getColumnModel();
			FontMetrics fm = getFontMetrics(getFont());
			int remainder = getWidth();
			TableColumn term_col = null;
			for (int i = 0; i < col_count; i++) {
				String col_name = getColumnName(i);
				int col_width = -1;
				if (col_name.equals(AssociationsTableModel.CODE_COL_NAME)) {
					col_width = fm.stringWidth("IDAXXX") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.TRASH_COL_NAME)) {
					col_width = fm.stringWidth("CHALLENGE  ") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.REFERENCE_COL_NAME)) {
					col_width = fm.stringWidth("PUBMED:0000000000 ") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.WITH_COL_NAME)) {
					col_width = fm.stringWidth("XXXX0000000000 XXXX0000000000 ") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.TERM_COL_NAME)) {
					term_col = col_model.getColumn(i);
				}
				if (col_width > 0) {
					TableColumn col = col_model.getColumn(i);
					//Get the column at index columnIndex, and set its preferred width.
					col.setPreferredWidth(col_width);
					col.setWidth(col_width);
					remainder -= col_width;
				}
			}
			//Get the column at index columnIndex, and set its preferred width.
			term_col.setPreferredWidth(remainder);
			term_col.setWidth(remainder);

			initRowHeights();
			widths_initialized = true;
		}
	}

	private void initRowHeights() {
		int rows = assoc_model.getRowCount();
		int row_height = getRowHeight() + 6;

		for (int row = 0; row < rows; row++) {
			WithCellModel with_model = (WithCellModel) assoc_model.getValueAt(row, AssociationsTableModel.WITH_COLUMN);
			int with_count = with_model.getSize();
			if (with_count == 0)
				with_count = 1;
			int with_height = Math.min(row_height * 4, row_height * with_count);
			setRowHeight(row, with_height);
		}
	}

	/* MouseListener methods */
	public void mouseClicked(MouseEvent event) {
		Point point = event.getPoint();
		int row = rowAtPoint(point);
		int column = columnAtPoint(point);
		if (row >= 0 && row <= assoc_model.getRowCount()) {
			if (HyperlinkLabel.class == assoc_model.getColumnClass(column)) {
				GeneAnnotation assoc = assoc_model.getEvidenceForRow(row);
				List<String> evi = assoc.getReferenceIds();
				String preferred_ref = HTMLUtil.getPMID(evi);
				if (preferred_ref.length() > 0 && !preferred_ref.startsWith(Constant.PAINT_REF)) {
					String [] xref = preferred_ref.split(":");
					String text = HTMLUtil.getURL(xref[0], xref[1], false);
					HTMLUtil.bringUpInBrowser(text);
				}
			}
			else if (String.class == assoc_model.getColumnClass(column)) {
				GeneAnnotation assoc = assoc_model.getEvidenceForRow(row);
				TermSelectEvent term_event = new TermSelectEvent(this, assoc.getCls(), false);
				EventManager.inst().fireTermEvent(term_event);		
				ListSelectionModel lsm = getSelectionModel();
				lsm.clearSelection();
				lsm.addSelectionInterval(row, row);
			} else if (PHYLO_ACTION.class == assoc_model.getColumnClass(column)) {
				GeneAnnotation assoc = assoc_model.getEvidenceForRow(row);
				PHYLO_ACTION value = (PHYLO_ACTION) assoc_model.getValueAt(row, column);
				// returns true is this is an annotation added by a PAINT curator
				// And it is either a direct annotation or a direct NOT
				/*
				 * Removing of annotations is only permitted for ancestral nodes
				 * that have been directly annotated to that term by the curator,
				 * so check first before deleting a term
				 */
				if (value.equals(PHYLO_ACTION.REMOVE)) {
					LogAction.inst().undo(PaintManager.inst().getFamily(), assoc);
					String deleted_term = assoc.getCls();
					assoc_model.fireTableDataChanged();
					/**
					 * Now unselect this term, if it was selected.
					 */
					String term = EventManager.inst().getCurrentTermSelection();
					if (term != null && term.equals(deleted_term)) {
						TermSelectEvent term_event = new TermSelectEvent (this, null, false);
						EventManager.inst().fireTermEvent(term_event);
						ListSelectionModel lsm = getSelectionModel();
						lsm.clearSelection();
					}
					// Notify listeners that the gene data has changed too
					EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
				} else if (value.equals(PHYLO_ACTION.RESTORE)) {
					LogAction.inst().undo(PaintManager.inst().getFamily(), assoc);
					EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
				} else if (value.equals(PHYLO_ACTION.CHALLENGE)) {
					List<GeneAnnotation> positive_annots = new ArrayList<>();
					positive_annots.add(assoc);
					ChallengeDialog override_dialog = new ChallengeDialog(GUIManager.getManager().getFrame(), assoc, positive_annots, null);
					if (override_dialog.okay()) {
						String rationale = override_dialog.getRationale();
						List<GeneAnnotation> challenge_assoc = new ArrayList<>();
						challenge_assoc.add(assoc);
						challengeAnnotation(challenge_assoc, rationale, true);
					}	
				} else if (value.equals(PHYLO_ACTION.LOST)) {
					if (!assoc_model.contradictoryNegation(assoc)) {
						setAnnotationAsLoss(assoc);
					}
				} else {
					/*
					 * This ought to be a regain
					 */
					log.info("Association table value = " + value.toString());
				}
			} else if (GeneAnnotation.class == assoc_model.getColumnClass(column)) {
				GeneAnnotation assoc = (GeneAnnotation) assoc_model.getValueAt(row, column);
				String term = assoc.getCls();
				String text = HTMLUtil.getURL("AMIGO", term, true);
				HTMLUtil.bringUpInBrowser(text);
				//			} else if (WithListModel.class == assoc_model.getColumnClass(column)) {
				//                log.info("Association table mouse click in with column");
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {	
	}

	public void mousePressed(MouseEvent event) {		
	}

	public void mouseReleased(MouseEvent arg0) {		
	}

	private List<GeneAnnotation> challengeAnnotation(List<GeneAnnotation> challenged_assocs, String rationale, boolean log_it) {
		Family family = PaintManager.inst().getFamily();
		/*
		 * Remove any experimental annotations that have been challenged
		 */
		List<GeneAnnotation> removed = 	PaintAction.inst().challengeExpAnnotation(family, challenged_assocs, rationale);

		for (GeneAnnotation positive_annot : challenged_assocs) {
			if (log_it) {
				LogAction.inst().logChallenge(positive_annot, removed, rationale);
			}
			/* 
			 * Important to log the challenge first, otherwise it
			 * is unavailable for display in the evidence/log panel.
			 */
			String aspect_name = AspectSelector.inst().getAspectName4Code(positive_annot.getAspect());
			ChallengeEvent challenge_event = new ChallengeEvent(aspect_name);
			EventManager.inst().fireChallengeEvent(challenge_event);
		}
		return removed;
	}

	private void setAnnotationAsLoss(GeneAnnotation lost_assoc) {
		String [] notStrings = Constant.Not_Strings;
		String evidence_type = null;
		/*
		 * First determine if any positive annotations are being challenged by 
		 * setting this to a loss of function
		 * 
		 */
		List<GeneAnnotation> positive_annots = assoc_model.collectExtantAnnotations(lost_assoc);
		/*
		 * Open a dialog with the user to 
		 * 1. Get the evidence type
		 * 2. If there are positive annotations, confirm that they want to challenge these
		 */
		Frame frame = GUIManager.getManager().getFrame();
		ChallengeDialog challenge_dialog = new ChallengeDialog(frame, lost_assoc, positive_annots, notStrings);
		// make sure the user hasn't cancelled the entire operation
		if (challenge_dialog.okay()) {
			evidence_type = challenge_dialog.getEvidenceType();
			List<GeneAnnotation> removed = null;

			// challenges?
			if (!positive_annots.isEmpty()) {
				String rationale = challenge_dialog.getRationale();
				/*
				 * It's entirely possible that this challenged annotation was the 
				 * sole supporting evidence for the annotation that is being negated
				 * If that is the case then there is no longer an annotation to be negated
				 */
				removed = challengeAnnotation(positive_annots, rationale, false);
			}
			String ev_code = Constant.NOT_QUALIFIERS_TO_EVIDENCE_CODES.get(evidence_type);
			PaintAction.inst().setNot(PaintManager.inst().getFamily(), node, lost_assoc, ev_code, true, removed);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
		}
	}

	private void refreshTermSelection(String term) {
		ListSelectionModel lsm = this.getSelectionModel();
		lsm.clearSelection();
		if (term != null) {
			int row = assoc_model.getRowForTerm(term);
			lsm.addSelectionInterval(row, row);
		} else {
			clearSelection();
		}
	}

	public void handleTermEvent(TermSelectEvent e) {
		if (e.getSource() != this) {
			Bioentity mrca = EventManager.inst().getCurrentSelectedNode();
			if (node != mrca) {
				setAnnotations(mrca);
			}
			String term_selection = e.getSelectedTerm();
			if (term_selection != null) {
				refreshTermSelection(term_selection);
			}
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		if (node != null && event.getSource() != null) {
			assoc_model.setNode(node);
			initRowHeights();
			assoc_model.fireTableDataChanged();		
		}
	}

	public void handleAspectChangeEvent(AspectChangeEvent event) {
		repaint();
	}

	public void handleGeneSelectEvent (GeneSelectEvent e) {
		if (e.getGenes().size() > 0) {
			setAnnotations((DisplayBioentity) e.getAncestor());
		} else {
			setAnnotations(null);
		}
		this.initRowHeights();
		clearSelection();
	}

	public void handleChallengeEvent(ChallengeEvent event) {
		setAnnotations(node);
	}
}


