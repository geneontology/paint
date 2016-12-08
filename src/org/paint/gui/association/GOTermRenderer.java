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

package org.paint.gui.association;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.util.OWLutil;
import org.bbop.swing.ScaledIcon;
import org.paint.config.IconResource;
import org.paint.gui.AspectSelector;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class GOTermRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean selected = false;
	protected int row = 0;
	protected int column = 0;
	protected static Logger log = Logger.getLogger(GOTermRenderer.class);

	protected static final String CONTRIBUTES_ICON = "images/contributes.gif";
	protected static final String COLOCATES_ICON = "images/colocate.gif";

	private GeneAnnotation assoc;
	private Color bg_color;

	protected ScaledIcon contributes_icon = new ScaledIcon(null);
	protected ScaledIcon colocates_icon = new ScaledIcon(null);

	public GOTermRenderer() {
		super();
		contributes_icon = new ScaledIcon(null);
		Icon icon;
		icon = IconResource.inst().getIconByName("colocate");
		colocates_icon.setIcon(icon);
		if (colocates_icon != null) {
			colocates_icon.setDimension(15);
		}
		icon = IconResource.inst().getIconByName("contribute");
		contributes_icon.setIcon(icon);
		if (contributes_icon != null) {
			contributes_icon.setDimension(15);
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		setFont(GuiConstant.DEFAULT_FONT);
		setForeground(GuiConstant.FOREGROUND_COLOR);
		selected = isSelected;
		this.row = row;
		this.column = column;
		this.assoc = ((AssociationsTableModel) table.getModel()).getEvidenceForRow(row);
		String aspect_name = AspectSelector.inst().getAspectName4Code(OWLutil.inst().getAspect(assoc.getCls()));
		bg_color = RenderUtil.getAspectColor(aspect_name);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		Rectangle bounds = getBounds();
		int x = 6; //bounds.x;
		int y = (bounds.height / 2);

		RenderUtil.paintBorder(g, new Rectangle(0, 0, bounds.width, bounds.height), bg_color, selected);

		if (assoc.isColocatesWith()) {
			if (colocates_icon != null) {
				colocates_icon.paintIcon(this, g, x, y-3);
				x += colocates_icon.getIconWidth();
			}
		}
		if (assoc.isContributesTo()) {
			if (contributes_icon != null) {
				contributes_icon.paintIcon(this, g, x, y-3);
				x += contributes_icon.getIconWidth();
			}
		}

		g.setColor(Color.blue);
		Font font = g.getFont();
		AttributedString as = new AttributedString(OWLutil.inst().getTermLabel(assoc.getCls()) + " (" + assoc.getCls() + ")");
		as.addAttribute(TextAttribute.FONT, font);
		if (assoc.isNegated()) {
			g.setColor(Color.magenta);
			as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		}
		as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		g.drawString(as.getIterator(), x, y+6);
	}

}