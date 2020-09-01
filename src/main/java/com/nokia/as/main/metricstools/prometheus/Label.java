package com.nokia.as.main.metricstools.prometheus;

public class Label {

    private String name;
    private String value;
    private boolean mutable;

    public Label(String name, String value, boolean mutable) {
        this.name = name;
        this.value = value;
        this.mutable = mutable;
    }

    public Label(String name, String value) {
        this(name, value, false);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isMutable() {
        return mutable;
    }
}
