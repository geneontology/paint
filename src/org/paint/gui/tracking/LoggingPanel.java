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

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.bbop.phylo.tracking.LogAction;
import org.bbop.phylo.tracking.LogEntry.LOG_ENTRY_TYPE;
import org.bbop.phylo.util.Constant;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.ChallengeEvent;
import org.paint.gui.event.ChallengeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.util.GuiConstant;

public class LoggingPanel extends JPanel implements 
FamilyChangeListener, 
AnnotationChangeListener,
ChallengeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static LoggingPanel singleton;

	private JTextArea annotation_log;
	private int aspect;

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
	 * Challenge mechanism
	 * 	
	 */
	public LoggingPanel(Border border, int aspect) {
		super ();
		this.aspect = aspect;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				
		annotation_log = new JTextArea();
		annotation_log.setEditable(false);
		annotation_log.setLineWrap(true);
		annotation_log.setWrapStyleWord(true);

		setOpaque(true);
	
		setBorder(border);
		setBackground(GuiConstant.BACKGROUND_COLOR);
		add(annotation_log);
		setLogText();
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
		EventManager.inst().registerChallengeListener(this);
	}

	private void setLogText() {
		annotation_log.setText("");
		List<String> contents = new ArrayList<>();
		switch (aspect) {
        case GuiConstant.HIGHLIGHT_BP:
			LogAction.inst().reportBP(contents, Constant.BP);
			break;
        case GuiConstant.HIGHLIGHT_CC:
			LogAction.inst().reportCC(contents, Constant.CC);
			break;
        case GuiConstant.HIGHLIGHT_MF:
			LogAction.inst().reportMF(contents, Constant.MF);
 			break;
        case GuiConstant.HIGHLIGHT_PRUNE:
			LogAction.inst().reportEntries(contents, LOG_ENTRY_TYPE.PRUNE);
			break;
        case GuiConstant.HIGHLIGHT_CHALLENGE:
        	LogAction.inst().reportEntries(contents, LOG_ENTRY_TYPE.CHALLENGE);
        	break;

		}
		StringBuffer text = new StringBuffer();
		for (String entry : contents) {
			if (entry.length() > 0)
				text.append(entry + '\n');
		}
		annotation_log.setText(text.toString());
		repaint();
	}
	
	protected void clearLog() {
		annotation_log.setText("");
	}

	@Override
	public void newFamilyData(FamilyChangeEvent e) {
		setLogText();
	}

	@Override
	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		setLogText();
	}

	@Override
	public void handleChallengeEvent(ChallengeEvent event) {
		setLogText();		
	}
}
