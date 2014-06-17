package recording.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONArray;
import org.json.JSONObject;

import places.servlets.RayPlace;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

@MultipartConfig
@WebServlet(name = "MongoDBServlet")
public class DeleteRecordingServlet extends HttpServlet {
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
    	try {
	    	Part recordPart = request.getPart("id");
	    	String recordId = readTextFromPart(recordPart);
	    	
	    	deleteDocFromDB(recordId);
	    	
	    	//TODO Remove file
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}
	}

	private String readTextFromPart (Part part) throws IOException{
		BufferedReader reader = new BufferedReader( new InputStreamReader(part.getInputStream()));
		String line ="";
		String result = "";

		while((line=reader.readLine())!=null)
		{
		    result += line;
		}
		return result;
	}
	
	protected void deleteDocFromDB(String recordId) {
        try {
            mongoDB.requestStart();

            DBCollection coll = mongoDB.getCollection("RecordingColl");
            
            BasicDBObject doc = new BasicDBObject();

            doc.put("_id", recordId);

            coll.remove(doc);
        } finally {
            mongoDB.requestDone();
        }
    }
}
