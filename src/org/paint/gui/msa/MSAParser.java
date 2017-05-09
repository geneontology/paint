package org.paint.gui.msa;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.io.panther.IDmap;
import org.bbop.phylo.io.panther.ParsingHack;
import org.bbop.phylo.model.Bioentity;
import org.paint.displaymodel.DisplayBioentity;

public class MSAParser{
	private static final String PREFIX_SEQ_START = ">";

	private static Logger log = Logger.getLogger(MSAParser.class);

	private static MSAParser INSTANCE = null;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public MSAParser() {
		// Exists only to defeat instantiation.
	}

	public static synchronized MSAParser inst() {
		if (INSTANCE == null) {
			INSTANCE = new MSAParser();
		}
		return INSTANCE;
	}

	/**
	 * Method declaration
	 *
	 * @param msaInfo
	 *
	 * @return
	 *
	 * @see
	 */
	public int parseSeqs(List<String> list) {
		int seq_length = 0;
		List<Bioentity> nodes = new ArrayList<Bioentity>();

		if (list != null) {
			IDmap mapper = IDmap.inst();
			StringBuffer  sb = new StringBuffer();
			Bioentity node = null;

			for (int i = 0; i < list.size(); i++){
				String line = list.get(i);
				if (line.startsWith(PREFIX_SEQ_START)) {
					String paint_id = line.replaceFirst(PREFIX_SEQ_START, "");
					// Record the sequence we've been concatenating
					if (node != null) {
						String seq = sb.toString();
						((DisplayBioentity) node).setSequence(seq);
						// Get the maximum sequence length
						if (seq_length == 0){
							seq_length = seq.length();
						}
					}
					// Get started with this new sequence for the node
					node = mapper.getGeneByANId(paint_id);
					if (node == null) {
						log.error("Unable to get gene " + paint_id + " for MSA data");
						continue;
					}
					sb.delete(0, sb.length());
					nodes.add(node);
				}
				else {
					sb.append(line.trim());
				}
			}
			if (node != null) {
				String seq = sb.toString();
				((DisplayBioentity) node).setSequence(seq);
				// Get the maximum sequence length
				if (seq_length == 0 || seq_length < seq.length()){
					seq_length = seq.length();
				}
			}
		}
		return seq_length;
	}

	protected boolean parseWts(List<String> wtsInfo) {
		boolean weighted = (wtsInfo != null && wtsInfo.size() > 0);
		if (weighted) {
			// Ignore first two lines
			for (int i = 2; i < wtsInfo.size(); i++){
				List<String>  seqWt = ParsingHack.tokenize(wtsInfo.get(i), " ");
				Bioentity node = ParsingHack.findThatNode(seqWt.get(0));
				if (node != null)
					((DisplayBioentity) node).setSequenceWt(new Double(seqWt.get(1)));
			}
		}
		return weighted;
	}

}
