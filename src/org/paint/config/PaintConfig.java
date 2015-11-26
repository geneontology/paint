package org.paint.config;

import java.awt.Color;
import java.io.StringWriter;

import org.yaml.snakeyaml.Yaml;
import org.bbop.phylo.config.TouchupConfig;
import org.paint.util.GuiConstant;

public class PaintConfig extends TouchupConfig {

//	private static Logger LOG = Logger.getLogger(PaintConfig.class);

	public static final String PREF_FILE = "config/preferences.yaml";
	
	public boolean use_distances;
	public double tree_distance_scaling; 
	public boolean collapse_no_exp;

	public Color expPaintColor;
	public Color curatedPaintColor;
	public Color inferPaintColor;

	public Color mfPaintColor;
	public Color ccPaintColor;
	public Color bpPaintColor;

	public float[] msa_threshold;
	public float[]  msa_weighted_threshold;

	public Color[]  msa_colors;
	public Color[]  msa_weighted_colors;

	public boolean full_msa;
	public boolean weighted;

	private static PaintConfig preferences;

	/**
	 * Constructor declaration
	 * @throws Exception
	 *
	 *
	 * @see
	 */
	public static PaintConfig inst() {
		if (preferences == null) {
			preferences = new PaintConfig();
		}
		return preferences;
	}

	// Define the defaults for optional fields.
	public PaintConfig() {
		super();
		use_distances = true;
		tree_distance_scaling = 50; 
		expPaintColor = new Color(142, 35, 35);
		curatedPaintColor = new Color(255, 127, 0);
		inferPaintColor = new Color(16, 64, 128);

		mfPaintColor = new Color(232, 248, 232);
		ccPaintColor = new Color(224, 248, 255);
		bpPaintColor = new Color(255, 248, 220);

		msa_threshold = new float[3];
		msa_threshold[0] = 80f;
		msa_threshold[1] = 60f;
		msa_threshold[2] = 40f;

		msa_weighted_threshold = new float[2];
		msa_weighted_threshold[0] = 90f;
		msa_weighted_threshold[1] = 75f;

		msa_colors = new Color[3];
		msa_colors[0] = new Color(51, 102, 77);
		msa_colors[1] = new Color(112, 153, 92);
		msa_colors[2] = new Color(204, 194, 143);

		msa_weighted_colors = new Color[2];
		msa_weighted_colors[0] = new Color(21, 138, 255);
		msa_weighted_colors[1] = new Color(220, 233, 255);

		full_msa = true;
		weighted = true;
		
		collapse_no_exp = false;
		
		preferences = this;

	}
	
	public static void main(String[] args) {
		PaintYaml configManager = new PaintYaml();
		configManager.loadConfig(PREF_FILE);
		PaintConfig blah = PaintConfig.inst();
		String s = blah.save();
		System.out.println(s);
		blah.save(PREF_FILE);
	}
		
	public Color getAspectColor(int aspect) {
		Color color = GuiConstant.BACKGROUND_COLOR;
		if (aspect > 0) {
			switch (aspect) {
			case GuiConstant.HIGHLIGHT_MF:
				color = mfPaintColor;
				break;
			case GuiConstant.HIGHLIGHT_CC:
				color = ccPaintColor;
				break;
			case GuiConstant.HIGHLIGHT_BP:
				color = bpPaintColor;
				break;
			}
		}
		return color;
	}

	public void setAspectColor(int aspect, Color color) {
		if (aspect > 0) {
			switch (aspect) {
			case GuiConstant.HIGHLIGHT_MF:
				mfPaintColor = color;
				break;
			case GuiConstant.HIGHLIGHT_CC:
				ccPaintColor = color;
				break;
			case GuiConstant.HIGHLIGHT_BP:
				bpPaintColor = color;
				break;
			}
		}
	}

	public float[] getMSAThresholds(boolean weighted) {
		if (weighted)
			return msa_weighted_threshold;
		else
			return msa_threshold;
	}

	public Color[] getMSAColors(boolean weighted) {
		if (weighted)
			return msa_weighted_colors;
		else
			return msa_colors;
	}

	public void setMSAThresholds(boolean weighted, float[] thresholds) {
		if (weighted) 
			msa_weighted_threshold = thresholds;
		else
			msa_threshold = thresholds;
	}

	public void setMSAColors(boolean weighted, Color[] colors) {
		if (weighted) 
			msa_weighted_colors = colors;
		else
			msa_colors = colors;
	}
	
	public String save() {
		PaintYaml configManager = new PaintYaml();
		Yaml yaml = configManager.getYaml();
		StringWriter writer = new StringWriter();
		yaml.dump(this, writer);
		return writer.toString();
	}
}

