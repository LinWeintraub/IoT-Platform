package com.IoTPlatform.Utilities;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHelper {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBHelper(String dbName) {
        String connectionString = "mongodb://localhost:27017";
        ConnectionString connString = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(dbName);
    }

    public void close() {
        mongoClient.close();
    }

    public ObjectId create(String collectionName, Document document) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
        return document.getObjectId("_id");
    }

    public List<Document> read(String collectionName, Document query) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.find(query).into(new ArrayList<>());
    }

    public void update(String collectionName, Document query, Document update) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.updateOne(query, new Document("$set", update));
    }

    public void delete(String collectionName, Document query) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(query);
    }
}
