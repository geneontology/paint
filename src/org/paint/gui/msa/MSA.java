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
package org.paint.gui.msa;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Bioentity;
import org.paint.config.PaintConfig;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.main.PaintManager;
import org.paint.util.GuiConstant;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class MSA {

	private boolean have_weights;

	/* by row and then by column */
	private Hashtable<DisplayBioentity, Color[]> nodeToColor = new Hashtable<DisplayBioentity, Color[]>();

	private static final String              msaFont = "Monospaced";          // Has to be fixed width font

	private static Logger log = Logger.getLogger(MSA.class);

	private boolean colors_initialized = false;
	private Font          font;
	private AminoAcidStats[] aminoAcidStats;

	private int selectedCol = -1;               // Selected column

	// Refers to sequence range that is currently visible (x-coord is the start position and y-coord is the end position
	private int [] seq_range;

	private static final int START_BASE = 0;
	private static final int END_BASE = 1;

	private final int    SEGMENTS = 25;
	private final int    SUB_SEGMENTS = 5;
	
	private final char [] full_ruler;
	private char [] condense_ruler;
	private final int seq_length;

	private int hmm_length;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param list
	 * @param gridInfo
	 *
	 * @see
	 */
	public MSA(List<String> list, List<String> list2) {
		MSAParser parser = MSAParser.inst();
		seq_length = parser.parseSeqs(list);
		full_ruler = setRuler(seq_length);
		condense_ruler = setCondensedSequences(PaintManager.inst().getRows(), seq_length);
		if (seq_length < SEGMENTS) {
			condense_ruler = "Sequence".toCharArray();
		}

		have_weights = parser.parseWts(list2);
		Font f = GuiConstant.DEFAULT_FONT;
		setFont(new Font(msaFont, f.getStyle(), f.getSize()));
		setColors();
	}

	protected void reorderRows(List<Bioentity> list) {
		// keep a local copy of the currently visible nodes (i.e. rows)
		colors_initialized = false;	// Reordering should not change colors, but might?
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param f
	 *
	 * @see
	 */
	protected void setFont(Font f){
		font = f;
	}

	protected void updateColors() {
		colors_initialized = false;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
	public void draw(Graphics g, Rectangle viewport){
		if (g == null) {
			return;
		}
		setColors();
		if (!colors_initialized) {
			g.drawString("No display specified", 100, 100);
			return;
		}
		displayPid(g, viewport);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @return
	 *
	 * @see
	 */
	public Rectangle getGridSize(Graphics g){
		if (null == font){
			return null;
		}
		Rectangle   r = new Rectangle();
		int row_height = PaintManager.inst().getRowHeight();
		int colWidth = getColumnWidth(g);
		if (row_height == 0) {
			log.warn("Row height is not set");
			row_height = 16;
		}
		r.height = row_height;
		r.width = 0;
		List<Bioentity> nodes = PaintManager.inst().getRows();
		if (nodes != null){
			r.height += nodes.size() * row_height;
			r.width += (getSeqLength(PaintConfig.inst().full_msa)) * colWidth;
		}
		return r;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
	private void displayPid(Graphics g, Rectangle viewport) {
		List<Bioentity> nodes = PaintManager.inst().getRows();
		if (nodes == null || nodes.size() == 0)
			return;

		g.setFont(font);

		int row_height = PaintManager.inst().getRowHeight();
		int curHeight = PaintManager.inst().getTopMargin();

		int charWidth = this.getColumnWidth(g);

		int headerHeight = PaintManager.inst().getTopMargin();
		int topInset = (headerHeight - font.getSize()) / 2;

		int header_y = viewport.y + headerHeight - topInset - 1;
		// Display the header at the top of the viewport
		if (Color.white != g.getColor()) {
			g.setColor(Color.white);
		}
		g.fillRect(viewport.x, viewport.y, viewport.width, headerHeight - topInset - 1);

		g.setColor(Color.black);

		seq_range = setSeqRange(viewport, charWidth, aminoAcidStats.length);
		int seq_x_position = viewport.x; // + seq_range[0] * charWidth + 1;

		int column_x = seq_x_position;
		final char [] ruler = getRuler(PaintConfig.inst().full_msa);
		for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
			g.drawChars(ruler, column, 1, column_x, header_y);
			column_x += charWidth;
		}

		for (int row = 0; row < nodes.size(); row++){
			DisplayBioentity  node = (DisplayBioentity) nodes.get(row);
			if (!node.isTerminus())
				continue;

			// Display this row if it is within the viewport range, but otherwise continue
			if ((viewport.y > (curHeight + row_height)) || ((viewport.y + viewport.height) < curHeight)){
				curHeight += row_height;
				continue;
			}

			String seq = (PaintConfig.inst().full_msa ? node.getSequence() : node.getHMMSeq());
			if ((null == seq) || (0 == seq.length())){
				curHeight += row_height;
				continue;
			}

			Color nodeColors[] = nodeToColor.get(node);
			if (null == nodeColors) {
				curHeight += row_height;
				continue;
			}

			if (node.isPruned()) {
				curHeight += row_height;
				continue;
			}

			Font f;
			if (node.isSelected()) {
				f = new Font(msaFont, Font.BOLD, font.getSize());
			}
			else {
				f = font;
			}

			column_x = seq_x_position;
			int seq_y = curHeight + row_height - topInset - 1;
			final char [] seq_chars = seq.toCharArray();
			for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
				Color color = nodeColors[column];
				if (column == selectedCol) {
					g.setColor(Color.pink);
					g.fillRect(column_x, curHeight, charWidth, row_height);
				}
				else {
					g.setColor(color);
					g.fillRect(column_x, curHeight, charWidth, row_height);
				}
				g.setColor(Color.black);
				g.setFont(f);
				g.drawChars(seq_chars, column, 1, column_x, seq_y);
				column_x += charWidth;
			}
			curHeight += row_height;
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param startPos
	 * @param viewport
	 *
	 * @return
	 *
	 * @see
	 */
	private int [] setSeqRange(Rectangle viewport, int charWidth, int seqMaxLen){
		seq_range  = new int [2];
		// Set the range start value
		seq_range[START_BASE] = ((viewport.x + 1) / charWidth) - 1;
		seq_range[START_BASE] = Math.max(0, seq_range[START_BASE]);

		// Set the end value
		seq_range[END_BASE] = seq_range[START_BASE] + (viewport.width / charWidth) + 1;

		if (seq_range[END_BASE] >= seqMaxLen){
			//seq_range[END_BASE] = seq.length();
			seq_range[END_BASE] = seqMaxLen - 1;
		}
		if (seq_range[START_BASE] >= seqMaxLen) {
			seq_range[START_BASE] = seqMaxLen - 1;
		}
		return seq_range;
	}

	private void setColors() {
		if (colors_initialized)
			return;

		double totalWt = initColumnWeights(isWeighted());
		if (isWeighted()) {
			colors_initialized = setWeightedColor(totalWt);
		} else {
			colors_initialized = setUnweightedColor();
		}
	}

	private boolean setUnweightedColor() {
		nodeToColor.clear();
		Color colors [] = PaintConfig.inst().getMSAColors(false);
		float threshold [] = PaintConfig.inst().getMSAThresholds(false);
		List<Bioentity> nodes = PaintManager.inst().getRows();
		int row_count = nodes != null ? nodes.size() : 0;
		for (int row = 0; row < row_count; row++) {
			DisplayBioentity node = (DisplayBioentity) nodes.get(row);
			String  seq = (PaintConfig.inst().full_msa ? node.getSequence() : node.getHMMSeq());
			int seqLength;
			Color [] columnColors = null;
			if (seq == null) {
				seqLength = -1;
			} else {
				seqLength = seq.length();
				columnColors = new Color[seqLength];
			}
			for (int column = 0; column < seqLength; column++) {
				columnColors[column] = Color.WHITE;
				char c = seq.charAt(column);
				if (c != '.' && c != '-' && c != ' ') {
					AminoAcidStats alignStats = aminoAcidStats[column];
					double frequency = alignStats.getAAFrequency(c);
					// calculate percentage in this column with same aa
					double weight = (frequency * 100) / row_count;
					for (int k = 0; k < threshold.length; k++) {
						if (weight > threshold[k]) {
							columnColors[column] = colors[k];
							break;
						}
					}

				}
			}
			nodeToColor.put(node, columnColors);
		}
		return true;
	}

	private boolean setWeightedColor(double totalWt) {
		nodeToColor.clear();
		Color colors [] = PaintConfig.inst().getMSAColors(true);
		float threshold [] = PaintConfig.inst().getMSAThresholds(true);
		List<Bioentity> nodes = PaintManager.inst().getRows();
		for (int row = 0; nodes != null && row < nodes.size(); row++) {
			DisplayBioentity node = (DisplayBioentity) nodes.get(row);
			String  seq = PaintConfig.inst().full_msa ? node.getSequence() : node.getHMMSeq();
			if (!node.isTerminus() || seq == null)
				continue;

			int seqLength = seq.length();
			Color []columnColors = new Color[seqLength];
			for (int column = 0; column < seqLength; column++) {
				columnColors[column] = Color.WHITE;
				char c = seq.charAt(column);
				if (c != '-' && c != '.') {
					// Get total weight of chars that count
					AminoAcidStats alignStats = aminoAcidStats[column];
					double weight = alignStats.getAAFrequency(c);
					double percent = 0;
					percent = (weight / totalWt) * 100;
					for (int k = 0; k < threshold.length; k++) {
						if (percent > threshold[k]) {
							columnColors[column] = colors[k];
							break;
						}
					}
				}
			}
			nodeToColor.put(node, columnColors);
		}
		return true;
	}

	protected void setSelectedColInfo(Point p, Graphics g) {
		if (null == g) {
			return;
		}
		// Set character width and height, if not already set
		int	charWidth = getColumnWidth(g);

		// Header row
		int length = getSeqLength(PaintConfig.inst().full_msa) * charWidth;
		if (0 <= p.x - length) {
			selectedCol = (p.x - length)/ charWidth;
		}
	}

	protected Bioentity getSelectedGene(Point p, Graphics g) {
		if (null == g) {
			return null;
		}
		// Header row
		int	charWidth = getColumnWidth(g);
		int header_width = getSeqLength(PaintConfig.inst().full_msa) * charWidth;

		int cur_y = PaintManager.inst().getTopMargin();
		// right most x position for the header (where the click should be)
		int right_x = header_width + seq_range[START_BASE] * charWidth;
		if (p.x > right_x || p.y <= cur_y) {
			// user did not click on the header
			return null;
		}
		int row = 0;
		int row_height = PaintManager.inst().getRowHeight();
		List<Bioentity> nodes = PaintManager.inst().getRows();
		while (p.y > cur_y + row_height && nodes != null && row < nodes.size()) {
			cur_y += row_height;
			row++;
		}
		if (row < nodes.size())
			return nodes.get(row);
		else
			return null;
	}

	protected int getColumnWidth(Graphics g) {
		return g.getFontMetrics(font).stringWidth("W") + 3;
	}

	protected Rectangle getSelectionRect(Graphics g, List<Bioentity> previous) {
		if (previous != null && !previous.isEmpty() && seq_range != null) {
			int min_row = -1;
			int max_row = -1;
			List<Bioentity> nodes = PaintManager.inst().getRows();
			if (nodes != null) {
				for (Bioentity node : nodes) {
					int row = nodes.indexOf(node);
					if (min_row < 0 || row < min_row) {
						min_row = row;						
					}
					if (max_row < 0 || row > max_row) {
						max_row = row;
					}					
				}
			} else {
				min_row = 0;
				max_row = 0;
			}
			int	charWidth = getColumnWidth(g);
			int x = seq_range[START_BASE] * charWidth;
			int width = (seq_range[END_BASE] - seq_range[START_BASE]) * charWidth;
			int row_height = PaintManager.inst().getRowHeight();
			int y = PaintManager.inst().getTopMargin() + (min_row * row_height);
			int height = (max_row - min_row + 1) * row_height;
			Rectangle rect = new Rectangle(x, y, width, height);
			return rect;
		} else {
			return null;
		}
	}

	/**
	 * Saves information about the counts at each position of the sequence
	 */
	private double initColumnWeights(boolean weighted) {

		int seq_length = getSeqLength(PaintConfig.inst().full_msa);
		/* this keeps the overall totals for each count of an AA in a column */
		aminoAcidStats = new AminoAcidStats[seq_length];

		// Calculate total weight of sequences for all nodes
		double totalWt = 0;
		// This use to start at one, trying with 0 instead, no need to skip first node
		for (int column = 0; column < seq_length; column++){
			AminoAcidStats alignStats = aminoAcidStats[column];
			if (alignStats == null) {
				alignStats = new AminoAcidStats();
				aminoAcidStats[column] = alignStats;
			}
			List<Bioentity> nodes = PaintManager.inst().getRows();
			if (nodes != null) {
				for (Bioentity node : nodes) {
					if (column == 0)
						totalWt += ((DisplayBioentity) node).getSequenceWt();
					/*
					 * this is the aligned sequence, with dashes inserted, so all of them are the same length
					 * and so we don't have to worry about which column we are counting
					 */
					String  sequence = (PaintConfig.inst().full_msa ? ((DisplayBioentity) node).getSequence() : ((DisplayBioentity) node).getHMMSeq());
					if (sequence == null) {
						continue;
					}
					char aa = sequence.charAt(column);
					double align_frequency = alignStats.getAAFrequency(aa);
					if (weighted) {
						align_frequency += ((DisplayBioentity) node).getSequenceWt();
					} else {
						align_frequency++;
					}
					alignStats.setAAFrequency(aa, align_frequency);
				}
			}
		}
		return totalWt;
	}

	protected void setWeighted(boolean weighted) {
		colors_initialized = (PaintConfig.inst().weighted != weighted);
		PaintConfig.inst().weighted = weighted;
	}

	protected boolean isWeighted() {
		return have_weights && PaintConfig.inst().weighted;
	}

	protected boolean haveWeights() {
		return have_weights;
	}

	protected boolean isFullLength() {
		return PaintConfig.inst().full_msa;
	}

	protected void setFullLength(boolean full) {
		if (full != PaintConfig.inst().full_msa) {
			PaintConfig.inst().full_msa = full;
			colors_initialized = false;
		}
	}
	
	private char [] setRuler(int seqMaxLen) {
		char [] ruler;
		if (seqMaxLen < SEGMENTS){
			ruler = "Sequence".toCharArray();
		}
		else {
			ruler = new char [seqMaxLen];
			for (int i = 0; i < seqMaxLen; i++){
				if (0 == (i + 1) % SEGMENTS){
					String  s = Integer.toString(i + 1);
					for (int j = 0; j < s.length(); j++) {
						ruler[i - s.length() + j] = s.charAt(j);
					}
					ruler[i] = '|';
				}
				else {
					if (0 == (i + 1) % SUB_SEGMENTS) {
						ruler[i] = '\'';
					}
					else {
						ruler[i] = ' ';
					}
				}
			}
		}
		return ruler;
	}

	private char [] setCondensedSequences(List<Bioentity> nodes, int seq_length) {
		int gap_size = 0;
		boolean column_needed;
		StringBuffer ruler = new StringBuffer();

		/* 
		 * Working through the primary sequence one column (amino acid) at a time
		 */
		for (int seq_position = 0; seq_position < seq_length; seq_position++) {
			column_needed = false;
			/*
			 * Find out whether there is any amino acid of significance at this column position
			 * among all the rows 
			 */
			for (int i = 0; i < nodes.size() && !column_needed; i++) {
				DisplayBioentity node = (DisplayBioentity) nodes.get(i);
				if (node.getSequence() != null) {
					String sequence = node.getSequence();
					char  c = sequence.charAt(seq_position);
					if (((c >= 'A') && (c <= 'Z')) || (c == '-')) {
						column_needed = true;
					}
				}
			}
			if (column_needed) {
				gap_size = 0;
			} else {
				gap_size++;
			}

			/*
			 * If just reentering good stuff then need to put the starting position in the ruler
			 */
			if (gap_size < 6 ) {
				if (0 == (seq_position + 1) % 10){
					String  s = Integer.toString(seq_position + 1);
					int pos = ruler.length() - s.length();
					ruler.replace(pos, pos + s.length(), s);
					ruler.append('|');
				}
				else if (0 == (seq_position + 1) % 5) {
					ruler.append('\'');
				}
				else {
					ruler.append(' ');
				}
			} else {
				if (ruler.charAt(ruler.length() - 1) != '~') {
					/*
					 * If just leaving good stuff then need to put the ending position in the ruler
					 */
					int pos = ruler.length() - 5;
					boolean digit = true;
					int end_gap = ruler.lastIndexOf("~");
					if (end_gap > 0) {
						int start_gap = end_gap - 1;
						while (start_gap > 0 && ruler.charAt(start_gap) == '~')
							start_gap--;
						if (ruler.charAt(start_gap) != '~')
							start_gap++;
						if (end_gap - start_gap < 4 && (pos - 5) < end_gap)
							ruler.replace(start_gap,  start_gap+5, "~~~~~");
					}
					for (int i = pos - 1; i >= 0 && digit; i--) {
						digit = Character.isDigit(ruler.charAt(i));
						if (digit) {
							ruler.setCharAt(i, ' ');
						}
					}
					ruler.replace(pos,  pos+5, "~~~~~");
				}
			}

			/* 
			 * Now go through every row (gene) and see what they have at this position
			 */
			for (Bioentity node : nodes) {
				DisplayBioentity protein = (DisplayBioentity) node;
				switch (gap_size) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					String sequence = protein.getSequence();
					char  c = sequence.charAt(seq_position);
					if (protein.getHMMSeq() == null) {
						protein.setHMMSeq(String.valueOf(c));
					} else {
						protein.setHMMSeq(protein.getHMMSeq() + c);
					}
					break;
				default:
					// assuming the gap length never goes beyond 10K
					String condensed = protein.getHMMSeq();
					//					if (condensed.charAt(ruler.length() - 1) != '.') {
					/*
					 * If just leaving good stuff then need to put the ending position in the ruler
					 */
					int pos = ruler.length() - 5;
					condensed = condensed.substring(0, pos) + ".....";
					protein.setHMMSeq(condensed);
					//					}
				}
			}
		}

		hmm_length = 0;
		for (Bioentity node : nodes) {
			DisplayBioentity protein = (DisplayBioentity) node;
			if (protein.getHMMSeq().length() > hmm_length)
				hmm_length = protein.getHMMSeq().length();
		}
		char [] condense_ruler = new char [ruler.length()];
		for (int i = 0; i < ruler.length(); i++)
			condense_ruler[i] = ruler.charAt(i);
		return condense_ruler;
	}
	
	protected int getSeqLength(boolean uncondensed) {
		if (uncondensed)
			return seq_length;
		else
			return hmm_length;
	}
	
	protected char [] getRuler(boolean uncondensed) {
		if (uncondensed)
			return full_ruler;
		else
			return condense_ruler;
	}

}