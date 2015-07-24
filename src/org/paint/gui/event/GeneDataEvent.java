package org.paint.gui.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.paint.displaymodel.DisplayBioentity;


public class GeneDataEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(GeneDataEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Set<DisplayBioentity> nodes;

	public GeneDataEvent(Object source, Set<DisplayBioentity> nodes) {
		super(source);
		this.nodes = nodes;
	}

	public GeneDataEvent(Object source, DisplayBioentity node) {
		super(source);
		nodes = new HashSet<DisplayBioentity> ();
		nodes.add(node);
	}

	public Set<DisplayBioentity> getGenes() {
		return nodes;
	}

}
