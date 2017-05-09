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

package org.paint.dialog.find;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.phylo.owl.OWLutil;
import org.paint.dialog.find.FindPanel.SEARCH_TYPE;
import org.paint.gui.AspectSelector;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class ResultRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(ResultRenderer.class);


	public ResultRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		setFont(GuiConstant.DEFAULT_FONT);
		setForeground(GuiConstant.FOREGROUND_COLOR);
		FoundResultsTable.MatchModel model = (FoundResultsTable.MatchModel) table.getModel();
		String selected_value = (String) model.getValueAt(row, 0);
		SEARCH_TYPE type = model.getType();
		Color bg_color = Color.white;
		
		if (type == SEARCH_TYPE.GENE) {
			if (isSelected) {
				bg_color = Color.lightGray;
			}
			setText(selected_value);
		} else if (model.getType() == SEARCH_TYPE.TERM) {
			String aspect = OWLutil.inst().getAspect(selected_value);
			String aspect_name = AspectSelector.inst().getAspectName4Code(aspect);
			bg_color = RenderUtil.getAspectColor(aspect_name);
			if (isSelected) {
				bg_color = bg_color.darker();
			}
			int count = model.getTermUsage(selected_value);
			setText(OWLutil.inst().getTermLabel(selected_value) + " [" + count + "]");
		}
		setBackground(bg_color);
		return this;
	}

}