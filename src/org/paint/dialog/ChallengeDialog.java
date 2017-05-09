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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.GeneAnnotation;
import org.bbop.phylo.owl.OWLutil;
import org.paint.gui.AspectSelector;
import org.paint.util.RenderUtil;

public class ChallengeDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected JTextArea reason_text;
	protected JLabel warning;
	protected String evidence_type;
	protected boolean challenge;
	protected boolean negation;
	protected boolean proceed;

	/** Creates the GUI shown inside the frame's content pane. */
	public ChallengeDialog(Frame frame, GeneAnnotation annot, List<GeneAnnotation> positive_annots, String [] notStrings) {
		super(frame, true);

		reason_text = null;
		challenge = positive_annots != null && positive_annots.size() > 0;
		negation = notStrings != null;

		setLayout(new BorderLayout());

		setContentPane(challengePane(annot, positive_annots, notStrings));
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public boolean okay() {
		return proceed;
	}

	public String getRationale() {
		String rationale = null;
		if (reason_text != null) {
			rationale = reason_text.getText();
		}
		return rationale;
	}

	public String getEvidenceType() {
		return evidence_type;
	}

	private JPanel challengePane (GeneAnnotation annot, List<GeneAnnotation> positive_annots, String [] notStrings) {
		JPanel challenge_pane = new JPanel();
		challenge_pane.setLayout(new BoxLayout(challenge_pane, BoxLayout.PAGE_AXIS));

		String aspect_name = AspectSelector.inst().getAspectName4Code(annot.getAspect());
		Color aspect_color = RenderUtil.getAspectColor(aspect_name);

		int challenge_count = positive_annots != null ? positive_annots.size() : 0;
		JPanel title_panel = createTitlePanel(challenge_count, aspect_color, notStrings);

		challenge_pane.add(title_panel);

		if (challenge) {
			JPanel annot_panel = createChallengedAnnotPanel(positive_annots, aspect_color);
			challenge_pane.add(annot_panel);
		}

		if (negation) {
			JPanel reason_panel = createEvidenceCodePanel(notStrings, aspect_color);
			challenge_pane.add(reason_panel);
		}

		JPanel button_panel = createButtonPanel(aspect_color);
		challenge_pane.add(button_panel);

		return challenge_pane;
	}

	/** Creates the panel shown by the first tab. */
	private JPanel createTitlePanel(int annot_count, Color aspect_color, String [] notStrings) {
		String user_information;
		if (challenge) {
			if (!negation) {
				user_information = "Challenge this annotation?";
			} else {
				user_information = "Warning, you are about to create a NOT annotation.\n\nThis action is in conflict with the ";
				user_information += (annot_count > 1 ? "annotations" : "annotation") + " below." ;
			}
		} else {
			user_information = "Please indicate the type of evidence for the loss";
		}
		JTextArea caption = new JTextArea(user_information);
		caption.setFont(new Font(caption.getFont().getFontName(), Font.BOLD, 14)); 
		caption.setBackground(aspect_color);
		caption.setEditable(false);

		JPanel box = new JPanel();
		box.setLayout(new BorderLayout());
		box.add(caption, BorderLayout.CENTER);
		box.setOpaque(true);
		box.setBackground(aspect_color);
		box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return box;
	}

	private JPanel createChallengedAnnotPanel(List<GeneAnnotation> positive_annots, Color aspect_color) {

		JPanel annotListPane = createAnnotListPane(positive_annots, aspect_color);

		JPanel rationalePane = createRationalePane(aspect_color);

		JPanel box = new JPanel();
		box.setLayout(new BorderLayout());		
		box.add(annotListPane, BorderLayout.NORTH);
		box.add(rationalePane, BorderLayout.CENTER);
		box.setOpaque(true);
		box.setBackground(aspect_color);
		box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		box.setBackground(aspect_color);
		return box;
	}

	private JPanel createAnnotListPane(List<GeneAnnotation> positive_annots, Color aspect_color) {
		JPanel annotListPane = new JPanel();
		annotListPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		annotListPane.setBackground(aspect_color);
		annotListPane.setLayout(new BorderLayout());

		DefaultListModel<String> annot_list = new DefaultListModel<>();
		JList<String> questionable = new JList<>(annot_list);
		questionable.setEnabled(false);
		questionable.setVisibleRowCount(-1);
		questionable.setForeground(Color.black);
		for (GeneAnnotation positive_annot : positive_annots) {
			Bioentity leaf = positive_annot.getBioentityObject();
			String pretty = leaf.getId() + "/" + leaf.getSeqDb() + ":" + leaf.getSeqId();
			pretty += " - ";
			pretty += OWLutil.inst().getTermLabel(positive_annot.getCls());
			pretty += " (" + positive_annot.getCls() + ")";
			annot_list.addElement(pretty);
		}
		JScrollPane annotScroller = new JScrollPane(questionable);
		annotScroller.setPreferredSize(new Dimension(250, 80));
		annotListPane.add(annotScroller, BorderLayout.CENTER);

		return annotListPane;
	}

	private JPanel createRationalePane(Color aspect_color) {
		JPanel rationalePane = new JPanel();
		rationalePane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		rationalePane.setBackground(aspect_color);
		rationalePane.setLayout(new BorderLayout());

		JLabel label = new JLabel("Please provide your reasons for the challenge");
		label.setBackground(aspect_color);
		rationalePane.add(label, BorderLayout.NORTH);

		reason_text = new JTextArea(5, 20);
		reason_text.setEditable(true);
		reason_text.setFont(new Font("Serif", Font.ITALIC, 16));
		reason_text.setLineWrap(true);
		reason_text.setWrapStyleWord(true);
		JScrollPane rationaleScroller = new JScrollPane(reason_text); 
		rationaleScroller.setPreferredSize(new Dimension(250, 150));
		rationalePane.add(rationaleScroller, BorderLayout.CENTER);
		
		warning = new JLabel("You must provide an explanation");
		warning.setForeground(Color.red);
		warning.setBackground(aspect_color);
		warning.setVisible(false);
		rationalePane.add(warning, BorderLayout.SOUTH);

		return rationalePane;
	}

	private JPanel createButtonPanel(Color aspect_color) {
		//Create the components.
		JButton ok = new JButton("Okay");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (challenge && (reason_text == null || reason_text.getText().isEmpty())) {
					warning.setVisible(true);
				} else {
					proceed = true;
					ChallengeDialog.this.dispose();
				}
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				proceed = false;
				ChallengeDialog.this.dispose();
			}
		});

		//Lay them out.
		JPanel buttonPane = new JPanel();
		buttonPane.setOpaque(true);
		buttonPane.setBackground(aspect_color);
		getRootPane().setDefaultButton(cancel);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(ok);
		buttonPane.add(cancel);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		return buttonPane;
	}

	private JPanel createEvidenceCodePanel(String [] notStrings, Color aspect_color) {
		JPanel evidencePane = new JPanel();
		evidencePane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		evidencePane.setBackground(aspect_color);
		if (negation) {
			evidencePane.setLayout(new GridLayout(0, 1));
			ButtonGroup group = new ButtonGroup();
			for (int i = 0; i < notStrings.length; i++) {
				String not_str = notStrings[i];
				JRadioButton not_button = new JRadioButton(not_str);
				not_button.setActionCommand(not_str);
				group.add(not_button);
				not_button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						evidence_type = e.getActionCommand();
					}
				});
				evidencePane.add(not_button);
				if (i == 0) {
					not_button.setSelected(true);
					evidence_type = not_str;
				}
			}
		}
		return evidencePane;
	}

}

