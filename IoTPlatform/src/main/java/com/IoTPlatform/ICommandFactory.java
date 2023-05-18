package com.IoTPlatform;

import org.bson.types.ObjectId;
import org.eclipse.persistence.sessions.coordination.Command;
import org.json.JSONObject;

import java.util.function.Function;

public interface ICommandFactory<T> {
    void addCreator(String key, Function<JSONObject, T> constructor);
    T executeCommand(String key, JSONObject params);
}
