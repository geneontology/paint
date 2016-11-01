/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.paint.gui.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.util.OWLutil;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.table.GeneTable;
import org.paint.gui.table.GeneTableModel;
import org.paint.gui.tree.TreePanel;
import org.paint.main.PaintManager;

public class EventManager {
	/**
	 * 
	 */
	private static EventManager INSTANCE = null;

//	private static final Logger log = Logger.getLogger(EventManager.class);

	private Set<NodeScrollListener> scroll_listeners;
	private Set<GeneSelectListener> gene_listeners;
	private Set<TermSelectionListener> term_listeners;
	private Set<NodeReorderListener> node_listeners;
	private Set<SubFamilyListener> subfamily_listeners;
	private Set<ProgressListener> progressListeners;
	private Set<AnnotationChangeListener> geneAnnotationChangeListeners;
	private Set<AspectChangeListener> aspectChangeListeners;
	private Set<CurationColorListener> colorChangeListeners;
	private Set<AnnotationDragListener> annotationDragListeners;
	private Set<ChallengeListener> challenge_listeners;

	protected List<Bioentity> selectedNodes;
	protected List<String> term_selection;
	protected Bioentity top_node;

	private List<FamilyChangeListener> family_listeners = new ArrayList<FamilyChangeListener>(6);

	private boolean is_adjusting = false;

	private EventManager() {

		// Exists only to defeat instantiation.

	}

	public static synchronized EventManager inst() {

		if (INSTANCE == null) {

			INSTANCE = new EventManager();
		}

		return INSTANCE;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();

	}

	/**
	 * Components that wish to be notified of scrolling of the various node tree and table panels 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - ProgressListener to register for events
	 */
	public void registerNodeScrollListener(NodeScrollListener listener) {
		if (scroll_listeners == null) 
			scroll_listeners = new HashSet<NodeScrollListener>();

		if (!scroll_listeners.contains(listener))
			this.scroll_listeners.add(listener);
	}

	/*
	 * Components that wish to be notified of changes, selections and other gene manipulations 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void registerGeneListener(GeneSelectListener listener) {
		if (gene_listeners == null) 
			gene_listeners = new HashSet<GeneSelectListener>();

		if (!gene_listeners.contains(listener))
			this.gene_listeners.add(listener);
	}

	/*
	 * Components that wish to be notified of changes and selections of GO terms
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void registerTermListener(TermSelectionListener listener) {
		if (term_listeners == null) 
			term_listeners = new HashSet<TermSelectionListener>();

		if (!term_listeners.contains(listener))
			this.term_listeners.add(listener);
	}

	/*
	 * Components that wish to be notified of tree manipulations 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * At present this is the table, the tree, and the MSA display.
	public void registerTreeListener(TreeListener listener) {
		if (tree_listeners == null) 
			tree_listeners = new HashSet<TreeListener>();

		if (!tree_listeners.contains(listener))
			this.tree_listeners.add(listener);
	}
	 */


	/*
	 * Components that wish to be notified of changes, selections and other gene manipulations 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void registerSubFamilyListener(SubFamilyListener listener) {
		if (subfamily_listeners == null) 
			subfamily_listeners = new HashSet<SubFamilyListener>();

		if (!subfamily_listeners.contains(listener))
			this.subfamily_listeners.add(listener);
	}

	/*
	 * Components that wish to be notified of tree manipulations 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void registerNodeReorderListener(NodeReorderListener listener) {
		if (node_listeners == null) 
			node_listeners = new HashSet<NodeReorderListener>();

		if (!node_listeners.contains(listener))
			this.node_listeners.add(listener);
	}

	/**
	 * Components that wish to be notified of progress events 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - ProgressListener to register for events
	 */
	public void registerProgressListener(ProgressListener listener) {
		if (progressListeners == null) 
			progressListeners = new HashSet<ProgressListener>();

		if (!progressListeners.contains(listener))
			this.progressListeners.add(listener);
	}

