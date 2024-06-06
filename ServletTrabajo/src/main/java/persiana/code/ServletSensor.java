package persiana.code;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




public class ServletSensor extends HttpServlet {


	/**
	 * 
	 */
	
	
	private static final long serialVersionUID = 5581936480104414707L;
	List<Sensor> sensorPass;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		 resp.setContentType("application/json");
	        PrintWriter out = resp.getWriter();
	        Gson gson = new Gson();
	        String jsonResponse = gson.toJson(sensorPass);
	        out.print(jsonResponse);
	        out.flush();
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		 resp.setContentType("application/json");
	        try (BufferedReader reader = req.getReader()) {
	            Gson gson = new Gson();
	            Sensor newUser = gson.fromJson(reader, Sensor.class);
	            if (newUser != null  && !newUser.getSensorPass().isEmpty()) {
	            	sensorPass.add(newUser);
	                resp.setStatus(HttpServletResponse.SC_CREATED);
	                PrintWriter out = resp.getWriter();
	                String jsonResponse = gson.toJson(newUser);
	                out.print(jsonResponse);
	                out.flush();
	            } else {
	                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	                PrintWriter out = resp.getWriter();
	                out.print("{\"error\":\"Invalid user or password\"}");
	                out.flush();
	            }
	        } catch (JsonSyntaxException e) {
	            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            PrintWriter out = resp.getWriter();
	            out.print("{\"error\":\"Invalid JSON format\"}");
	            out.flush();
	        }
	};

	public void init() throws ServletException {
		sensorPass = new ArrayList<>();
		sensorPass.add(new Sensor(0, "Sensor 0"));
		sensorPass.add(new Sensor(1, "Sensor 1"));
		sensorPass.add(new Sensor(2, "Sensor 2"));
		super.init();
	}
	

}
