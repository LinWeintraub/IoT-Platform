package com.IoTPlatform;

import com.IoTPlatform.Utilities.BuildInCommands;
import com.IoTPlatform.Utilities.MongoDBHelper;
import com.IoTPlatform.Utilities.Tools;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.IoTPlatform.ThreadPool.ThreadPool;

@WebServlet(name = "GatewayServer", value = "/GatewayServer")
public class GatewayServer extends HttpServlet {
    private SingletonCommandFactory<ObjectId> commandFactory;
    private Tools tools = new Tools();
    private BuildInCommands buildInCommands;
    private MongoDBHelper databaseHelper;
    private ThreadPool<Void> threadPool;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.commandFactory = SingletonCommandFactory.getInstance();
            this.threadPool = new ThreadPool<>(20);
            this.databaseHelper = new MongoDBHelper("IOTDB");
            this.buildInCommands = new BuildInCommands(databaseHelper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GatewayServer", e);
        }

        // initialize the SingletonCommandFactory here
        commandFactory = SingletonCommandFactory.getInstance();
        commandFactory.addCreator("CreateCompany", buildInCommands::CreateCompanyCommand);
        commandFactory.addCreator("CreateProduct", buildInCommands::CreateProductCommand);
        commandFactory.addCreator("CreateIOTDevice", buildInCommands::CreateIOTDeviceCommand);
        commandFactory.addCreator("UpdateIOTDevice", buildInCommands::UpdateIOTDeviceCommand);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String requestBody = (String) request.getAttribute("request");
        JSONObject json = tools.parseJson(requestBody);

        // get the command from the JSON request
        String commandName = json.getString("Command");

        // execute the command using the thread pool
        threadPool.submit(() -> {
            ObjectId result = commandFactory.executeCommand(commandName, json);
            tools.sendResponseMongoDB(response, result);
            return null;
        });
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String requestBody = (String) request.getAttribute("request");
        JSONObject json = tools.parseJson(requestBody);

        // get the command from the JSON request
        String commandName = json.getString("Command");

        // execute the command using the thread pool
        threadPool.submit(() -> {
            ObjectId result = commandFactory.executeCommand(commandName, json);
            tools.sendResponseMongoDB(response, result);
            return null;
        });
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        String requestBody = (String) request.getAttribute("request");
        JSONObject json = tools.parseJson(requestBody);

        // get the command from the JSON request
        String commandName = json.getString("Command");

        // execute the command using the thread pool
        threadPool.submit(() -> {
            ObjectId result = commandFactory.executeCommand(commandName, json);
            tools.sendResponseMongoDB(response, result);
            return null;
        });
    }

//    protected ObjectId executeCommand(String requestBody) throws ExecutionException, InterruptedException {
//        JSONObject json = tools.parseJson(requestBody);
//
//        // get the command from the JSON request
//        String commandName = json.getString("Command");
//
//        // execute the command using the thread pool
//        Future<Object> result = threadPool.submit(() -> {
//            commandFactory.executeCommand(commandName, json);
//            return null;
//        });
//
//        return (ObjectId) result.get();
//    }
}
