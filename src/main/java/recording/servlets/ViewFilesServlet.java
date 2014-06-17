package recording.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ViewFilesServlet")
public class ViewFilesServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filePath = request.getRequestURI();
		ServletOutputStream os = response.getOutputStream();
		
	    File fileDir = new File(System.getenv("OPENSHIFT_DATA_DIR"));
	    
	    for (File file : fileDir.listFiles()){
	    	os.println(file.getName());
	    }
	    	 
	    os.close();
	}

}
