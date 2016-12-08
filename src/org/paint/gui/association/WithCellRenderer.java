package org.paint.gui.association;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class WithCellRenderer extends JScrollPane implements TableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private JList<String> list;

	public WithCellRenderer() {
		list = RenderUtil.initWithList();
		getViewport().add(list);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value != null) {
			WithCellModel model = (WithCellModel) value;
			list.setModel(model);
			list.setBackground(model.getBackground());
			list.setSelectionBackground(model.getBackground());
			list.setSelectedIndex(model.getSelectedIndex());
		} 
		return this;
	}
}
