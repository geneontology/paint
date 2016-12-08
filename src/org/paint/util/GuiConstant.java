package org.paint.util;

import java.awt.Color;
import java.awt.Font;

public class GuiConstant {
	public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12); 
	public static final int HIGHLIGHT_BP = 1;
	public static final int HIGHLIGHT_CC = 2;
	public static final int HIGHLIGHT_MF = 4;
	public static final int HIGHLIGHT_ALLGO = 6;
	public static final int HIGHLIGHT_PRUNE = 3;
	public static final int HIGHLIGHT_CHALLENGE = 5;

	public static final  Color FOREGROUND_COLOR = Color.black;
	public static final  Color BACKGROUND_COLOR = Color.white;
//	public static final  Color SELECTION_COLOR = new Color(216,154,26);
	public static final  Color SELECTION_COLOR = new Color(249, 198, 41);

	// greens
	public static final Color mf_inf_color = new Color(180, 210, 170);//new Color(232, 255, 224);
	public static final Color mf_mrc_color = new Color(105, 255, 105);
	public static final Color mf_exp_color = new Color(52, 159, 52);
	
	// blues
	public static final Color bp_inf_color = new Color(199, 223, 243);
	public static final Color bp_mrc_color = new Color(20, 38, 255);
	public static final Color bp_exp_color = new Color(110, 168, 230);
	
	// turquoise
	public static final Color cc_inf_color = new Color(197, 243, 233);
	public static final Color cc_mrc_color = new Color(8, 232, 222);
	public static final Color cc_exp_color = new Color(28, 133, 136);
	
//	public static final Color all_inf_color = new Color(190, 185, 175);
//	public static final Color all_mrc_color = new Color(0, 0, 0);
//	public static final Color all_exp_color = new Color(91, 86, 81);
}
