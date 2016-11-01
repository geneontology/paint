package org.paint.gui.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.bbop.phylo.model.Bioentity;

public class TermSelectEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//initialize logger
	private List<String> terms;
	private Bioentity select_node;
	
	public TermSelectEvent(Object source, List<String> terms) {
		super(source);
		this.terms = terms;
		this.select_node = null;
	}

	public TermSelectEvent(Object source, String term) {
		super(source);
		terms = new ArrayList<String>();
		terms.add(term);
		this.select_node = null;
	}

	public TermSelectEvent(Object source, String term, Bioentity node) {
		super(source);
		terms = new ArrayList<String>();
		terms.add(term);
		this.select_node = node;
	}

	public List<String> getTermSelection() {
		return terms;
	}

	public Bioentity selectNode() {
		return select_node;
	}

}

