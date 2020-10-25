package com.tanbt;

import com.google.gson.Gson;

public class Change {
    private final String oldValue;
    private final String value;

    public Change(String oldValue, String value) {
        this.oldValue = oldValue;
        this.value = value;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getValue() {
        return value;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Change fromJson(String obj) {
        return new Gson().fromJson(obj, Change.class);
    }
}
