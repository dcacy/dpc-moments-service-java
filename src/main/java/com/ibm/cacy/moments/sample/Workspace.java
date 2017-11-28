package com.ibm.cacy.moments.sample;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
//import com.ibm.json.java.JsonArray;
//import com.ibm.json.java.JsonObject;
import com.google.gson.JsonElement;

public class Workspace {

	private boolean DEBUG = false;//Boolean.parseBoolean(System.getenv("MOMENTS_DEBUG"));
	private static Logger logger = Logger.getLogger(Workspace.class.getName());

	public Workspace(Properties props) {
		DEBUG = Boolean.parseBoolean(props.getProperty("MOMENTS_DEBUG"));
	}
	/**
	 * Get the spaces to which this app has been added
	 * @param token the JSON Web Token for this Workspace session
	 * @return {JsonObject} containing the list of spaces
	 */
	public JsonObject getSpaces(String token) {
		log("------> entering getSpaces");
		JsonObject result = new JsonObject();
		try {
			Executor executor = Executor.newInstance();//.auth(APP_ID, APP_SECRET);
			String baseurl = "https://api.watsonwork.ibm.com/graphql";
			String query = "query getSpaces { spaces(first: 20) {items {title id}}}";
			URI serviceURI = new URI(baseurl);
			JsonObject postData = new JsonObject();
	    	postData.addProperty("query",query);
			String content = 
				executor.execute(Request.Post(serviceURI)
					.addHeader("Content-Type", "application/json")
					.addHeader("jwt", token)					
					.bodyString(postData.toString(), ContentType.APPLICATION_JSON)
					).returnContent().asString();
			result = new JsonParser().parse(content).getAsJsonObject();
		}
		catch(HttpResponseException e) {
			log("response exception code: " + e.getStatusCode());
			log("response message: " + e.getMessage());
			e.printStackTrace();
		}
		catch(ClientProtocolException e) {
			log("ClientProtocolException message: " + e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		log("<----- exiting getSpaces");
		return result;
	}
		

	/**
	 * Get the moments for a space for the number of days we indicate
	 * @param token the JSON Web Token for this Workspace session
	 * @param id the ID of the space we want to query
	 * @param nbrOfDaysString the number of days of moments we want to capture
	 * @return {JsonObject} containing the moments
	 */
	public JsonObject getMoments(String token, String id, String nbrOfDaysString) {

		log("-------> entering getMoments");
		int nbrOfDays = Integer.parseInt(nbrOfDaysString);
		JsonObject result = new JsonObject();
		try {
		String query =
				"query getMomentsInConversation { "
			+ " conversation(id: \"" + id + "\") {"
		  + "  moments { "
		  + "    items { "
		  + "      id "
		  + "      endTime "
		  + "      keyMessage { id }"
		  + "      summaryPhrases { "
		  + "        label "
		  + "        score "
		  + "      } "
		  + "    } "
		  + "   } "
		  + "  } "
			+ " }"
			;
		Executor executor = Executor.newInstance();
		String baseurl = "https://api.watsonwork.ibm.com/graphql";
		URI serviceURI = new URI(baseurl);
		JsonObject postData = new JsonObject();
    	postData.addProperty("query", query);

		String content = 
			executor.execute(Request.Post(serviceURI)
				.addHeader("Content-Type", "application/json")
				.addHeader("jwt", token)
				.addHeader("x-graphql-view","PUBLIC,BETA")
				.bodyString(postData.toString(), ContentType.APPLICATION_JSON)
				).returnContent().asString();
//			log("result is " + result);
//			log("content is " + content);
//			JsonObject json = JsonObject.parse(content);
			JsonObject json = new JsonParser().parse(content).getAsJsonObject();
//			JsonArray moments = new JsonArray();
			JsonArray moments = new JsonArray();
			if ( json.get("data") != null
				&& (JsonObject)(json.get("data")) != null 
				&& ((JsonObject)(json.get("data"))).get("conversation") != null
				&& (((JsonObject)(((JsonObject)(json.get("data"))).get("conversation"))).get("moments")) != null
				)
			{
//				JsonArray items = (JsonArray)((JsonObject)(((JsonObject)(((JsonObject)(json.get("data"))).get("conversation"))).get("moments"))).get("items");
				JsonArray items = json.get("data").getAsJsonObject().get("conversation").getAsJsonObject().get("moments").getAsJsonObject().get("items").getAsJsonArray();
//				log("items: " + items);

				for ( Iterator<JsonElement> it = items.iterator(); it.hasNext(); ) {
					JsonObject item = it.next().getAsJsonObject();
//					log("item: " + item.toString());
//					log("keyMessage:" + item.get("keyMessage").getAsJsonObject());
					String keyMessageId = item.get("keyMessage").getAsJsonObject().get("id").getAsString();
//					log("keyMessageId: " + keyMessageId);
//					log("endTime: " + item.get("endTime"));
					if ( calcDaysAway(item.get("endTime").getAsString()) < nbrOfDays ) {
						JsonArray summaryPhrases = (JsonArray)item.get("summaryPhrases");
//						log("summaryPhrases:" + summaryPhrases);
						if ( summaryPhrases != null ) {
							for ( Iterator<JsonElement> summaryIt = summaryPhrases.iterator(); summaryIt.hasNext(); ) {
								JsonObject summary = summaryIt.next().getAsJsonObject();
								summary.addProperty("keyMessageId", keyMessageId);
								moments.add(summary);
							}
						}
					}
				}
			}
//			log("moments:" + moments);
			result.addProperty("nbrOfMoments",  moments.size());
			result.addProperty("spaceId",  id);
			result.add("moments", moments);
		} catch(Exception e) {
			e.printStackTrace();
		}
		log("<----- exiting getMoments");
		return result;
	}
	
	/**
	 * return the number of days from today for a particular date
	 * @param endTime The endTime value from the moment in format 2017-10-25T19:01:18.490+0000
	 * @return {int} the number of days from today for a date
	 */
	private int calcDaysAway(String endTime) {
		int result = 0;
		try {
			int oneDay= 1000 * 60 * 60 * 24; // one day in milliseconds
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'+0000'", Locale.ENGLISH);
			long endTimeInMillis = df.parse(endTime).getTime();
			long nowInMillis = new Date().getTime();
			long diffInMillis = nowInMillis - endTimeInMillis;
			result = Math.round(diffInMillis/oneDay);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * a handy logging method
	 * @param o {Object}
	 */
	private void log(Object o) {
		if (DEBUG)
			logger.info(o.toString());
	}
	
}
