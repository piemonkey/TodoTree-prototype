package com.placeholder.rich.todotreeprototype.model;

public class Item {
    private final String name;
    private boolean complete;

    public Item(String name) {
        this(name, false);
    }

    public Item(String name, boolean complete) {
        this.name = name;
        this.complete = complete;
    }

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public String toString() {
        return name;
    }
}
