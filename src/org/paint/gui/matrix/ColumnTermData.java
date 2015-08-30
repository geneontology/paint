package org.paint.gui.matrix;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;
import org.bbop.phylo.util.OWLutil;
import org.paint.config.CustomTermList;
import org.paint.main.PaintManager;

public class ColumnTermData extends JPopupMenu implements ActionListener {
	private AnnotMatrix matrix;
	private ButtonGroup term_buttons;
	private List<String>term_list;
	private boolean odd_column = false;
	private String narrower_term = null;
	
	private static final Logger log = Logger.getLogger(PaintManager.class);

	public ColumnTermData() {
		super("");
	}

	public void showMenu(MouseEvent e, AnnotMatrix matrix) {
		this.matrix = matrix;
		show(e.getComponent(), e.getX(), e.getY());
	}

	public boolean isOddColumn() {
		return odd_column;
	}
	
	public void setOddColumn(boolean odd) {
		odd_column = odd;
	}
	
	protected void setNarrowTerm(String t) {
		narrower_term = t;
	}
	
	protected boolean isDeletable() {
		return narrower_term != null;
	}
	
	protected void initTermMenu(String column_term, List<String> column_terms) {
		if (isDeletable()) {
			JMenuItem del_column = new JMenuItem("Remove column");
			del_column.addActionListener(this);
			add(del_column);
			del_column.setEnabled(true);
			addSeparator();
		}
		term_buttons = new ButtonGroup();
		term_list = new ArrayList<String>();
		JRadioButtonMenuItem radio = addTermMenuItem(column_term, term_list);
		radio.setSelected(true);
		Font font = radio.getFont();
		radio.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
		addParentTerms (column_term, column_terms, term_list);
	}

	private void addParentTerms(String term, List<String> column_terms, List<String> term_list) {
		Set<String> exclusionTerms = CustomTermList.inst().getExclusionList();
		List<String> ancestors = OWLutil.inst().getAncestors(term);
		if (ancestors != null) {
			for (String ancestor : ancestors) {
				String label = OWLutil.inst().getTermLabel(ancestor);
				boolean add_item = !term.equals(ancestor);
				add_item &= !exclusionTerms.contains(ancestor);
				add_item &= !label.endsWith(" part");
				add_item &= !column_terms.contains(ancestor);
				add_item &= !term_list.contains(ancestor);
				if (add_item) {
					addTermMenuItem(ancestor, term_list);
				}
			}
		}		
	}

	private JRadioButtonMenuItem addTermMenuItem(String term, List<String> term_list) {
		JRadioButtonMenuItem radio = null;
		if (!term_list.contains(term)) {
			radio = new JRadioButtonMenuItem(OWLutil.inst().getTermLabel(term));
			term_buttons.add(radio);
			radio.setActionCommand(OWLutil.inst().getTermLabel(term));
			radio.addActionListener(this);
			add(radio);
			term_list.add(term);
		}
		return radio;
	}

	public void actionPerformed(ActionEvent e) {
		String term_name = e.getActionCommand();
		String term = null;
		for (Iterator<String> iter = term_list.iterator(); iter.hasNext() && term == null; ) {
			String check = iter.next();
			if (OWLutil.inst().getTermLabel(check).equals(term_name)) {
				term = check;
			}
		}
		String col_term = term_list.get(0);
		String [] origin = new String[2];
		if (term == null) {
			// remove this column
			origin[AnnotMatrixModel.BROADER] = col_term;
			origin[AnnotMatrixModel.NARROWER] = narrower_term;
			matrix.modifyColumns (origin, true);
		} 
		else if (term != col_term) {
			// Need to add another column for the broader term that has been selected
			origin[AnnotMatrixModel.BROADER] = term;
			origin[AnnotMatrixModel.NARROWER] = col_term;
			matrix.modifyColumns (origin, false);
		}
		setVisible(false);
	}
}


