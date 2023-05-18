package com.IoTPlatform;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SingletonCommandFactory<T> implements ICommandFactory<T> {
    private static volatile SingletonCommandFactory<?> instance;
    private final Map<String, Function<JSONObject, T>> creators;

    private SingletonCommandFactory() {
        creators = new HashMap<>();
    }

    public static <T> SingletonCommandFactory<T> getInstance() {
        if (instance == null) {
            synchronized (SingletonCommandFactory.class) {
                if (instance == null) {
                    instance = new SingletonCommandFactory();
                }
            }
        }

        return (SingletonCommandFactory<T>)instance;
    }

    public void addCreator(String key, Function<JSONObject, T> constructor) {
        creators.put(key, constructor);
    }

    public T executeCommand(String key, JSONObject params) {
        Function<JSONObject, T> creator = creators.get(key);

        if (creator == null) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }

        return creator.apply(params);
    }
}