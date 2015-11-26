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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.bbop.phylo.util.OWLutil;
import org.paint.util.RenderUtil;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;

public class ChallengeDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean override;

	/** Creates the GUI shown inside the frame's content pane. */
	public ChallengeDialog(Frame frame, List<GeneAnnotation> positive_annots) {
		super(frame, true);
		override = false;
		setLayout(new BorderLayout());
		setContentPane(challengePane(positive_annots));
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public boolean challenge() {
		return override;
	}

	private JPanel challengePane (List<GeneAnnotation> positive_annots) {
		JPanel qualify = new JPanel();
		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		JPanel title_panel = createTitlePanel(positive_annots.size() > 1 ? "annotations" : "annotation");
		
		JScrollPane annot_panel = createAnnotPanel(positive_annots);
		
		JPanel button_panel = createButtonPanel();

		qualify.add(title_panel);
		qualify.add(annot_panel);
		qualify.add(button_panel);
		return qualify;
	}

	/** Creates the panel shown by the first tab. */
	private JPanel createTitlePanel(String noun) {
		String description;
		description = "Warning, you are about to create a NOT annotation.\nThis action is in conflict with the " + noun + " below." ;
		JTextArea warning = new JTextArea(description);
		warning.setFont(new Font(warning.getFont().getFontName(), Font.BOLD, 14)); 
		JPanel box = new JPanel();
		box.add(warning);
		box.setOpaque(true);
		box.setBackground(RenderUtil.getAspectColor());
		box.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		return box;
	}
	
	private JScrollPane createAnnotPanel(List<GeneAnnotation> positive_annots) {
		DefaultListModel<String> annot_list = new DefaultListModel<>();
		JList<String> questionable = new JList<>(annot_list);
		questionable.setEnabled(false);
		questionable.setVisibleRowCount(-1);
		questionable.setForeground(Color.black);
		for (GeneAnnotation positive_annot : positive_annots) {
			Bioentity leaf = positive_annot.getBioentityObject();
			String pretty = leaf.getDBID() + " - " + leaf.getSpeciesLabel() + " to: ";
			pretty += OWLutil.inst().getTermLabel(positive_annot.getCls());
			pretty += " (" + positive_annot.getCls() + ")";
			annot_list.addElement(pretty);
		}
		JScrollPane listScroller = new JScrollPane(questionable);
		listScroller.setPreferredSize(new Dimension(250, 80));

		Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
		listScroller.setBorder(padding);
		listScroller.setOpaque(true);
		listScroller.setBackground(RenderUtil.getAspectColor());
		return listScroller;
	}
	
	private JPanel createButtonPanel() {
		//Create the components.
		JButton ok = new JButton("Override");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				override = true;
				ChallengeDialog.this.dispose();
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				override = false;
				ChallengeDialog.this.dispose();
			}
		});

		//Lay them out.
		JPanel buttonPane = new JPanel();
		buttonPane.setOpaque(true);
		buttonPane.setBackground(RenderUtil.getAspectColor());
		getRootPane().setDefaultButton(cancel);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(ok);
		buttonPane.add(cancel);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		return buttonPane;
	}
}
