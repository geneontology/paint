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

package org.paint.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.phylo.annotate.PaintAction;
import org.bbop.phylo.tracking.LogAction;
import org.paint.config.PaintConfig;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.displaymodel.DisplayTree;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AnnotationDragEvent;
import org.paint.gui.event.AnnotationDragListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.CurationColorEvent;
import org.paint.gui.event.CurationColorListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.table.GeneTable;
import org.paint.main.PaintManager;
import org.paint.util.GuiConstant;

import owltools.gaf.Bioentity;

public class TreePanel extends JPanel 
implements MouseListener, 
MouseMotionListener, 
Scrollable, 
GeneSelectListener, 
CurationColorListener,
TermSelectionListener, 
AnnotationChangeListener, 
AspectChangeListener, 
AnnotationDragListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean             first_time = true;
	protected int                 prevWidth = 0;

	private double tree_distance_scaling = -1;

	protected static Logger logger = Logger.getLogger(TreePanel.class.getName());
	
	protected static final String TOOLTIP_FOREGROUND = "ToolTip.foreground";

	public static final String POPUP_MENU_COLLAPSE = "Collapse node";
	public static final String POPUP_MENU_EXPAND = "Expand node";
	public static final String POPUP_MENU_OUTPUT_SEQ = "Output seq ids for leaves";
	public static final String POPUP_MENU_REROOT = "Reroot to node";
	public static final String POPUP_MENU_RESET_ROOT = "Reset Root to Main";
	public static final String POPUP_MENU_PRUNE = "Prune";

	private static final String OUTPUT_SEQ_INFO_TITLE = "#Descendant sequence information for node ";
	private static final String OUTPUT_SEQ_DELIM = "\t";
	private static final String STR_EMPTY = "";
	private static final String NEWLINE = "\n";
	private static final String OUTPUT_SEQ_INFO_COLUMNS = "#Database id" + OUTPUT_SEQ_DELIM + "Sequence id";

	// indicates whether or not the y values need to be recalculated on next draw
	private boolean need_update = true;
	private Rectangle tree_rect = new Rectangle(0, 0, 0, 0);

	protected static final int LEFTMARGIN = 20;

	private DisplayTree tree;

