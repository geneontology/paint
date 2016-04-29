package org.paint.gui.association;

import java.util.Comparator;

import org.bbop.phylo.util.OWLutil;

import owltools.gaf.GeneAnnotation;

public class EvidenceComparator implements Comparator<GeneAnnotation> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	public int compare(GeneAnnotation fa, GeneAnnotation fb) {
		String term_a = fa.getCls();
		String term_b = fb.getCls();

		String aspect_a = getAspect(term_a);
		String aspect_b = getAspect(term_b);

		/* 
		 * Want the ordering to be MF, CC, BP, convert the aspect
		 * according to this order
		 */
		int comparison = compareStrings(aspect_a, aspect_b);

		if (comparison == EQUAL_TO) {
			/*
			 * Within a single aspect we want the more general terms first
			 */
			comparison = OWLutil.inst().moreSpecific(term_a, term_b) ? LESS_THAN : EQUAL_TO;
			if (comparison == EQUAL_TO) {
				comparison = OWLutil.inst().moreSpecific(term_b, term_a) ? GREATER_THAN : EQUAL_TO;
			}
			if (comparison == EQUAL_TO) {
				comparison = compareStrings(OWLutil.inst().getTermLabel(term_a), OWLutil.inst().getTermLabel(term_b));
			}

		}
		return comparison;
	}

	private String getAspect(String term) {
		String aspect = OWLutil.inst().getAspect(term);
		if (aspect.charAt(0) == 'F')
			return "A";
		else if (aspect.charAt(0) == 'C')
			return "B";
		else
			return "C";
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
