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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.bbop.phylo.owl.OWLutil;
import org.bbop.phylo.species.TaxonFinder;
import org.paint.util.RenderUtil;

public class TaxonDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Map<JCheckBox, Integer> selections;
	boolean loss;

	/** Creates the GUI shown inside the frame's content pane. */
	public TaxonDialog(Frame frame, String go_id, List<String> invalids) {
		super(frame, true);
		setLayout(new BorderLayout());
		setContentPane(taxonPane(go_id, invalids));
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public boolean isLost() {
		// this.paintComponents(this.getGraphics());
		return loss;
	}

	private JPanel taxonPane (String go_id, List<String> invalids) {
		JPanel qualify = new JPanel();
		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		//Create the components.
		JPanel selectionPane = createTaxaListPane(go_id, invalids);

		//Lay them out.

		JPanel buttonPane = new JPanel();
		buttonPane.setOpaque(true);
		buttonPane.setBackground(RenderUtil.getAspectColor());
		JButton lossButton = null;	 	 
		lossButton = new JButton("Set as loss");
		lossButton.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) { 
			    loss = true;
			    TaxonDialog.this.dispose();
			  } 
			} );
		JButton cancelButton = null;	 	 
		cancelButton = new JButton("Cancel");
		getRootPane().setDefaultButton(cancelButton);
		cancelButton.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) { 
			    loss = false;
			    TaxonDialog.this.dispose();
			  } 
			} );
		
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(lossButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));

		qualify.add(selectionPane);
		qualify.add(buttonPane);
		return qualify;
	}

	/** Creates the panel shown by the first tab. */
	private JPanel createTaxaListPane(String go_id, List<String> invalids) {
		String description;
		description = "Do you intend the evolutionary loss of " + OWLutil.inst().getTermLabel(go_id) + " in these taxa?";

		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		box.setOpaque(true);
		box.setBackground(RenderUtil.getAspectColor());

		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);

		for (String taxon : invalids) {
			label = new JLabel(TaxonFinder.getLabel(taxon));
			label.setBackground(Color.white);
			label.setOpaque(true);
			box.add(label);
		}
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(box, BorderLayout.PAGE_START);
		Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
		pane.setBorder(padding);
		pane.setOpaque(true);
		pane.setBackground(RenderUtil.getAspectColor());
		return pane;
	}
	
}
