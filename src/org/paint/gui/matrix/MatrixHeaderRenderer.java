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

package org.paint.gui.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.bbop.phylo.util.OWLutil;
import org.paint.config.IconResource;
import org.paint.gui.AspectSelector;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class MatrixHeaderRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MatrixHeaderRenderer.class);

	public MatrixHeaderRenderer(JTableHeader header) {
		setOpaque(true);
		this.setText("");
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		header.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				AnnotMatrix table = (AnnotMatrix) ((JTableHeader) e.getSource()).getTable();
				TableColumnModel columnModel = table.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				AnnotMatrixModel model = (AnnotMatrixModel) table.getModel();
				if (viewColumn >= 0 && viewColumn < model.getColumnCount()) {
					ColumnTermData td = model.getTermData(viewColumn);
					td.showMenu(e, table);
				}
			}
		});
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		Icon icon = IconResource.inst().getIconByName("arrowDown");
		setIcon(icon);
		setForeground(GuiConstant.FOREGROUND_COLOR);

		Color bg_color = GuiConstant.BACKGROUND_COLOR;
		AnnotMatrixModel matrix = (AnnotMatrixModel) table.getModel();
		if (column >= 0 && column < matrix.getColumnCount()) {
			String term = matrix.getTermForColumn(column);
			String col_name = OWLutil.inst().getTermLabel(term);
			setToolTipText(col_name);
			if (col_name == null || (col_name != null && col_name.length() == 0))
				log.debug("No term name for column " + column);
			UIManager.put("ToolTip.foreground", GuiConstant.FOREGROUND_COLOR);
			ToolTipManager.sharedInstance().setDismissDelay(999999999);
			
			String aspect_code = OWLutil.inst().getAspect(term);
			String aspect_name = AspectSelector.inst().getAspectName4Code(aspect_code);
			bg_color = RenderUtil.getAspectColor(aspect_name);
			ColumnTermData td = matrix.getTermData(column);
			if (td != null && td.isDeletable()) {
				bg_color = bg_color.darker();
			}
		}
		setBackground(bg_color);

		Border border;
		if (isSelected) {
			border = BorderFactory.createLineBorder(Color.BLACK);
		}
		else {
			border = BorderFactory.createEtchedBorder(); // default is lowered etched border
		}
		setBorder(border);

		return this;
	}
}