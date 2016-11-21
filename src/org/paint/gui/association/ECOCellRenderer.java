package org.paint.gui.association;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.bbop.phylo.annotate.AnnotationUtil;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.util.OWLutil;
import org.bbop.swing.ScaledIcon;
import org.paint.config.IconResource;
import org.paint.gui.AspectSelector;
import org.paint.util.RenderUtil;

public class ECOCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ScaledIcon scaledIcon = new ScaledIcon(null);
	private boolean selected;
	private Color bg_color;
	
	public ECOCellRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {

		selected = isSelected;
		GeneAnnotation assoc = ((AssociationsTableModel) table.getModel()).getEvidenceForRow(row);
		Bioentity node = assoc.getBioentityObject();

		Icon icon = null;
		String label = assoc.getShortEvidence();
		if (assoc.isMRC()) {
			icon = IconResource.inst().getIconByName("paint");
		} else {
			if (node != null) {
				if (assoc.isNegated()) {
					icon = IconResource.inst().getIconByName("not");
				} else if (AnnotationUtil.isExpAnnotation(assoc)) {
					icon = IconResource.inst().getIconByName("exp");
				} else {
					icon = IconResource.inst().getIconByName("inherited");
				}
			} else {
				label = "bug, please report database identifier.";
			}
		}
		setText (label);
		scaledIcon.setIcon(icon);
		if (scaledIcon != null) {
			scaledIcon.setDimension(15);
			setIcon(scaledIcon);
		} else {
			setIcon(null);
		}
		String aspect_name = AspectSelector.inst().getAspectName4Code(OWLutil.inst().getAspect(assoc.getCls()));
		bg_color = RenderUtil.getAspectColor(aspect_name);
		setBackground(bg_color);
		
		setVerticalAlignment(SwingConstants.CENTER);

		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	 	RenderUtil.paintBorder(g, new Rectangle(0, 0, this.getWidth(), this.getHeight()), null, selected);
	}

}
