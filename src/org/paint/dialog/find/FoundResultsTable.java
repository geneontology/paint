package org.paint.dialog.find;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.util.OWLutil;
import org.paint.dialog.find.FindPanel.SEARCH_TYPE;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.TermSelectEvent;
import org.paint.main.PaintManager;

public class FoundResultsTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** populates table with results */

	private String [] columns = {"ID"};
	List<Bioentity> gene_results;
	List<String> term_results;
	MatchModel model;
	private SEARCH_TYPE search_type;	

	public FoundResultsTable() {
		super();
		model = new MatchModel();
		super.setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(new TableSelector());
	}

	public void setGeneResults(List<Bioentity> gene_results2) {
		if (search_type == SEARCH_TYPE.GENE) {
			this.gene_results = gene_results2;
			if (gene_results2 != null && gene_results2.size() > 0) {
				getSelectionModel().addSelectionInterval(0, 0);
				Bioentity node = gene_results2.get(0);
				ArrayList<Bioentity> selection = new ArrayList<> ();
				selection.add(node);
				PaintManager.inst().getTree().getDescendentList(node, selection);
				GeneSelectEvent event = new GeneSelectEvent(this, selection, node);
				EventManager.inst().fireGeneEvent(event);
			}
		}
		model.fireTableDataChanged();
	}

	public void setTermResults(List<String> term_results) {
		if (search_type == SEARCH_TYPE.TERM) {
			this.term_results = term_results;
			if (term_results != null && term_results.size() > 0) {
				setRowSelectionInterval(0, 0);
				String selected_term = term_results.get(0);
				TermSelectEvent term_event = new TermSelectEvent (this, selected_term);
				List<Bioentity> selection = EventManager.inst().fireTermEvent(term_event);	
				GeneSelectEvent gene_event = new GeneSelectEvent(this, selection, EventManager.inst().getAncestralSelection());
				EventManager.inst().fireGeneEvent(gene_event);
			}
		}
		model.fireTableDataChanged();
	}

	public void setType(SEARCH_TYPE search_type) {
		this.search_type = search_type;
		setGeneResults(gene_results);
		setTermResults(term_results);
	}

	private class TableSelector implements ListSelectionListener {

		public TableSelector() {
			super();
		}

		public void valueChanged(ListSelectionEvent e) {

			int row = getSelectedRow();
			if (row >= 0 && !e.getValueIsAdjusting() ) {
				if (search_type == SEARCH_TYPE.GENE) {
					Bioentity node = gene_results.get(row);
					ArrayList<Bioentity> selection = new ArrayList<> ();
					selection.add(node);
					PaintManager.inst().getTree().getDescendentList(node, selection);
					GeneSelectEvent event = new GeneSelectEvent(this, selection, node);
					EventManager.inst().fireGeneEvent(event);
					// zoom in on new selection (with some padding)
				} else if (search_type == SEARCH_TYPE.TERM) {
					String selected_term = term_results.get(row);
					TermSelectEvent term_event = new TermSelectEvent (this, selected_term);
					List<Bioentity> selection = EventManager.inst().fireTermEvent(term_event);	
					GeneSelectEvent gene_event = new GeneSelectEvent(this, selection, EventManager.inst().getAncestralSelection());
					EventManager.inst().fireGeneEvent(gene_event);
				}
			}
		}
	}

	/**
	 * This is the TableModel for the table 
	 * Takes a Vector of SequenceMatch in setData.
	 * Each SequenceMatch represents a row
	 */
	protected class MatchModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int getRowCount() {
			if (search_type == SEARCH_TYPE.GENE && gene_results != null) {
				return gene_results.size();
			}
			else if (search_type == SEARCH_TYPE.TERM && term_results != null) {
				return term_results.size();
			} else {
				return 0;
			}
		}

		public int getColumnCount() {
			return columns.length;
		}

		public String getColumnName(int column) {
			return (String) columns[column];
		}

		public Object getValueAt(int row, int column) {
			if (search_type == SEARCH_TYPE.GENE) {
				Bioentity match = gene_results.get(row);
				return match.getDBID();
			} else if (search_type == SEARCH_TYPE.TERM) {
				String match = term_results.get(row);
				return OWLutil.inst().getTermLabel(match);
			} else {
				return "";
			}
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, 0).getClass();
		}

	} // end GeneMatchModel inner class



}
