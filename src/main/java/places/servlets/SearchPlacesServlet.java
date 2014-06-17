package places.servlets;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

@WebServlet(name = "SearchPlacesServlet")
public class SearchPlacesServlet extends HttpServlet {
    private Mongo mongo;
    private DB mongoDB;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        String sport = System.getenv("OPENSHIFT_MONGODB_DB_PORT");
        String db = System.getenv("OPENSHIFT_APP_NAME");
        
        String user = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
        String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
        int port = Integer.decode(sport);
        
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
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
	    	response.setContentType("text/html");
	    	
	    	String userId = request.getParameter("uId");
	    	double radius = Double.parseDouble(request.getParameter("radius"));
	    	double lat = Double.parseDouble(request.getParameter("lat"));
	    	double lng = Double.parseDouble(request.getParameter("lng"));
	    	
	    	JSONArray placesArr = new JSONArray();
	    	if (userId.equals("public_ray"))
	    		placesArr = getJSONArrayResultsFromSearch(userId, radius, lat, lng);
	    	else {
	    		JSONArray personalPlaces = getJSONArrayResultsFromSearch(userId, radius, lat, lng);
	    		JSONArray publicPlaces = getJSONArrayResultsFromSearch("public_ray", radius, lat, lng);
	    		
	    		for (int i= 0; i < personalPlaces.length(); i++)
	    			placesArr.put(personalPlaces.get(i));
	    		for (int i= 0; i < publicPlaces.length(); i++)
	    			placesArr.put(publicPlaces.get(i));
	    	}
	    	
	    	response.getOutputStream().println(placesArr.toString());
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}

	}
	
    private JSONArray getJSONArrayResultsFromSearch(String userId, double rad, double lat, double lng){
        JSONArray returnedArray = new JSONArray();
    	
        try {
            mongoDB.requestStart();
            DBCollection coll = mongoDB.getCollection(userId);
            
            String geoHash = GeoHash.encodeHash(lat, lng);
            String shortGeoHash = geoHash.substring(0, GeoHash.SHORT_HASHES);
            BasicDBObject searchQuery = new BasicDBObject();
        	searchQuery.put("shortGeoHash", shortGeoHash);
         
        	DBCursor cur = coll.find(searchQuery);
            
            String tmp = "";
            
            while (cur.hasNext()){
            	tmp = cur.next().toString();
            	try {
            		JSONObject newObj = new JSONObject(tmp);
            		
            		double placeLat = newObj.getDouble("lat");
            		double placeLng = newObj.getDouble("lng");
            		double dist = getDist(placeLat, placeLng, lat, lng);
            		
            		if (dist <= rad){
            			newObj.put("dist", dist);
            		
            			returnedArray.put(newObj);
            		}
            	} catch (Exception e){
            		e.printStackTrace();
            	}
            }
        } finally {
            mongoDB.requestDone();
        }
        
        return returnedArray;
    }
    
	/**
	 * Returns the distance between two points given in latitude/longitude
	 * @param lat_1 latitude of first point
	 * @param lon_1 longitude of first point
	 * @param lat_2 latitude of second point
	 * @param lon_2 longitude of second point
	 * @return the distance in meters
	 */
	public static double getDist(double lat_1, double lon_1, double lat_2, double lon_2) {
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
}