	/**
	 * Components that wish to be notified of gene annotation change events 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - GeneAnnotationChangeListener to register for events
	 */
	public void registerGeneAnnotationChangeListener(AnnotationChangeListener listener) {
		if (geneAnnotationChangeListeners == null) 
			geneAnnotationChangeListeners = new HashSet<AnnotationChangeListener>();

		if (!geneAnnotationChangeListeners.contains(listener))
			this.geneAnnotationChangeListeners.add(listener);
	}

	/**
	 * Components that wish to be notified of aspect change events 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - AspectChangeListener to register for events
	 */
	public void registerAspectChangeListener(AspectChangeListener listener) {
		if (aspectChangeListeners == null) 
			aspectChangeListeners = new HashSet<AspectChangeListener>();

		if (!aspectChangeListeners.contains(listener))
			this.aspectChangeListeners.add(listener);
	}

	/**
	 * Components that wish to be notified of GeneNode color change events 
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - NodeColorChangeListener to register for events
	 */
	public void registerCurationColorListener(CurationColorListener listener) {
		if (colorChangeListeners == null) 
			colorChangeListeners = new HashSet<CurationColorListener>();

		if (!colorChangeListeners.contains(listener))
			this.colorChangeListeners.add(listener);
	}

	/**
	 * Components that wish to be notified of annotation drag events
	 * need to register themselves with the manager so that they will be notified whenever any
	 * event occurs. 
	 * 
	 * @param listener - AnnotationDragListener to register for events
	 */
	public void registerAnnotationDragListener(AnnotationDragListener listener) {
		if (annotationDragListeners == null) 
			annotationDragListeners = new HashSet<AnnotationDragListener>();

		if (!annotationDragListeners.contains(listener))
			this.annotationDragListeners.add(listener);
	}

	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void fireGeneEvent(GeneSelectEvent e) {
		List<Bioentity> new_genes = e.getGenes();
		boolean selection_changed = (selectedNodes == null || (selectedNodes != null && (new_genes.size() != selectedNodes.size())));
		if (!selection_changed) {
			/*
			 * This convoluted if statement serves simply to determine if the selection has changed
			 * If it hasn't changed then there is no need to fire an event
			 */
			for (Iterator<Bioentity> it = selectedNodes.iterator(); it.hasNext() && !selection_changed;) {
				Bioentity current_gene = it.next();
				selection_changed = !new_genes.contains(current_gene);
			}
		}

		// first turn all of the currently selected genes off
		selectNodes(false);
		e.setPrevious(selectedNodes);
		top_node = e.getAncestor();
		selectedNodes = e.getGenes();
		// then turn on the newly selected genes
		selectNodes(true);

		if (selection_changed) {
			// Ensure all of the selections have their nodes expanded in the tree
			TreePanel tree = PaintManager.inst().getTree();
			if (tree.ensureExpansion(selectedNodes)) {
				NodeReorderEvent event = new NodeReorderEvent(this);
				event.setNodes(tree.getTerminusNodes());
				for (Iterator<NodeReorderListener> it = node_listeners.iterator(); it.hasNext();) {
					NodeReorderListener listener = it.next();
					listener.handleNodeReorderEvent(event);
				}
			}
			for (Iterator<GeneSelectListener> it = gene_listeners.iterator(); it.hasNext();) {
				GeneSelectListener listener = it.next();
				listener.handleGeneSelectEvent(e);
			}
		}
	}

