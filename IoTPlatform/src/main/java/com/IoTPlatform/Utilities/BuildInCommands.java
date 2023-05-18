package com.IoTPlatform.Utilities;

import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.bson.Document;

public class BuildInCommands {
    private MongoDBHelper databaseHelper;

    public BuildInCommands(MongoDBHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public ObjectId CreateCompanyCommand(JSONObject json) {
        try {
            String collectionName = "Company";
            Document document = Document.parse(json.toString());
            return databaseHelper.create(collectionName, document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

        public ObjectId CreateProductCommand(JSONObject json) {
        try {
            String collectionName = "Product";
            Document document = Document.parse(json.toString());
            return databaseHelper.create(collectionName, document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectId CreateIOTDeviceCommand(JSONObject json) {
        try {
            String collectionName = "IOTDevice";
            Document document = Document.parse(json.toString());
            return databaseHelper.create(collectionName, document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectId UpdateIOTDeviceCommand(JSONObject json) {
        try {
            String collectionName = "IOTDevice";
            Document document = Document.parse(json.toString());
            Document query = new Document("_id", document.getObjectId("_id"));
            Document update = new Document("Info", document.getString("Info"));
            databaseHelper.update(collectionName, query, update);
            return document.getObjectId("_id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
