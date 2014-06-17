package recording.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import places.servlets.RayPlace;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class RecordingFileManagerServlet extends HttpServlet {
	//TODO
	private int BUFFER_LENGTH = 8192;
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
		    PrintWriter out = response.getWriter();
		    for (Part part : request.getParts()) {
		        InputStream is = request.getPart(part.getName()).getInputStream();
		        String fileName = getFileName(part);
		        FileOutputStream os = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR") + fileName);
		        byte[] bytes = new byte[BUFFER_LENGTH];
		        int read = 0;
		        while ((read = is.read(bytes, 0, BUFFER_LENGTH)) != -1) {
		            os.write(bytes, 0, read);
		        }
		        os.flush();
		        is.close();
		        os.close();
		        response.setStatus(HttpServletResponse.SC_OK);
		    }
		} catch (Exception e){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getOutputStream().println(e.getMessage());
		}
	  }
	 
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  try {
		    String filePath = request.getRequestURI();
		 
		    File file = new File(System.getenv("OPENSHIFT_DATA_DIR") + filePath.replace("/uploads/",""));
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
			  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			  response.getOutputStream().println(e.getMessage());
		  }
	  }
	 
	  private String getFileName(Part part) {
	        for (String cd : part.getHeader("content-disposition").split(";")) {
	          if (cd.trim().startsWith("filename")) {
	            return cd.substring(cd.indexOf('=') + 1).trim()
	                    .replace("\"", "");
	          }
	        }
	        return null;
	 }
}
