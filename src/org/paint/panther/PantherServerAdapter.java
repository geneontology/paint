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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.bbop.phylo.model.Family;
import org.bbop.phylo.model.Tree;
import org.bbop.phylo.panther.PantherAdapter;
import org.bbop.phylo.panther.ParsingHack;
import org.paint.displaymodel.DisplayBioentity;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.util.LoginUtil;

import owltools.gaf.Bioentity;
import owltools.gaf.species.TaxonFinder;

import com.sri.panther.paintCommon.FixedInfo;
import com.sri.panther.paintCommon.RawComponentContainer;
import com.sri.panther.paintCommon.TransferInfo;

public class PantherServerAdapter extends PantherAdapter {
	
	public static final String PANTHER_URL = " http://paintcuration.usc.edu";
	
	public static final String CHAR_ENCODING = "UTF-8";
	public static final String STRING_EMPTY = "";

	public static final String MSG_ERROR_UNABLE_TO_LOCK_BOOKS = "Error unable to lock books";
	public static final String MSG_ERROR_CONCAT = "Server has returned the following error:  ";
	public static final String MSG_SUCCESS = new String();

	private static final String SERVLET_CONNECTION_CONTENT_TYPE = "Content-Type";
	private static final String SERVLET_CONNECTION_OBJECT_TYPE_JAVA = "java/object";
	private static final String SERVLET_REQUEST_PROPERTY_COOKIE = "Cookie";
	private static final String SERVLET_PATH = "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action=";

	public static final String REQUEST_SEARCH_GENE_NAME = "searchGeneName";
	public static final String REQUEST_SEARCH_GENE_EXT_ID = "searchGeneExtId";
	public static final String REQUEST_SEARCH_PROTEIN_EXT_ID = "searchProteinExtId";
	public static final String REQUEST_SEARCH_DEFINITION = "searchDefinition";
	public static final String REQUEST_SEARCH_ALL_BOOKS = "allBooks";
	public static final String REQUEST_LOCK_BOOKS = "LockBooks";
	public static final String REQUEST_UNLOCK_BOOKS = "UnlockBooks";
	public static final String REQUEST_LOCK_UNLOCK_BOOKS = "LockUnLockBooks";
	public static final String REQUEST_MY_BOOKS = "MyBooks";

	public static final String REQUEST_OPEN_BOOK = "OpenBook";
	public static final String REQUEST_OPEN_BOOK_FOR_GO_USER = "openBookForGOUsr";
	public static final String REQUEST_CLS_INFO = "requestClsInfo";

	public static final String REQUEST_GET_EVIDENCE_SF_LOCK = "getEvidenceSubfamilyLock";
	public static final String REQUEST_GET_EVIDENCE_LEAF_LOCK = "getEvidenceALeafLock";
	public static final String REQUEST_EVIDENCE_SAVE_SUBFAMILY = "saveSubFamilyEvidence";
	public static final String REQUEST_EVIDENCE_SAVE_SEQUENCE = "saveSequenceEvidence";
	public static final String REQUEST_UNLOCK_SEQUENCE = "unlockSequence";
	public static final String REQUEST_UNLOCK_SUBFAMILY = "unlockSubFamily";

	public static final String SERVER_ERROR = "Server cannot access information, please contact Systems Administrator";

	public static String server_status;
	
	private static Logger LOG = Logger.getLogger(LoginUtil.class);

	private static PantherServerAdapter INSTANCE = null;

	public static synchronized PantherServerAdapter inst() {
		if (INSTANCE == null) {
			INSTANCE = new PantherServerAdapter();
		}
		return INSTANCE;
	}

	public boolean fetchTree(Family family, Tree tree) {
		boolean ok = false;
		if (family != null) {
			RawComponentContainer rcc = PantherServerAdapter.inst().getRawPantherFam(LoginUtil.getUserInfo(), family.getFamily_name());
			if (rcc != null){
			    
				Vector<String[]> info = ((Vector<String[]>) rcc.getTree());
				family.setTreeContent(new ArrayList<String>(Arrays.asList(info.elementAt(0))));
				
				family.setAttrContent(new ArrayList<String>(Arrays.asList(rcc.getAttributeTable())));

				info = (Vector<String[]>) rcc.getMSA();
				if (info != null && info.size() > 0) {
					family.setMsaContent(new ArrayList<String>(Arrays.asList(info.elementAt(0))));
				}
				if (info != null && info.size() > 1) {
					family.setWtsContent(new ArrayList<String>(Arrays.asList(info.elementAt(1))));
				}

				Bioentity root = parsePantherTree(family.getTreeContent());
		        recordOrigChildOrder(root);

				if (root != null) {
					tree.growTree(root);

					// Load the attr file to obtain the PTN #s
					List<List<String>> rows = ParsingHack.parsePantherAttr(family.getAttrContent());
					decorateNodes(rows, tree);

					if (tree.getRoot().getNcbiTaxonId() == null) {
						String taxon = TaxonFinder.getTaxonID("LUCA");
						tree.getRoot().setNcbiTaxonId(taxon);
					}
					ok = true;
				}
			}
		}
		return ok;
	}

