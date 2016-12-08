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

package org.paint.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;

public class RenderUtil {

	private static final Logger LOG = Logger.getLogger(RenderUtil.class);

	private static final String STR_DOT_DOT = "...";
	private static final String STR_EMPTY = "";

	private static HashMap<String, Color> ortho_colors;

	public static void paintBorder(Graphics g, Rectangle r, Color bg_color, boolean selected) {
		if (bg_color != null) {
			g.setColor(bg_color);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
		if (selected) {
			Color color = GuiConstant.SELECTION_COLOR;
			g.setColor(color);
			// line across the top of the cell in the table
			g.drawLine(r.x, r.y, r.x + r.width, r.y);
		} else {
			g.setColor(Color.lightGray);
		}
		// line across the bottom of the cell in the table
		int bottom_y = r.y + r.height - 1;
		g.drawLine(r.x, bottom_y, r.x + r.width, bottom_y);
	}

	/**
	 *  If string does not fit into cell, replace the end of the string with '...' to denote missing text
	 */
	public static String formatText(Graphics g, Insets insets, int boxWidth, String text, Font font) {
		if (text == null) {
			return "";
		}
		FontMetrics fm = g.getFontMetrics(font);

		int neededWidth = RenderUtil.getTextWidth(fm, text);

		if (boxWidth >= neededWidth) {
			return text;
		}

		String finalStr = STR_DOT_DOT;
		neededWidth = RenderUtil.getTextWidth(fm, finalStr);

		// Return empty string if the ellipsis cannot fit into the column.
		if (neededWidth > boxWidth) {
			return STR_EMPTY;
		}

		StringBuffer sb = new StringBuffer(finalStr);
		int i = 0;
		while ((neededWidth < boxWidth) && (i <= text.length() - 1)) {
			sb.insert(i, text.charAt(i));
			try {
				neededWidth = RenderUtil.getTextWidth(fm, sb.toString());
			}
			catch (ArrayIndexOutOfBoundsException e) {

				LOG.error("ArrayIndexOutOfBoundsException " + e.getMessage() + " returned while attempting to calculate Text size.");

			}
			i++;
		}

		// Remove last added character
		if (neededWidth > boxWidth) {
			try {
				sb.setLength(i - 1);
				sb.append(finalStr);
			}
			catch (StringIndexOutOfBoundsException  e) {

				LOG.error("StringIndexOutOfBoundsException " + e.getMessage() + " returned while attempting to delete character from string buffer.");

			}
		}
		return  sb.toString();
	}

	public static int getTextWidth(FontMetrics fm, String s) {
		int width = 0;
		try {
			width = fm.stringWidth(s);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			LOG.error("ArrayIndexOutOfBoundsException " + e.getMessage() + 
					" returned while attempting to calculate Text size.");
		}
		return width;
	}

	public static int getWidth(FontMetrics fm, String text, Insets insets) {
		return getTextWidth(fm, text + insets.left + insets.right);
	}

	public static Font getNodeFont(Bioentity node) {
		Font f = GuiConstant.DEFAULT_FONT;
		if (hasAspectExpAnnotation(node)) {
			f = new Font(f.getFontName(), Font.BOLD, f.getSize());				
		}
		return f;
	}

	public static boolean hasAspectExpAnnotation(Bioentity node) {
		String aspect_name = AspectSelector.inst().getAspectCode();
		List<GeneAnnotation> associations = AnnotationUtil.getAspectExpAssociations(node, aspect_name);
		return (associations != null && associations.size() > 0);
	}
	

	public static Color annotationFontColor(Bioentity node) {
		Color color = GuiConstant.FOREGROUND_COLOR;
		if (hasAspectExpAnnotation(node)) {
			color = getExpColor();
		}
		return color;
	}
	
	public static Color annotationStatusColor(DisplayBioentity node, Color c, boolean brighten) {
		/*
		 * Default is to make it the same as the background
		 */
		Color color = new Color(c.getRGB());
		String aspect_code = AspectSelector.inst().getAspectCode();
		List<GeneAnnotation> associations;
//		String all_aspects = AspectSelector.inst().getAspectCode(AspectSelector.Aspect.ALL_TERMS.toString());
//		if (aspect_code.equals(all_aspects)) {
//			associations = AnnotationUtil.getExperimentalAssociations(node);
//		} else {
			associations = AnnotationUtil.getAspectExpAssociations(node, aspect_code);
//		}
		if (associations.size() > 0) {
			color = getExpColor();
		} else {
//			if (aspect_code.equals(all_aspects)) {
//				associations = AnnotationUtil.getPaintAssociations(node);
//			} else {
				associations = AnnotationUtil.getAspectPaintAssociations(node, aspect_code);
//			}
			if (associations.size() > 0) {
				boolean direct_annotation = false;
				for (int i = 0; i < associations.size() && !direct_annotation; i++) {
					GeneAnnotation assoc = associations.get(i);
					if (assoc.isMRC() && !node.isLeaf()) {
						color = getMRCColor();
						direct_annotation = true;
					}
				}
				if (!direct_annotation) {
					color = getAspectColor();
				}
			}
		}
		return color;
	}

	public static Color selectedColor(boolean selected, Color color, Color c) {
		if (selected) {
			if (color.equals(c)) {
				color = Color.gray;
			} else {
				color = color.brighter();
			}
		}
		return color;
	}

	public static Color getAspectColor() {
		String cv = AspectSelector.inst().getAspectName();
		return getAspectColor(cv);
	}
	
	public static Color getAspectColor(String cv) {
		if (cv != null) {
			if (cv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()))
				return GuiConstant.bp_inf_color;
			if (cv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()))
				return GuiConstant.cc_inf_color;
			if (cv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()))
				return GuiConstant.mf_inf_color;
//			if (cv.equals(AspectSelector.Aspect.ALL_TERMS.toString()))
//				return GuiConstant.all_inf_color;
		}
		return GuiConstant.BACKGROUND_COLOR;
	}

	public static Color getExpColor() {
		String cv = AspectSelector.inst().getAspectName();
		return getExpColor(cv);
	}
	
	public static Color getExpColor(String cv) {
		if (cv != null) {
			if (cv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()))
				return GuiConstant.bp_exp_color;
			if (cv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()))
				return GuiConstant.cc_exp_color;
			if (cv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()))
				return GuiConstant.mf_exp_color;
