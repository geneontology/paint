package org.paint.dialog;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.paint.config.IconResource;
import org.paint.config.PaintConfig;
import org.paint.gui.event.CurationColorEvent;
import org.paint.gui.event.EventManager;

public class CurationStatusColorPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private Color oldExperimentalColor = PaintConfig.inst().expPaintColor;
	private Color oldCuratedColor = PaintConfig.inst().curatedPaintColor;
	private Color oldInferColor = PaintConfig.inst().inferPaintColor;

	private JColorChooser chooser = new JColorChooser();

	private JRadioButton experimental = new JRadioButton("Experimental");
	private JRadioButton curated = new JRadioButton("Curated");
	private JRadioButton inferred = new JRadioButton("Inferred");
	
	public CurationStatusColorPane() {
		super();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final CustomPreviewPanel previewPanel = new CustomPreviewPanel(chooser);
		
		previewPanel.setSize(previewPanel.getPreferredSize());
		previewPanel.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));

		experimental.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldExperimentalColor);
					chooser.setColor(PaintConfig.inst().expPaintColor);
				}
			}
		});
		experimental.setSelected(true);

		curated.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldCuratedColor);
					chooser.setColor(PaintConfig.inst().curatedPaintColor);
				}
			}
		});

		inferred.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldInferColor);
					chooser.setColor(PaintConfig.inst().inferPaintColor);
				}
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(experimental);
		group.add(curated);
		group.add(inferred);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		radioPanel.add(experimental);
		radioPanel.add(curated);
		radioPanel.add(inferred);

		chooser.setPreviewPanel(previewPanel);
		chooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				PaintConfig preferences = PaintConfig.inst();
				if (experimental.isSelected()) {
					preferences.expPaintColor = chooser.getColor();
				}
				else if (curated.isSelected()) {
					preferences.curatedPaintColor = chooser.getColor();
				}
				else if (inferred.isSelected()) {
					preferences.inferPaintColor = chooser.getColor();
				}
				CurationColorEvent colorEvent = new CurationColorEvent(this);
				EventManager.inst().fireCurationColorEvent(colorEvent);
			}
		});

		add(radioPanel);
		add(chooser);
	}	

	public void cancelColorChange() {
		PaintConfig preferences = PaintConfig.inst();
		preferences.expPaintColor = oldExperimentalColor;
		preferences.curatedPaintColor = oldCuratedColor;
		preferences.inferPaintColor = oldInferColor;
		CurationColorEvent colorEvent = new CurationColorEvent(this);
		EventManager.inst().fireCurationColorEvent(colorEvent);
	}

	public void resetColorChange() {
		cancelColorChange();
		Color c = chooser.getColor();
		if (experimental.isSelected()) {
			c = oldExperimentalColor;
		}
		else if (curated.isSelected()) {
			c = oldCuratedColor;
		}
		else if (inferred.isSelected()) {
			c = oldInferColor;
		}
		chooser.setColor(c);
		CurationColorEvent colorEvent = new CurationColorEvent(this);
		EventManager.inst().fireCurationColorEvent(colorEvent);
	}
	
}