//	private static Logger log = Logger.getLogger(TreePanel.class);

	/**
	 * Constructor declaration
	 *
	 * @see
	 */

	public TreePanel() {
		setBackground(Color.white);
		addMouseListener(this);
		addMouseMotionListener(this);
		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerGeneAnnotationChangeListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerTermListener(this);
		manager.registerCurationColorListener(this);

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	public void setTreeModel(DisplayTree tree2) {
		this.tree = tree2;
		setNeedPositionUpdate();
	}

	public DisplayTree getTreeModel() {
		return tree;
	}

	public Bioentity getRoot() {
		if (tree != null) {
			return tree.getRoot();
		} else
			return null;
	}

	public Bioentity getCurrentRoot(){
		if (tree != null) {
			return tree.getCurrentRoot();
		} else
			return null;
	}

	public List<Bioentity> getAllNodes() {
		if (tree != null) {
			return tree.getAllNodes();
		} else
			return null;
	}

	public void scaleTree(double scale){
		if (this.getDistanceScaling() != scale) {
			this.setDistance(scale);
			setNeedPositionUpdate();
			PaintConfig.inst().tree_distance_scaling = scale;
		}
	}

	public void speciesOrder() {
		if (tree != null) {
			tree.speciesOrder();
			setNeedPositionUpdate();
		}
	}

	public void descendentCountLadder(boolean most_leaves_at_top) {
		if (tree != null) {
			tree.descendentCountLadder(most_leaves_at_top);
			setNeedPositionUpdate();
		}
	}

	public List<Bioentity> getTerminusNodes() {
		if (tree != null) {
			return tree.getTerminusNodes();
		} else
			return null;
	}

	public void getDescendentList(Bioentity node, List<Bioentity> v) {
		if (tree != null) {
			tree.getDescendentList(node, v);
		}
	}

	public void getLeafDescendants(Bioentity node, Vector<Bioentity> leafList){
		if (tree != null) {
			tree.getLeafDescendants(node, leafList);
		}
	}

	public void adjustTree() {
		setNeedPositionUpdate();
	}

	public void expandAllNodes() {
		if (tree != null) {
			tree.expandAllNodes();
			setNeedPositionUpdate();
		}
	}

	public void collapseNonExperimental() {
		if (tree != null) {
			tree.collapseNonExperimental();
			setNeedPositionUpdate();
		}
	}

	public void resetRootToMain() {
		if (tree != null) {
			if (tree.resetRootToMain())
				setNeedPositionUpdate();
		}
	}

	public Bioentity getMRCA(Bioentity gene1, Bioentity gene2) {
		if (tree != null) {
			return tree.getMRCA(gene1, gene2);
		} else
			return null;
	}

	public Bioentity getTopLeafNode(Bioentity root) {
		if (tree != null) {
			return tree.getTopLeafNode(root);
		} else
			return null;
	}

	public Bioentity getBottomLeafNode(Bioentity node) {
		if (tree != null) {
			return tree.getBottomLeafNode(node);
		} else
			return null;
	}

	public void handlePruning(DisplayBioentity node) {
		boolean shift = tree.handlePruning(node);
		if (shift) {
			setNeedPositionUpdate();
		}
	}

	// Override paintComponent method to draw tree image

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if (tree != null && g != null) {
			Rectangle r = ((JViewport) getParent()).getViewRect();
			paintTree(g, r);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 * @param r
	 *
	 * @see
	 */
	private void paintTree(Graphics g, Rectangle r){
		Bioentity current_root = getCurrentRoot();
		if ((null == g) || (null == current_root)){
			return;
		}
		boolean use_distances = PaintConfig.inst().use_distances;
		if (need_update) {
			updateNodePositions(current_root, g, PaintManager.inst().getRowHeight(), use_distances);
		}
		Font  f = g.getFont();
		paintBranch(current_root, current_root, g, r, use_distances);
		g.setFont(f);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param g
	 * @param r
	 *
	 * @see
	 */
	private void paintBranch(Bioentity current_root, Bioentity dsn, Graphics g, Rectangle r, boolean use_distances){
		if (null == dsn){
			return;
		}
		List<Bioentity>  topChildren = tree.getTopChildren(dsn);
		List<Bioentity>  bottomChildren = tree.getBottomChildren(dsn);

		// draw the children that are vertically above first
		if (!dsn.isTerminus()){
			if (null != topChildren){
				for (Bioentity node : topChildren) {
					paintBranch(current_root, node, g, r, use_distances);
				}
			}
		}

		// Add the parent
		DisplayBioentity node = (DisplayBioentity) dsn;
		node.drawMarker(current_root, g, ((current_root == dsn) && (current_root != tree.getRoot())), r);

		// Repeat for last half
		if (!dsn.isTerminus()){
			if (null != bottomChildren){
				for (Iterator<Bioentity> it = bottomChildren.iterator(); it.hasNext();) {
					paintBranch(current_root, it.next(), g, r, use_distances);
				}
			}
		}
	}

	// Methods used for node position

	/**
	 * Method declaration
	 *
	 * @param Graphics g
	 * @param int row_height
	 * @param boolean use_distances

	 * @see
	 */
	private void updateNodePositions(Bioentity current_root, Graphics g, int row_height, boolean use_distances) {
		int x = TreePanel.LEFTMARGIN + getNodeWidth(g, current_root);
		setNodeRectangle(current_root, row_height, x, 0, use_distances, g);
		tree_rect = calcTreeSize(g);
//		revalidate();
//		repaint();
		need_update = false;
	}

	protected void setNeedPositionUpdate() {
		need_update = true;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @return
	 *
	 * @see
	 */
	private Rectangle calcTreeSize(Graphics g) {
		tree_rect.setBounds(0, 0, 0, 0);
		List<Bioentity> terminus_nodes = tree.getTerminusNodes();
		DisplayBioentity bottom_node =  (DisplayBioentity) terminus_nodes.get(terminus_nodes.size() - 1);
		Rectangle bottomRect = bottom_node.getScreenRectangle();
		// Set the height
		tree_rect.height = bottomRect.y;

		// Determine the widest point
		for (int i = 0; i < terminus_nodes.size(); i++){
			DisplayBioentity dsn =  (DisplayBioentity) terminus_nodes.get(i);
			int x = dsn.getScreenRectangle().x;
			int width = getNodeWidth(g, dsn);
			if (tree_rect.width < x + width){
				tree_rect.width = x + width;
			}
		}
		return tree_rect;
	}

	private Rectangle getTreeSize(Graphics g) {
		if (tree != null) {
			if (need_update) {
				tree.nodesReordered();
				updateNodePositions(getCurrentRoot(), g, PaintManager.inst().getRowHeight(), PaintConfig.inst().use_distances);
			}
		}
		return tree_rect;
	}

	private int setNodeRectangle(Bioentity dot, int row_height, float base_x, float tree_depth, boolean use_distances, Graphics g) {
		int x;
		int y;
		int width;
		int height;
		DisplayBioentity node = (DisplayBioentity) dot;
		/*
		 * Calculate the left-right x position first
		 */
		double scale = getDistanceScaling();
		Float f;
		if (!use_distances) {
			f = new Float(base_x + (tree_depth * scale));
		}
		else {
			f = new Float(base_x + (Math.abs(node.getDistanceFromParent()) * scale));
		}
		x = f.intValue();

		/*
		 * Calculate width using a method call because it's used more than once
		 */
		width = getNodeWidth(g, node);

		/* 
		 * Then calculate vertical y position
		 */
		if (node.isTerminus()) {
			int margin = PaintManager.inst().getTopMargin() + 8;
			int row = tree.getTerminusNodes().indexOf(node);
			GeneTable mate = PaintManager.inst().getGeneTable();
			Rectangle position = mate.getCellRect(row, 0, false);
			double better_y = margin + position.getY();
			y = (int) Math.round(better_y);
		} else {
			List<Bioentity>  children = node.getChildren();

			if (null == children){
				y = 0;
			}
			int top_y = -1;
			int bottom_y = -1;
			float child_depth = tree_depth + 1;
			float child_base = f + width;
			for (Bioentity kid : children) {
				DisplayBioentity child = (DisplayBioentity) kid;
				int child_y = setNodeRectangle(child, row_height, child_base, child_depth, use_distances, g);
				if (top_y < 0 || child_y < top_y)
					top_y = child_y;
				if (bottom_y < 0 || child_y > bottom_y)
					bottom_y = child_y;
			}
			y = ((top_y + bottom_y) / 2) - (row_height / 2);
		}
		/*
		 * Calculate the height
		 */
		if (node.isTerminus() && node.getNodeLabel() != null && !node.getNodeLabel().equals(""))
			height = row_height;
		else
			height =  DisplayBioentity.GLYPH_DIAMETER;

		node.setNodeArea(x, y, height, width);

		return y + (height / 2);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	protected double getDistanceScaling(){
		if (tree_distance_scaling < 0) {
			tree_distance_scaling = PaintConfig.inst().tree_distance_scaling;
		}
		return tree_distance_scaling;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param scale
	 *
	 * @see
	 */
	protected void setDistance(double scale){
		tree_distance_scaling = scale;
		PaintConfig.inst().tree_distance_scaling = scale;
	}

	private int getNodeWidth(Graphics g, Bioentity node) {
		int width = getTextWidth(g, node);
		if (width == 0)
			width = DisplayBioentity.GLYPH_DIAMETER;
		else
			width += DisplayBioentity.GLYPH_DIAMETER + DisplayBioentity.NODE_TO_TEXT_OFFSET;
		return width;
	}

	private int getTextWidth(Graphics g, Bioentity node) {
		int width = 0;
		if (node.isTerminus()) {
			String s = ((DisplayBioentity) node).getNodeLabel();
			if (s != null && !s.equals(STR_EMPTY)) {
				FontMetrics fm = g.getFontMetrics(GuiConstant.DEFAULT_FONT);
				width = (fm.stringWidth(s));
			}
		}
		return width;
	}

	// MouseListener implementation methods

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseClicked(MouseEvent e) {

		// Called just after the user clicks the listened-to component.
		setToolTipText(STR_EMPTY);
		int modifiers = e.getModifiers();
		if ((modifiers & InputEvent.BUTTON1_MASK) != 0 &&
				(modifiers      & InputEvent.BUTTON3_MASK) == 0) {
			if (tree != null) {
				DisplayBioentity node = getClickedInNodeArea(e.getPoint());
				if (node != null) {
					boolean new_select = !node.isSelected();
					if (node.getParent() != null)
						new_select |= node.isSelected() && ((DisplayBioentity)node.getParent()).isSelected();

					if (new_select) {
						ArrayList<Bioentity> selection = new ArrayList<Bioentity>();
						selection.add(node);
						tree.getDescendentList(node, selection);
						GeneSelectEvent ge = new GeneSelectEvent (this, selection, node);
						EventManager.inst().fireGeneEvent(ge);
					} else {
						ArrayList<Bioentity> selection = new ArrayList<Bioentity>();
						GeneSelectEvent ge = new GeneSelectEvent (this, selection, node);
						EventManager.inst().fireGeneEvent(ge);						
					}
				}
			}
		}
		if (InputEvent.BUTTON3_MASK == (modifiers & InputEvent.BUTTON3_MASK) || 
				(((modifiers & InputEvent.BUTTON1_MASK) != 0 && (modifiers      & InputEvent.BUTTON3_MASK) == 0) && (true == e.isMetaDown())) ){
			JPopupMenu popup = createPopupMenu(e);
			if (popup != null)
				showPopup(popup, e.getComponent(), new Point(e.getX(), e.getY()));
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param popup
	 * @param comp
	 * @param position
	 *
	 * @see
	 */
	private void showPopup(JPopupMenu popup, Component comp, Point position){

		// Get root frame
		Component root = comp;

		while ((root != null) && (false == (root instanceof JFrame))){
			root = root.getParent();
		}
		if (root != null){
			SwingUtilities.convertPointToScreen(position, comp);
			Point     rootPos = root.getLocationOnScreen();
			Dimension rootSize = root.getSize();
			Dimension popSize = popup.getPreferredSize();
			int       x = position.x;
			int       y = position.y;
			Insets    insets = popup.getInsets();

			if (position.x + popSize.width + (insets.left + insets.right) > rootPos.x + rootSize.width){
				x = rootPos.x + rootSize.width - popSize.width - insets.left;
			}
			if (position.y + popSize.height + (insets.top + insets.bottom) > rootPos.y + rootSize.height){
				y = rootPos.y + rootSize.height - popSize.height - insets.top;
			}
			if (x >= rootPos.x + insets.left && y >= rootPos.y + insets.top){
				position.setLocation(x, y);
			}
			SwingUtilities.convertPointFromScreen(position, comp);
		}

		// Show popup menu.
		popup.show(comp, position.x, position.y);
	}

	private DisplayBioentity getPopupNode(MouseEvent e) {
		if (null == tree) {
			return null;
		}
		return (getClicked(e.getPoint()));
	}

	private JPopupMenu createPopupMenu(MouseEvent e) {
		JPopupMenu  popup = null;
		DisplayBioentity dsn = getPopupNode(e);
		if (dsn != null) {
			popup = new JPopupMenu();
			if (!dsn.isLeaf() && !dsn.isPruned()) {
				JMenuItem menuItem;
				if (dsn.isExpanded())
					menuItem = new JMenuItem(POPUP_MENU_COLLAPSE);
				else
					menuItem = new JMenuItem(POPUP_MENU_EXPAND);
				menuItem.addActionListener(new CollapseExpandNodeActionListener(dsn));
				popup.add(menuItem);                            
			}
			// Now add the reroot to node information
			if (!dsn.isLeaf() && !dsn.isPruned()) {
				JMenuItem menuItem;
				if (dsn != getCurrentRoot()) {
					menuItem = new JMenuItem(POPUP_MENU_REROOT);
					menuItem.addActionListener(new InternalRerootActionListener(dsn));
				} 
				else {
					menuItem = new JMenuItem(POPUP_MENU_RESET_ROOT);
					menuItem.addActionListener(new InternalRerootActionListener((DisplayBioentity) tree.getRoot()));

				}
				popup.add(menuItem);
			}
			if (!dsn.isPruned()) {
				JMenuItem menuItem = new JMenuItem(POPUP_MENU_OUTPUT_SEQ);
				menuItem.addActionListener(new OutputSeqIdsActionListener(e, dsn));
				popup.add(menuItem); 
			}
			JCheckBoxMenuItem checkItem = new JCheckBoxMenuItem(POPUP_MENU_PRUNE);
			checkItem.addActionListener(new PruneActionListener(e, dsn));
			checkItem.setSelected(dsn.isPruned());
			popup.add(checkItem); 
		}
		return popup;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseEntered(MouseEvent e){
		// handleMouseEvent(e);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseExited(MouseEvent e){
		setToolTipText(STR_EMPTY);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mousePressed(MouseEvent e){
		// Called just after the user presses a mouse button while the cursor is over the listened-to component.
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseReleased(MouseEvent e) {}
	// MouseMotionListener implementation methods

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseDragged(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseMoved(MouseEvent e){
		if (tree != null) {
			if (pointInNode(e.getPoint())){
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			else{
				this.setCursor(Cursor.getDefaultCursor());
			}
			DisplayBioentity  node = getClickedInNodeArea(e.getPoint());
			String tool_tip;
			if (null == node){
				tool_tip =  STR_EMPTY;
			} else {
				tool_tip = getToolTipInfo(node);
			}

			setToolTipText(tool_tip);
			if (!tool_tip.equals(STR_EMPTY)) {
				UIManager.put(TOOLTIP_FOREGROUND, new ColorUIResource(GuiConstant.FOREGROUND_COLOR));
				ToolTipManager.sharedInstance().setEnabled(true);
				ToolTipManager.sharedInstance().mouseMoved(e);
			}
		}
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class InternalRerootActionListener implements ActionListener{
		DisplayBioentity  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		InternalRerootActionListener(DisplayBioentity node){
			this.node = node;
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
			tree.nodeReroot(node);
			setNeedPositionUpdate();
			revalidate();
			repaint();
		}

	}

	private class CollapseExpandNodeActionListener implements ActionListener{
		DisplayBioentity  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		CollapseExpandNodeActionListener(DisplayBioentity node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			tree.handleCollapseExpand(node);
			setNeedPositionUpdate();
			revalidate();
			repaint();
		}

	}

	private class OutputSeqIdsActionListener implements ActionListener{
		Bioentity  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		OutputSeqIdsActionListener(MouseEvent e, DisplayBioentity node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			Vector <Bioentity> leafList = new Vector<Bioentity>();
			tree.getLeafDescendants(node, leafList);
			outputInfo(node, leafList);
		}
	}

	private class PruneActionListener implements ActionListener{
		DisplayBioentity  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		PruneActionListener(MouseEvent e, DisplayBioentity node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			node.setPrune(!node.isPruned());
			if (node.isPruned()) {
				PaintAction.inst().pruneBranch(node, true);
			} else {
				LogAction.logGrafting(PaintManager.inst().getFamily(), node);
			}
			handlePruning(node);
			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
		}
	}

	protected void outputInfo(Bioentity n, Vector<Bioentity> leafList) {
		// Get the information
		StringBuffer sb =
				new StringBuffer(OUTPUT_SEQ_INFO_TITLE + n.getSeqId());
		sb.append(NEWLINE);
		sb.append(OUTPUT_SEQ_INFO_COLUMNS);
		sb.append(NEWLINE);
		for (int i = 0; i < leafList.size(); i++) {
			Bioentity aNode = leafList.get(i);
			sb.append(aNode.getDBID());
			sb.append(OUTPUT_SEQ_DELIM);
			sb.append(aNode.getSeqId());
			sb.append(NEWLINE);
		}

		// Prompt user for file name
		JFileChooser dlg = new JFileChooser();
		if (null != PaintConfig.inst().gafdir) {
			File gaf_dir = new File(PaintConfig.inst().gafdir);
			dlg.setCurrentDirectory(gaf_dir);
		}
		int rtrnVal = dlg.showSaveDialog(GUIManager.getManager().getFrame());

		if (JFileChooser.APPROVE_OPTION != rtrnVal) {
			return;
		}
		File f = dlg.getSelectedFile();

		try {
			FileWriter fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sb.toString());
			//Close the output stream
			out.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public void handleGeneSelectEvent(GeneSelectEvent event) {
		List<Bioentity> selection = event.getPrevious();
		repaintNodes(selection);
		selection = event.getGenes();
		repaintNodes(selection);
	}

	public void handleTermEvent(TermSelectEvent e) {
		repaintNodes (tree.getCurrentNodes());
	}

	private void repaintNodes(Collection<Bioentity> genes) {
		Rectangle r = null;
		int width = 0;
		int height = 0;
		if (genes != null) {
			for (Bioentity kid : genes) {
				DisplayBioentity node = (DisplayBioentity) kid;
				Rectangle select_area_rect = new Rectangle(node.getScreenRectangle());
				// add some padding to make sure the entire area is cleared
				select_area_rect.x -= DisplayBioentity.GLYPH_DIAMETER;
				select_area_rect.width += 2 * DisplayBioentity.GLYPH_DIAMETER;
				select_area_rect.y -= 1;
				select_area_rect.height += 2;
				if (r == null) {
					r = select_area_rect;
				} else {
					if (select_area_rect.x < r.x)
						r.x = select_area_rect.x;
					if (select_area_rect.y < r.y)
						r.y = select_area_rect.y;
				}
				height += select_area_rect.height;
				width = Math.max(width, select_area_rect.width);
			}
		}
		if (r != null) {
			r.height = height;
			r.width = width;
			repaint(r);
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = getPreferredScrollableViewportSize();
		d.height += FamilyViews.inst().getHScrollerHeight(FamilyViews.TREE_PANE);
		d.height += FamilyViews.inst().getBottomMargin(FamilyViews.TREE_PANE) + 2;
		return d;
	}

	public Dimension getPreferredScrollableViewportSize() {
		Dimension tree_size = new Dimension();
		if (getGraphics() != null) {
			Rectangle tree_rect = getTreeSize(getGraphics());
			tree_size.width = (int) tree_rect.getWidth();
			tree_size.height = (int) tree_rect.getHeight();
		}
		return tree_size;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			int row_height = PaintManager.inst().getRowHeight();
			int rows = visibleRect.height / row_height;
			return (rows + 1) * row_height;
		} else {
			return 1;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		int currentPosition;
		int maxUnitIncrement;
		if (orientation == SwingConstants.VERTICAL) {
			currentPosition = visibleRect.y;
			maxUnitIncrement = PaintManager.inst().getRowHeight();
		} else {
			currentPosition = visibleRect.x;
			maxUnitIncrement = 1;
		}

		//Return the number of pixels between currentPosition
		//and the nearest tick mark in the indicated direction.
		int increment;
		if (direction < 0) {
			int newPosition = currentPosition -
					(currentPosition / maxUnitIncrement)
					* maxUnitIncrement;
			increment = (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			increment = ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement
					- currentPosition;
		}
		return increment;
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		revalidate();
		repaint();
	}

	public void handleAspectChangeEvent(AspectChangeEvent event) {
		repaint();
	}

	public void handleAspectChangeEvent(AnnotationDragEvent event) {
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	public void setDropInfo(DisplayBioentity node, Point dropPoint, String dropLabel) {

		String tool_tip = (dropLabel != null ? dropLabel : STR_EMPTY);

		setToolTipText(tool_tip);

		UIManager.put(TOOLTIP_FOREGROUND, new ColorUIResource(GuiConstant.FOREGROUND_COLOR));
		MouseEvent phantom = new MouseEvent(
				this,
				MouseEvent.MOUSE_MOVED,
				System.currentTimeMillis(),
				0,
				dropPoint.x,
				dropPoint.y,
				0,
				false);
		ToolTipManager.sharedInstance().mouseMoved(phantom);

	}


	public void handleCurationColorEvent(CurationColorEvent e) {
		repaint();
	}

	public boolean pointInNode(Point p) {
		DisplayBioentity current_root = (DisplayBioentity) getCurrentRoot();
		if (null == getClicked(current_root, p)){
			return false;
		}
		return true;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param p
	 *
	 * @return
	 *
	 * @see
	 */
	 protected DisplayBioentity getClicked(Point p){
		DisplayBioentity current_root = (DisplayBioentity) getCurrentRoot();
		return getClicked(current_root, p);
	 }

	 public DisplayBioentity getClickedInNodeArea(Point p) {
		 DisplayBioentity current_root = (DisplayBioentity) getCurrentRoot();
		 return getClickedInNodeArea(current_root, p);
	 }

	 /**
	  * Method declaration
	  *
	  *
	  * @param dsn
	  * @param p
	  *
	  * @return
	  *
	  * @see
	  */
	 private DisplayBioentity getClicked(DisplayBioentity dsn, Point p){
		 if (null == dsn || dsn.getScreenRectangle() == null){
			 return null;
		 }
		 if (dsn.getScreenRectangle().contains(p)){
			 return dsn;
		 }
		 List<Bioentity> children = dsn.getChildren();

		 if (null == children){
			 return null;
		 }
		 if (dsn.isTerminus()){
			 return null;
		 }
		 for (int i = 0; i < children.size(); i++){
			 DisplayBioentity  gnHit = null;
			 DisplayBioentity child = (DisplayBioentity) children.get(i);
			 gnHit = getClicked(child, p);
			 if (null != gnHit){
				 return gnHit;
			 }
		 }
		 return null;
	 }

	 public DisplayBioentity getClickedInNodeArea(DisplayBioentity dsn, Point p) {
		 if (null == dsn || dsn.getScreenRectangle() == null){
			 return null;
		 }
		 if (dsn.getScreenRectangle().contains(p)){
			 return dsn;
		 }

		 List<Bioentity>  children = dsn.getChildren();

		 if (children != null && !dsn.isTerminus()){
			 DisplayBioentity  dsnHit = null;
			 for (int i = 0; i < children.size(); i++){
				 DisplayBioentity child = (DisplayBioentity) children.get(i);
				 dsnHit = getClickedInNodeArea(child, p);
				 if (null != dsnHit){
					 return dsnHit;
				 }
			 }
		 }
		 return null;
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
	 protected String getToolTipInfo(DisplayBioentity node){
		 return node.getNodeLabel();
	 }

	public boolean ensureExpansion(List<Bioentity> genes) {
		List<DisplayBioentity> nodes_to_make_visible = new ArrayList<>();
		for (Bioentity kid : genes) {
			DisplayBioentity node = (DisplayBioentity) kid;
			Rectangle node_rect = node.getScreenRectangle();
			if (node_rect == null) {
				// must be invisible because a parent node is collapsed
				Bioentity parent = kid.getParent();
				while (parent != null && ((DisplayBioentity) parent).isExpanded()) {
					parent = parent.getParent();
				}
				if (parent == null) {
					logger.info("Crap, how did this ever happen");
					return false;
				}
				nodes_to_make_visible.add((DisplayBioentity) parent);
			}
		}
		if (!nodes_to_make_visible.isEmpty()) {
			tree.handleCollapseExpand(nodes_to_make_visible);
			Graphics g = getGraphics();
			int x = TreePanel.LEFTMARGIN + getNodeWidth(g, getCurrentRoot());
			setNodeRectangle(getCurrentRoot(), PaintManager.inst().getRowHeight(), x, 0, PaintConfig.inst().use_distances, g);
			tree_rect = calcTreeSize(g);
			return true;
		} else {
			return false;
		}
	}

}
