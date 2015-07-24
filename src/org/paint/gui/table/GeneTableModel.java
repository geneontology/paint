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
package org.paint.gui.table;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;

import owltools.gaf.Bioentity;

public class GeneTableModel extends AbstractGeneTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int currentRow;
	protected Vector<Bioentity> contents;

	public static final String ACC_COL_NAME = "Accession";
	public static final String ORTHO_COL_NAME = "O";
	public static final String DB_COL_NAME = "Database";
	public static final String DBID_COL_NAME = "ID";
	public static final String SYMB_COL_NAME = "Name";
	public static final String SPEC_COL_NAME = "Species";
	public static final String DESC_COL_NAME = "Description";
	public static final String PERMNODEID_COL_NAME = "Permanent Tree ID";
	public static final String STR_EMPTY = "";

	protected static final String[] column_headings = {
		ORTHO_COL_NAME, 
		ACC_COL_NAME, 
		DB_COL_NAME,
		DBID_COL_NAME,
		SYMB_COL_NAME,
		SPEC_COL_NAME, 
		PERMNODEID_COL_NAME,
		DESC_COL_NAME,		
	};

	protected static Logger log = Logger.getLogger(GeneTableModel.class);

	public GeneTableModel() {
		super();
	}

	public GeneTableModel(List<Bioentity> rows) {
		contents = new Vector<Bioentity>();

		if (rows == null) {
			return;
		}
		reorderRows(rows);
	}

	public void reorderRows (List<Bioentity>  node_list) {
		contents.clear();
		contents.addAll(node_list);
	}

	public int getColumnCount() {
		return column_headings.length;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		if (getValueAt(0, c) == null) {
			return String.class;
		}
		Class<?> check = getValueAt(0, c).getClass();
		if (null == check) {
			log.debug("Table returning null for column " + c);
		}
		return check;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return column_headings[columnIndex];
	}

	public int getRowCount() {
		if (contents != null)
			return contents.size();
		else
			return 0;
	}

	/**
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return String, but never, ever null of the find breaks.
	 */
	public String getTextAt(int rowIndex, int columnIndex) {
		if (null == contents) {
			return null;
		}
		Bioentity node = contents.elementAt(rowIndex);
		String tag = column_headings[columnIndex];

		if (tag.equals(ORTHO_COL_NAME)) {
			return ((DisplayBioentity) node).getOrthoMCL();
		} else if (tag.equals(ACC_COL_NAME)) {
			return node.getSeqId();
		} else if (tag.equals(DB_COL_NAME)) {
			return node.getDb();
		} else if (tag.equals(DBID_COL_NAME)) {
			return node.getLocalId();
		} else if (tag.equals(SYMB_COL_NAME)) {
			return node.getSymbol();
		} else if (tag.equals(SPEC_COL_NAME)) {
			return node.getSpeciesLabel();
		} else if (tag.equals(DESC_COL_NAME)) {
			return ((DisplayBioentity) node).getDescription();
		} else if (tag.equals(PERMNODEID_COL_NAME)) {
			return node.getPersistantNodeID();
		}
		return STR_EMPTY;  
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (null == contents) {
			return null;
		}
		String tag = column_headings[columnIndex];
		DisplayBioentity node = (DisplayBioentity) contents.elementAt(rowIndex);
		if (tag.equals(ACC_COL_NAME)) {
			return node.getAccLabel();
		} else if (tag.equals(DBID_COL_NAME)) {
			return node.getModLabel();
		} else if (tag.equals(ORTHO_COL_NAME)) {
			return node.getOrthoCell();
		} else if (tag.equals(PERMNODEID_COL_NAME)) {
			return node.getPermaCell();
		} else {
			return getTextAt(rowIndex, columnIndex);
		}
	}

	public void resetHiddenRows() {
		for (int i = 0; i < contents.size(); i++) {
			DisplayBioentity row = (DisplayBioentity) contents.elementAt(i);
			row.setVisible(false);
		}
	}

	public void setVisibleRows(List<Bioentity> visibleNodes) {
		for (int i = 0; i < contents.size(); i++) {
			Bioentity node = contents.get(i);
			boolean visible = visibleNodes.contains(node);
			((DisplayBioentity) node).setVisible(visible);
		}
	}

	public int getRow(Bioentity dsn) {
		try {
			return contents.indexOf(dsn);
		} catch (NullPointerException e) {
			System.out.println("Could not find gene " + dsn.getDBID() + " in contents");
			return -1;
		}
	}

	public Bioentity getNode(int row) {
		if (row >= contents.size()) {
			System.out.println("Asking for row " + row + " which is > than the number of rows (" + contents.size() + ")");
			return null;
		} else {
			Bioentity node = contents.elementAt(row);
			return node;
		}

	}

	public boolean isSquare(int column) {
		return getColumnName(column).equals(ORTHO_COL_NAME);
	}

	private static void fireProgressChange(String message, int percentageDone) {
		ProgressEvent event = new ProgressEvent(GeneTableModel.class, message, percentageDone);
		EventManager.inst().fireProgressEvent(event);
	}

}