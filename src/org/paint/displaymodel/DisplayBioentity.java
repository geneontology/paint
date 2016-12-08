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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.species.TaxonFinder;
import org.bbop.swing.HyperlinkLabel;
import org.paint.config.PaintConfig;
import org.paint.gui.event.TermHyperlinkListener;
import org.paint.gui.table.GeneTableModel;
import org.paint.gui.table.OrthoCell;
import org.paint.util.DuplicationColor;
import org.paint.util.GuiConstant;
import org.paint.util.HTMLUtil;
import org.paint.util.RenderUtil;

public class DisplayBioentity extends Bioentity {
	/**
	 * 
	 */
	public static final int NODE_TO_TEXT_OFFSET = 10;
	public static final int	GLYPH_DIAMETER = 8;
	private static final int TEXT_TO_LINE_OFFSET = 5;
	private static final int GLYPH_RADIUS = GLYPH_DIAMETER / 2;

	private final static BasicStroke DASHED = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] {2f, 2f}, 0f);

	private static final String STR_EMPTY = "";

	private String nodeNote;
	/*
	 * Use one variable for all of the different possible programs 
	 * and just use the appropriate bit mask to determine whether it belongs or not
	 */
	private String ortho_mcl;

	private List<Bioentity> originalChildrenOrder;

	private boolean is_subfamily;
	private boolean is_expanded;
	//	private boolean blocked;
	private boolean selected;
	private boolean visible;

	private Rectangle screenRectangle;
	private Point screenPosition;

	private static Logger log = Logger.getLogger(DisplayBioentity.class);

	private String subFamilyName;
	private Color  subFamilyColor = Color.black;

	private int dupColorIndex;

	private double sequenceWt = 0;
	private String hmm_seq;

	private HyperlinkLabel accLabel;
	private HyperlinkLabel modLabel;
	private OrthoCell ortho_cell;
	private HyperlinkLabel permaID;

	private Hashtable<String, String> attrLookup = new Hashtable<String, String>();

	private Color dropColor;
	private Color fillColor;

	// Methods
	public DisplayBioentity(boolean isExpanded) {
		super();
		this.is_expanded = isExpanded;
		this.visible = true;
		this.ortho_mcl = STR_EMPTY;
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
	private void connectToParent(DisplayBioentity currentRoot, Graphics g){
		DisplayBioentity  parent = (DisplayBioentity) getParent();
		if ((null != parent) && (currentRoot != this)){
			Point parentPos = new Point(parent.getScreenPosition());
			Point currentPos = new Point(getScreenPosition());
			g.setColor(RenderUtil.getLineColor(this));
			/* first draw a vertical hook up/down from the parent 
			 * try to avoid drawing on top of the parent glyph
			 */
			parentPos.x += GLYPH_RADIUS;
			if (parentPos.y < currentPos.y) {
				parentPos.y += Math.min(GLYPH_RADIUS, currentPos.y - parentPos.y);
			}
			else if (parentPos.y > currentPos.y)
				parentPos.y -= Math.min(GLYPH_RADIUS, parentPos.y - currentPos.y);
			if (parentPos.y != currentPos.y)
				g.drawLine(parentPos.x, parentPos.y, parentPos.x, currentPos.y);

			/*
			 * and then draw a horizontal line from the parent to the child
			 * don't need to worry about drawing on top of child glyph because
			 * it hasn't been drawn yet
			 */
			g.drawLine(parentPos.x, currentPos.y, currentPos.x, currentPos.y);

			boolean stopped = false;
			List<GeneAnnotation> all_assocs = getAnnotations();
			for (int i = 0; i < all_assocs.size() && !stopped; i++) {
				GeneAnnotation assoc = all_assocs.get(i);
				stopped |= assoc.isDirectNot();
			}
			if (stopped) {
				int distance = currentPos.x - parentPos.x;
				int spacing = distance / 2;
				int x = parentPos.x + spacing;
				int top = currentPos.y - 4;
				g.setColor(Color.red);
				g.fillRect(x, top, 3, 9);
			}
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param g
	 * @param triangle
	 * @param screenWidth
	 *
	 * @see
	 * Called from DrawableTree as it does a full branch and bound over the tree.
	 *	dsn.drawMarker(g, r, ((currentRoot == dsn) && (currentRoot != root)), screenWidth);
	 */
	public void drawMarker(Bioentity currentRoot, Graphics g, boolean triangle, Rectangle viewport) {
		if (!nodeAndParentFallInViewport(viewport)) {
			return;
		}
		DisplayBioentity root = (DisplayBioentity) currentRoot;
		
		Font f = RenderUtil.getNodeFont(this);

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(f);


		Rectangle r = new Rectangle(getScreenRectangle());
		Point p = new Point(getScreenPosition());
		fillColor = dropColor != null ? dropColor :
			RenderUtil.annotationStatusColor(this, GuiConstant.BACKGROUND_COLOR, true);
		Color lineColor = isSelected() ? GuiConstant.SELECTION_COLOR : GuiConstant.FOREGROUND_COLOR;

		connectToParent(root, g);

		// Used for drawing lines from the node to the end of the drawing area
		FontMetrics fm;

		if (isSubfamily()){
			// Draw diamond
			int[] xCoords = {
					// going anti-clockwise from 3 o'clock
					p.x, p.x + GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.x + GLYPH_RADIUS
			};
			int[] yCoords = {
					p.y, p.y - GLYPH_RADIUS, p.y, p.y + GLYPH_RADIUS
			};
			g.setColor(fillColor);
			g.fillPolygon(xCoords, yCoords, 4);
			g.setColor(lineColor);
			g.drawPolygon(xCoords, yCoords, 4);
		}
		else if (!isLeaf()) {
			if (triangle) {
				/* 
				 * If the root has been reset to descendant node in the tree
				 * then draw a triangle
				 */
				int[] xCoords = {p.x, p.x, p.x + GLYPH_DIAMETER};
				int[] yCoords = {p.y - GLYPH_RADIUS, p.y + GLYPH_RADIUS, p.y};

				g.setColor(fillColor);
				g.fillPolygon(xCoords, yCoords, 3);
				g.setColor(lineColor);
				g.drawPolygon(xCoords, yCoords, 3);
			} else if (isExpanded() && !isPruned()) {
				if (isDuplication()) {
					/*
					 * This is a duplication event
					 * Draw this as a square
					 */
					g.setColor(fillColor);
					g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
					g.setColor(lineColor);
					g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
				}
				else if (isHorizontalTransfer()) {
					// Draw diamond
					p.x = p.x -1;
					int[] xCoords = {
							// going clockwise from 9 o'clock
							p.x, p.x + GLYPH_RADIUS + 1, p.x + GLYPH_DIAMETER + 2, p.x + GLYPH_RADIUS + 1
					};
					int[] yCoords = {
							p.y, p.y - GLYPH_RADIUS - 1, p.y, p.y + GLYPH_RADIUS + 1
					};
					g.setColor(fillColor);
					g.fillPolygon(xCoords, yCoords, 4);
					g.setColor(lineColor);
					g.drawPolygon(xCoords, yCoords, 4);
				}
				else {
					/* 
					 * This is a speciation node
					 * Draw this as a circle
					 * circle starts a little higher, from upper left
					 */
					g.setColor(fillColor);
					g.fillOval(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
					g.setColor(lineColor);
					g.drawOval(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
				}
			}
			else if (isPruned()) {
				/* 
				 * This is if the user has said the tree is wrong and
				 * an entire branch should be removed
				 * Not sure what to draw here, trying a cone for now, very crude
				 */
				g.setColor(Color.gray.brighter());
				g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
				g.setColor(lineColor);
				g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
				g.drawLine(p.x, p.y - GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.y + GLYPH_RADIUS);
				g.drawLine(p.x, p.y + GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.y - GLYPH_RADIUS);
			} else {
				/* 
				 * This is if the user has collapsed the node
				 * Not sure what to draw here, trying a small vertical rectangle for now
				 */
				g.setColor(fillColor);
				g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_RADIUS, GLYPH_DIAMETER);
				g.setColor(lineColor);
				g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_RADIUS, GLYPH_DIAMETER);
			}
		}

		if (isTerminus()){
			/*
			 * if this is a terminus then there is a label
			 * draw the little line that connects the node to the label
			 */
			int x = p.x + NODE_TO_TEXT_OFFSET;

			/*
			 */	
			String  s = getNodeLabel();

			if (isPruned()) {
				s = "XXX-" + s;
				g.setColor(GuiConstant.BACKGROUND_COLOR);
			} else
				g.setColor(DuplicationColor.inst().getDupColor(getDupColorIndex()));
			g.fillRect(x, p.y - GLYPH_RADIUS, viewport.width, GLYPH_DIAMETER * 2);

			AttributedString as = new AttributedString(s);
			as.addAttribute(TextAttribute.FONT, f);
			if (isSelected()) {
				as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			}
			Color text_color = RenderUtil.annotationFontColor(this);
			g.setColor(text_color);
			if (null != s) {
				int text_x = p.x + NODE_TO_TEXT_OFFSET;
				int text_y = p.y + (r.height / 2);
				g.drawString(as.getIterator(), text_x, text_y);
				fm = g.getFontMetrics(f);
				int text_width = fm.stringWidth(s);	
				x += text_width;
			} else {
				log.debug("Why is label null for " + this.toString());
			}

			x += TEXT_TO_LINE_OFFSET;

			g.setColor(lineColor);
			if (!(g instanceof Graphics2D)) {
				g.drawLine(x, p.y, viewport.width, p.y);
			}
			else {
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(DASHED);
				g2.drawLine(x, p.y, viewport.x + viewport.width, p.y);
				g2.setStroke(oldStroke);
			}
		}

	}


	/**
	 * Returns if node and or its parent fall in the viewport.  If information cannot be determined, return true.
	 * @param node
	 * @param viewport
	 */
	private boolean nodeAndParentFallInViewport(Rectangle viewport) {
		if (getParent() == null) {
			return true;
		}

		// Do not want to consider the x-coordinate, since, horizontal dotted line has to be drawn
		Rectangle parentRect = ((DisplayBioentity)getParent()).getScreenRectangle();
		if (screenRectangle != null) {
			if ((screenRectangle.y < viewport.y && screenRectangle.y + screenRectangle.height < viewport.y &&
					parentRect.y < viewport.y && parentRect.y + parentRect.height < viewport.y) ||
					(viewport.y + viewport.height <  screenRectangle.y &&
							viewport.y + viewport.height < parentRect.y)) {
				return false;
			}
			return true;
		} else
			return false;
	}

	public String getNodeLabel(){
		String  s = null;
		if (isLeaf()){
			// only display the node name, as requested
			s = getSymbol();
			if (s == null || s.length() == 0) {
				s = getId();
			}
			if (s == null || s.length() == 0) {
				s = getFullName();
			} else if (getFullName() != null && getFullName().length() > 0) {
				s += " (" + getFullName() + ")";
			}
		}
		else {
			if (s == null || s.length() == 0){
				s = this.getDBID();
			}
		}
		String fiver = TaxonFinder.getCode(this.getNcbiTaxonId());
		if (fiver != null && fiver.length() > 0)
			s = fiver + ": " + s;

		return s;
	}

	public void setNodeArea(double x, double y, double w, double h) {
		int x_spot = (int) Math.round(x);
		int y_spot = (int) Math.round(y);
		int width = (int) Math.round(w);
		int height = (int) Math.round(h);
		Point p = new Point(x_spot, y_spot);
		setScreenPosition(p);
		screenRectangle = new Rectangle(x_spot, y_spot, height, width);
	}

	public Color getSubFamilyColor() {
		return subFamilyColor;
	}

	public void setSubFamilyColor(Color subFamilyColor) {
		this.subFamilyColor = subFamilyColor;
	}

	public Rectangle getScreenRectangle() {
		return screenRectangle;
	}

	private void setScreenPosition (Point position) {
		screenPosition = position;
	}

	private Point getScreenPosition() {
		return screenPosition;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isTerminus() {
		boolean terminus = super.isTerminus();
		return terminus || (!is_expanded);
	}

	public void setSequenceWt(double sequenceWt) {
		this.sequenceWt = sequenceWt;
	}

	public double getSequenceWt() {
		return sequenceWt;
	}

	public HyperlinkLabel getAccLabel() {
		if (accLabel == null) {
			accLabel = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());
			HTMLUtil.setHyperlinkField(this, accLabel, GeneTableModel.ACC_COL_NAME);
		}
		return accLabel;
	}

	public HyperlinkLabel getModLabel() {
		if (modLabel == null) {
			modLabel = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());	
			HTMLUtil.setHyperlinkField(this, modLabel, getDb());
		}
		return modLabel;
	}

	public HyperlinkLabel getPermaCell() {
		if (permaID == null) {
			permaID = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());	
			HTMLUtil.setHyperlinkField(this, permaID, GeneTableModel.PERMNODEID_COL_NAME);
		}
		return permaID;
	}

	public OrthoCell getOrthoCell() {
		if (ortho_cell == null) {
			Color color = ortho_mcl.equals(STR_EMPTY) ? GuiConstant.BACKGROUND_COLOR : RenderUtil.getOrthoColor(ortho_mcl);
			ortho_cell = new OrthoCell(color, ortho_mcl);	
		}
		return ortho_cell;
	}

	public OrthoCell getOrthoCell(String heading) {
		if (ortho_cell == null) {
			String value = this.getAttrLookup(heading);
			if (null == value) {
				value = STR_EMPTY;
			}
			Color color = value.equals(STR_EMPTY) ? GuiConstant.BACKGROUND_COLOR : RenderUtil.getOrthoColor(value);
			ortho_cell = new OrthoCell(color, ortho_mcl);   
		}
		return ortho_cell;
	}

	public void setOrthoMCL(String ortho_mcl) {
		this.ortho_mcl = ortho_mcl;
	}

	public String getOrthoMCL() {
		return this.ortho_mcl;
	}

	public void setAttrLookup(String type, String value) {
		if (null != type && null != value) {
			attrLookup.put(type, value);
		}
	}

	public String getAttrLookup(String type) {
		return attrLookup.get(type);
	}

	public void setSubFamilyName(String name) {
		subFamilyName = name;
	}

	public String getSubFamilyName() {
		return subFamilyName;
	}

	public void setIsSubfamily(boolean subfamily) {
		is_subfamily = subfamily;
	}

	public boolean isSubfamily() {
		return is_subfamily;
	}

	public void setNodeNote(String note) {
		nodeNote = note;
	}

	public String getNodeNote() {
		return nodeNote;
	}

	public void setExpanded(boolean expanded) {
		is_expanded = expanded;
		if (expanded) {
			PaintConfig.inst().collapse_no_exp = false;
		}
	}

	public boolean isExpanded() {
		return is_expanded;
	}

	public void setSelected (boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected () {
		return this.selected;
	}

	public String getHMMSeq() {
		return hmm_seq;
	}

	public void setHMMSeq(String hmm_seq) {
		this.hmm_seq = hmm_seq;
	}

	public void setDropColor(Color dropColor) {
		this.dropColor = dropColor;
	}

	public int getDupColorIndex() {
		return dupColorIndex;
	}

	public void setDupColorIndex(int sfColorIndex) {
		this.dupColorIndex = sfColorIndex;
	}

	public List<Bioentity> getOriginalChildrenOrder() {
		// Return a copy of the vector and not the original vector, else
		// the information can be changed.
		if (null == originalChildrenOrder) {
			return null;
		}
		List<Bioentity> copy = new ArrayList<Bioentity> (originalChildrenOrder.size());
		copy.addAll(originalChildrenOrder);
		return copy;
	}

	public void setOriginalChildrenToCurrentChildren() {
		if (children == null) {
			return;
		}
		if (originalChildrenOrder == null) {
			originalChildrenOrder = new ArrayList<Bioentity> ();
		}
		originalChildrenOrder.clear();
		originalChildrenOrder.addAll(children);
	}

	public Color getFillColor() {
		return fillColor;
	}
}