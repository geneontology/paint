package org.paint.gui.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bbop.phylo.model.Bioentity;

public class TermCountComparator implements Comparator<String> {

	private static final int LESS_THAN = -1;
	private static final int GREATER_THAN = 1;
	private static final int EQUAL_TO = 0;

	private Map<String, List<Bioentity>> term2nodes;

	public TermCountComparator (Map<String, List<Bioentity>> term2nodes) {
		this.term2nodes = term2nodes;
	}

	public int compare(String term_a, String term_b) {
		int count_a = 0;
		int count_b = 0;

		if (term2nodes.get(term_a) != null) {
			count_a = term2nodes.get(term_a).size();
		}
		if (term2nodes.get(term_b) != null) {
			count_b = term2nodes.get(term_b).size();
		}

		if (count_b > count_a)
			return GREATER_THAN;
		else if (count_b < count_a)
			return LESS_THAN;
		else
			return EQUAL_TO;
	}

}
