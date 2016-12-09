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

package org.paint.config;

import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Used for reading previous or default user settings from property file and storing current user settings
 */

public class VersionResource { // extends DirectoryUtil {
	/**
	 * 
	 */

	protected static Logger log = Logger.getLogger("org.panther.paint.config.Preferences");

	private String version;

	private static VersionResource resource;

	/**
	 * Constructor declaration
	 * @throws Exception 
	 *
	 *
	 * @see
	 */
	public VersionResource() { //throws Exception {
	}
	
	public static VersionResource inst() {
		if (resource == null) {
			resource = new VersionResource();
		}
		return resource;
	}

	public String getVersion() {
		if (version == null) {
			try {
				InputStream inputStream = getExtensionLoader().getResourceAsStream(
						"org/paint/resources/VERSION");
				if (inputStream != null) {
					List<String> lines = IOUtils.readLines(inputStream);
					if (!lines.isEmpty()) {
						version = lines.get(0);
					}
				}
			} catch (Exception e) {
				log.warn("Clould not load version from resource", e);
			}
			if (version == null) {
				version = "2.0";
			}
		}
		return version;
	}

	private static ClassLoader getExtensionLoader() {
		return VersionResource.class.getClassLoader();
	}
}
