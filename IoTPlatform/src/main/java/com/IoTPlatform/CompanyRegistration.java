package com.IoTPlatform;

import com.IoTPlatform.Utilities.SQLHelper;
import com.IoTPlatform.Utilities.Tools;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "CompanyRegistration", value = "/CompanyRegistration")
public class CompanyRegistration extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Tools tools = new Tools();
    private SQLHelper databaseHelper;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Create an instance of DatabaseHelper
            databaseHelper = new SQLHelper("CompanyDB");
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize DatabaseHelper", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String requestBody = tools.getRequestBody(request);
            request.setAttribute("request", requestBody);

            JSONObject json = tools.parseJson(requestBody);

            int companyID = json.getInt("ID");
            String companyName = json.getString("Name");
            String companyAddress = json.getString("Address");
            String companyPhone = json.getString("Phone");

            // Prepare the SQL statement
            String sql = "INSERT INTO Company (ID, Name, Address, Phone) VALUES (?, ?, ?, ?)";

            // Execute the statement
            int rowsAffected = databaseHelper.executeUpdate(sql, companyID, companyName, companyAddress, companyPhone);

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/GatewayServer");
            dispatcher.forward(request, response);

            tools.sendResponseSQL(response, rowsAffected);

        } catch (IOException | ServletException | SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Prepare the SQL statement
            String sql = "SELECT * FROM Company";

            // Execute the statement
            ResultSet rs = databaseHelper.executeQuery(sql);

            // Create a JSON array to store the company information
            JSONArray companyInfo = new JSONArray();
            while (rs.next()) {
                JSONObject company = new JSONObject();
                company.put("ID", rs.getString("ID"));
                company.put("Name", rs.getString("Name"));
                company.put("Address", rs.getString("Address"));
                company.put("Phone", rs.getString("Phone"));
                companyInfo.put(company);
            }

            // Send the response
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(companyInfo);
            out.flush();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            // Close the DatabaseHelper instance
            if (databaseHelper != null) {
                databaseHelper.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}