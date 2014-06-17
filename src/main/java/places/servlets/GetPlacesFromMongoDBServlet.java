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
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

@WebServlet(name = "MongoDBServlet")
public class GetPlacesFromMongoDBServlet extends HttpServlet {
    private Mongo mongo;
    private DB mongoDB;
    
    private RayPlace mNewPlace;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        String sport = System.getenv("OPENSHIFT_MONGODB_DB_PORT");
        String db = System.getenv("OPENSHIFT_APP_NAME");
        
        String user = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
        String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
        int port = Integer.decode(sport);

        mNewPlace = new RayPlace();
        
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
	    	
	    	JSONArray placesArr = getJSONArrayResult(userId);
	    	for (int i = 0; i < placesArr.length(); i++)
	    		response.getOutputStream().println(placesArr.getJSONObject(i).toString() + "<br>");
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}
    }

    private JSONArray getJSONArrayResult(String userId){
        JSONArray returnedArray = new JSONArray();
    	
        try {
            mongoDB.requestStart();

            DBCollection coll = mongoDB.getCollection(userId);
            
            DBCursor cur = coll.find();
            String tmp = "";
            
            while (cur.hasNext()){
            	tmp = cur.next().toString();
            	try {
            		JSONObject newObj = new JSONObject(tmp);
            		returnedArray.put(newObj);
            	} catch (Exception e){
            		e.printStackTrace();
            	}
            }
        } finally {
            mongoDB.requestDone();
        }
        
        return returnedArray;
    }
}