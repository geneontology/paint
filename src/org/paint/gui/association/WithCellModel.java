package org.paint.gui.association;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.annotate.WithEvidence;
import org.bbop.phylo.io.panther.IDmap;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.model.Tree;
import org.paint.gui.AspectSelector;
import org.paint.main.PaintManager;
import org.paint.util.RenderUtil;

public class WithCellModel implements ListModel<String> {

	protected static Logger log = Logger.getLogger(AssociationsTableModel.class);
	
	private Color background_color;

	private List<String> with_list;

	private int selected_index;

	public WithCellModel(GeneAnnotation assoc) {

		with_list = new ArrayList<>();

		String aspect_name = AspectSelector.inst().getAspectName4Code(assoc.getAspect());
		background_color = RenderUtil.getAspectColor(aspect_name);

		Bioentity node = assoc.getBioentityObject();
		if (node.isLeaf() && AnnotationUtil.isPAINTAnnotation(assoc)) {
			Tree tree = PaintManager.inst().getTree().getTreeModel();
			List<String> with_strings = addExpWith(tree, node, assoc);
			addWithsToList(with_strings, with_list, background_color);
		} else {
			Collection<String> ancestor_withs = assoc.getWithInfos();
			if (ancestor_withs != null) {
				List<String> with_strings = new ArrayList<>();
				with_strings.addAll(ancestor_withs);
				addWithsToList(with_strings, with_list, background_color);
			}
		}
	}

	protected Color getBackground() {
		return background_color;
	}
	
	protected int getRowHeight() {
		int row_height = 22;
		int with_count = with_list.size();
		if (with_count == 0)
			with_count = 1;
		return Math.min(row_height * 4, row_height * with_count);
	}
	private void addWithsToList(List<String> with_strings, List<String> with_list, Color background_color) {
		for (String with : with_strings) {
			with_list.add(with);
		}
	}

	private List<String> addExpWith(Tree tree, Bioentity node, GeneAnnotation annotation) {
		List<String> withs = new ArrayList<>();
		String ancestor_id = annotation.getWithInfos().iterator().next();
		withs.add(ancestor_id);

		List<String> exp_withs;
		List<Bioentity> ancestor = IDmap.inst().getGeneByDbId(ancestor_id);
		if (ancestor != null) {
			if (ancestor.size() > 1) {
				log.info("More than one ancestor? " + ancestor_id);
			}
			for (Bioentity a : ancestor) {
				WithEvidence evidence = new WithEvidence(tree, a, annotation.getCls());
				exp_withs = evidence.getExpWiths();
				withs.addAll(exp_withs);
			}
		} else {
			log.error("Where is the ancestral node for " + node + " to inherit " + annotation.getCls() + '?');
		}
		return withs;
	}

	protected void setSelectedIndex(int index) {
		this.selected_index = index;
	}

	protected int getSelectedIndex() {
		return selected_index;
	}

	public String getElementAt(int index) {
		return with_list.get(index);
	}

	public int getSize() {
		return with_list.size();
	}

	public void addListDataListener(ListDataListener l) {
	}

	public void removeListDataListener(ListDataListener l) {
	}
}