	private void selectNodes(boolean selected) {
		if (selectedNodes != null) {
			for (Bioentity node : selectedNodes) {
				((DisplayBioentity) node).setSelected(selected);
			}
		}
	}

	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * This event is fired when terms are selected so that the nodes can update their highlight color 
	 */
	public List<Bioentity> fireTermEvent(TermSelectEvent e) {
		/*
		 * Hold onto current terms, but replace current term_selection object with the event's new term selection
		 */
		Collection<String> old_terms = term_selection;
		Collection<String> new_terms = e.getTermSelection();
		List<Bioentity> new_nodes = new ArrayList<>();
		/*
		 * This reads weird, but the event has a TermSelection object which holds a collection of terms
		 * include all of the currently selected terms for this go aspect
		 */
		term_selection = null;
		if ((new_terms != null && !new_terms.isEmpty()) && (old_terms != null && !old_terms.isEmpty())) {
			/* 
			 * Retain terms from the other GO aspects in the selection as well
			 */
			String aspect = AspectSelector.inst().getAspectCode();
			for (String old_term : old_terms) {
				String old_aspect = OWLutil.inst().getAspect(old_term);
				if (!old_aspect.equals(aspect) && !new_terms.contains(old_term)) {
					new_terms.add(old_term);
				}
			}
		}

		if (new_terms != null && new_terms.size() > 0) {
			Bioentity mrca = null;
			top_node = e.selectNode();
			if (top_node == null) {
				TreePanel tree = PaintManager.inst().getTree();
				List<Bioentity> genes = tree.getBioentities();
				Bioentity min_node = null;
				Bioentity max_node = null;
				for (int i = 0; i < genes.size(); i++) {
					Bioentity node = (Bioentity) genes.get(i);
					((DisplayBioentity) node).setSelected(false);
					for (String term : new_terms) {
						GeneAnnotation assoc = AnnotationUtil.isAnnotatedToTerm(node.getAnnotations(), term);
						if (assoc != null && AnnotationUtil.isExpAnnotation(assoc) && node.isLeaf()) {
							((DisplayBioentity) node).setSelected(true);
							new_nodes.add(node);
							if (min_node == null)
								min_node = node;
							max_node = node;
						}
					}
				}
				/* 
				 * Now have to figure out the most recent common ancestor of these 2
				 * and expand the selection to any siblings
				 */
				if (!min_node.equals(max_node)) {
					mrca = tree.getMRCA(min_node, max_node);
					new_nodes.add(mrca);

				} else {
					mrca = min_node;
				}
				top_node = mrca;
			} else {
				new_nodes.add(top_node);
			}
			((DisplayBioentity) top_node).setSelected(true);
			for (Iterator<TermSelectionListener> it = term_listeners.iterator(); it.hasNext();) {
				TermSelectionListener listener = it.next();
				listener.handleTermEvent(e);
			}
		}
		return new_nodes;
	}

	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is the table and the MSA display.
	 * Will need to add node info panel to clean up its display too.
	 */
	public void fireNodeReorderEvent(NodeReorderEvent e) {
		for (Iterator<NodeReorderListener> it = node_listeners.iterator(); it.hasNext();) {
			NodeReorderListener listener = it.next();
			listener.handleNodeReorderEvent(e);
		}
		GeneTable table = PaintManager.inst().getGeneTable();
		int row_index = ((GeneTableModel) table.getModel()).getRow(top_node);
		table.scrollToVisible(row_index);
	}

	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is only the tree listening for events from the tree menu items.
	public void fireTreeEvent(TreeEvent e) {
		for (Iterator<TreeListener> it = tree_listeners.iterator(); it.hasNext();) {
			TreeListener listener = it.next();
			listener.handleTreeEvent(e);
		}
	}
	 */


	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is the table, the tree, and the MSA display.
	 */
	public void fireSubFamilyEvent(GeneDataEvent e) {
		for (Iterator<SubFamilyListener> it = subfamily_listeners.iterator(); it.hasNext();) {
			SubFamilyListener listener = it.next();
			listener.handleSubFamilyEvent(e);
		}
	}

	/**
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is the table, the tree, and the MSA display.
	 *
	 * @param event - ProgressEvent to fire
	 */
	public void fireProgressEvent(ProgressEvent event) {
		if (progressListeners != null) {
			for (ProgressListener listener : progressListeners) {
				listener.handleProgressEvent(event);
			}
		}
	}