//			if (cv.equals(AspectSelector.Aspect.ALL_TERMS.toString()))
//				return GuiConstant.all_exp_color;
		}
		return GuiConstant.BACKGROUND_COLOR;
	}

	public static Color getMRCColor() {
		String cv = AspectSelector.inst().getAspectName();
		return getMRCColor(cv);
	}
	
	public static Color getMRCColor(String cv) {
		if (cv != null) {
			if (cv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()))
				return GuiConstant.bp_mrc_color;
			if (cv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()))
				return GuiConstant.cc_mrc_color;
			if (cv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()))
				return GuiConstant.mf_mrc_color;
//			if (cv.equals(AspectSelector.Aspect.ALL_TERMS.toString()))
//				return GuiConstant.all_mrc_color;
		}
		return GuiConstant.BACKGROUND_COLOR;
	}

	public static Color getLineColor(DisplayBioentity node) {
		return node.getSubFamilyColor();
	}

	public static Color getOrthoColor(String ortho_name) {
		if (ortho_colors == null) {
			ortho_colors = new HashMap<String, Color> ();
		}
		Color color = ortho_colors.get(ortho_name);
		if (color == null) {
			int red_val = 0;
			int green_val = 0;
			int blue_val = 0;
			while ((red_val + green_val + blue_val) < 128) {
				red_val = (int)(Math.random() * 255);
				green_val = (int)(Math.random() * 255);
				blue_val = (int)(Math.random() * 255);
			}
			color = new Color(red_val, green_val, blue_val);
			ortho_colors.put(ortho_name, color);
		}
		return color;
	}

	public static void showPopup(JPopupMenu popup, Component comp, Point position){

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

	public static JList<String> initWithList() {
		JList<String> list = new JList<>();
		list.setForeground(Color.blue);
		list.setSelectionBackground(Color.gray);
		list.setFont(GuiConstant.DEFAULT_FONT);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(4);
		return list;
	}

}
