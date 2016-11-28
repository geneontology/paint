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

package org.paint.gui.matrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.phylo.annotate.PaintAction;
import org.bbop.phylo.annotate.WithEvidence;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.model.Tree;
import org.bbop.phylo.tracking.LogEntry;
import org.bbop.phylo.util.OWLutil;
import org.bbop.phylo.util.TaxonChecker;
import org.paint.dialog.AnnotUtil;
import org.paint.dialog.QualifierDialog;
import org.paint.dialog.TaxonDialog;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.ChallengeEvent;
import org.paint.gui.event.ChallengeListener;
import org.paint.gui.event.CurationColorEvent;
import org.paint.gui.event.CurationColorListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneDataEvent;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.tree.TreePanel;
import org.paint.main.PaintManager;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class AnnotMatrix extends JTable 
implements 
AnnotationChangeListener,
MouseListener, 
GeneSelectListener, 
NodeReorderListener, 
CurationColorListener,
TermSelectionListener, 
AspectChangeListener, 
ChallengeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MatrixHeaderRenderer header_renderer;
	private MatrixCellRenderer matrix_renderer;
	private AnnotationTransferHandler annot_handler;
	private int selectedColumn = -1;

	private Map<String, AnnotMatrixModel> models;

	private static Logger log = Logger.getLogger(AnnotMatrix.class);

	public AnnotMatrix() {
		super();

		setOpaque(true);
		setBackground(GuiConstant.BACKGROUND_COLOR);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(true);
		getTableHeader().setReorderingAllowed(false);
		setAutoCreateColumnsFromModel(false);

		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));

		this.addMouseListener(this);
		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerCurationColorListener(this);
		manager.registerNodeReorderListener(this);
		manager.registerTermListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerGeneAnnotationChangeListener(this);
		manager.registerChallengeListener(this);

		setFont(GuiConstant.DEFAULT_FONT);

		setDragEnabled(true);
		annot_handler = new AnnotationTransferHandler();
		setTransferHandler(annot_handler);

		//single cell selection
		setRowMargin(0);

		matrix_renderer = new MatrixCellRenderer();
		setDefaultRenderer(GeneAnnotation.class, matrix_renderer);
		header_renderer = new MatrixHeaderRenderer(getTableHeader());
	}

	public void setModels(List<Bioentity> list) {
		if (models == null) {
			models = new HashMap<String, AnnotMatrixModel>();
		}
		models.clear();
		AnnotMatrixModel annot_model;
		annot_model = new AnnotMatrixModel(list, AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString());
		models.put(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString(), annot_model);
		annot_model = new AnnotMatrixModel(list, AspectSelector.Aspect.CELLULAR_COMPONENT.toString());
		models.put(AspectSelector.Aspect.CELLULAR_COMPONENT.toString(), annot_model);
		annot_model = new AnnotMatrixModel(list, AspectSelector.Aspect.MOLECULAR_FUNCTION.toString());
		models.put(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString(), annot_model);

		String go_aspect = AspectSelector.inst().getAspectName();
		AnnotMatrixModel matrix = models.get(go_aspect);
		setModel(matrix);
		setSelectedColumn(0);
		revalidate();
		System.gc();
	}

	public Map<String, AnnotMatrixModel> getModels() {
		return models;
	}

	// Set the text and icon values on the second column for the icon render
	public void setModel(AnnotMatrixModel model) {
		if (model != null) {
			super.setModel(model);

			int columns = model.getColumnCount();
			TableColumnModel column_model = new DefaultTableColumnModel();
			for (int i = 0; i < columns; i++) {
				TableColumn col = new TableColumn(i);
				col.setPreferredWidth(12);
				col.setMaxWidth(12);
				col.setHeaderRenderer(header_renderer);
				col.setResizable(false);
				column_model.addColumn(col);
			}
			setDefaultRenderer(GeneAnnotation.class, matrix_renderer);
			setColumnModel(column_model);
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		int pad = FamilyViews.inst().getBottomMargin(FamilyViews.MATRIX_PANE);
		d.height += pad;
		return d;
	}

	/*
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 */
	@Override
	public int getEditingRow(){
		return (getColumnClass(editingColumn) == AnnotMatrixModel.class) ? -1 : editingRow;
	}

	public void handleGeneSelectEvent (GeneSelectEvent e) {
		if (e.getGenes() == null) {
			log.debug("AnnotationTable: wierd, have a null gene selected");
		}
		else if (e.getSource() != this) {
			// not sure if this is the correct way to proceed
			if (getModel() instanceof AnnotMatrixModel) {
				AnnotMatrixModel genes = (AnnotMatrixModel) this.getModel();
				int total = genes.getRowCount();
				if (total > 0) {
					ListSelectionModel lsm = this.getSelectionModel();
					lsm.clearSelection();
					List<Bioentity> selection = e.getGenes();
					if (selection != null && !selection.isEmpty()) {
						Bioentity node = e.getAncestor();
						TreePanel tree = PaintManager.inst().getTree();		
						setSelectedRows(lsm, tree, node);
					}
					repaint();
				}
			}
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		DisplayBioentity node = (DisplayBioentity) event.getSource();
		String class_name = getModel().getClass().getName();
		if (class_name.equals("org.paint.gui.matrix.AnnotMatrixModel")) {
			AnnotMatrixModel model = (AnnotMatrixModel) getModel();
			model.resetAssoc(node);
			model.fireTableDataChanged();
		}
	}

	public void handleChallengeEvent(ChallengeEvent event) {
		String aspect_name = (String) event.getSource();
		List<Bioentity> orderedNodes = PaintManager.inst().getTree().getTerminusNodes();
		AnnotMatrixModel annot_model = new AnnotMatrixModel(orderedNodes, aspect_name);
		models.put(aspect_name, annot_model);
		setModel(annot_model);
		annot_model.fireTableStructureChanged();
		//		repaint();
	}

	public void handleTermEvent(TermSelectEvent e) {
		if (e.getSource() != this) {
			List<Bioentity> genes = PaintManager.inst().getTree().getTerminusNodes();
			int total = genes.size();
			if (total > 0) {
				ListSelectionModel lsm = this.getSelectionModel();
				lsm.clearSelection();
				for (int i = 0; i < total; i++) {
					DisplayBioentity node = (DisplayBioentity) genes.get(i);
					if (node.isSelected()) {
						lsm.addSelectionInterval(i, i);
					}
				}
				AnnotMatrixModel model = (AnnotMatrixModel) getModel();
				String term = e.getSelectedTerm();
				int column = model.getTermColumn(term);
				setSelectedColumn(column);
			}
		}
	}

	// Assumes table is contained in a JScrollPane. Scrolls the 
	// cell (rowIndex, vColIndex) so that it is visible within the viewport. 
	public void scrollToColumn(int colIndex) {
		Rectangle row_rect = getCellRect(0, colIndex, true); 
		scrollToVisible(row_rect);
	}

	private void scrollToVisible(Rectangle rect) {
		if (!(getParent() instanceof JViewport)) { 
			return; 
		} 
		JViewport viewport = (JViewport)getParent(); 
		// This rectangle is relative to the table where the 
		// northwest corner of cell (0,0) is always (0,0).

		// These are actual screen coordinates, not just relative to the matrix itself NOT
		Rectangle visible = viewport.getViewRect();

		if (visible.x <= rect.x && (visible.x + visible.width) >= (rect.x + rect.width))
			return;

		int focal_x = Math.max(0, rect.x - (visible.width / 2));
		Point point_of_view = new Point(focal_x, visible.y);
		//		log.info("Scrolling to pixel position " + point_of_view.x);

		// Scroll the area into view, upper left hand part.
		viewport.setViewPosition(point_of_view);		
		repaint();

	}

	public void handleSubFamilyEvent (GeneDataEvent e) {
		AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
		model.fireTableDataChanged();
		repaint();
	}

	public void handleAspectChangeEvent(AspectChangeEvent event) {
		AnnotMatrixModel matrix = null;
		if (models != null) {
			String go_aspect = AspectSelector.inst().getAspectName();
			matrix = models.get(go_aspect);
			String term = EventManager.inst().getCurrentTermSelection();
			if (term != null) {
				String term_aspect = OWLutil.inst().getAspect(term);
				AspectSelector aspect_setter = AspectSelector.inst();
				String current = aspect_setter.getAspectCode();
				if (!term_aspect.equals(current)) {
					selectedColumn = 0;
				} else {
					selectedColumn = matrix.getTermColumn(term);
				}
			}
		}
		setModel(matrix);
		revalidate();
		repaint();
	}

	public void handleNodeReorderEvent(NodeReorderEvent e) {
		if (models != null && models.size() > 0) {
			Set<String> aspects = models.keySet();
			for (String aspect : aspects) {
				AnnotMatrixModel model = models.get(aspect);
				model.modifyRows(e.getNodes());
			}
			String go_aspect = AspectSelector.inst().getAspectName();
			setModel(models.get(go_aspect));
			revalidate();
			repaint();
		}
	}

	protected void setSelectedRows(ListSelectionModel lsm, TreePanel tree, Bioentity node) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		if (((DisplayBioentity) node).isExpanded() && !node.isPruned()) {
			Bioentity low_gene = tree.getTopLeafNode(node);
			int low_row = model.getRow(low_gene);
			Bioentity high_gene = tree.getBottomLeafNode(node);
			int high_row = model.getRow(high_gene);
			for (int row = low_row; row <= high_row; row++)
				updateRow(row);
		} else {
			int row = model.getRow(node);
			updateRow(row);
		}
	}

	private void updateRows(List<Bioentity> genes) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		for (Bioentity gene : genes) {
			int row = model.getRow(gene);
			updateRow(row);
		}
	}

	private void updateRow (int row) {
		AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
		for (int col = 0; col < this.getColumnCount() && row >= 0; col++) {
			model.fireTableCellUpdated(row, col);
		}		
	}

	public int getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(int col) {
		updateColumn(selectedColumn);
		selectedColumn = col;
		updateColumn(selectedColumn);
		scrollToColumn(selectedColumn); 
	}

	private void updateColumn (int col) {
		for (int row = 0; row < getRowCount() && col >= 0; row++) {
			AnnotMatrixModel model = (AnnotMatrixModel) getModel();
			model.fireTableCellUpdated(row, col);
		}		
	}

	public Color getRowForegroundColor(int row) {
		Bioentity node = ((AnnotMatrixModel) this.getModel()).getNode(row);
		if (null == node) {
			return null;
		}
		return ((DisplayBioentity) node).getSubFamilyColor();
	}

	public void mouseClicked(MouseEvent event) {
	}

	public void mouseEntered(MouseEvent arg0) {		
	}

	public void mouseExited(MouseEvent arg0) {		
	}

	public void mousePressed(MouseEvent event) {		
		Point point = event.getPoint();
		int row = rowAtPoint(point);
		if (row >= 0 && row < getRowCount()) {
			int modifiers = event.getModifiers();
			/* 
			 * Left mouse button selects the column/term and all genes annotated to that term
			 */
			if ((InputEvent.BUTTON1_MASK == (modifiers & InputEvent.BUTTON1_MASK)) &&
					(!event.isMetaDown() && !event.isShiftDown() && !event.isAltDown() && !event.isControlDown())) {
				String term = getTermAtPoint(event.getPoint());
				if (term != null) {
					if (getModel() instanceof AnnotMatrixModel) {
						AnnotMatrixModel genes = (AnnotMatrixModel) this.getModel();
						Bioentity selected_gene = genes.getNode(row);
						List<Bioentity> previous_genes = EventManager.inst().getCurrentGeneSelection();	
						TermSelectEvent term_event = new TermSelectEvent (this, term, false);
						EventManager.inst().fireTermEvent(term_event);
						setSelectedColumn(columnAtPoint(event.getPoint()));
						boolean change = !(previous_genes.isEmpty() && selected_gene == null) ||
								(!previous_genes.isEmpty() && selected_gene == null) ||
								(previous_genes.isEmpty() && selected_gene != null) ||
								(previous_genes.size() == 1 && selected_gene != null && 
								 previous_genes.contains(selected_gene));
						if (change) {
							if (!previous_genes.isEmpty()) {
								for (Bioentity node : previous_genes) {
									((DisplayBioentity) node).setSelected(false);
								}
								updateRows(previous_genes);
							}
							if (selected_gene != null) {
								((DisplayBioentity) selected_gene).setSelected(true);
								updateRow(row);
								List<Bioentity> selected_genes = new ArrayList<>();
								selected_genes.add(selected_gene);
								GeneSelectEvent ge = new GeneSelectEvent (this, selected_genes, selected_gene);
								EventManager.inst().fireGeneEvent(ge);
							}
							annot_handler.exportAsDrag(this, event, TransferHandler.COPY);
						}
					}
				}
			}
			/*
			 * Right mouse button or left mouse button and the meta-key then show popup to make an annotation
			 */
			else if (InputEvent.BUTTON3_MASK == (modifiers & InputEvent.BUTTON3_MASK) || 
					((InputEvent.BUTTON1_MASK == (modifiers & InputEvent.BUTTON1_MASK)) && 
							(event.isMetaDown()))) {
				JPopupMenu popup = createPopupMenu(event);
				if (popup != null) {
					TreePanel tree = PaintManager.inst().getTree();
					Bioentity gene = EventManager.inst().getCurrentSelectedNode();
					int y = tree.scrollToNode(gene);
					if (y < 0)
						y = event.getY();
					RenderUtil.showPopup(popup, event.getComponent(), new Point(event.getX(), y));
				}
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void handleCurationColorEvent(CurationColorEvent e) {
		repaint();
	}

	protected void modifyColumns(String added_term [], boolean remove) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		boolean modified = model.modifyColumns(added_term, remove);
		if (modified) {
			setModel(model);
			if (remove)
				setSelectedColumn(model.getTermColumn(added_term[AnnotMatrixModel.NARROWER]));
			else
				setSelectedColumn(model.getTermColumn(added_term[AnnotMatrixModel.BROADER]));
			repaint();
		}
	}

	public void addAnnotatedColumn(String term, List<String> with_terms) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		List<String> terms = model.getTermList();
		if (!terms.contains(term)) {
			// sigh
			String [] origin = new String[2];
			boolean found = false;
			for (int i = 0; i < with_terms.size() && !found; i++) {
				String original = with_terms.get(i);
				if (terms.contains(original)) {
					found = true;
					origin[AnnotMatrixModel.BROADER] = term;
					origin[AnnotMatrixModel.NARROWER] = original;
					modifyColumns (origin, true);
				}
			}
			if (!found)
				log.debug("Annotated to " + term + ", but can't find original evidence");
		}
	}

	private JPopupMenu createPopupMenu(MouseEvent e) {
		JPopupMenu  popup = null;
		Bioentity ancestor = EventManager.inst().getCurrentSelectedNode();	
		String term = getTermAtPoint(e.getPoint());
		Tree tree = PaintManager.inst().getTree().getTreeModel();
		if (ancestor != null && !ancestor.isLeaf() && !ancestor.isPruned() && term != null) {
			popup = new JPopupMenu();
			LogEntry.LOG_ENTRY_TYPE because = PaintAction.inst().isValidTerm(term, ancestor, tree);
			if (because != null)  {
				String invalid_item;
				if (because != LogEntry.LOG_ENTRY_TYPE.WRONG_TAXA)
					invalid_item = "Annotation of " + ancestor.getSymbol() + " to " + OWLutil.inst().getTermLabel(term) + " " + because;
				else
					invalid_item = TaxonChecker.getTaxonError();
				popup.add(new JMenuItem(invalid_item));                            
			} else {
				JMenuItem menuItem;
				menuItem = new JMenuItem("Annotate " + ancestor.getSymbol() + " to " + OWLutil.inst().getTermLabel(term));
				menuItem.addActionListener(new AnnotateActionListener(ancestor, term));
				popup.add(menuItem);                            
			}
		}
		return popup;
	}

	private class AnnotateActionListener implements ActionListener{
		Bioentity  ancestor;
		String term;

		AnnotateActionListener(Bioentity node, String term){
			this.ancestor = node;
			this.term = term;
		}

		public void actionPerformed(ActionEvent e){
			Tree tree = PaintManager.inst().getTree().getTreeModel();
			AnnotUtil.inst().propagateAssociation(ancestor, tree, term, null);
//			boolean valid_for_all_descendents = TaxonChecker.checkTaxons(tree, ancestor, term, false);
//			if (!valid_for_all_descendents) {
//				List<String> invalid_taxa = TaxonChecker.getInvalidTaxa(ancestor, term);
//				TaxonDialog taxon_dialog = new TaxonDialog(GUIManager.getManager().getFrame(), term, invalid_taxa);
//				valid_for_all_descendents = taxon_dialog.isLost();
//			}
//			if (valid_for_all_descendents) {
//				WithEvidence withs = new WithEvidence(tree, ancestor, term);
//				int qualifiers = withs.getWithQualifiers();
//				if (qualifiers > 0) {
//					QualifierDialog qual_dialog = new QualifierDialog(GUIManager.getManager().getFrame(), qualifiers);
//					qualifiers = qual_dialog.getQualifiers();
//				}
//				PaintAction.inst().propagateAssociation(PaintManager.inst().getFamily(), ancestor, term, withs, null, qualifiers);
//				EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(ancestor));
//			}
		}

	}

	private String getTermAtPoint(Point point) {
		int column = columnAtPoint(point);
		String term = ((AnnotMatrixModel)getModel()).getTermForColumn(column);
		return term;
	}
}