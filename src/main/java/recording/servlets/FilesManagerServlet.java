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

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

@MultipartConfig
@WebServlet(name = "MongoDBServlet")
public class FilesManagerServlet extends HttpServlet {
	//TODO
	private int BUFFER_LENGTH = 8192;
	
	/**
	 * Handle POST request
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String query = request.getParameter("query");
    	
    	if (query.equals("upload"))
    		uploadRecord(request, response);
    	else if (query.equals("delete"))
    		deleteRecord(request, response);
    }
    
    private void uploadRecord(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	try {
	        Part filePart = request.getPart("file");
	        
	        String result = writeFileFromPart(filePart);
	        
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getOutputStream().println(result);
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}
    }
    
    private void deleteRecord(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	try {
    		StringBuilder sb = new StringBuilder();
	    	String line;
	    	while ((line = request.getReader().readLine()) != null)
	    		sb.append(line);
	    	
	    	JSONObject jsonObj = new JSONObject(sb.toString());
	        
    		String fileName = jsonObj.getString("file_name");
	    	 
	        File file = new File(System.getenv("OPENSHIFT_DATA_DIR") + fileName);
	        if (file.exists()){
	        	file.delete();
	        	response.getOutputStream().println(file.getAbsolutePath() + " deleted.");
	        }
	        
	        response.setStatus(HttpServletResponse.SC_OK);
    	} catch (Exception e) {
    		response.getOutputStream().println(e.getMessage());
    	}
    }

    /**
     * Handle GET request
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String query = request.getParameter("query");
    	
    	if (query.equals("download"))
    		downloadFile(request, response);
    }
    
    private void downloadFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		String fileName = request.getParameter("file_name");
	    	 
	        File file = new File(System.getenv("OPENSHIFT_DATA_DIR") + fileName);
	        InputStream input = new FileInputStream(file);
	     
	        response.setContentLength((int) file.length());
	        response.setContentType(new MimetypesFileTypeMap().getContentType(file));
	     
	        OutputStream output = response.getOutputStream();
	        byte[] bytes = new byte[BUFFER_LENGTH];
	        int read = 0;
	        while ((read = input.read(bytes, 0, BUFFER_LENGTH)) != -1) {
	            output.write(bytes, 0, read);
	            output.flush();
	        }
	     
	        input.close();
	        output.close();
    	} catch (Exception e){
    		response.getOutputStream().println(e.getMessage());
    	}
    }
	
	private String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
		return null;
	}
	
	private String writeFileFromPart(Part filePart) throws Exception{
        InputStream is = filePart.getInputStream();
        String fileName = getFileName(filePart);
        
        String filePath = System.getenv("OPENSHIFT_DATA_DIR") + fileName;
        FileOutputStream os = new FileOutputStream(filePath);
        byte[] bytes = new byte[BUFFER_LENGTH];
        int read = 0;
        while ((read = is.read(bytes, 0, BUFFER_LENGTH)) != -1) {
            os.write(bytes, 0, read);
        }
        
        os.flush();
        is.close();
        os.close();
        return filePath;
	}
}