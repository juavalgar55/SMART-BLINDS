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




public class ServletActuador extends HttpServlet {


	/**
	 * 
	 */
	
	
	private static final long serialVersionUID = 55819364801044107L;
	List<Actuador> actuadorPass;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		 resp.setContentType("application/json");
	        PrintWriter out = resp.getWriter();
	        Gson gson = new Gson();
	        String jsonResponse = gson.toJson(actuadorPass);
	        out.print(jsonResponse);
	        out.flush();
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		 resp.setContentType("application/json");
	        try (BufferedReader reader = req.getReader()) {
	            Gson gson = new Gson();
	            Actuador newUser = gson.fromJson(reader, Actuador.class);
	            if (newUser != null  && !newUser.getActuadorPass().isEmpty()) {
	            	actuadorPass.add(newUser);
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
		actuadorPass = new ArrayList<>();
		actuadorPass.add(new Actuador(0, "actuador 0"));
		actuadorPass.add(new Actuador(1, "actuador 1"));
		actuadorPass.add(new Actuador(2, "actuador 2"));
		super.init();
	}
	

}
