package com.IoTPlatform.Utilities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.bson.types.ObjectId;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Tools {
    public String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuffer requestBody = new StringBuffer();
        String line;

        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        reader.close();
        return requestBody.toString();
    }

    public JSONObject parseJson(String jsonStr) {
        return new JSONObject(jsonStr);
    }

    public void sendResponseMongoDB(HttpServletResponse response, ObjectId objectId) throws IOException {
        PrintWriter out = response.getWriter();

        if (objectId != null) {
            out.println("Object created successfully with ID: " + objectId.toHexString());
        } else {
            out.println("Error creating object");
        }

        out.close();
    }

    public void sendResponseSQL(HttpServletResponse response, int rowsAffected) throws IOException {
        PrintWriter out = response.getWriter();

        if (rowsAffected > 0) {
            out.println("Company information added successfully");
        } else {
            out.println("Error adding company information");
        }

        out.close();
    }
}
