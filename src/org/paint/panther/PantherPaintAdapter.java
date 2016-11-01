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
import org.bbop.phylo.io.panther.PantherServerAdapter;
import org.bbop.phylo.model.Bioentity;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.Tree;
import org.paint.displaymodel.DisplayBioentity;

public class PantherPaintAdapter extends PantherServerAdapter {
	
	private static Logger LOG = Logger.getLogger(PantherPaintAdapter.class);

	private static PantherPaintAdapter INSTANCE = null;

	public static synchronized PantherPaintAdapter inst() {
		if (INSTANCE == null) {
			INSTANCE = new PantherPaintAdapter();
		}
		return INSTANCE;
	}

	public boolean fetchTree(Family family, Tree tree) {
		boolean okay = super.fetchTree(family, tree);
		if (okay) {
		    recordOrigChildOrder(tree.getCurrentRoot());	
		}
		return okay;
	}

	public Bioentity createNode() {
		return new DisplayBioentity(true);
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

