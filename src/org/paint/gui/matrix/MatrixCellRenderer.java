package org.paint.gui.matrix;

/**
 * Creates a new CellRenderer that displays a colored square if the value is included in the OrthoMCL group
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.util.OWLutil;
import org.bbop.swing.ScaledIcon;
import org.paint.config.IconResource;
import org.paint.config.PaintConfig;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class MatrixCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean selected;
	private GeneAnnotation assoc;
	private boolean isAncestor;
	private boolean isNot;

	protected static Logger log = Logger.getLogger("MatrixCellRenderer");

	public MatrixCellRenderer() {
		setText("");
		setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(
			JTable table, Object value,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		AnnotMatrix annot_table = (AnnotMatrix) table;
		AnnotMatrixModel matrix = (AnnotMatrixModel) table.getModel();
		AnnotMatrixModel.AssociationData associationData = (AnnotMatrixModel.AssociationData)value;
		if (value == null) {
			assoc = null;
			return this;
		}
		assoc = associationData.getAssociation();
		isAncestor = associationData.isAncestor();
		isNot = associationData.isNot();
		DisplayBioentity node = (DisplayBioentity) matrix.getNode(row);
		selected = node.isSelected();
		
		Color c;
		Color color;
		ColumnTermData td = matrix.getTermData(column);
		if (td == null) 
			return this;
		
		if (!td.isOddColumn())
			c = GuiConstant.BACKGROUND_COLOR;
		else
			c = new Color(224, 224, 224);
		ScaledIcon scaledIcon = null;
		if (assoc != null) {
			if (AnnotationUtil.isExpAnnotation(assoc)) {
				color = PaintConfig.inst().expPaintColor;
			} else {
				color = PaintConfig.inst().inferPaintColor;				
			}
			if (isNot) {
				scaledIcon = new ScaledIcon(null);
				scaledIcon.setIcon(IconResource.inst().getIconByName("not"));
				scaledIcon.setDimension(15);
			}
		} else {
			color = c;
		}
		
		String col_term = OWLutil.inst().getTermLabel(matrix.getTermForColumn(column));
		if (associationData != null && associationData.getAssociation() != null) {
			String term_name = OWLutil.inst().getTermLabel(associationData.getAssociation().getCls());
			String row_name = node.getSymbol();
			if (term_name != null && term_name.length() > 0 && !col_term.equals(term_name))
				col_term += " (" + term_name + ')';
			col_term = row_name + " - " + col_term;
		}
		if (col_term == null || (col_term != null && col_term.length() == 0))
			log.debug("No term name for column " + column);
		setToolTipText(col_term);

		setIcon(scaledIcon);

		color = RenderUtil.selectedColor(selected, color, c);

		setBackground(color);

		if (annot_table.getSelectedColumn() == column) {
			this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.black));
		} else {
			this.setBorder(null);
		}

		return this;
	}

	/**
	 * Transforms the Graphics for vertical rendering and invokes the
	 * super method.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (assoc != null) {
			if (!isAncestor) {
				g.setColor(Color.black);
				g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);
			}
			else {
				g.setColor(Color.white);
				g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);
				g.setColor(Color.black);
				g.drawRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);
			}
		}
		RenderUtil.paintBorder(g, new Rectangle(0, 0, this.getWidth(), this.getHeight()), null, selected);
	}

}
