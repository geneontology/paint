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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.util.OWLutil;
import org.paint.config.CustomTermList;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;

public class AnnotMatrixModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Bioentity> nodes;
	private Map<String, ColumnTermData> term2menu;
	private List<String> term_list;
	private List<String[]> added_term_list;

	protected static final int BROADER = 0;
	protected static final int NARROWER = 1;

	private AssociationData [][]associationMatrix;

	private String aspect_name;

	private static Logger log = Logger.getLogger(AnnotMatrixModel.class);

	public AnnotMatrixModel(List<Bioentity> orderedNodes, String aspect_name) {

		this.aspect_name = aspect_name;

		nodes = new ArrayList<Bioentity>();
		term_list = new ArrayList<String> ();
		added_term_list = new ArrayList<String []> ();	
		term2menu = new HashMap<String, ColumnTermData> ();

		/* get the table headers from the from the gene experimental annotations */
		if (orderedNodes != null) {
			nodes.clear();
			nodes.addAll(orderedNodes);
		}

		List<String> exp_list = possibleTerms();		
		initTerms(exp_list, added_term_list);
	}

	private List<String> possibleTerms() {
		List<String> exp_list = new ArrayList<String> ();
		Set<String> exclusionTerms = CustomTermList.inst().getExclusionList();
		for (Bioentity node : nodes) {
			if (node.getId().contains("S000002218")) {
				log.debug("weird yeast gene");
			}
			List<GeneAnnotation> assocs = AnnotationUtil.getAspectExpAssociations(node, AspectSelector.inst().getAspectCode(aspect_name));
			// need to see how this matches up with "do not use for annotation"
			if (assocs != null) {
				for (GeneAnnotation assoc : assocs) {
					String term = assoc.getCls();
					if (!exp_list.contains(term) && !exclusionTerms.contains(term)) {
						exp_list.add(term);
					}
				}
			}
		}
		return exp_list;
	}

	protected void initTerms(List<String> exp_list, List<String []> added_term_list) {
		term_list.clear();
		term2menu.clear();
		List<String> temp_list = new ArrayList<String> ();

		if (nodes != null) {
			if (!added_term_list.isEmpty()) {
				// Make a note of any terms that have already been added to a term menu
				for (String [] added_term : added_term_list) {
					temp_list.add(added_term[BROADER]);
				}
			}
			temp_list.addAll(exp_list);
			associationMatrix = new AssociationData[nodes.size()][temp_list.size()];
			sortTerms(nodes, temp_list);
		}

		for (String t : term_list) {
			ColumnTermData td = term2menu.get(t);
			for (String [] added_term : added_term_list) {
				if (added_term[BROADER].equals(t))
					td.setNarrowTerm(added_term[NARROWER]);
			}
			td.initTermMenu(t, term_list);
		}
		fireTableDataChanged();
	}

	private void sortTerms(List<Bioentity> nodes, List<String> temp_list) {
		/* 
		 * This sort
		 * 	Puts all cellular processes first
		 *  Sorts by number of annotated genes
		 *  Integrates child terms
		 *  Appends singlets alphabetically
		 * 
		 */
		List<String> cellular_list = new ArrayList<String> ();

		/* Hack alert - hard coded the GO id for cellular process here */
		String cellular = "GO:0009987";
		for (String term : temp_list) {
			if (OWLutil.inst().moreSpecific(term, cellular)) {
				cellular_list.add(term);
			}
		}

		for (String term : cellular_list) {
			temp_list.remove(term);
		}

		/*
		 * First an alphabetic sort
		 */
		Collections.sort(cellular_list, new TermComparator());
		Collections.sort(temp_list, new TermComparator());
		/*
		 * Then sort by the number of genes annotated to each term
		 * The more genes annotated the higher in the list the term will be
		 */
		Collections.sort(cellular_list, new TermCountComparator(nodes));
		Collections.sort(temp_list, new TermCountComparator(nodes));
		/*
		 * But then insert the related terms immediately after their child
		 */
		boolean odd_column = true;
		odd_column = groupParentTerms(cellular_list, odd_column);
		groupParentTerms(temp_list, odd_column);
	}

	private boolean groupParentTerms(List<String> orig_termlist, boolean odd_column) {
		while (orig_termlist.size() > 0) {
			String cur_term = orig_termlist.remove(0);
			term_list.add(cur_term);
			ColumnTermData td = new ColumnTermData();
			td.setOddColumn(odd_column);
			term2menu.put(cur_term, td);
			for (int i = 0; i < orig_termlist.size();) {
				String other_term = orig_termlist.get(i);
				if (OWLutil.inst().moreSpecific(other_term, cur_term) || OWLutil.inst().moreSpecific(cur_term, other_term)) {
					orig_termlist.remove(i);
					term_list.add(other_term);
					td = new ColumnTermData();
					td.setOddColumn(odd_column);
					term2menu.put(other_term, td);
				} else {
					i++;
					odd_column = !odd_column;
				}
			}
		}
		return !odd_column;
	}

	protected boolean modifyColumns(String added_term[], boolean remove) {
		boolean modify = false;
		if (remove) {
			for (int i = 0; i < added_term_list.size() && !modify; i++) {
				String [] check = added_term_list.get(i);
				if (check[BROADER].equals(added_term[BROADER]) && check[NARROWER].equals(added_term[NARROWER])) {
					added_term_list.remove(i);
					modify = true;
				}
			}
		} else {
			modify = true;
			for (int i = 0; i < added_term_list.size() && modify; i++) {
				String [] check = added_term_list.get(i);
				modify &= !(check[BROADER].equals(added_term[BROADER]) 
						&& check[NARROWER].equals(added_term[NARROWER]));
			}
			if (modify) 
				added_term_list.add(added_term);
		}
		if (modify)
			initTerms(possibleTerms(), added_term_list);
		return modify;
	}

	protected void modifyRows(List<Bioentity> orderedNodes) {
		nodes.clear();
		/* get the table headers from the from the gene experimental annotations */
		if (orderedNodes != null) {
			nodes.clear();
			nodes.addAll(orderedNodes);
		}

		/* 
		 * Need to save this, but possibly may need to trim it down
		 * if the relevant narrower terms are no longer included for these nodes
		 */
		List<String []> allowable_terms = new ArrayList<String []> ();
		List<String> exp_terms = possibleTerms();
		for (String [] check : added_term_list) {
			boolean allowable = false;
			for (int i = 0; i < exp_terms.size() && !allowable; i++) {
				String exp_term = exp_terms.get(i);
				if (check[NARROWER].equals(exp_term)
						&& !exp_terms.contains(check[BROADER]) 
						&& !allowable_terms.contains(check)) {
					allowable_terms.add(check);
					allowable = true;
				}
			}
		}
		initTerms(exp_terms, allowable_terms);
	}

	public int getColumnCount() {
		return term_list.size();
	}

	protected List<String> getTermList() {
		return term_list;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return GeneAnnotation.class;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String name = OWLutil.inst().getTermLabel(term_list.get(columnIndex));
		return name;
	}

	public int getRowCount() {
		return nodes.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (nodes == null || term2menu == null) {
			return null;
		}
		if ((rowIndex > (nodes.size() - 1)) || (columnIndex >= (term2menu.size()))) {
			return null;
		}
		AssociationData data = associationMatrix[rowIndex][columnIndex];
		if (data == null) {
			String term = term_list.get(columnIndex);
			Bioentity node = nodes.get(rowIndex);
			data = this.getCellAssoc(term, node);
			associationMatrix[rowIndex][columnIndex] = data;
		}
		return data;
	}

	private AssociationData getCellAssoc(String term, Bioentity node) {
		List<GeneAnnotation> self = new ArrayList<GeneAnnotation>();
		List<GeneAnnotation> broader_terms = new ArrayList<GeneAnnotation>();
		List<GeneAnnotation> narrower_terms = new ArrayList<GeneAnnotation>();
		AssociationData cell;

		/*
		 * First look only at experimental annotations
		 */
		getRelatedAssociationsToTerm(node, term, self, broader_terms, narrower_terms, true);
		cell = annotationToTerm(self);
		if (cell == null) {
			cell = annotationToFinerTerm(broader_terms);
		}
		if (cell == null) {
			cell = annotationToCoarserTerm(narrower_terms);
		}
		/* 
		 * If no experimental annotations to the term check for PAINT annotations
		 */
		if (cell == null) {
			self.clear();
			broader_terms.clear();
			narrower_terms.clear();
			getRelatedAssociationsToTerm(node, term, self, broader_terms, narrower_terms, false);
			cell = annotationToTerm(self);
			if (cell == null) {
				cell = annotationToFinerTerm(broader_terms);
			}
			if (cell == null) {
				cell = annotationToCoarserTerm(narrower_terms);
			}
		}
		if (cell == null)
			cell = new AssociationData(null, false, false);
		return cell;
	}

	private AssociationData annotationToTerm(List<GeneAnnotation> self) {
		GeneAnnotation assoc = null;
		boolean isNot = false;
		if (!self.isEmpty()) {
			assoc = self.get(0);
			for (GeneAnnotation a : self) {
				isNot |= a.isNegated();
				if (a.isNegated())
					assoc = a;
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, false, isNot));
		else
			return null;
	}

	private AssociationData annotationToFinerTerm(List<GeneAnnotation> descendants) {
		GeneAnnotation assoc = null;
		boolean isNot = false;
		if (!descendants.isEmpty()) {
			for (GeneAnnotation a : descendants) {
				isNot |= a.isNegated();
				if (a.isNegated() && assoc == null) {
					assoc = a;
				}
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, true, isNot));
		else
			return null;
	}

	private AssociationData annotationToCoarserTerm(List<GeneAnnotation> ancestors) {
		GeneAnnotation assoc = null;
		if (!ancestors.isEmpty()) {
			for (GeneAnnotation a : ancestors) {
				if (!a.isNegated()) {
					assoc = a;
				}
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, true, false));
		else
			return null;
	}

	private void getRelatedAssociationsToTerm(Bioentity node, String term, List<GeneAnnotation> self,
			List<GeneAnnotation> broader_terms, List<GeneAnnotation> narrower_terms, boolean experimentalOnly) {
		List<GeneAnnotation> associations = node.getAnnotations();
		for (GeneAnnotation assoc : associations) {
			String assoc_term = assoc.getCls();
			if (term.equals(assoc_term)) {
				self.add(assoc);
			}
			/*
			 * Is term4column a parent term of annotated2term?
			 */
			else if (OWLutil.inst().moreSpecific(assoc_term, term)) {
				if (experimentalOnly) {
					if (AnnotationUtil.isExpAnnotation(assoc)) {
						narrower_terms.add(assoc);
					}
				}
				else {
					narrower_terms.add(assoc);
				}
			}
			/*
			 * Conversely is term4column a child term of annotated2term
			 */
			else if (OWLutil.inst().moreSpecific(term, assoc_term)) {
				if (experimentalOnly) {
					if (AnnotationUtil.isExpAnnotation(assoc)) {
						broader_terms.add(assoc);
					}
				}
				else {
					broader_terms.add(assoc);
				}
			}
		}	
	}

	public void resetAssoc(Bioentity parent) {
		List<Bioentity> leaves = new ArrayList<Bioentity>();
		parent.getTermini(leaves);
		if (leaves == null || leaves.size() == 0) {
			return;
		}
		int offset = getRow(leaves.get(0));
		for (int i = 0; i < leaves.size(); i++) {
			Bioentity node = leaves.get(i);
			if (nodes.contains(node)) {
				int j = 0;
				for (String term : term_list) {
					AssociationData data = this.getCellAssoc(term, node);
					associationMatrix[offset + i][j++] = data;
				}
			}
		}
	}

	public void resetHiddenRows() {
		for (int i = 0; i < nodes.size(); i++) {
			Bioentity row = nodes.get(i);
			((DisplayBioentity) row).setVisible(false);
		}
	}

	public int getRow(Bioentity gene) {
		try {
			return nodes.indexOf(gene);
		} catch (NullPointerException e) {
			log.debug("Could not find gene " + gene.getSeqId() + " in contents");
			return -1;
		}
	}

	public Bioentity getNode(int row) {
		if (row >= nodes.size()) {
			System.out.println("Asking for row " + row + " which is > than the number of rows (" + nodes.size() + ")");
			return null;
		} else {
			Bioentity node = nodes.get(row);
			return node;
		}
	}

	public List<String> searchForTerm(String term) {
		List<String> matches = new ArrayList<String> ();
		if (term != null && term.length() > 0) {
			if (term.charAt(0) == '*')
				term = term.length() > 1 ? term.substring(1) : "";
				if (term.length() > 0 && term.endsWith("*"))
					term = term.length() > 1 ? term.substring(0, term.length() - 1) : "";
					Pattern p = Pattern.compile(".*" + term + ".*");
					for (String check : term_list) {
						String s = OWLutil.inst().getTermLabel(check);
						if (p.matcher(s).matches()) {
							matches.add(check);
						}
					}
		}
		return matches;
	}

	ColumnTermData getTermData(int column) {
		if (column >= 0 && column < getColumnCount()) {
			String column_term = term_list.get(column);
			return term2menu.get(column_term);
		} else 
			return null;
	}

	public int getTermColumn(String term){
		return term_list.indexOf(term);
	}

	public String getTermForColumn(int column) {
		if (column >= 0 && column < term_list.size())
			return term_list.get(column);
		else
			return null;
	}

	public class AssociationData {

		private GeneAnnotation association;
		private boolean is_implied;
		private boolean not;

		public AssociationData(GeneAnnotation association, boolean is_implied,
				boolean not) {
			super();
			this.association = association;
			this.is_implied = is_implied;
			this.not = not;
		}

		public GeneAnnotation getAssociation() {
			return association;
		}

		public void setAssociation(GeneAnnotation association) {
			this.association = association;
		}

		public boolean isAncestor() {
			return is_implied;
		}

		public void setAncestor(boolean ancestor) {
			this.is_implied = ancestor;
		}

		public boolean isNot() {
			return not;
		}

		public void setNot(boolean not) {
			this.not = not;
		}
	}

}
