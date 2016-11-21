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

package org.paint.panther;

import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.phylo.io.panther.PantherAdapter;
import org.bbop.phylo.io.panther.PantherFileAdapter;
import org.bbop.phylo.io.panther.PantherLoadAdapterI;
import org.bbop.phylo.io.panther.PantherParserI;
import org.bbop.phylo.io.panther.PantherServerAdapter;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.Tree;
import org.bbop.phylo.util.Constant;
import org.bbop.phylo.util.TimerUtil;
import org.paint.config.PaintConfig;
import org.paint.displaymodel.DisplayBioentity;

public class PaintPantherAdapter extends PantherAdapter {

	private static Logger log = Logger.getLogger(PaintPantherAdapter.class);

	private static PantherLoadAdapterI active_adapter;

	public PaintPantherAdapter (String family_name, boolean use_server) {
		if (active_adapter == null) {
			if (use_server) {
				active_adapter = new PantherServerAdapter();
			} else {
				active_adapter = new PantherFileAdapter();			
				((PantherFileAdapter) active_adapter).setFamilyDir (PaintConfig.inst().gafdir);
				((PantherFileAdapter) active_adapter).setTreeFileName(family_name + Constant.TREE_SUFFIX);
				((PantherFileAdapter) active_adapter).setAttrFileName(family_name + Constant.ATTR_SUFFIX);
				((PantherFileAdapter) active_adapter).setMSAFileName(family_name + Constant.MSA_SUFFIX);
				((PantherFileAdapter) active_adapter).setWtsFileName(family_name + Constant.WTS_SUFFIX);
			}
		}
	}

	public boolean loadFamily(Family family, Tree tree) {
		TimerUtil timer = new TimerUtil();
		boolean okay = active_adapter.loadFamily(family, tree);
		if (okay) {
			log.info("\tFetched " + family.getFamily_name() + ": " + timer.reportElapsedTime());
			recordOrigChildOrder(tree.getCurrentRoot());	
			PantherParserI parser = new PaintPantherParser();
			parser.parseFamily(family, tree);
			log.info("Loaded " + family.getFamily_name() + ": " + timer.reportElapsedTime());
		} else {
			log.info("Unable to load " + family.getFamily_name() + ": " + timer.reportElapsedTime());
		}
		return okay;
	}

	private void recordOrigChildOrder(Bioentity node) {
		if (null == node) {
			return;
		}

		((DisplayBioentity) node).setOriginalChildrenToCurrentChildren();
		List<Bioentity> children = node.getChildren();
		if (children != null) {
			for (Bioentity child : children) {
				recordOrigChildOrder(child);
			}
		}
	}

}

