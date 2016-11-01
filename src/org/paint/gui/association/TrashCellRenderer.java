package org.paint.gui.association;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.util.OWLutil;
import org.paint.gui.AspectSelector;
import org.paint.util.RenderUtil;

public class TrashCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//	protected ScaledIcon scaledIcon = new ScaledIcon(null);
	private boolean selected;
	private Color bg_color;

	public TrashCellRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		selected = isSelected;
		setText("");
		AssociationsTable annot_table = (AssociationsTable) table;
		GeneAnnotation assoc = ((AssociationsTableModel) annot_table.getModel()).getEvidenceForRow(row);
		String text = "";
		switch ((AssociationsTable.PHYLO_ACTION) value) {
		case CHALLENGE:
			text = "Challenge this exp. annotation";
			break;
		case RESTORE:
			text = "Remove loss at this ancestral protein";
			break;
		case REMOVE:
			text = "Remove this association";
			break;
		case LOST:
			text = "Annotate a loss at this ancestral protein";
			break;
		case REGAIN:
			text = "";
			break;
		case DEPENDENCIES:
			text = "Loss would be contraindicated by exp. annotations";
			break;
		}
		setText(text);
		String aspect_name = AspectSelector.inst().getAspectName4Code(OWLutil.inst().getAspect(assoc.getCls()));
		bg_color = RenderUtil.getAspectColor(aspect_name);
		setBackground(bg_color);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		RenderUtil.paintBorder(g, new Rectangle(0, 0, this.getWidth(), this.getHeight()), null, selected);
	}

}