	/**
	 * Inform listeners that annotations in a gene have changed.
	 * 
	 * @param event - GeneAnnotationChangeEvent to fire
	 */
	public void fireAnnotationChangeEvent(AnnotationChangeEvent event) {
		if (geneAnnotationChangeListeners != null) {
			for (AnnotationChangeListener listener : geneAnnotationChangeListeners) {
				listener.handleAnnotationChangeEvent(event);
			}
			DirtyIndicator.inst().dirtyGenes(true);
		}
	}

	/**
	 * Inform listeners that the aspect has changed.
	 * 
	 * @param event - AspectChangeEvent to fire
	 */
	public void fireAspectChangeEvent(AspectChangeEvent event) {
		if (aspectChangeListeners != null) {
			for (AspectChangeListener listener : aspectChangeListeners) {
				listener.handleAspectChangeEvent(event);
			}
		}
		if (term_selection == null)
			term_selection = new ArrayList<String>();
		else
			term_selection.clear();
		TermSelectEvent term_event = new TermSelectEvent (event.getSource(), term_selection);
		fireTermEvent(term_event);
	}

	/**
	 * Inform listeners that GeneNode color has changed.
	 * 
	 * @param event - AspectChangeEvent to fire
	 */
	public void fireCurationColorEvent(CurationColorEvent event) {
		if (colorChangeListeners != null) {
			for (CurationColorListener listener : colorChangeListeners) {
				listener.handleCurationColorEvent(event);
			}
		}
	}

	/**
	 * Inform listeners that an annotation drag event has occurred.
	 * 
	 * @param event - AspectChangeEvent to fire
	 */
	public void fireAnnotationDragEvent(AnnotationDragEvent event) {
		if (annotationDragListeners != null) {
			for (AnnotationDragListener listener : annotationDragListeners) {
				listener.handleAspectChangeEvent(event);
			}
		}
	}

	public List<Bioentity> getCurrentGeneSelection() {
		return this.selectedNodes;		
	}

	public List<String> getCurrentTermSelection() {
		return term_selection;
	}

	public Bioentity getAncestralSelection() {
		return top_node;
	}

	public void fireNewFamilyEvent(Object source, Family data_bag) {
		FamilyChangeEvent e = new FamilyChangeEvent(source, data_bag);
		if (data_bag != null) {
			for (FamilyChangeListener l : family_listeners)
				l.newFamilyData(e);
		}
		GeneTable table = PaintManager.inst().getGeneTable();
		table.scrollToVisible(0);
		if (selectedNodes != null)
			selectedNodes = null;
		if (term_selection != null)
			term_selection.clear();
		top_node = null;
	}

	/*
	 * Inform the components that have registered an interest that an event has occurred
	 * so that they can handle it.
	 * At present this is the table and the MSA display.
	 * Will need to add node info panel to clean up its display too.
	 */
	public void fireNodeScrollEvent(NodeScrollEvent e) {
		if (scroll_listeners != null) {
			is_adjusting = true;
			for (Iterator<NodeScrollListener> it = scroll_listeners.iterator(); it.hasNext();) {
				NodeScrollListener listener = it.next();
				listener.handleNodeScrollEvent(e);
			}
			is_adjusting = false;
		}
	}

	public void registerFamilyListener(FamilyChangeListener l) {
		family_listeners.add(l);
	}

	public boolean isAdjusting() {
		return is_adjusting;
	}

	public void registerChallengeListener(ChallengeListener listener) {
		if (challenge_listeners == null) 
			challenge_listeners = new HashSet<ChallengeListener>();
		if (!challenge_listeners.contains(listener))
			challenge_listeners.add(listener);
	}
	
	/**
	 * Inform listeners that an experimental annotation has been challenged.
	 */
	public void fireChallengeEvent(ChallengeEvent event) {
		if (challenge_listeners != null) {
			for (ChallengeListener listener : challenge_listeners) {
				listener.handleChallengeEvent(event);
			}
		}
	}

}