	protected Bioentity createNode() {
		return new DisplayBioentity(true);
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @param fi
	 * @param cp
	 * @param userInfo
	 * @param uplVersion
	 * @param familyID
	 *
	 * @return
	 *
	 * @see
	 */
	public RawComponentContainer getRawPantherFam(Vector<? extends Object> userInfo, String familyID) {

		Vector  objs = new Vector();

		objs.addElement(userInfo);
		objs.addElement(PantherDbInfo.getDbAndVersionKey());
		objs.addElement(familyID);
		Object  o = sendAndReceive(REQUEST_OPEN_BOOK, objs, null, null);


		if (null == o){
			return null;
		}

		Vector  output = (Vector) o;

		TransferInfo  ti = (TransferInfo) output.elementAt(0);

		if (0 != ti.getInfo().length()){
			System.out.println("Server cannot access information for transfer: " + ti.getInfo());
			return null;
		}

		RawComponentContainer container = (RawComponentContainer) output.elementAt(1);

		return container;

	}

	public Vector searchGeneName(Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(PANTHER_URL, REQUEST_SEARCH_GENE_NAME,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public Vector searchGeneExtId(Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(PANTHER_URL, REQUEST_SEARCH_GENE_EXT_ID,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public Vector searchProteinExtId(Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(PANTHER_URL, REQUEST_SEARCH_PROTEIN_EXT_ID,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public Vector searchDefinition(Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(PANTHER_URL, REQUEST_SEARCH_DEFINITION,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public Vector searchAllBooks(Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(PANTHER_URL, REQUEST_SEARCH_ALL_BOOKS,
				sendInfo, sessionIdName, sessionIdValue);
	}

	private void fireProgressChange(String message, int percentageDone, ProgressEvent.Status status) {
		ProgressEvent event = new ProgressEvent(PantherServerAdapter.class, message, percentageDone, status);
		EventManager.inst().fireProgressEvent(event);
	}

	private void fireProgressChange(String message, int percentageDone) {
		fireProgressChange(message, percentageDone, ProgressEvent.Status.RUNNING);
	}

	private Vector doSearch(String servletURL, String actionRequest, Object sendInfo, String sessionIdName, String sessionIdValue) {
		Object serverOutput = sendAndReceiveZip(servletURL, actionRequest, sendInfo, sessionIdName, sessionIdValue);
		Vector returnInfo = new Vector();
		if (null == serverOutput) {
			returnInfo.add(SERVER_ERROR);
			return returnInfo;
		}
		TransferInfo ti = (TransferInfo) ((Vector) serverOutput).elementAt(0);
		String errorMsg = ti.getInfo();
		if (0 != errorMsg.length()) {
			returnInfo.add(errorMsg);
			return returnInfo;
		}
		returnInfo.add(STRING_EMPTY);
		returnInfo.add(((Vector) serverOutput).elementAt(1));
		return returnInfo;
	}

	public String getServerStatus() {
		return server_status;
	}

	public static void setServerStatus(String serverStatus) {
		server_status = serverStatus;
	}


	protected Object sendAndReceiveZip(String servletURL, String actionRequest, Object sendInfo, String sessionIdName, String sessionIdValue) {
		String message = null;
		Object            outputFromServlet = null;
		try{

			String progressMessage = "Fetching zip data";
			fireProgressChange(progressMessage, 0, ProgressEvent.Status.START);

			// connect to the servlet
			URL                     servlet =
					new URL(servletURL +  SERVLET_PATH + actionRequest);
			java.net.URLConnection  servletConnection = servlet.openConnection();

			servletConnection.setRequestProperty(SERVLET_CONNECTION_CONTENT_TYPE, SERVLET_CONNECTION_OBJECT_TYPE_JAVA);

			// Set the session id, if necessary
			if ((null != sessionIdName) && (null != sessionIdValue)){
				servletConnection.setRequestProperty(SERVLET_REQUEST_PROPERTY_COOKIE, sessionIdName + "=".concat(sessionIdValue));
			}

			// Connection should ignore caches if any
			servletConnection.setUseCaches(false);

			// Indicate sending and receiving information from the server
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);
			ObjectOutputStream  objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(servletConnection.getOutputStream()));
			fireProgressChange(progressMessage, 50);

			objectOutputStream.writeObject(sendInfo);
			objectOutputStream.flush();
			objectOutputStream.close();
			ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			outputFromServlet = servletOutput.readObject();
			fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);

			servletOutput.close();
			return outputFromServlet;
		}
		catch (MalformedURLException muex){
			message = ("MalformedURLException " + muex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
			muex.printStackTrace();
		}
		catch (IOException ioex){
			message = ("IOException " + ioex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}
		catch (Exception e){
			message = ("Exception " + e.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}      
		if (message != null) {
			// Oh dear
			EventManager.inst().fireProgressEvent(new ProgressEvent(this, message, 0, ProgressEvent.Status.FAIL));
		}
		return outputFromServlet;			
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param servletURL
	 * @param actionRequest
	 * @param sendInfo
	 * @param sessionIdName
	 * @param sessionIdValue
	 *
	 * @return
	 *
	 * @see
	 */
	public Object sendAndReceive(String actionRequest, Object sendInfo,
			String sessionIdName, String sessionIdValue){
		String message = null; // if no message, then it's all lovely
		Object            outputFromServlet = null;
		try{
			// connect to the servlet
			URL                     servlet =
					new URL(PANTHER_URL + "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action="
							+ actionRequest);
			java.net.URLConnection  servletConnection = servlet.openConnection();

			servletConnection.setRequestProperty("Content-Type", "application/octet-stream");

			// Set the session id, if necessary
			if ((null != sessionIdName) && (null != sessionIdValue)){
				servletConnection.setRequestProperty("Cookie", sessionIdName + "=".concat(sessionIdValue));
			}

			// Connection should ignore caches if any
			servletConnection.setUseCaches(false);

			// Indicate sending and receiving information from the server
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);
			ObjectOutputStream  objectOutputStream = new ObjectOutputStream(servletConnection.getOutputStream());

			objectOutputStream.writeObject(sendInfo);
			objectOutputStream.flush();
			objectOutputStream.close();
			ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			outputFromServlet= servletOutput.readObject();

			servletOutput.close();
		}
		catch (MalformedURLException muex){
			message = ("MalformedURLException " + muex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
			muex.printStackTrace();
		}
		catch (IOException ioex){
			message = ("IOException " + ioex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}
		catch (Exception e){
			message = ("Exception " + e.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}
		if (message != null) {
			// Oh dear
			EventManager.inst().fireProgressEvent(new ProgressEvent(this, message, 0, ProgressEvent.Status.FAIL));
		}
		return outputFromServlet;			
	}

	public FixedInfo getFixedInfoFromServer() {
		Vector objs = null;
		server_status = "";
		try {
			// try to get if from the session
			// connect to the servlet
			LOG.info("Logging in to Panther URL: " + PANTHER_URL);
			URL servlet = new URL(PANTHER_URL + "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action=FixedInfo");
			URLConnection servletConnection = servlet.openConnection();

			// Don't used a cached version of URL connection.
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);
			//
			// The servlet will return a serialized vector containing a DataTransfer object
			//
			ObjectInputStream inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			objs = (Vector) inputFromServlet.readObject();
			inputFromServlet.close();
		}
		catch (MalformedURLException muex){
			setServerStatus(muex.getLocalizedMessage());
		}
		catch (IOException ioex) {
			setServerStatus(ioex.getLocalizedMessage());
		}
		catch (ClassNotFoundException cnfex){
			setServerStatus(cnfex.getLocalizedMessage());
		}
		if (null != objs) {
			TransferInfo ti = (TransferInfo)objs.elementAt(0);
			if (0 != ti.getInfo().length()) {
				setServerStatus("Server cannot access information for transfer: " + ti.getInfo());
				return null;
			}
			return (FixedInfo)objs.elementAt(1);
		}
		return null;
	}         

	public Vector<? extends Object> listFamilies(Vector<? extends Object> vector, String dbClsId) {
		Vector  objs = new Vector();

		objs.addElement(vector);
		objs.addElement(dbClsId);
		Vector  returnInfo = new Vector();

		try{

			// connect to the servlet
			URL               servlet =
					new URL(PANTHER_URL
							+ "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action=BookList");
			HttpURLConnection servletConnection = (HttpURLConnection) servlet.openConnection();

			servletConnection.setRequestMethod("POST");

			// Connection should ignore caches if any
			servletConnection.setUseCaches(false);

			// Indicate sending and receiving information from the server
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);
			servletConnection.setRequestProperty("Content-Type", "java/object");
			ObjectOutputStream  objectOutputStream = new ObjectOutputStream(servletConnection.getOutputStream());

			objectOutputStream.writeObject(objs);
			objectOutputStream.flush();
			objectOutputStream.close();

			ObjectInputStream inputFromServlet =
					new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			Object            inputFromServer = inputFromServlet.readObject();

			inputFromServlet.close();
			if (null != inputFromServer){
				TransferInfo  ti = (TransferInfo) ((Vector) inputFromServer).elementAt(0);

				if (0 != ti.getInfo().length()){
					returnInfo.addElement(ti.getInfo());
					return returnInfo;
				}
				Vector  books = (Vector) ((Vector) inputFromServer).elementAt(1);
				String  bookList[] = new String[books.size()];

				books.copyInto(bookList);
				returnInfo.addElement("");
				returnInfo.addElement(bookList);
				return returnInfo;
			}
			returnInfo.addElement("Server did not return any information");
			return returnInfo;
		}
		catch (MalformedURLException muex){
			muex.printStackTrace();
			returnInfo.addElement("System error, please contact system administrator");
			return returnInfo;
		}
		catch (IOException ioex){
			ioex.printStackTrace();
			returnInfo.addElement("System error, please contact system administrator");
			return returnInfo;
		}
		catch (Exception e){
			e.printStackTrace();
			returnInfo.addElement("System error, please contact system administrator");
			return returnInfo;
		}
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

