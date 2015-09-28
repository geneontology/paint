/* Copyright (C) 2008 SRI International
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sri.panther.paintCommon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class Classification implements Serializable, Comparable {
    String id;
    String accession;
    Vector <Classification> children;
    Vector <Classification> parents;
    String name;
    String description;
    String type;
    Vector<String> relationshipTypeList;
    int rank;
    Hashtable<String, Classification> accToClsTbl;  // When information is parsed, this field is set for the root record

    public static final String CAT_MOLECULAR_FUNCTION = "molec";
    public static final String CAT_BIOLOGICAL_PROCESS = "biol";
    public static final String CAT_CELLULAR_COMPONENT = "cellul";
    public static final String CAT_PROTEIN_CLASS = "prote";
    
    public static final String CAT_TYPE_MF = "MF";
    public static final String CAT_TYPE_BP = "BP";
    public static final String CAT_TYPE_CC = "CC";
    public static final String CAT_TYPE_PC = "PC";
    
    public Classification(String id, String accession, String name, String description, int rank) {
        this.id = id;
        this.accession = accession;
        this.name = name;
        this.description = description;
        this.rank = rank;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getAccession() {
        return accession;
    }

    public void setChildren(Vector<Classification> children) {
        this.children = children;
    }

    public Vector<Classification> getChildren() {
        return children;
    }
    
    public void addChild (Classification child) {
        if (null == children) {
            children = new Vector<Classification>(1);
        }
        if (false == children.contains(child)) {
            children.add(child);
        }

    }

    public void setParents(Vector<Classification> parents) {
        this.parents = parents;
    }

    public Vector<Classification> getParents() {
        return parents;
    }
    
    public void addParent(Classification parent) {
        if (null == parents) {
            parents = new Vector<Classification>(1);
        }
        if (false == parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    protected static void setType(Classification c) {
        Vector<Classification>  children = c.getChildren();
        if (null == children) {
            return;
        }
        
        for (int i = 0; i < children.size(); i++) {
            Classification child = children.get(i);
            String aType = child.getName();
            
            setType(child, aType);
        }
    }
    
    protected static void setType(Classification c, String aType) {
        if (null == c) {
            return;
        }
        c.type = aType;
        Vector<Classification>  children = c.getChildren();
        if (null == children) {
            return;
        }
        
        for (int i = 0; i < children.size(); i++) {
            Classification child = children.get(i);
            setType(child, aType);
        }
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
    
    public Hashtable<String, Classification> getAccToClsTbl() {
        return accToClsTbl;
    }
    
    
    public int compareTo (Object o) {
        Classification comp = (Classification)o;
        if (rank == comp.rank) {
            return 0;
        }
        else if (rank > comp.rank) {
            return 1;
        }
        else {
            return -1;
        }
    }
    
    
    public Classification checkChildName(String name) {
        if (null == children) {
            return null;
        }
        String lower = name.toLowerCase().substring(0, 5);
        for (int i = 0; i < children.size(); i++) {
            Classification child = children.get(i);
            
            // Compare first  5 characters only
            if (true == child.getName().toLowerCase().substring(0, 5).equals(lower)) {
                return child;
            }
        }
        return null;
    }

    public void addRelationshipType(String relationshipType) {
        if (null == this.relationshipTypeList) {
            this.relationshipTypeList = new Vector<String>(1);
        }

        // Only add unique relations
        Map<String, String> tmpTbl = new HashMap<>();
        for (int i = 0; i < relationshipTypeList.size(); i++) {
            String str = relationshipTypeList.get(i);
            tmpTbl.put(str, str);
        }
        if (null == tmpTbl.get(relationshipType)) {
            relationshipTypeList.add(relationshipType);
        }
    }

    public Vector<String> getRelationshipTypeList() {
        return relationshipTypeList;
    }
    
    
    public String getType() {
        return type;
    }
    
    public static String convertType(String s) {
        if (null == s) {
            return null;
        }
        s = s.toLowerCase();
        if (0 == s.indexOf(CAT_MOLECULAR_FUNCTION)) {
            return CAT_TYPE_MF;
        }
        if (0 == s.indexOf(CAT_BIOLOGICAL_PROCESS)) {
            return CAT_TYPE_BP;
        }
        if (0 == s.indexOf(CAT_CELLULAR_COMPONENT)) {
            return CAT_TYPE_CC;
        }
        if (0 == s.indexOf(CAT_PROTEIN_CLASS)) {
            return CAT_TYPE_CC;
        }
        else {
            return Constant.STR_DASH + Constant.STR_DASH;
        }
    }
        
        
    


}
