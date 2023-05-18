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
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "ProductRegistration", value = "/ProductRegistration")
public class ProductRegistration extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Tools tools = new Tools();
    private SQLHelper databaseHelper;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Create an instance of DatabaseHelper
            databaseHelper = new SQLHelper("ProductDB");
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize DatabaseHelper", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String requestBody = tools.getRequestBody(request);
            request.setAttribute("request", requestBody);

            JSONObject json = tools.parseJson(requestBody);

            int productID = json.getInt("ID");
            String productName = json.getString("Name");
            String companyID = json.getString("CompanyID");
            String productModel = json.getString("Model");

            // Prepare the SQL statement
            String sql = "INSERT INTO Company (ID, Name, CompanyID, Model) VALUES (?, ?, ?, ?)";

            // Execute the statement
            int rowsAffected = databaseHelper.executeUpdate(sql, productID, productName, companyID, productModel);

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
            String sql = "SELECT * FROM Product";

            // Execute the statement
            ResultSet rs = databaseHelper.executeQuery(sql);

            // Create a JSON array to store the company information
            JSONArray productInfo = new JSONArray();
            while (rs.next()) {
                JSONObject product = new JSONObject();
                product.put("ID", rs.getString("ID"));
                product.put("Name", rs.getString("Name"));
                product.put("CompanyID", rs.getString("CompanyID"));
                product.put("Model", rs.getString("Model"));
                productInfo.put(product);
            }

            // Send the response
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(productInfo);
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
