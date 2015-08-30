package org.paint.gui.matrix;

import java.util.Comparator;

import org.bbop.phylo.util.OWLutil;


public class TermComparator implements Comparator<String> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	public int compare(String term_a, String term_b) {
//		String aspect_a = OWLutil.getAspect(term_a);
//		String aspect_b = OWLutil.getAspect(term_b);

		/* 
		 * Want the ordering to be MF, CC, BP, so reverse the sign by
		 * multiplying by negative 1
		 */
//		int comparison = compareStrings(aspect_a, aspect_b) * -1;
//
//		if (comparison == EQUAL_TO) {
			/*
			 * Within a single aspect we want the more specific terms first
			 */
 				int comparison = isParent(term_a, term_b);
				if (comparison == EQUAL_TO) {
					comparison = isParent(term_b, term_a) * -1;
				}
				if (comparison == EQUAL_TO) {
					comparison = compareStrings(OWLutil.inst().getTermLabel(term_a), OWLutil.inst().getTermLabel(term_b));
				}
//		}
		return comparison;
	}

	private int compareStrings(String a, String b) {
		
		int comparison = a.toLowerCase().compareTo(b.toLowerCase());
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
	}
	
	/*
	 * Returns less_than if c is a child of p
	 * 
	 */
	protected static int isParent(String c, String p) {
		int comparison = EQUAL_TO;
		if (OWLutil.inst().moreSpecific(p, c)) {
			comparison = LESS_THAN;
		}
		return comparison;
	}
}
