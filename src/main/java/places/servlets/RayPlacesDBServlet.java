package places.servlets;
/* JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import recording.servlets.Recording;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

@WebServlet(name = "RayPlacesDBServlet")
public class RayPlacesDBServlet extends HttpServlet {
	public static String 	RAY_PLACES_DB_ID = "public_ray";
	public static String 	RECORDINGS_COLLECTION = "recordings";
	public static String 	USERS_COLLECTION = "places_users";
	public static String 	ADMINS_COLLECTION = "places_admins";
	// Logger
	public static String 	LOGS_COLLECTION = "serverlogs";
	
	// Request for user authentication
	public static String 	SIGNUP_QUERY = "signup";
	public static String 	SIGNIN_QUERY = "signin";
	
	// Post request for places
	public static String 	ADD_PLACE_QUERY = "add_place";
	public static String 	ADD_RECORD_QUERY = "add_record";
	public static String 	UPDATE_QUERY = "update";
	public static String	REMOVE_PLACE_QUERY = "remove_place";
	public static String	REMOVE_RECORD_QUERY = "remove_record";
    	
	// Get request for places
	public static String	VIEW_PLACES_DB_QUERY = "view_places_db";
	public static String	VIEW_RECORDS_DB_QUERY = "view_records_db";
	public static String	VIEW_RECORDS_FILES_QUERY = "view_records_files";
	public static String	SEARCH_PLACES_QUERY = "search_places";
	public static String	GET_RECORDS_BY_PLACE_QUERY = "get_records_by_place";
	
	// Logger
	public static String	VIEW_LOGS_QUERY = "view_logs";
	public static String	VIEW_RECORDINGS_QUERY = "view_recordings";
	public static String	GET_RECORDS_DB_SIZE_QUERY = "get_records_db_size";
	public static String	GET_LOGS_DB_SIZE_QUERY = "get_logs_db_size";
	
    private Mongo mongo;
    private DB mongoDB;
    
    private RayPlace mRayPlace;
    private Recording mNewRecording;

    /**
     * Initialize the communications with the DB
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        String sport = System.getenv("OPENSHIFT_MONGODB_DB_PORT");
        String db = System.getenv("OPENSHIFT_APP_NAME");
        
        String user = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
        String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
        int port = Integer.decode(sport);

        mRayPlace = new RayPlace();
        
        try {
            mongo = new Mongo(host , port);
        } catch (UnknownHostException e) {
            throw new ServletException("Failed to access Mongo server", e);
        }
        mongoDB = mongo.getDB(db);
        if(mongoDB.authenticate(user, password.toCharArray()) == false) {
            throw new ServletException("Failed to authenticate against db: "+db);
        }
    }
    
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.addHeader("Access-Control-Allow-Origin", "http://places-ngohgia.rhcloud.com");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, TRACE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "accept, content-type");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.addHeader("Access-Control-Allow-Origin", "http://places-ngohgia.rhcloud.com");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, TRACE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "accept, content-type");
        //response.setHeader("Access-Control-Max-Age", "86400");
        response.setCharacterEncoding("UTF-8");
        
        String query = request.getParameter("query");
        if (query.equals(ADD_PLACE_QUERY)){
        	addNewPlaceQuery(request, response);
        } else if (query.equals(ADD_RECORD_QUERY)){
        	addNewRecordQuery(request, response);
        } else if (query.equals(REMOVE_PLACE_QUERY)){
        	removePlaceQuery(request, response);
        } else if (query.equals(REMOVE_RECORD_QUERY)){
        	removeRecordQuery(request, response);
        } else if (query.equals(SIGNUP_QUERY)){
        	signupQuery(request, response);
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        response.addHeader("Access-Control-Allow-Origin", "http://places-ngohgia.rhcloud.com");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers:", "Content-Type");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try {
	        if (query.equals(VIEW_PLACES_DB_QUERY)){
	        	viewPlacesDBQuery(request, response);
	        } else if (query.equals(VIEW_RECORDS_FILES_QUERY)){
	        	viewRecordFilesQuery(request, response);
	        } else if (query.equals(SEARCH_PLACES_QUERY)){
	        	searchPlacesQuery(request, response);
	        } else if (query.equals(GET_RECORDS_BY_PLACE_QUERY)){
	        	getRecordsByPlaceQuery(request, response);
	        } else if (query.equals(VIEW_LOGS_QUERY)){
	        	viewLogsQuery(request, response);
	        } else if (query.equals(VIEW_RECORDINGS_QUERY)){
	        	viewRecordingsQuery(request, response);
	        } else if (query.equals(GET_RECORDS_DB_SIZE_QUERY)){
	        	getRecordsDbSizeQuery(request, response);
	        } else if (query.equals(GET_LOGS_DB_SIZE_QUERY)){
	        	getLogsDbSizeQuery(request, response);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

	/******************************************************************************************************
	 * FUNCTIONS TO PROCESS POST REQUEST OF THE DB
	 ******************************************************************************************************/
    /**
     * Function to add new a new place to the server
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void addNewPlaceQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	StringBuilder sb = new StringBuilder();
        try {
        	String line;
        	while ((line = request.getReader().readLine()) != null)
        		sb.append(line);
        	
        	JSONObject jsonObj;
        	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
        	
        	try {
				jsonObj = new JSONObject(sb.toString().trim());
				
	            //response.setStatus(HttpServletResponse.SC_OK);
	            
	            mRayPlace.setId(jsonObj.getString("id"));
	            mRayPlace.setUserId(jsonObj.getString("uid"));
	            mRayPlace.setCreatedBy(jsonObj.getString("created_by"));
	            mRayPlace.setGooglePlaceId(jsonObj.getString("google_id"));
	            mRayPlace.setGooglePlaceRef(jsonObj.getString("google_ref"));
	            mRayPlace.setFoursquareId(jsonObj.getString("foursquare_id"));
	            mRayPlace.setCategoriesFromJSONArr(jsonObj.getJSONArray("categories"));
	            mRayPlace.setPlaceName(jsonObj.getString("name"));
	            mRayPlace.setLng(jsonObj.getDouble("lng"));
	            mRayPlace.setLat(jsonObj.getDouble("lat"));
	            mRayPlace.setPhone(jsonObj.getString("phone"));
	            mRayPlace.setAddress(jsonObj.getString("address"));
	            mRayPlace.setAccessInfoFromJSONArr(jsonObj.getJSONArray("access_info"));
	            mRayPlace.setCustomizedAccessInfoFromJSONArr(jsonObj.getJSONArray("customized_access_info"));
	            mRayPlace.setIsPublic(jsonObj.getBoolean("is_public"));
	            mRayPlace.updateGeoHash();
	            
	            addNewPlaceDocToDB(writer);
	            
	            // Logger
	            response.setStatus(HttpServletResponse.SC_OK);
	            addNewLog(request, response, sb.toString(), "", "");
	            
			} catch (Exception e) {
				// Logger
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				response.getOutputStream().println(sw.toString());
				//writer.write(sb.toString());
				//writer.write(e.getStackTrace().toString());
				
	            //addNewLog(request, response, sb.toString(), "", e.getMessage());
				//writer.write(e.getMessage());
			}
        	writer.flush();
            writer.close();
        } catch (IOException ioe) {
        	// Logger
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            addNewLog(request, response, sb.toString(), "", ioe.getMessage());
        	
        	response.getOutputStream().println(ioe.getMessage());
        }
    }
    
    /**
     * Function to add a new record to the server
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void addNewRecordQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
    	String line;
    	while ((line = request.getReader().readLine()) != null)
    		sb.append(line);
    	
    	try {
			mNewRecording = new Recording();
	    	
	    	JSONObject jsonObj = new JSONObject(sb.toString());
	    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
	        
	        mNewRecording.placeId = jsonObj.getString("place_id");
	        mNewRecording.userId = jsonObj.getString("user_id");
	        
	        addNewRecordDocToDB(writer);
	    	writer.flush();
	        writer.close();
	        //response.setStatus(HttpServletResponse.SC_OK);
	        
	        // Logger
	        response.setStatus(HttpServletResponse.SC_OK);
            addNewLog(request, response, sb.toString(), "", "");
		} catch (Exception e) {
			response.getOutputStream().println(e.getMessage());
			// Logger
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            addNewLog(request, response, sb.toString(), "", e.getMessage());
		}
    }

    /**
     * Function to add the new place doc to the DB collection
     * @param writer
     * @return id of the new document in the DB
     */
	protected void addNewPlaceDocToDB(OutputStreamWriter writer) throws Exception{
        mongoDB.requestStart();

        boolean isPublic = mRayPlace.getIsPublic();
        String userId = mRayPlace.getUserId();
        
        DBCollection coll;
        if (isPublic)
        	coll = mongoDB.getCollection(RAY_PLACES_DB_ID);
        else
        	coll = mongoDB.getCollection(userId);
        
        BasicDBObject doc = new BasicDBObject();
        doc.put("name", mRayPlace.getPlaceName());
        doc.put("created_by", mRayPlace.getCreatedBy());
        doc.put("google_id", mRayPlace.getGooglePlaceId());
        doc.put("google_ref", mRayPlace.getGooglePlaceRef());
        doc.put("foursquare_id", mRayPlace.getFoursquareId());
        doc.put("lat", mRayPlace.getLat());
        doc.put("lng", mRayPlace.getLng());
        doc.put("geoHash", mRayPlace.getGeoHash());
        doc.put("shortGeoHash", mRayPlace.getShortGeoHash());
        doc.put("phone", mRayPlace.getPhone());
        doc.put("address", mRayPlace.getAddress());
        doc.put("categories", mRayPlace.getPlaceType());
        doc.put("is_public", mRayPlace.getIsPublic());
        doc.put("access_info", createMongoDocListFromMap(mRayPlace.getAccessInfo()));
        doc.put("customized_access_info", createMongoDocListFromMap(mRayPlace.getCustomizedAccessInfo()));

        if (mRayPlace.getId().equals("")){
            coll.insert(doc);
            writer.write(doc.get("_id").toString());
        } else {
        	BasicDBObject query = new BasicDBObject("_id", new ObjectId(mRayPlace.getId()));

            coll.update(query, doc);
            writer.write(mRayPlace.getId());
        }
        
        //writer.write(mRayPlace.getId());
        //writer.write(doc.toString());
        //writer.write(doc.get("_id").toString());

        mongoDB.requestDone();
    }
	
    /**
     * Function to add new recording doc to the DB
     * @param writer
     * @return id of the newly added recording
     * @throws IOException 
     */
	protected void addNewRecordDocToDB(OutputStreamWriter writer) throws IOException {
        mongoDB.requestStart();

        DBCollection coll = mongoDB.getCollection(RECORDINGS_COLLECTION);
        
        BasicDBObject doc = new BasicDBObject();

        doc.put("userId", mNewRecording.userId);
        doc.put("placeId", mNewRecording.placeId);
        coll.insert(doc);
        
    	writer.write(doc.get("_id").toString());
        
        mongoDB.requestDone();
    }
	
	private void removePlaceQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder sb = new StringBuilder();
    	String line;
    	while ((line = request.getReader().readLine()) != null)
    		sb.append(line);
		
		try {	    	
	    	JSONObject jsonObj = new JSONObject(sb.toString());
	        
	        String placeId = jsonObj.getString("place_id");
	        String collId = jsonObj.getString("coll_id");
	        
        	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
	        
	        removePlaceFromDB(placeId, collId, writer);
	    	
	        //response.setStatus(HttpServletResponse.SC_OK);
	    	writer.flush();
	        writer.close();
	        
	        // Logger
	        response.setStatus(HttpServletResponse.SC_OK);
            addNewLog(request, response, sb.toString(), "", "");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().println(e.getMessage());
			// Logger
            addNewLog(request, response, sb.toString(), "", e.getMessage());
		}
    }
	
	protected void removePlaceFromDB(String placeId, String collId, OutputStreamWriter writer) throws IOException {
        mongoDB.requestStart();

        DBCollection coll;
        if (collId.equals("public_ray"))
        	coll = mongoDB.getCollection(RAY_PLACES_DB_ID);
        else
        	coll = mongoDB.getCollection(collId);
        
        BasicDBObject doc = new BasicDBObject("_id", new ObjectId(placeId));

        writer.write("Before place removed: " + coll.count() + "\n");
        coll.remove(doc);
        writer.write("Removed place doc: " + doc.toString() + "\n");
        writer.write("After place removed: " + coll.count());

        mongoDB.requestDone();
    }
	
	private void removeRecordQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder sb = new StringBuilder();
    	String line;
    	while ((line = request.getReader().readLine()) != null)
    		sb.append(line);
		
		try {	    	
	    	JSONObject jsonObj = new JSONObject(sb.toString());
	        
	        String recordId = jsonObj.getString("record_id");
	        
        	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
	        
	        removeRecordFromDB(recordId, writer);
	    	
	        //response.setStatus(HttpServletResponse.SC_OK);
	    	writer.flush();
	        writer.close();
	        
	        // Logger
	        response.setStatus(HttpServletResponse.SC_OK);
            addNewLog(request, response, sb.toString(), "", "");
		} catch (Exception e) {
			response.getOutputStream().println(e.getMessage());
			// Logger
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            addNewLog(request, response, sb.toString(), "", e.getMessage());
		}
    }
	
	protected void removeRecordFromDB(String recordId, OutputStreamWriter writer) throws IOException {
        mongoDB.requestStart();

        DBCollection coll;
        coll = mongoDB.getCollection(RECORDINGS_COLLECTION);
        
        BasicDBObject doc = new BasicDBObject("_id", new ObjectId(recordId));

        writer.write("Before record remove: " + coll.count());
        coll.remove(doc);
        writer.write("Removed record doc: " + doc.toString() + "\n");
        writer.write("After record remove: " + coll.count());
        
        mongoDB.requestDone();
    }
	
	/******************************************************************************************************
	 * FUNCTIONS TO PROCESS GET REQUEST OF THE DB
	 ******************************************************************************************************/
	
	protected void getRecordsByPlaceQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
    	String placeId = request.getParameter("place_id");
    	
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());

    	JSONArray recordsArr = new JSONArray();
    	try {
    	    mongoDB.requestStart();

    	    DBCollection coll = mongoDB.getCollection(RECORDINGS_COLLECTION);
    	    BasicDBObject query = new BasicDBObject();
    	    query.append("placeId", placeId);
    	    
    	    DBCursor cur = coll.find(query);
    	    String tmp = "";
    	    
    	    while (cur.hasNext()){
    	    	tmp = cur.next().toString();
    	    	try {
    	    		JSONObject newObj = new JSONObject(tmp);
    	    		
    	    		recordsArr.put(newObj);
    	    	} catch (Exception e){
    	    		e.printStackTrace();
    	    	}
    	    }
    	    
    	    writer.write(recordsArr.toString());
            writer.flush();
            writer.close();
    	    //printJSONArray(placesArr, writer);
            
            // Logger
            addNewLog(request, response, "", recordsArr.toString(), "");
    	} finally {
    	    mongoDB.requestDone();
    	}
    }
    
	/**
	 * Function to view places DB
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws JSONException 
	 */
    protected void viewPlacesDBQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
    	String userId = request.getParameter("uid");
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());

    	JSONArray placesArr = new JSONArray();
    	try {
    	    mongoDB.requestStart();

    	    DBCollection coll = mongoDB.getCollection(userId);
    	    
    	    DBCursor cur = coll.find();
    	    String tmp = "";
    	    
    	    while (cur.hasNext()){
    	    	tmp = cur.next().toString();
    	    	try {
    	    		JSONObject newObj = new JSONObject(tmp);
    	    		placesArr.put(newObj);
    	    	} catch (Exception e){
    	    		e.printStackTrace();
    	    	}
    	    }
    	    
    	    printJSONArray(placesArr, writer);
    	    // Logger
            addNewLog(request, response, "", placesArr.toString(), "");
    	} finally {
    	    mongoDB.requestDone();
    	}
    }
    
    protected void searchPlacesQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
    	String userId = request.getParameter("uid");
    	double lat = Double.parseDouble(request.getParameter("lat"));
    	double lng = Double.parseDouble(request.getParameter("lng"));
    	double rad = Double.parseDouble(request.getParameter("rad"));
    	
    	String geoHash = GeoHash.encodeHash(lat,  lng);
    	String shortGeoHash = geoHash.substring(0, GeoHash.SHORT_HASHES);
    	
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());

    	JSONArray placesArr = new JSONArray();
    	try {
    	    mongoDB.requestStart();

    	    DBCollection coll = mongoDB.getCollection(userId);
    	    BasicDBObject query = new BasicDBObject();
    	    query.append("shortGeoHash", shortGeoHash);
    	    //writer.write("Query GeoHash: " + shortGeoHash);	// Only for debug
    	    
    	    DBCursor cur = coll.find(query);
    	    String tmp = "";
    	    
    	    while (cur.hasNext()){
    	    	tmp = cur.next().toString();
    	    	
	    		JSONObject newObj = new JSONObject(tmp);
	    		
	    		double placeLat = newObj.getDouble("lat");
	    		double placeLng = newObj.getDouble("lng");
	    		
	    		double dist = getDist(lat, lng, placeLat, placeLng);
	    		//writer.write("<p>" + newObj.getString("_id") + ", name: " + newObj.getString("name") + ", shortGeoHash: " + newObj.getString("shortGeoHash") + " " + ", Dist: " + dist + "</p>"); //Only for debug
	    		
	    		if (dist < rad){
	    			newObj.put("dist", dist);
	    			placesArr.put(newObj);
	    		}
    	    }
    	    // Logger
    	    response.setStatus(HttpServletResponse.SC_OK);
            addNewLog(request, response, "", placesArr.toString(), "");
    	    
    	    //writer.write("<p>RESULT</p>"); 	// Only for debug
    	    writer.write(placesArr.toString());
            writer.flush();
            writer.close();
    	    //printJSONArray(placesArr, writer);
	    } catch (Exception e){// Logger
    	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            addNewLog(request, response, "", placesArr.toString(), e.getMessage());
    	} finally {
    	    mongoDB.requestDone();
    	}
    }
    
    protected void viewRecordFilesQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		ServletOutputStream os = response.getOutputStream();
		
	    File fileDir = new File(System.getenv("OPENSHIFT_DATA_DIR"));
	    
	    for (File file : fileDir.listFiles()){
	    	os.println(file.getName() + "<br>");
	    }
	    	 
	    os.close();
    }
    
    /************************************************************************************************/
    /* USER AUTHENTICATION																			*/
    /************************************************************************************************/
    
    /**
     * Function to add new user to DB
     */
    private void signupQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	StringBuilder sb = new StringBuilder();
        try {
        	String line;
        	while ((line = request.getReader().readLine()) != null)
        		sb.append(line);
        	
        	JSONObject jsonObj;
        	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
        	
        	try {
        		mongoDB.requestStart();

                DBCollection userColl = mongoDB.getCollection(USERS_COLLECTION);
                BasicDBObject doc = new BasicDBObject();
        		
        		jsonObj = new JSONObject(sb.toString().trim());
				
        		doc.put("email", jsonObj.getString("email"));
        		doc.put("pwd", jsonObj.getString("pwd"));
        		doc.put("business", jsonObj.getString("business"));
        		doc.put("fname", jsonObj.getString("fname"));
        		doc.put("lname", jsonObj.getString("lname"));
        		doc.put("phone", jsonObj.getString("phone"));
	            
        		if (findUserByEmail(userColl, doc.getString("email")).equals("")){
        			userColl.insert(doc);
        		
		            // Logger
		            response.setStatus(HttpServletResponse.SC_OK);
		            //writer.write(doc.get("_id").toString());
		            writer.write("");
        		} else {
        			writer.write("The email account you entered has already been used");
        		}
	            addNewLog(request, response, sb.toString(), "", "");
	            
			} catch (Exception e) {
				// Logger
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				writer.write(sb.toString());
				writer.write(e.getStackTrace().toString());
				
	            addNewLog(request, response, sb.toString(), "", e.getMessage());
				writer.write(e.getMessage());
			} finally {
				mongoDB.requestDone();
			}
        	writer.flush();
            writer.close();
        } catch (IOException e) {
        	// Logger
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            addNewLog(request, response, sb.toString(), "", e.getMessage());
        	
        	response.getOutputStream().println(e.getMessage());
        }
    }
    
    private String findUserByEmail(DBCollection userColl, String email){
    	BasicDBObject query = new BasicDBObject();
	    query.append("email", email);
	    
	    DBCursor cur = userColl.find(query);
	    
	    if (cur.hasNext())
	    	return cur.next().get("_id").toString();
	    else
	    	return "";
    }
    
    /************************************************************************************************/
    /* DATA LOGGER																					*/
    /************************************************************************************************/
    
    /**
     * Add a new log to DB
     */
	protected void addNewLog(HttpServletRequest request, HttpServletResponse response,
			String requestData, String responseData, String error) {
        try {
            mongoDB.requestStart();

            DBCollection coll;
            coll = mongoDB.getCollection(LOGS_COLLECTION);
            
            BasicDBObject doc = new BasicDBObject();
            
            BasicDBObject requestObj = new BasicDBObject();
            requestObj.put("method", request.getMethod());
            requestObj.put("ip", request.getRemoteAddr());
            requestObj.put("query", request.getQueryString());
            requestObj.put("request_data", requestData);
            doc.put("request", requestObj);
            
            BasicDBObject responseObj = new BasicDBObject();
            responseObj.put("status", response.getStatus());
            responseObj.put("response_data", responseData);
            doc.put("response", responseObj);

            coll.insert(doc);
        } catch (Exception e){
        	e.printStackTrace();
        } finally {
            mongoDB.requestDone();
        }
    }
	
	/**
	 * View logs
	 * @param request
	 */
	protected void viewLogsQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
    	int start = Integer.parseInt(request.getParameter("start"));
    	int size = Integer.parseInt(request.getParameter("size"));
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());

    	try {
    	    mongoDB.requestStart();

    	    DBCollection coll = mongoDB.getCollection(LOGS_COLLECTION);
    	    
    	    DBCursor cur = coll.find().limit(size).skip(start);
        	JSONArray logsArr = new JSONArray();
    	    
    	    while (cur.hasNext()){
    	    	DBObject obj = cur.next();
    	    	//writer.write(obj.toString());
    	    	try {
    	    		JSONObject newObj = new JSONObject(obj.toString());
    	    		//writer.write(newObj.toString());
    	    		
    	    		long ts = ((ObjectId) obj.get( "_id" )).getTime();
	        		newObj.put("ts", ts);
    	    		
    	    		logsArr.put(newObj);
    	    	} catch (Exception e){
    	    		e.printStackTrace();
    	    	}
    	    }
    	    
    	    printJSONArray(logsArr, writer);
    	} catch (Exception e){
        	e.printStackTrace();
        } finally {
    	    mongoDB.requestDone();
    	}
    }
	
	/**
     * Function to view recordings DB
     */
	protected void viewRecordingsQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		int start = Integer.parseInt(request.getParameter("start"));
    	int size = Integer.parseInt(request.getParameter("size"));
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
    	
    	try {
            mongoDB.requestStart();
	        DBCollection coll = mongoDB.getCollection(RECORDINGS_COLLECTION);
	        
	        DBCursor cur = coll.find().limit(size).skip(start);
	        JSONArray recordsArray = new JSONArray();
	        
	        while (cur.hasNext()){
	        	DBObject obj = cur.next();
	        	try {
	        		JSONObject newObj = new JSONObject(obj.toString());
	        		
	        		long ts = ((ObjectId) obj.get( "_id" )).getTime();
	        		newObj.put("ts", ts);
	        		
	        		recordsArray.put(newObj);
	        	} catch (Exception e){
	        		e.printStackTrace();
	        	}
	        }
	        printJSONArray(recordsArray, writer);
    	} finally {
            mongoDB.requestDone();
        }
    }
	
	/**
     * Get size of recordings DB
     */
	protected void getRecordsDbSizeQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
    	
    	try {
            mongoDB.requestStart();
	        DBCollection coll = mongoDB.getCollection(RECORDINGS_COLLECTION);
	        String countStr = String.valueOf(coll.count());
	        
	        writer.write(countStr);
	        writer.flush();
	        writer.close();
    	} finally {
            mongoDB.requestDone();
        }
	}
	
	/**
     * Get size of logs DB
     */
	protected void getLogsDbSizeQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
    	
    	try {
            mongoDB.requestStart();
	        DBCollection coll = mongoDB.getCollection(LOGS_COLLECTION);
	        String countStr = String.valueOf(coll.count());
	        
	        writer.write(countStr);
	        writer.flush();
	        writer.close();
    	} finally {
            mongoDB.requestDone();
        }
	}
    
    private void printJSONArray(JSONArray arr, OutputStreamWriter writer) throws IOException, JSONException{
    	JSONObject result = new JSONObject();
    	
    	result.put("count", arr.length());
    	result.put("result", arr);
    	writer.write(result.toString());
        writer.flush();
        writer.close();
    }
    
	/**
	 * Returns the distance between two points given in latitude/longitude
	 * @param lat_1 latitude of first point
	 * @param lon_1 longitude of first point
	 * @param lat_2 latitude of second point
	 * @param lon_2 longitude of second point
	 * @return the distance in meters
	 */
	private double getDist(double lat_1, double lon_1, double lat_2, double lon_2) {
		// source: http://www.movable-type.co.uk/scripts/latlong.html
		double dLon = lon_2 - lon_1;
		double dLat = lat_2 - lat_1;
		lat_1 = Math.toRadians(lat_1);
		lon_1 = Math.toRadians(lon_1);
		lat_2 = Math.toRadians(lat_2);
		lon_2 = Math.toRadians(lon_2);
		dLon = Math.toRadians(dLon);
		dLat = Math.toRadians(dLat);
		
		double r = 6378137; // km
		double a = Math.sin(dLat/2)*Math.sin(dLat/2) + 
					Math.cos(lat_1)*Math.cos(lat_2) *
					Math.sin(dLon/2)*Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return c*r;
	}
	
	private ArrayList<BasicDBObject> createMongoDocListFromMap(HashMap<String, String> infoMap){
		ArrayList<BasicDBObject> doc = new ArrayList<BasicDBObject>();
		
		for (Entry<String, String> entry : infoMap.entrySet()){
			BasicDBObject item = new BasicDBObject();
		    item.put(entry.getKey(), entry.getValue());
		    doc.add(item);
		}
		
		return doc;
	}
}
