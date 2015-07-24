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

package org.paint.main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;
import org.bbop.util.OSUtil;

public class PAINT {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Thread runner;

	private static String[] args;


	/**
	 * Method declaration
	 *
	 *
	 * @param args
	 *
	 * @see
	 */
	public static void main(String[] args) {
		PAINT.args = args;
		//if PAINT is launched on a Mac, set the system property to setup the correct
		//application menu name
		if (OSUtil.isMacOSX()) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", getAppName());
		}

		PAINT theRunner = new PAINT();

		SwingUtilities.invokeLater(theRunner.mainRun);
	}

	Runnable mainRun =
		new Runnable() {
		// this thread runs in the AWT event queue
		public void run() {
			try {
				GUIManager.getManager().addStartupTask(new PaintStartupTask(args));
				GUIManager.getManager().start();
			}
			catch (Exception e) { // should catch RuntimeException
				JOptionPane.showMessageDialog(
						null,
						e,
						"Warning",
						JOptionPane.WARNING_MESSAGE
				);
				e.printStackTrace();
				System.exit(2);
			}
		}
	};

	public static String getAppName() {
		/*
		 * If you want the version # included, then use getAppId
		 */
		return "Paint";
	}
}
