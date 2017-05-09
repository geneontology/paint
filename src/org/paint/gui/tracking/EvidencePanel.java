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

package org.paint.gui.tracking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bbop.phylo.model.Family;
import org.bbop.phylo.tracking.LogAction;
import org.bbop.phylo.tracking.LogAlert;
import org.bbop.phylo.tracking.Logger;
import org.paint.gui.AbstractPaintGUIComponent;
import org.paint.gui.AspectSelector.Aspect;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.main.PaintManager;
import org.paint.util.GuiConstant;

public class EvidencePanel extends AbstractPaintGUIComponent implements FamilyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static EvidencePanel singleton;

	private JTextArea comment_text;
	private JTextArea warning_text;
	private LoggingPanel mf_panel;
	private LoggingPanel cc_panel;
	private LoggingPanel bp_panel;
	private LoggingPanel prune_panel;
	private LoggingPanel challenge_panel;

	private boolean user_comment;
	private CommentListener comment_listener;

	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EvidencePanel.class);

	/*
	 * Separated into sections by aspect ?
	 * Include dates?
	 * Section for References
	 * 	http://en.wikipedia.org/wiki/SKI_protein
	 * 	PMID: 19114989 
	 * Phylogeny
	 * 	Two main clades, SKOR and SKI/SKIL, plus an outlier from Tetrahymena which aligns poorly, so I have not annotated AN0.

	 * Propagate GO:0004647 "phosphoserine phosphatase activity" to AN1 and GO:0016791 "phosphatase activity" to AN0.
	 * -Propagate "cytoplasm" to AN1 based on 3 annotations.
	 * -Propagate "chloroplast" to plants/chlamy.
	 * 
	 * Need to also log all NOTs
	 * 
	 * Challenge mechanism
	 * 	
	 */
	public EvidencePanel() {
		super("evidence:evidence");
		user_comment = false;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(GuiConstant.BACKGROUND_COLOR);

		JPanel pane = new JPanel();
		pane.setOpaque(true);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		LogAction.inst().clearLog();
		LogAlert.clearLog();

		initCommentPane();
		pane.add(comment_text);

		initWarningPane();
		pane.add(warning_text);

		mf_panel = initLogPane(GuiConstant.HIGHLIGHT_MF, Aspect.MOLECULAR_FUNCTION.toString());
		cc_panel = initLogPane(GuiConstant.HIGHLIGHT_CC, Aspect.CELLULAR_COMPONENT.toString());
		bp_panel = initLogPane(GuiConstant.HIGHLIGHT_BP, Aspect.BIOLOGICAL_PROCESS.toString());
		prune_panel = initLogPane(GuiConstant.HIGHLIGHT_PRUNE, "Pruned");
		challenge_panel = initLogPane(GuiConstant.HIGHLIGHT_CHALLENGE, "Challenged");
		pane.add(mf_panel);
		pane.add(cc_panel);
		pane.add(bp_panel);
		pane.add(prune_panel);
		pane.add(challenge_panel);

		JScrollPane scrollPane = new JScrollPane(new OnlyVerticalScrollPanel(pane));
		add(scrollPane, BorderLayout.CENTER);
		super.setTitle(getPaintEvidenceAcc());
		EventManager.inst().registerFamilyListener(this);
	}

	public static EvidencePanel inst() {
		if (singleton == null) 
			singleton = new EvidencePanel();
		return (EvidencePanel) singleton;
	}

	private void initCommentPane() {
		//  add the text pane to a JPanel using a BorderLayout and 
		// then add the panel to the scroll pane:

		comment_text = new JTextArea();
		comment_text.setOpaque(true);
		comment_text.setLineWrap(true);
		comment_text.setWrapStyleWord(true);
		Border titled = loggerBorder(GuiConstant.BACKGROUND_COLOR, "NOTES");
		comment_text.setBorder(titled);
		comment_text.setText("");

		comment_listener = new CommentListener(comment_text);
		comment_text.getDocument().addDocumentListener(comment_listener);

	}

	private void initWarningPane() {
		warning_text = new JTextArea();
		warning_text.setOpaque(true);
		warning_text.setEditable(false);
		warning_text.setLineWrap(true);
		warning_text.setWrapStyleWord(true);
		Border titled = loggerBorder(Color.RED.darker(), "WARNINGS");
		warning_text.setBorder(titled);
		warning_text.setVisible(false);
		warning_text.setText("");
	}

	private LoggingPanel initLogPane(int aspect, String log_category) {
		Color border_color = Color.black;
		switch (aspect) {
		case GuiConstant.HIGHLIGHT_BP:
			border_color = GuiConstant.bp_inf_color;
		case GuiConstant.HIGHLIGHT_CC:
			border_color = GuiConstant.cc_inf_color;
		case GuiConstant.HIGHLIGHT_MF:
			border_color = GuiConstant.mf_inf_color;
			break;
		case GuiConstant.HIGHLIGHT_PRUNE:
			border_color = Color.GRAY;
			break;
		case GuiConstant.HIGHLIGHT_CHALLENGE:
			border_color = Color.yellow.darker();
			break;
		}
		Border border = loggerBorder(border_color, log_category);
		return (new LoggingPanel(border, aspect));
	}

	private Border loggerBorder(Color border_color, String category) {
		Border raisedbevel = BorderFactory.createBevelBorder(BevelBorder.RAISED, border_color, border_color.darker());
		Border loweredbevel = BorderFactory.createBevelBorder(BevelBorder.LOWERED, border_color, border_color.darker());
		Border compound = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
		Border titled = BorderFactory.createTitledBorder(
				compound, category,
				TitledBorder.LEFT,
				TitledBorder.TOP);		
		return titled;
	}

	/**
	 * A panel that, when placed in a {@link JScrollPane}, only scrolls vertically and resizes horizontally as needed.
	 */
	@SuppressWarnings("serial")
	private class OnlyVerticalScrollPanel extends JPanel implements Scrollable
	{
		public OnlyVerticalScrollPanel()
		{
			this(new GridLayout(0, 1));
		}

		public OnlyVerticalScrollPanel(LayoutManager lm)
		{
			super(lm);
		}

		public OnlyVerticalScrollPanel(Component comp)
		{
			this();
			add(comp);
		}

		public Dimension getPreferredScrollableViewportSize()
		{
			return(getPreferredSize());
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction)
		{
			return(10);
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction)
		{
			// might want to change this to be governed by amount of text
			return(100);
		}

		public boolean getScrollableTracksViewportWidth()
		{
			return(true);
		}

		public boolean getScrollableTracksViewportHeight()
		{
			return(false);
		}
	}

	private String getPaintEvidenceAcc() {
		/*
		 * Use the PANTHER family name as the reference
		 * Update every time in case a new book is loaded
		 */
		Family family = PaintManager.inst().getFamily();

		if (family != null) {
			String pthr_id = family.getFamily_name();
			int acc = Integer.valueOf(pthr_id.substring("PTHR".length())).intValue();
			String paint_id = String.format("%1$07d", acc);
			return paint_id;
		} else
			return "";
	}

	public void newFamilyData(FamilyChangeEvent e) {
		user_comment = false;
		String comment = getLoggedComment();
		updateText(comment_text, comment);
		log.info("set comment");
		String warnings = getWarnings();
		if (warnings != null && warnings.length() > 0) {
			updateText(warning_text, warnings);
			log.info("set warnings");
			warning_text.setVisible(true);
		} else {
			log.info("no warnings");
			warning_text.setVisible(false);
		}
	};

	private String getLoggedComment() {
		List<String> contents = new ArrayList<>();
		Logger.logNotes(contents);
		StringBuffer buf = new StringBuffer();
		for (String line : contents) {
			buf.append(line + '\n');
		}
		return buf.toString();
	}

	private String getWarnings() {
		List<String> contents = new ArrayList<>();
		LogAlert.report(contents);
		StringBuffer buf = new StringBuffer();
		for (String line : contents) {
			buf.append(line + '\n');
		}
		return buf.toString();
	}

	public class CommentListener implements DocumentListener {
		JTextArea textarea;

		public CommentListener(JTextArea textarea) {
			this.textarea = textarea;
		}

		public void removeUpdate(DocumentEvent e) {
			if (user_comment) {
				DirtyIndicator.inst().dirtyGenes(user_comment);
				String comment = comment_text.getText();
				SwingUtilities.invokeLater(new CommentTask(comment));
			}
		}

		public void insertUpdate(DocumentEvent e) {
			if (user_comment) {
				DirtyIndicator.inst().dirtyGenes(user_comment);
				String comment = comment_text.getText();
				SwingUtilities.invokeLater(new CommentTask(comment));
			}
		}

		public void changedUpdate(DocumentEvent e) {
			if (user_comment) {
				DirtyIndicator.inst().dirtyGenes(user_comment);
				String comment = comment_text.getText();
				SwingUtilities.invokeLater(new CommentTask(comment));
			}
		}
	}

	private class CommentTask implements Runnable {
		String comment;

		CommentTask(String comment) {
			this.comment = comment;
		}

		public void run() {
			Logger.updateNotes(comment);
		}
	}

	public class TextTask {
		public void setText(JTextArea outputArea, String messageToAppend) {
			outputArea.setText(messageToAppend);
			user_comment = true;
		}
	}

	private void updateText(JTextArea textarea, String text) {
		final JTextArea outputArea = textarea;
		final String messageToAppend = text;
		Thread text_thread = new Thread(new Runnable() {
			public void run() {
				final TextTask a = new TextTask();
				if (SwingUtilities.isEventDispatchThread()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							a.setText(outputArea, messageToAppend);
						}
					});
				} else {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								a.setText(outputArea, messageToAppend);
							}
						});
					} catch (InterruptedException ex) {
						log.info(ex.getLocalizedMessage());
					} catch (InvocationTargetException ex) {
						log.info(ex.getMessage());
					}
				}
			}
		});
		text_thread.start();
	}
}


