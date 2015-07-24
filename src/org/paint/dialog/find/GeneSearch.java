package org.paint.dialog.find;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.table.GeneTable;

import owltools.gaf.Bioentity;
import owltools.gaf.GeneAnnotation;


public class GeneSearch {

	private static GeneSearch singleton;

	private static Logger log = Logger.getLogger(GeneTable.class);

	private GeneSearch() {
	}

	public static synchronized GeneSearch inst() {
		if (singleton == null) {
			singleton = new GeneSearch();
		}
		return singleton;
	}

	public List<Bioentity> search(List<Bioentity> list, String searchStr) {
		List<Bioentity> matches = new ArrayList<Bioentity> ();
		if (list != null && searchStr != null && !searchStr.equals("")) {
			if (searchStr.charAt(0) == '*')
				searchStr = searchStr.length() > 1 ? searchStr.substring(1) : "";
			if (searchStr.length() > 0 && searchStr.endsWith("*"))
				searchStr = searchStr.length() > 1 ? searchStr.substring(0, searchStr.length() - 1) : "";
			Pattern p = Pattern.compile(".*"+searchStr+".*", Pattern.CASE_INSENSITIVE);
			for (int i = 0; i < list.size(); i++) {
				Bioentity node = list.get(i);
				boolean matched = false;
				matched = check4match(node.getSeqId(), p);
				if (!matched) {
					DisplayBioentity dot = (DisplayBioentity) node;
					matched = check4match(dot.getNodeLabel(), p);
				}			
				if (!matched) {
					matched = check4match(node.getLocalId(), p);
				}
				if (!matched) {
					matched = check4match(node.getSeqId(), p);
				}
				if (!matched) {
					matched = check4match(node.getFullName(), p);
				}
				if (!matched) {
					matched = check4match(node.getPersistantNodeID(), p);
				}
				if (!matched) {
					matched = check4match(node.getSpeciesLabel(), p);
				}
				if (!matched) {
					matched = check4match(node.getDb(), p);
				}
				if (!matched) {
					matched = check4match(node.getSeqDb(), p);
				}
				if (!matched) {
					DisplayBioentity dot = (DisplayBioentity) node;
					matched = check4match(dot.getDescription(), p);
				}
				if (!matched) {
					List<GeneAnnotation> associations = node.getAnnotations();
					if (associations != null) {
						for (GeneAnnotation assoc : associations) {
							List<String> refs = assoc.getReferenceIds();
							for (String ref : refs) {
									matched |= check4match(ref, p);
							}
						}
					}
				}
				if (matched) {
					matches.add(node);
				}
			}
		}
		return matches;
	}

	private boolean check4match(String value, Pattern p) {
		boolean matched = false;
		if (value != null) {
			Matcher m = p.matcher(value);
			matched = m.matches();
		}
		return matched;
	}
}
