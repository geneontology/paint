package org.paint.gui.event;

import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Bioentity;

public class NodeReorderEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(NodeReorderEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int action;
	List<Bioentity> node_list;

	public NodeReorderEvent(Object source) {
		super(source);
	}

	public List<Bioentity> getNodes() {
		return node_list;
	}

	public void setNodes(List<Bioentity> node_list) {
		this.node_list = node_list;
	}

}
