package recording.servlets;
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

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

@MultipartConfig
@WebServlet(name = "AddRecordServlet")
public class AddRecordServlet extends HttpServlet {
    private Mongo mongo;
    private DB mongoDB;
    
    private Recording mNewRecording;
    
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

    /**
     * Add new recording to the DB
     * @param name
     * @param placeId
     * @param userId 
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html");
    	try {
	        StringBuilder sb = new StringBuilder();
        	String line;
        	while ((line = request.getReader().readLine()) != null)
        		sb.append(line);
        	
        	JSONObject jsonObj = new JSONObject(sb.toString());
        	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
	        response.setStatus(HttpServletResponse.SC_OK);
	        
	        mNewRecording.placeId = jsonObj.getString("place_id");
	        mNewRecording.userId = jsonObj.getString("user_id");
	        writer.write("placeIDDDD: " + mNewRecording.placeId);
	        
	        addNewDocToDB(writer);
        	writer.flush();
            writer.close();
	        
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}
    }

    /**
     * get all recordings from the DB
     * @param placeId
     * @param userId 
     * @return all recordings of the placeId and userId
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
    	writer.write("<h1>Hello</h1>");
    	
    	try {
            mongoDB.requestStart();
	        DBCollection coll = mongoDB.getCollection("recordings");
	        
	        DBCursor cur = coll.find();
	        JSONArray returnedArray = new JSONArray();
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
	        writer.write(returnedArray.toString());
	        writer.flush();
	        writer.close();
    	} finally {
            mongoDB.requestDone();
        }
    }
    
    /**
     * Function to add new recording to the DB
     * @param writer
     * @return id of the newly added recording
     * @throws IOException 
     */
	protected void addNewDocToDB(OutputStreamWriter writer) throws IOException {
        try {
            mongoDB.requestStart();

            DBCollection coll = mongoDB.getCollection("recordings");
            
            BasicDBObject doc = new BasicDBObject();

            doc.put("name", mNewRecording.name);
            doc.put("userId", mNewRecording.userId);
            doc.put("placeId", mNewRecording.placeId);
            coll.insert(doc);
            
            writer.write("Name: " + mNewRecording.name);
            writer.write("userID: " + mNewRecording.userId);
        	writer.write(doc.get("_id").toString());
            
        } catch (Exception e) {
        	writer.write(e.getMessage());
        } finally {
            mongoDB.requestDone();
        }
    }
}