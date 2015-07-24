package org.paint.gui.event;

import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;

import owltools.gaf.Bioentity;


public class GeneSelectEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(GeneSelectEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<Bioentity> genes;
	protected Bioentity ancestor;
	protected List<Bioentity> previous;

	public GeneSelectEvent(Object source, List<Bioentity> genes, Bioentity ancestor) {
		super(source);
		this.genes = genes;
		this.ancestor = ancestor;
	}

	public List<Bioentity> getGenes() {
		return genes;
	}

	public void setPrevious(List<Bioentity> previous) {
		this.previous = previous;
	}

	public List<Bioentity> getPrevious() {
		return previous;
	}

	public Bioentity getAncestor() {
		return ancestor;
	}

	public void setAncestor(Bioentity ancestor) {
		this.ancestor = ancestor;
	}
}
