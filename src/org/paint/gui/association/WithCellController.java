package org.paint.gui.association;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.paint.util.GuiConstant;
import org.paint.util.HTMLUtil;
import org.paint.util.RenderUtil;

class WithCellController extends DefaultCellEditor {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(WithCellController.class);

	protected JList<String> list;
	protected JScrollPane scrollpane;
	protected WithCellModel model;

	public WithCellController() {
		super(new JCheckBox());
		scrollpane = new JScrollPane();
		list = RenderUtil.initWithList();

		scrollpane.getViewport().add(list);
		
		list.addListSelectionListener(new WithListSelectionHandler());
		
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				int index = list.locationToIndex(evt.getPoint());
				if (evt.getClickCount() == 1 && list.getSelectedIndex() == index) {
					// Double-click detected
//					list.setSelectedIndex(index);
					String with = list.getSelectedValue();
					showWithInBrowser(with);
				}
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value != null) {
			model = (WithCellModel) value;
			list.setModel(model);
			list.setBackground(model.getBackground());
			list.setSelectionBackground(model.getBackground());
			list.setSelectedIndex(model.getSelectedIndex());
		}
		return scrollpane;
	}

	public WithCellModel getCellEditorValue() {
		model.setSelectedIndex(list.getSelectedIndex());
		return model;
	}

	class WithListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) { 
			if (!e.getValueIsAdjusting() && list != null) {
				String with = list.getSelectedValue();
//				log.info("Not bringing up " + with + " in browser, since it's solely a selection");
//				showWithInBrowser(with);
			}
		}
	}

	protected void showWithInBrowser(String with) {
		if (with != null) {
			String [] xref = with.split(":");
			if (xref.length > 2) {
				xref[1] = xref[1] + ":" + xref[2];
			}
			String url = HTMLUtil.getURL(xref[0], xref[1], true);
			if (url != null)
				HTMLUtil.bringUpInBrowser(url);
			else
				log.info("No URL for " + with);
		}
	}
}
