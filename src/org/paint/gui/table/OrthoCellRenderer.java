package org.paint.gui.table;

/**
 * Creates a new CellRenderer that displays a colored square if the value is included in the OrthoMCL group
 */

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.paint.gui.GuiConstant;


public class OrthoCellRenderer extends JLabel
implements TableCellRenderer {
	/**
	 * 
	 */
	 private static final long serialVersionUID = 1L;
	 Border selectedBorder = null;
	 HashMap<Color, Border> unselectedBorders;

	 public OrthoCellRenderer() {
		 setText("");
		 unselectedBorders = new HashMap<Color, Border> ();
		 setOpaque(true); //MUST do this for background to show up.
	 }

	 public Component getTableCellRendererComponent(
			 JTable table, Object value,
			 boolean isSelected, boolean hasFocus,
			 int row, int column) {
		 OrthoCell ortho_cell = (OrthoCell) value;
		 Color program_color = ortho_cell.getProgramColor();
		 if (program_color == null) {
			 program_color = GuiConstant.BACKGROUND_COLOR;
		 } 
		 setBackground(program_color);

		 if (isSelected) {
			 if (selectedBorder == null) {
				 selectedBorder = BorderFactory.createMatteBorder(1,1,1,1, GuiConstant.FOREGROUND_COLOR);
			 }
			 setBorder(selectedBorder);
		 } else {
			 Border unselectedBorder = unselectedBorders.get(program_color);
			 if (unselectedBorder == null) {
				 unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, program_color);
				 unselectedBorders.put(program_color, unselectedBorder);
			 }
			 setBorder(unselectedBorder);
		 }

		 setToolTipText(ortho_cell.getProgramName());

		 return this;
	 }
}
