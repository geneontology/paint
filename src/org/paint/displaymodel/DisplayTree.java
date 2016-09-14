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

package org.paint.displaymodel;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.Tree;
import org.paint.main.PaintManager;
import org.paint.util.DuplicationColor;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;

public class DisplayTree extends Tree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(DisplayTree.class);

	private Bioentity currentRoot = null;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	public DisplayTree(String id) {
		super(id);
	}
	
	public void growTree(Bioentity dsn) {
		currentRoot = dsn;
		super.growTree(dsn);
		DuplicationColor.inst().initColorIndex();
		setDupColorIndex(currentRoot, 0);
		setSubtreeColor((DisplayBioentity) root, Color.BLACK);
		log.info("There are " + bioentities.size() + " nodes in the " + PaintManager.inst().getFamily().getFamily_name() + " tree");
	}

	// Distance methods

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public void expandAllNodes(){
		expandAllNodes(currentRoot);
	}

	// Method to set subtree color

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param c
	 *
	 * @see
	 */
	private void setSubtreeColor(Bioentity dsn, Color c){
		if (null == dsn){
			return;
		}

		// Set values for node
		((DisplayBioentity) dsn).setSubFamilyColor(c);

		// Set values for children
		List<Bioentity>  children = dsn.getChildren();

		if (null == children){
			return;
		}
		for (Bioentity kid : children) {
			setSubtreeColor(kid, c);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	private void resetExpansion(Bioentity dsn){
		if (!((DisplayBioentity) dsn).isExpanded()){
			setNodeExpanded((DisplayBioentity) dsn);
		}
		List<Bioentity>  children = dsn.getChildren();

		if (null != children){
			for (Bioentity kid : children) {
				resetExpansion(kid);
			}
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @return
	 *
	 * @see
	 */
	private void expandAllNodes(Bioentity dsn){
		resetExpansion(dsn);
		initCurrentNodes();
	}

	public void collapseNonExperimental() {
		resetExpansion(root);
		collapseMRC (root);
		initCurrentNodes();
	}

	private void collapseMRC(Bioentity mrc) {
		List<Bioentity> twigList = new ArrayList<Bioentity>();
		getLeafDescendants(mrc, twigList);
		boolean no_exp = true;
		for (int i = 0; i < twigList.size() && no_exp; i++) {
			Bioentity check = twigList.get(i);
			List<GeneAnnotation> exp_assoc = AnnotationUtil.getExperimentalAssociations(check);
			no_exp &= (exp_assoc == null || exp_assoc.size() == 0);
		}
		if (no_exp) {
			((DisplayBioentity) mrc).setExpanded(false);
		} else {			
			List<Bioentity> children = mrc.getChildren();
			if (children != null) {
				for (Bioentity child : children) {
					collapseMRC(child);
				}
			}
		}
	}

    public Bioentity getCurrentRoot(){
        return currentRoot;
    }

	private void setDupColorIndex(Bioentity node, int color_index) {
		((DisplayBioentity) node).setDupColorIndex(color_index);
		if (!node.isLeaf()) {
			List<Bioentity> children = node.getChildren();
			if (node.isDuplication()) {
				boolean only_leaves = true;
				for (Bioentity child : children) {
					only_leaves &= child.isLeaf();;
				}
				if (!only_leaves) {
					List<Bioentity> ordered_by_distance = new ArrayList<Bioentity>();
					ordered_by_distance.addAll(children);
					Collections.sort(ordered_by_distance, new DistanceSort());				
					for (Bioentity child : ordered_by_distance) {
						int index = ordered_by_distance.indexOf(child);
						if (index < ordered_by_distance.size() - 2) {
							Bioentity sib = ordered_by_distance.get(index+1);
							if (sib.getDistanceFromParent() == child.getDistanceFromParent()) {
								color_index = DuplicationColor.inst().getNextIndex();
								log.info(child.getId() + " and " + sib.getId() + " are equally distant from parent");
							}
						}
						setDupColorIndex((DisplayBioentity)child, color_index);
						if (ordered_by_distance.indexOf(child) < ordered_by_distance.size() - 1)
							color_index = DuplicationColor.inst().getNextIndex();
					}
				}
				else {
					for (Bioentity child : children) {
						setDupColorIndex((DisplayBioentity)child, color_index);
					}
				}
			}
			else {
				for (Bioentity child : children) {
					setDupColorIndex((DisplayBioentity)child, color_index);
				}
			}
		}
	}

	private void setNodeExpanded(DisplayBioentity dsn){
		if (!dsn.isLeaf() && !dsn.isPruned()){
			dsn.setExpanded(!dsn.isExpanded());
		}
	}

	public void nodeReroot(Bioentity node){
		if ((node != null) && !node.isTerminus()){
			resetToRoot(node);
		}
	}

	public void handleCollapseExpand(DisplayBioentity node) {
		boolean change = (node != null && !node.isLeaf() && !node.isPruned());
		if (change) {
			setNodeExpanded(node);
			initCurrentNodes();
		}
	}
	
	public void handleCollapseExpand(List<DisplayBioentity> nodes) {
		boolean change = false;
		for (DisplayBioentity node : nodes) {
			if (!node.isLeaf() && !node.isPruned())
				setNodeExpanded(node);
			change |= !node.isLeaf() && !node.isPruned();
		}
		if (change) 
			initCurrentNodes();
	}

	public boolean handlePruning(DisplayBioentity node) {
		boolean change = (node != null && !node.isLeaf());
		if (change) {
			initCurrentNodes();
		}
		return change;
	}

	public boolean resetRootToMain(){
		return resetToRoot(root);
	}

	protected boolean resetToRoot(Bioentity dsn){
		// If it is not the current root, make it the current root
		if (currentRoot != dsn){
			currentRoot =  dsn;
			initCurrentNodes();
			return true;
		} else 
			return false;
	}

	// Tree operations

	// Methods used for laddering functionality
	public void speciesOrder() {
		/* 
		 * Sort each nodes list of children accordingly
		 */
		Comparator<Bioentity> comp = new SpeciesSort();
		ladder(currentRoot, comp);

		/* 
		 * Reinitialize the full list of nodes
		 */
		bioentities.clear();
		addChildNodesInOrder(currentRoot, bioentities);
		// Save new ordering that are visible as well
		initCurrentNodes();
	}

	public void descendentCountLadder(boolean most_leaves_at_top) {
		/* 
		 * Sort each nodes list of children accordingly
		 */
		Comparator<Bioentity> comp = new LeafCountSort(most_leaves_at_top);
		ladder(currentRoot, comp);

		/* 
		 * Reinitialize the full list of nodes
		 */
		bioentities.clear();
		addChildNodesInOrder(currentRoot, bioentities);
		// Save new ordering that are visible as well
		initCurrentNodes();
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	private void ladder(Bioentity dsn, Comparator<Bioentity> comp){
		List<Bioentity>  children = dsn.getChildren();
		if (null == children){
			return;
		}

		// start at the bottom and work the way back up
		for (Iterator<Bioentity> it = children.iterator(); it.hasNext();) {
			Bioentity child = it.next();
			ladder(child, comp);
		}

		// Sort the children
		Collections.sort(children, comp);

	}

	private class LeafCountSort implements Comparator<Bioentity> {
		private boolean most_leaves_at_top;

		private LeafCountSort(boolean most_leaves_at_top) {
			this.most_leaves_at_top = most_leaves_at_top;
		}
		public int compare(Bioentity o1, Bioentity o2) {
			int o1_descendents = descendent_count.get(o1).intValue();
			int o2_descendents = descendent_count.get(o2).intValue();
			int sort_value = o1_descendents - o2_descendents;
			if (most_leaves_at_top)
				sort_value = sort_value * -1;
			return sort_value;
		}
	}

	private class SpeciesSort implements Comparator<Bioentity> {

		public int compare(Bioentity o1, Bioentity o2) {
			int o1_descendents = species_index.get(o1).intValue();
			int o2_descendents = species_index.get(o2).intValue();
			return (o1_descendents - o2_descendents);
		}
	}

	private class DistanceSort implements Comparator<Bioentity> {

		private DistanceSort() {
		}

		public int compare(Bioentity o1, Bioentity o2) {
			float o1_distance = o1.getDistanceFromParent();
			float o2_distance = o2.getDistanceFromParent();
			if (o1_distance < o2_distance)
				return -1;
			else if (o1_distance < o2_distance)
				return 1;
			else
				return 0;
		}
	}
}
