package org.paint.gui.matrix;

import java.util.Comparator;
import java.util.List;

import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.util.OWLutil;
import org.paint.displaymodel.DisplayBioentity;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;


public class TermCountComparator implements Comparator<String> {

	private static final int LESS_THAN = -1;
	private static final int GREATER_THAN = 1;
	private static final int EQUAL_TO = 0;

	private List<Bioentity> nodes;
	
	public TermCountComparator (List<Bioentity> nodes2) {
		this.nodes = nodes2;
	}
	
	public int compare(String term_a, String term_b) {
		int count_a = 0;
		int count_b = 0;
		
		if (nodes != null) {
			for (Bioentity node : nodes) {
				GeneAnnotation assoc_a = OWLutil.inst().isAnnotatedToTerm(node.getAnnotations(), term_a);
				GeneAnnotation assoc_b = OWLutil.inst().isAnnotatedToTerm(node.getAnnotations(), term_b);
				count_a += assoc_a != null && AnnotationUtil.isExpAnnotation(assoc_a) ? 1 : 0;
				count_b += assoc_b != null && AnnotationUtil.isExpAnnotation(assoc_b) ? 1 : 0;
			}
		}		
		if (count_b > count_a)
			return GREATER_THAN;
		else if (count_b < count_a)
			return LESS_THAN;
		else
			return EQUAL_TO;
	}

}
