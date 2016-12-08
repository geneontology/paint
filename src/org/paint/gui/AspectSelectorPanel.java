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

package org.paint.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.bbop.phylo.util.OWLutil;
import org.paint.config.PaintConfig;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.util.GuiConstant;

public class AspectSelectorPanel extends JPanel implements AspectChangeListener, TermSelectionListener {

	private static final long serialVersionUID = 1L;

//	private JRadioButton allButton;
	private JRadioButton bpButton;
	private JRadioButton ccButton;
	private JRadioButton mfButton;

	private Border plainBorder;

	public AspectSelectorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		//		setOpaque(true);
		//		setBackground(Preferences.inst().getBackgroundColor());

//		allButton = new JRadioButton("  All annotations  ", true);
		mfButton = new JRadioButton("  Molecular Function  ", false);
		ccButton = new JRadioButton("  Cellular Component  ", false);
		bpButton = new JRadioButton("  Biological Process  ", false);

		AspectSelectorListener aspectSelectionListener = new AspectSelectorListener();
//		allButton.addActionListener(aspectSelectionListener);
		bpButton.addActionListener(aspectSelectionListener);
		ccButton.addActionListener(aspectSelectionListener);
		mfButton.addActionListener(aspectSelectionListener);

		add(mfButton);
		add(ccButton);
		add(bpButton);
//		add(allButton);

		ButtonGroup group = new ButtonGroup();
		group.add(mfButton);
		group.add(ccButton);
		group.add(bpButton);
//		group.add(allButton);

		mfButton.setOpaque(true);
		ccButton.setOpaque(true);
		bpButton.setOpaque(true);
//		allButton.setOpaque(true);

		mfButton.setBackground(GuiConstant.mf_inf_color);
		ccButton.setBackground(GuiConstant.cc_inf_color);
		bpButton.setBackground(GuiConstant.bp_inf_color);
//		allButton.setBackground(GuiConstant.all_inf_color);
		
		plainBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		mfButton.setMargin(new Insets(2, 12, 2, 12));

		mfButton.setBorderPainted(true);
		ccButton.setBorderPainted(true);
		bpButton.setBorderPainted(true);
//		allButton.setBorderPainted(true);

		mfButton.setBorder(plainBorder);
		ccButton.setBorder(plainBorder);
		bpButton.setBorder(plainBorder);
//		allButton.setBorder(plainBorder);

		EventManager.inst().registerAspectChangeListener(this);
		EventManager.inst().registerTermListener(this);
	}

	class AspectSelectorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton)e.getSource();
			switchAspect(button);
		}
	}

	public void handleAspectChangeEvent(AspectChangeEvent event) {
		if (event.getSource() == this)
			return;

		if (mfButton.isSelected()) {
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
		if (bpButton.isSelected()) {
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
		else if (ccButton.isSelected()) {
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
//		else if (allButton.isSelected()) {
//			mfButton.setBackground(GuiConstant.mf_inf_color);
//			ccButton.setBackground(GuiConstant.cc_inf_color);
//			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
//		}
	}

	public void handleTermEvent(TermSelectEvent e) {
		String term = e.getSelectedTerm();
		if (term != null) {
			String term_aspect = OWLutil.inst().getAspect(term);
			AspectSelector aspect_setter = AspectSelector.inst();
			String current = aspect_setter.getAspectCode();
			String aspect_name = aspect_setter.getAspectName4Code(term_aspect);
			if (!term_aspect.equals(current)) {
				aspect_setter.setAspect(term_aspect);
				if (aspect_name.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString())) {
					bpButton.setSelected(true);
				}
				else if (aspect_name.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString())) {
					ccButton.setSelected(true);
				}
				if (aspect_name.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString())) {
					mfButton.setSelected(true);
				}
			}
		}
	}

	private void switchAspect(JRadioButton button) {
//		PaintConfig prefs = PaintConfig.inst();
		if (button == bpButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.BIOLOGICAL_PROCESS);
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
		else if (button == ccButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.CELLULAR_COMPONENT);
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
		else if (button == mfButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.MOLECULAR_FUNCTION);
			mfButton.setBackground(GuiConstant.mf_inf_color);
			ccButton.setBackground(GuiConstant.cc_inf_color);
			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
		}
//		else if (button == allButton) {
//			AspectSelector.inst().setAspect(AspectSelector.Aspect.ALL_TERMS);
//			mfButton.setBackground(GuiConstant.mf_inf_color);
//			ccButton.setBackground(GuiConstant.cc_inf_color);
//			bpButton.setBackground(GuiConstant.bp_inf_color);
//			allButton.setBackground(GuiConstant.all_inf_color);
//		}
	}
}
