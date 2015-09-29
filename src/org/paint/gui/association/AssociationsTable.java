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
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.annotate.PaintAction;
import org.bbop.phylo.tracking.LogAction;
import org.bbop.phylo.util.Constant;
import org.bbop.phylo.util.OWLutil;
import org.bbop.swing.HyperlinkLabel;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;
import org.paint.gui.GuiConstant;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.tree.TreePanel;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;

public class AssociationsTable extends JTable
implements GeneSelectListener, MouseListener, FamilyChangeListener,
TermSelectionListener, AnnotationChangeListener,
AspectChangeListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AssociationsTableModel assoc_model;

	private Bioentity node;
	private boolean is_adjusting;
	private boolean widths_initialized;

	private static Logger log = Logger.getLogger(AssociationsTable.class);

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
		WithCellRenderer with_cell_renderer = new WithCellRenderer();
		setDefaultRenderer(HashSet.class, with_cell_renderer);
		setDefaultRenderer(String.class, new ECOCellRenderer());
		setDefaultRenderer(Boolean.class, new TrashCellRenderer());

		setShowGrid(false);
		setIntercellSpacing(new Dimension(1, 1));

		addMouseListener(this);
		EventManager.inst().registerGeneListener(this);
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerTermListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
		EventManager.inst().registerAspectChangeListener(this);

		ListSelectionModel select_model = getSelectionModel();
		select_model.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		select_model.addListSelectionListener(new TermSelectionListener (this));
		setSelectionModel(select_model);

		widths_initialized = false;
	}

	public void handleGeneSelectEvent (GeneSelectEvent e) {
		if (e.getGenes().size() > 0)
			setAnnotations((DisplayBioentity) e.getAncestor());
		else
			setAnnotations(null);
		clearSelection();
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
					col_width = fm.stringWidth(col_name) + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.REFERENCE_COL_NAME)) {
					col_width = fm.stringWidth("PUBMED:0000000000") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationsTableModel.WITH_COL_NAME)) {
					col_width = fm.stringWidth("XXXX0000000000") + insets.left + insets.right + 2;
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
			widths_initialized = true;
		}
	}

	/* MouseListener methods */
	public void mouseClicked(MouseEvent event) {
		Point point = event.getPoint();
		int row = rowAtPoint(point);
		if (row >= 0 && row <= assoc_model.getRowCount()) {
			int column = columnAtPoint(point);
			if (HyperlinkLabel.class == getModel().getColumnClass(column)) {
				GeneAnnotation assoc = ((AssociationsTableModel) getModel()).getEvidenceForRow(row);
				List<String> evi = assoc.getReferenceIds();
				String preferred_ref = HTMLUtil.getPMID(evi);
				if (preferred_ref.length() > 0) {
					String [] xref = preferred_ref.split(":");
					String text = HTMLUtil.getURL(xref[0], xref[1], false);
					HTMLUtil.bringUpInBrowser(text);
				}
			}
			else if (String.class == getModel().getColumnClass(column)) {
				// this event is handled on mouse press, not click (so menu will disappear)
			} else if (Boolean.class == getModel().getColumnClass(column)) {
				GeneAnnotation assoc = ((AssociationsTableModel) getModel()).getEvidenceForRow(row);
				Boolean value = (Boolean) getModel().getValueAt(row, column);
				boolean deletable = value.booleanValue();
				/*
				 * Removing of annotations is only permitted for ancestral nodes
				 * that have been directly annotated to that term by the curator,
				 * so check first before deleting a term
				 */
				if (deletable && assoc.isMRC()) {
					LogAction.undo(PaintManager.inst().getFamily(), assoc);
					String deleted_term = assoc.getCls();
					assoc_model.fireTableDataChanged();
					/**
					 * Now unselect this term, if it was selected.
					 */
					List<String> terms = EventManager.inst().getCurrentTermSelection();
					if (terms != null && terms.contains(deleted_term)) {
						terms.remove(deleted_term);
						TermSelectEvent term_event = new TermSelectEvent (this, terms);
						EventManager.inst().fireTermEvent(term_event);
					}
					// Notify listeners that the gene data has changed too
					EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
				} else if (deletable && assoc.isDirectNot()) {
					LogAction.undo(PaintManager.inst().getFamily(), assoc);
					EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
				}
			} else if (GeneAnnotation.class == getModel().getColumnClass(column)) {
				GeneAnnotation assoc = (GeneAnnotation) getModel().getValueAt(row, column);
				String term = assoc.getCls();
				String text = HTMLUtil.getURL("AMIGO", term, true);
				HTMLUtil.bringUpInBrowser(text);
			} else if (HashSet.class == getModel().getColumnClass(column)) {
				GeneAnnotation assoc = ((AssociationsTableModel) getModel()).getEvidenceForRow(row);
				Collection<String> withs = assoc.getWithInfos();
				if (withs != null && withs.size() == 1) {
					String [] xref = withs.iterator().next().split(":");
					String text = HTMLUtil.getURL(xref[0], xref[1], true);
					HTMLUtil.bringUpInBrowser(text);
				}
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {	
	}

	public void mousePressed(MouseEvent event) {		
		Point point = event.getPoint();
		int row = rowAtPoint(point);
		if (row >= 0 && row <= assoc_model.getRowCount()) {
			int column = columnAtPoint(point);
			if (String.class == getModel().getColumnClass(column)) {
				GeneAnnotation evi = ((AssociationsTableModel) getModel()).getEvidenceForRow(row);
				showFlowMenu(evi, row, point.x, point.y);
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) {		
	}

	private void refreshTermSelection(Collection<String> terms) {
		ListSelectionModel lsm = this.getSelectionModel();
		lsm.clearSelection();
		if (terms != null && !terms.isEmpty()) {
			for (String go_term : terms) {
				int row = assoc_model.getRowForTerm(go_term);
				lsm.addSelectionInterval(row, row);
			} 
		} else {
			clearSelection();
		}
	}

	private void showFlowMenu(GeneAnnotation assoc, int row, int x, int y) {
		JPopupMenu flow_menu = new JPopupMenu();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getDefault()); // local time
		String date_str;
		try {
			Date assoc_date;
			assoc_date = sdf.parse(assoc.getLastUpdateDate());
			SimpleDateFormat table_sdf = new SimpleDateFormat("d-MMM-yyyy");
			date_str = table_sdf.format(assoc_date);
		} catch (ParseException e) {
			log.info("Could not parse date from " + assoc.getLastUpdateDate());
			date_str = assoc.getLastUpdateDate();
		}

		String [] notStrings = Constant.Not_Strings;
		List<String> exclude_reasons = new ArrayList<>();
		org.paint.gui.tree.TreePanel tree = PaintManager.inst().getTree();
		Vector<Bioentity> leafList = new Vector<Bioentity>();
		tree.getLeafDescendants(node, leafList);

		if (AnnotationUtil.isExpAnnotation(assoc)) {
			flow_menu.add(date_str);			
		} else if (assoc.isMRC()) {
			flow_menu.add("MRCA: " + date_str);			
		} else if (assoc.isDirectNot()) {
			flow_menu.add("Negated MRCA: " + date_str);			
		} else {
			boolean validNot = true;			
			for (Bioentity leaf : leafList) {
				List<GeneAnnotation> leafAssocs = AnnotationUtil.getAspectExpAssociations(leaf, AspectSelector.inst().getAspectCode());
				if (leafAssocs != null) {
					for (GeneAnnotation leafAssoc : leafAssocs) {
						if (leafAssoc.getCls().equals(assoc.getCls()) && leafAssoc.isNegated()) {
							notStrings = Constant.Not_Strings_Ext;
						}
						else if (!leafAssoc.isNegated()) {
							if (leafAssoc.getCls().equals(assoc.getCls()) ||
									OWLutil.inst().moreSpecific(assoc.getCls(), leafAssoc.getCls())) {
								validNot = false;
								exclude_reasons.add(leaf.getId() + " has experimental annotation to " + 
								OWLutil.inst().getTermLabel(leafAssoc.getCls()) + " (" + leafAssoc.getCls() + ")");
								break;
							}
						}
					}
				}
			}
			if (validNot) {
				extendNotMenu(flow_menu, assoc, notStrings, row, x, y);
			} else {
				flow_menu.add("Can't negate because —");
				for (String reason : exclude_reasons) {
				flow_menu.add(reason);
				}
			}
		}
		flow_menu.show(this, x, y);
	}

	private void extendNotMenu(JPopupMenu flow_menu, GeneAnnotation assoc, String [] notStrings, int row, int x, int y) {
		ActionListener checker = new notActionListener(assoc, row, notStrings);
		JCheckBoxMenuItem [] qual_item = new JCheckBoxMenuItem[notStrings.length];
		for (int i = 0; i < notStrings.length; i++) {
			String not_str = notStrings[i];
			qual_item[i] = new JCheckBoxMenuItem(not_str);
			flow_menu.add(qual_item[i]);
			qual_item[i].addActionListener(checker);
		}
	}

	public class notActionListener implements ActionListener {

		private GeneAnnotation assoc;
		int row;
		String [] menu_str;

		public notActionListener(GeneAnnotation evidence, int row, String [] menu_str) {
			this.assoc = evidence;
			this.row = row;
			this.menu_str = menu_str;
		}

		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			String s = item.getText();
			String ev_code = Constant.NOT_QUALIFIERS_TO_EVIDENCE_CODES.get(s);
			PaintAction.inst().setNot(PaintManager.inst().getFamily(), node, assoc, ev_code, true);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
		}
	}

	public void handleTermEvent(TermSelectEvent e) {
		if (e.getSource().getClass() != this.getClass()) {
			is_adjusting = true;
			Bioentity mrca = EventManager.inst().getAncestralSelection();
			if (node != mrca) {
				setAnnotations(mrca);
			}
			List<String> term_selection = e.getTermSelection();
			if (term_selection != null) {
				refreshTermSelection(term_selection);
			}
			is_adjusting = false;
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		if (node != null && event.getSource() != null) {
			assoc_model.setNode(node);
			assoc_model.fireTableDataChanged();		
		}
	}

	class TermSelectionListener implements ListSelectionListener {
		AssociationsTable table;

		public TermSelectionListener (AssociationsTable table) {
			this.table = table;
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting() && !is_adjusting) { 
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				int min_index = lsm.getMinSelectionIndex();

				if (!lsm.isSelectionEmpty() && min_index >= 0) {
					// Find out which indexes are selected.
//					int max_index = lsm.getMaxSelectionIndex();
//					AssociationsTableModel assoc_model = (AssociationsTableModel) table.getModel();
//					List<Term> terms = new LinkedList<Term>();
//
//					String message = "Selecting: ";
//					for (int i = min_index; i <= max_index; i++) {
//						Term t = assoc_model.getTermForRow(i);
//						terms.add(t);
//						message = message + t.getName();
//					}
//					log.debug(message);
//					EventManager.inst().fireTermEvent(new TermSelectEvent(table, terms));
				}
			}
		}
	}

	public void handleAspectChangeEvent(AspectChangeEvent event) {
		repaint();
	}

}


