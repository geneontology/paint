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

package org.paint.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.EventManager;

public class AspectSelector {

	private static AspectSelector selector;

	private Aspect aspect;

	public enum Aspect {
		BIOLOGICAL_PROCESS,
		CELLULAR_COMPONENT,
		MOLECULAR_FUNCTION;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private final static HashMap<String, String> aspects = new HashMap<String, String> ();
	static	{
		aspects.put(Aspect.BIOLOGICAL_PROCESS.toString(), "P");
		aspects.put(Aspect.CELLULAR_COMPONENT.toString(), "C");
		aspects.put(Aspect.MOLECULAR_FUNCTION.toString(), "F");
	}
	
	

	private AspectSelector() {
		aspect = Aspect.MOLECULAR_FUNCTION;
	}

	public static AspectSelector inst() {
		if (selector == null) {
			selector = new AspectSelector();
		}
		return selector;
	}

	public void setAspect(Aspect new_aspect) {
		if (aspect != new_aspect) {
			boolean change = aspect != null;
			this.aspect = new_aspect;
			if (change)
				EventManager.inst().fireAspectChangeEvent(new AspectChangeEvent(this));
		}
	}
	
	public void setAspect(String code) {
		Set<String> names = aspects.keySet();
		Aspect new_aspect = null;
		for (String name : names) {
			String current = aspects.get(name);
			if (current.equals(code)) {
				if (name.equals(Aspect.BIOLOGICAL_PROCESS.toString())) {
					new_aspect = Aspect.BIOLOGICAL_PROCESS;
				} else if (name.equals(Aspect.MOLECULAR_FUNCTION.toString())) {
					new_aspect = Aspect.MOLECULAR_FUNCTION;
				} else if (name.equals(Aspect.CELLULAR_COMPONENT.toString())) {
					new_aspect = Aspect.CELLULAR_COMPONENT;
				}
			}
		}
		if (new_aspect != null) {
			setAspect(new_aspect);
		}
	}
	
	public Aspect getAspect() {
		return aspect;
	}
	
	public String getAspectName() {
		return aspect.toString();
	}
	
	public String getAspectCode() {
		return getAspectCode(aspect.toString());
	}
	
	public String getAspectCode(String aspect_name) {
		return aspects.get(aspect_name);
	}

	public String getAspectName4Code(String code) {
		Set<String> keys = aspects.keySet();
		String aspect_name = null;
		for (Iterator<String> i = keys.iterator(); i.hasNext() && aspect_name == null;) {
			String key = i.next();
			if (aspects.get(key).equals(code)) {
				aspect_name = key;
			}
		}
		return aspect_name;
	}

}
