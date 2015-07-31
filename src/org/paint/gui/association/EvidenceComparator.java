package org.paint.gui.association;

import java.util.Comparator;

import org.bbop.phylo.util.OWLutil;

import owltools.gaf.GeneAnnotation;

import com.sri.panther.paintCommon.GO.Evidence;


public class EvidenceComparator implements Comparator<GeneAnnotation> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	public int compare(GeneAnnotation fa, GeneAnnotation fb) {
		String term_a = fa.getCls();
		String term_b = fb.getCls();

		String aspect_a = OWLutil.getAspect(term_a);
		String aspect_b = OWLutil.getAspect(term_b);

		/* 
		 * Want the ordering to be MF, CC, BP, so reverse the sign by
		 * multiplying by negative 1
		 */
		int comparison = compareStrings(aspect_a, aspect_b) * -1;

		if (comparison == EQUAL_TO) {
			/*
			 * Within a single aspect we want the more general terms first
			 */
			comparison = OWLutil.moreSpecific(term_a, term_b) ? LESS_THAN : EQUAL_TO;
			if (comparison == EQUAL_TO) {
				comparison = OWLutil.moreSpecific(term_b, term_a) ? GREATER_THAN : EQUAL_TO;
			}
			if (comparison == EQUAL_TO) {
				comparison = compareStrings(OWLutil.getTermLabel(term_a), OWLutil.getTermLabel(term_b));
			}

		}
		return comparison;
	}

	private int compareStrings(String a, String b) {
		int comparison = a.compareTo(b);
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
	}

}
