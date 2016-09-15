package org.paint.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.swing.DynamicMenu;
import org.paint.config.PaintConfig;
import org.paint.dialog.ScaleTreeDlg;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.gui.tree.TreePanel;
import org.paint.main.PaintManager;

public class TreeMenu extends DynamicMenu 
implements FamilyChangeListener, NodeReorderListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(TreeMenu.class.getName());

	private static final String expand = "Expand all nodes";
	private static final String collapse = "Collapse nodes without experimental data";
	private static final String distance = "Use distances";
	private static final String order = "Order leaves ";
	private static final String ladder_top = "Most leaves above";
	private static final String ladder_bottom = "Most leaves below";
	private static final String species = "By species";
	private static final String scale = "Scale...";

	private static final int TREE_COLLAPSE_NONEXP_NODES = 201;
	private static final int TREE_EXPAND_ALL_NODES = 202;
	private static final int TREE_USE_DISTANCES = 204;
	private static final int TREE_SPECIES = 205;
	private static final int TREE_TOP = 206;
	private static final int TREE_BOTTOM = 207;
	
	private JRadioButtonMenuItem species_order;
	private JMenuItem collapseNonExpNodesItem;
	
	public TreeMenu() {
		super("Tree");
		this.setMnemonic('t');

		JMenuItem expandAllNodesItem = new JMenuItem(expand);
		expandAllNodesItem.addActionListener(new TreeActionListener(TREE_EXPAND_ALL_NODES));
		this.add(expandAllNodesItem);

		collapseNonExpNodesItem = new JCheckBoxMenuItem(collapse);
		collapseNonExpNodesItem.setSelected(PaintConfig.inst().collapse_no_exp);
		collapseNonExpNodesItem.addActionListener(new TreeActionListener(TREE_COLLAPSE_NONEXP_NODES));
		this.add(collapseNonExpNodesItem);

		// Separator line
		this.addSeparator();

		JCheckBoxMenuItem useDistances = new JCheckBoxMenuItem(distance);
		useDistances.setSelected(PaintConfig.inst().use_distances);
		useDistances.addActionListener(new TreeActionListener(TREE_USE_DISTANCES));
		this.add(useDistances);

		JMenuItem scaleTree = new JMenuItem(scale);
		scaleTree.addActionListener(new ScaleTreeActionListener());
		this.add(scaleTree);

		// Separator line
		this.addSeparator();

		JMenu tree_ordering = new JMenu(order);
		species_order = new JRadioButtonMenuItem(species);
		JRadioButtonMenuItem top_order = new JRadioButtonMenuItem(ladder_top);
		JRadioButtonMenuItem bottom_order = new JRadioButtonMenuItem(ladder_bottom);

		species_order.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(species_order);
		group.add(top_order);
		group.add(bottom_order);

		species_order.addItemListener(new TreeReorderListener(TREE_SPECIES));
		top_order.addItemListener(new TreeReorderListener(TREE_TOP));
		bottom_order.addItemListener(new TreeReorderListener(TREE_BOTTOM));

		tree_ordering.add(species_order);
		tree_ordering.add(top_order);
		tree_ordering.add(bottom_order);

		this.add(tree_ordering);

		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerNodeReorderListener(this);
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 * 
	 * Since this has to open a dialog to allow the user to enter the new scaling factor it gets
	 * its own listener, rather than the all-purpose one below.
	 * 
	 */
	private class ScaleTreeActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			ScaleTreeDlg scaleTreeDlg = new ScaleTreeDlg(GUIManager.getManager().getFrame(), PaintConfig.inst().tree_distance_scaling);
			Double  d = scaleTreeDlg.display();
			if (null == d){
				return;
			}
			TreePanel tree = PaintManager.inst().getTree();
			if (tree != null) {
				tree.scaleTree(d);
			} else {
				PaintConfig.inst().tree_distance_scaling = d;
			}
		}
	}

	private class TreeActionListener implements ActionListener{
		int action;
		TreeActionListener(int action) {
			this.action = action;
		}
		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e) {
			TreePanel tree = PaintManager.inst().getTree();
			if (tree != null) {
				switch (action) {
				case TREE_USE_DISTANCES:
					PaintConfig.inst().use_distances = ((JCheckBoxMenuItem) e.getSource()).isSelected();
					tree.rescaleTree();
					break;
				case TREE_EXPAND_ALL_NODES:
					PaintConfig.inst().collapse_no_exp = false;
					collapseNonExpNodesItem.setSelected(false);
					tree.expandAllNodes();
					break;
				case TREE_COLLAPSE_NONEXP_NODES:
					PaintConfig.inst().collapse_no_exp = ((JCheckBoxMenuItem) e.getSource()).isSelected();
					if (PaintConfig.inst().collapse_no_exp) {
						tree.collapseNonExperimental();
					} else {
						tree.expandAllNodes();
					}
					break;
				}
			}
		}
	}

	private class TreeReorderListener implements ItemListener {
		int action;
		TreeReorderListener(int action) {
			this.action = action;
		}
		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void itemStateChanged(ItemEvent e){
			if (e.getStateChange() == ItemEvent.SELECTED) {
				TreePanel tree = PaintManager.inst().getTree();
				if (tree != null) {
					switch (action) {
					case TREE_SPECIES:
						tree.speciesOrder();
						break;
					case TREE_TOP:
						tree.descendentCountLadder(true);
						break;
					case TREE_BOTTOM:
						tree.descendentCountLadder(false);
						break;
					}
				}	
			}
		}
	}

	public void newFamilyData(FamilyChangeEvent e) {
		species_order.setSelected(true);		
	}

	public void handleNodeReorderEvent(NodeReorderEvent e) {
		collapseNonExpNodesItem.setSelected(PaintConfig.inst().collapse_no_exp);
	}

}

