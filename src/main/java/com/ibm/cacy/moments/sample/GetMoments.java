package com.ibm.cacy.moments.sample;

import java.io.IOException;
//import java.util.Enumeration;
//import java.util.Hashtable;
import java.util.Iterator;
//import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
//import com.ibm.json.java.JSONObject;

/**
 * Servlet implementation class GetMoments
 */
@WebServlet("/GetMoments")
public class GetMoments extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = false;//Boolean.parseBoolean(System.getenv("MOMENTS_DEBUG"));
	private static Logger logger = Logger.getLogger(GetMoments.class.getName());
	private OAuth oauth = null;
	Properties props = new Properties();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		/**
		 * Try to get a token from Workspace.
		 * Try 10 times, waiting 2 seconds between tries.
		 * If we succeed, then calling this.getToken will always return a valid token.
		 */
//	    Properties props = new Properties();
	    try {
//	    	InputStream input = ;
	    	props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("moments-service.properties"));
//	    	input.close();
//	    	prop.load(getServletContext().getResourceAsStream("moments-service.properties"));
	    	System.out.println("debug status is " + props.get("MOMENTS_DEBUG"));
	    	Set<String> keys = props.stringPropertyNames();
	    	for (Iterator<String> it = keys.iterator(); it.hasNext();) {
	    		String key = it.next();
	    		log("key: " + key + ", value:  " + props.getProperty(key));
	    	}
//	    	Enumeration<Object> e = props.keys();
//	    	while (e.hasMoreElements()) {
//	    		Object key = e.nextElement();
//	    		props.g
//	    	}
	    	oauth = new OAuth(props);
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
		log("APP_ID is " + props.getProperty("MOMENTS_WORKSPACE_APP_ID"));
		int errors = 0;
		while ( errors < 10 ) {
			String token = oauth.getToken(); 
			if ( token == null ) {
				log("Failed to get OAuth token; attempt " + ++errors);
				try {
				    Thread.sleep(2000); //1000 milliseconds is one second
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			} else {
				log("initialized token");
				break;
			}
		}
	}
	
	/**
	 * always returns a valid JSON Web Token
	 * @return
	 */
	private String getToken() {
		return oauth.getToken();
	}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMoments() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ( getToken() == null ) {
			log("unable to log in to Watson Workspace; check WORKSPACE_APP_ID and WORKSPACE_APP_SECRET environment variables.");
			log("WORKSPACE_APP_ID is [" + props.getProperty("MOMENTS_WORKSPACE_APP_ID") + "]");
//			response.setContentType("application/json");
//			response.getWriter().print("{error: \"Unable to log in to Workspace.\"}");	
			response.sendError(500,"{error: \"Unable to log in to Workspace.\"}");
//			response.set
		} else {
			/**
			 * If we are passed a space ID, then get the moments for that space.
			 * If not, then return the list of spaces.
			 */
			
			String id = request.getParameter("id");
			String days = request.getParameter("days");
			debug("id is " + id + " and days is " + days);
			if ( id == null || id.equals("") ) {
				Workspace workspace = new Workspace(props);
				JsonObject json = workspace.getSpaces(getToken());
				JsonObject results = new JsonObject();
				results.addProperty("error","Please provide a workspace ID");
				results.add("choices",json);
				response.setContentType("application/json");
				response.getWriter().print(results.toString());
			} else {
				if ( days == null || days.equals("") ) {
					days = "7"; // default = 7
				}
				Workspace workspace = new Workspace(props);
				JsonObject json = workspace.getMoments(getToken(), id, days);
				response.setContentType("application/json");
				response.getWriter().print(json.toString());
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private void debug(Object o) {
		if (DEBUG)
			logger.info(o.toString());
	}

}
