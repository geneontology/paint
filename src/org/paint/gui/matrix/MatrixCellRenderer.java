package org.paint.gui.matrix;

/**
 * Creates a new CellRenderer that displays a colored square if the value is included in the OrthoMCL group
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.owl.OWLutil;
import org.bbop.swing.ScaledIcon;
import org.paint.config.IconResource;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.AspectSelector;
import org.paint.util.GuiConstant;
import org.paint.util.RenderUtil;

public class MatrixCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean gene_selected;
	private boolean term_selected;
	private GeneAnnotation assoc;
	private boolean isAncestor;
	private boolean isNot;
	private Color assoc_color;

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
		AnnotMatrixModel.AssociationData associationData = (AnnotMatrixModel.AssociationData) value;
		if (value == null) {
			assoc = null;
			return this;
		}
		isAncestor = associationData.isAncestor();
		isNot = associationData.isNot();
		DisplayBioentity node = (DisplayBioentity) matrix.getNode(row);
		gene_selected = node.isSelected();
		
		ColumnTermData td = matrix.getTermData(column);
		if (td == null) 
			return this;
		Color column_color;
		if (td.isCellular()) {
			if (!td.isOddColumn()) 
				column_color = GuiConstant.BACKGROUND_COLOR;
			else
				column_color = new Color(224, 224, 224);
		} else {
			if (!td.isOddColumn())
				column_color = new Color(255, 255, 153);
			else
				column_color = new Color(255, 255, 204);
		}
		
		assoc = associationData.getAssociation();
		ScaledIcon scaledIcon = null;
		if (assoc != null) {
			String aspect_code = OWLutil.inst().getAspect(assoc.getCls());
			String aspect_name = AspectSelector.inst().getAspectName4Code(aspect_code);
			if (AnnotationUtil.isExpAnnotation(assoc)) {
				assoc_color = RenderUtil.getExpColor(aspect_name);
			} else if (assoc.isMRC()) {
				assoc_color = RenderUtil.getMRCColor(aspect_name);				
			}  else {
				assoc_color = RenderUtil.getAspectColor(aspect_name);
			}
			if (isNot) {
				scaledIcon = new ScaledIcon(null);
				scaledIcon.setIcon(IconResource.inst().getIconByName("not"));
				scaledIcon.setDimension(15);
			}
		} else {
			assoc_color = column_color;
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

//		if (isSelected) {
//			if (assoc_color.equals(column_color)) {
//				setBackground(assoc_color.darker());
//			} else {
//				setBackground(assoc_color.brighter());
//			}
//		}

		setBackground(assoc_color);
//		setBackground(RenderUtil.selectedColor(gene_selected, assoc_color, column_color));

		term_selected = annot_table.getSelectedColumn() == column;

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
			// This fills in the small dot in the middle of the grid to indicate
			// whether the annotation to this term is implied by another term annotated to this node 
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
		int width = this.getWidth();
		int height = this.getHeight();
		RenderUtil.paintBorder(g, new Rectangle(0, 0, width, height), null, gene_selected);
		if (term_selected) {
			g.setColor(GuiConstant.SELECTION_COLOR);
			// line to the left and right of the cell
			g.drawLine(0, 0, 0, height);
			g.drawLine(1, 0, 1, height);
			g.drawLine(width, 0, width, height);
			g.drawLine(width-1, 0, width-1, height);
		}		
	}

}
