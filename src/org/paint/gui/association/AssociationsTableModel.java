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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.util.OWLutil;
import org.bbop.swing.HyperlinkLabel;
import org.paint.gui.PaintTable;
import org.paint.gui.event.TermHyperlinkListener;
import org.paint.util.HTMLUtil;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;


public class AssociationsTableModel extends AbstractTableModel 
implements PaintTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final String TERM_COL_NAME = "Term";
	protected static final String CODE_COL_NAME = "ECO";
	protected static final String REFERENCE_COL_NAME= "Reference";
	protected static final String WITH_COL_NAME = "With";
	protected static final String TRASH_COL_NAME = "DEL";

	protected static final String[] column_headings = {
		CODE_COL_NAME, 
		TERM_COL_NAME, 
		REFERENCE_COL_NAME,
		WITH_COL_NAME,
		TRASH_COL_NAME
	};

	protected Bioentity node;
	protected ArrayList<GeneAnnotation> associations;
	protected HashMap<GeneAnnotation, HyperlinkLabel> pub_labels;
	protected HashMap<GeneAnnotation, Set<HyperlinkLabel>> with_labels;

	protected static Logger log = Logger.getLogger(AssociationsTableModel.class);

	public AssociationsTableModel() {
		associations = new ArrayList<GeneAnnotation> ();
		pub_labels = new HashMap<GeneAnnotation, HyperlinkLabel> ();
		with_labels = new HashMap<GeneAnnotation, Set<HyperlinkLabel>> ();
	}

	public void setNode(Bioentity gene) {
		this.node = gene;
		associations.clear();
		pub_labels.clear();
		with_labels.clear();
		if (node != null) {
			Collection<GeneAnnotation> all_associations = node.getAnnotations();
			if (all_associations != null) {
				for (GeneAnnotation assoc : all_associations) {
					String code = assoc.getShortEvidence();
					if (code == null) {
						log.debug("How did " + node + " association to " + assoc.getCls() + " lose its evidence code!!");
					}
					if (!node.isLeaf() || (node.isLeaf() && !code.equals("ND") && !code.equals("IEA"))) {
						associations.add(assoc);
					} else if (code.equals("ND")) {
						log.info(node.getSeqId() + " has ND to term " + assoc.getCls());
					}
				}
			}
		}

		/*
		 * Important not to create the table until the evidence is sorted
		 */
		sort();
		for (GeneAnnotation assoc : associations) {
			/*
			 * This should be the link to the publication record
			 */
			HyperlinkLabel field = new HyperlinkLabel();
			field.setEnabled(true);
			field.addHyperlinkListener(new TermHyperlinkListener());
			List<String> xrefs = assoc.getReferenceIds();
			if (xrefs.size() > 0) {
				String[] xref = xrefs.get(0).split(":");
				String xref_text = HTMLUtil.getHTML(xref[0], xref[1], false);
				field.setText(xref_text);
				pub_labels.put(assoc, field);
			}
			Collection<String> withs = assoc.getWithInfos();
			field = new HyperlinkLabel();
			field.setEnabled(true);
			field.addHyperlinkListener(new TermHyperlinkListener());
			String with_text;
			if (withs != null && !withs.isEmpty()) {
				if (withs.size() == 1) {
					String[] with = withs.iterator().next().split(":");
					with_text = HTMLUtil.getHTML(with[0], with[1], true);
				} 
				else {
					with_text = HTMLUtil.HTML_TEXT_BEGIN+withs.toString()+HTMLUtil.HTML_TEXT_END;
				}
			}
			else {
				with_text = HTMLUtil.HTML_TEXT_BEGIN+HTMLUtil.HTML_TEXT_END;
			}
			field.setText(with_text);
			Set<HyperlinkLabel> with_links = new HashSet<HyperlinkLabel>();
			with_links.add(field);					
			with_labels.put(assoc, with_links);
		}
	}

	public void removeAssociation(GeneAnnotation assoc) {
		for (int i = associations.size() - 1; i >= 0; i--) {
			GeneAnnotation table_evi = associations.get(i);
			if (table_evi.equals(assoc)) {
				pub_labels.remove(table_evi);
				with_labels.remove(table_evi);
				associations.remove(i);
			}
		}
	}

	public String getTextAt(int row, int column) {
		if (getRowCount() == 0) {
			return null;
		}
		String tag = column_headings[column];
		GeneAnnotation association = associations.get(row);

		if (tag.equals(CODE_COL_NAME)) {
			return CODE_COL_NAME;
		} else if (tag.equals(TERM_COL_NAME)) {
			return OWLutil.inst().getTermLabel(association.getCls());
		} else if (tag.equals(REFERENCE_COL_NAME)) {
			String xref_text = association.getReferenceIds().toString();
			return xref_text;
		} else if (tag.equals(TRASH_COL_NAME)) {
			return TRASH_COL_NAME;
		} else if (tag.equals(WITH_COL_NAME)) {
			return association.getWithInfos().toString();
		} else {
			return "";
		}
	}

	protected int getRowForTerm(String term) {
		for (int row = 0; row < associations.size(); row++) {
			GeneAnnotation evi = associations.get(row);
			if (evi.getCls().equals(term))
//					|| TermUtil.isDescendant(potentialAncestor, potentialDescendant))
				return row;
		}
		return -1;
	}

	public String getTermForRow(int row) {
		GeneAnnotation association = associations.get(row);
		return association.getCls();
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	@Override
	public Class getColumnClass(int columnIndex) {
		Class check = null;
		Object value = getValueAt(0, columnIndex);
		if (value != null) {
			check = value.getClass();
			if (check == null) {
				System.out.println("Table returning null for column " + columnIndex);
			}
		} else {
			String tag = column_headings[columnIndex];
			if (tag.equals(CODE_COL_NAME)) {
				check = String.class;
			} else if (tag.equals(TERM_COL_NAME)) {
				// this is the column with the term in it
				check = GeneAnnotation.class;
			} else if (tag.equals(REFERENCE_COL_NAME)) {
				// this is the published reference for the annotation
				check = HyperlinkLabel.class;
			} else if (tag.equals(TRASH_COL_NAME)) {
				// the evidence code
				check = Boolean.class;
			} else if (tag.equals(WITH_COL_NAME)) {
				// and what (if appropriate) the inference was based on, e.g. another sequence or an interpro domain
				check = Set.class;
			}
		}
		return check;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return column_headings[columnIndex];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (null == associations || rowIndex >= getRowCount() || rowIndex < 0) {
			return null;
		}
		GeneAnnotation assoc = associations.get(rowIndex);
		String tag = column_headings[columnIndex];
		if (tag.equals(CODE_COL_NAME)) {
			return assoc.getShortEvidence();
		} else if (tag.equals(TERM_COL_NAME)) {
			// this is the column with the term in it
			return assoc;
		} else if (tag.equals(REFERENCE_COL_NAME)) {
			// this is the published reference for the annotation
			// It is of type Hyperlink.class
			return pub_labels.get(assoc);
		} else if (tag.equals(TRASH_COL_NAME)) {
			// whether or not the annotation was done in PAINT
			if (AnnotationUtil.isPAINTAnnotation(assoc)) {
				if (assoc.isMRC() || assoc.isDirectNot()) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
				}
			} else {
				return Boolean.FALSE;
			}
		} else if (tag.equals(WITH_COL_NAME)) {
			// and what (if appropriate) the inference was based on, e.g. another sequence or an interpro domain
			//			Set<DBXref> withs = evi.getWiths();
			return with_labels.get(assoc);
			//			return withs;
		} else {
			return null;
		}
	}

	public int getColumnCount() {
		return column_headings.length;
	}

	public int getRowCount() {
		return associations.size();
	}

	public GeneAnnotation getEvidenceForRow(int row) {
		return associations.get(row);
	}
	
	public boolean isCellEditable(int rowIndex, int colIndex) {
		Object cell = getValueAt(rowIndex, colIndex);
		if (cell != null && cell instanceof Boolean) {
			return true;
		}
		return false;
	}

	private void sort() {
		Collections.sort(associations, new EvidenceComparator());
	}

	/**
	 * All paint tables must implement this method so that the column width utility can be used
	 * 
	 */
	public boolean isSquare(int column) {
		return getColumnName(column).equals(TRASH_COL_NAME);
	}
}
