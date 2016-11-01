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

package org.paint.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.bbop.phylo.model.GeneAnnotation;
import org.paint.util.RenderUtil;

public class QualifierDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Map<JCheckBox, Integer> selections;
	int qual_list;

	/** Creates the GUI shown inside the frame's content pane. */
	public QualifierDialog(Frame frame, int quals) {
		super(frame, true);
		setLayout(new BorderLayout());
		setContentPane(qualifyPane(quals));
		pack();
		setLocationRelativeTo(frame);
	}

	public int getQualifiers() {
		setVisible(true);
		// this.paintComponents(this.getGraphics());
		return qual_list;
	}

	private JPanel qualifyPane (int quals) {
		JPanel qualify = new JPanel();
		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		//Create the components.
		JPanel selectionPane = createSelectionPane(quals);

		//Lay them out.

		JPanel buttonPane = new JPanel();
		buttonPane.setOpaque(true);
		buttonPane.setBackground(RenderUtil.getAspectColor());
		JButton doneButton = null;	 	 
		doneButton = new JButton("Continue");
		doneButton.addActionListener(this);
		getRootPane().setDefaultButton(doneButton);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(doneButton);

		qualify.add(selectionPane);
		qualify.add(buttonPane);
		return qualify;
	}

	/** Creates the panel shown by the first tab. */
	private JPanel createSelectionPane(int quals) {
		String description;
		description = "Check the boxes of any qualifiers you wish propagated.";

		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		box.setOpaque(true);
		box.setBackground(RenderUtil.getAspectColor());

		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);

		selections = new HashMap<JCheckBox, Integer>();
		JCheckBox check;
		check = addCheckbox(quals, GeneAnnotation.COLOCALIZES_MASK, "COLOCALIZES");
		if (check != null) {
			box.add(check);
		}
		check = addCheckbox(quals, GeneAnnotation.CONTRIBUTES_TO_MASK, "CONTRIBUTES TO");
		if (check != null) {
			box.add(check);
		}
		check = addCheckbox(quals, GeneAnnotation.INTEGRAL_TO_MASK, "INTEGRAL_TO");
		if (check != null) {
			box.add(check);
		}
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(box, BorderLayout.PAGE_START);
		Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
		pane.setBorder(padding);
		pane.setOpaque(true);
		pane.setBackground(RenderUtil.getAspectColor());
		return pane;
	}
	
	private JCheckBox addCheckbox(int quals, int mask, String label) {
		if ((quals & mask) == mask) {
			JCheckBox check = new JCheckBox();
			check.setText(label);
			check.setSelected(false);
			selections.put(check, new Integer(mask));
			return(check);
		}
		return null;
	}

	public void actionPerformed(ActionEvent e) {
		Set<JCheckBox> checkboxes = selections.keySet();
		qual_list = 0;
		for (JCheckBox check : checkboxes) {
			if (check.isSelected()) {
				Integer q = selections.get(check);
				int qual = q.intValue();
				qual_list |= qual;
			}
		}
		this.setVisible(false);				
	}
}
