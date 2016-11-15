package org.paint.gui.event;

import java.util.EventObject;

public class TermSelectEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//initialize logger
	private String term;
	private boolean select_mrca;
	
	public TermSelectEvent(Object source, String term, boolean select_mrca) {
		super(source);
		this.term = term;
		this.select_mrca = select_mrca;
	}

	public String getSelectedTerm() {
		return term;
	}

	public boolean selectMRCA() {
		return select_mrca;
	}
}

