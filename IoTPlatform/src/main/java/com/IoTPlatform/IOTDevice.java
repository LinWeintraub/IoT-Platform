package com.IoTPlatform;

import com.IoTPlatform.Utilities.Tools;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "IOTDevice", value = "/IOTDevice")
public class IOTDevice extends HttpServlet {
    private Tools tools = new Tools();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestBody = tools.getRequestBody(request);
        request.setAttribute("request", requestBody);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/GatewayServer");
        dispatcher.forward(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestBody = tools.getRequestBody(request);
        request.setAttribute("request", requestBody);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/GatewayServer");
        dispatcher.forward(request, response);
    }
}
