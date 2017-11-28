package com.ibm.cacy.moments.sample;

//import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.ibm.json.java.JSONObject;

public class OAuth {

	private boolean DEBUG = false;//Boolean.parseBoolean(System.getenv("MOMENTS_DEBUG"));
	private Logger logger = Logger.getLogger(OAuth.class.getName());
	private String APP_ID = "";//System.getenv("MOMENTS_WORKSPACE_APP_ID");
	private String APP_SECRET = "";//System.getenv("MOMENTS_WORKSPACE_APP_SECRET");
	private Date expiresAtDate = null;
	private String token = null;
//	private Properties props = new Properties();

	public OAuth(Properties props) {
//		System.out.println("DEBUG: " + DEBUG);
//		System.out.println("APP_ID: " + APP_ID);
//		try {
//			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("moments-service.properties"));
			DEBUG = Boolean.parseBoolean(props.getProperty("MOMENTS_DEBUG"));
//			APP_ID = "0c314492-4886-4f3c-8474-eccaeec9f709"; //props.getProperty("MOMENTS_WORKSPACE_APP_ID");
//			APP_SECRET = "OtWp6dhnEZzGUMuv1sq6TU1Xwtzv"; //props.getProperty("MOMENTS_WORKSPACE_APP_SECRET");
			APP_ID = props.getProperty("MOMENTS_WORKSPACE_APP_ID");
			APP_SECRET = props.getProperty("MOMENTS_WORKSPACE_APP_SECRET");
//		} catch(IOException e) {
//			e.printStackTrace();
//		}

	}
	/**
	 * Always returns a valid JSON Web Token
	 * @return {String} containing a valid JSON Web Token
	 */
	public String getToken() {
		if ( token == null ) {
			refresh();
		}
		return token;
	}



	/**
	 * Log in to Workspace. If successful, start a Timer to refresh the token before it expires.
	 * @return {String} a JSON Web Token
	 */
	private String refresh() {
		log("-----> entering refresh with APP_ID: " + APP_ID);
		try {
			Executor executor = Executor.newInstance().auth(APP_ID, APP_SECRET);
			URI serviceURI = new URI("https://api.watsonwork.ibm.com/oauth/token").normalize();
			String result = executor.execute(Request.Post(serviceURI).addHeader("Accept", "application/json")
				.bodyForm(Form.form().add("grant_type", "client_credentials").build())
				).returnContent().asString();
			JsonObject json = new JsonParser().parse(result).getAsJsonObject();
//			JSONObject json = JSONObject.parse(result);
//			token = (String) (json.get("access_token"));
			token = json.get("access_token").getAsString();
			DecodedJWT decoded = JWT.decode(token); 
			expiresAtDate = decoded.getExpiresAt();

			double ttl = ttl(expiresAtDate);
			log("at " + new Date() + " Token TTL is " + expiresAtDate + " which is about " 
				+ Math.round(ttl(expiresAtDate) / (1000D * 60D * 60D)) + " hour(s)");

			// now set a timer to refresh the token one minute before it expires
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				  @Override
				  public void run() {
				    refresh();
				  }
				}, (long)ttl - 60000);

		} catch (Exception e) {
			e.printStackTrace();
		}
		log("<----- exiting refresh");
		return token;

	}

	/**
	 * returns the time betwee now and a Date in the future, in milliseconds
	 * @param date
	 * @return
	 */
	private long ttl(Date date) {
		return date.getTime() - new Date().getTime();
	}

	private void log(Object o) {
		if (DEBUG)
			logger.info(o.toString());
	}
}
