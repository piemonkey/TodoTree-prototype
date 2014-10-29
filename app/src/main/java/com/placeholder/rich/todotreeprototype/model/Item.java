package com.placeholder.rich.todotreeprototype.model;

public class Item {
    private final String name;
    private boolean complete;
    private int nSubItems;
    private int nItemsLeft;

    public Item(String name) {
        this(name, false, 0, 0);
    }

    public Item(String name, boolean complete) {
        this(name, complete, 0, 0);
    }

    public Item(String name, boolean complete, int nSubItems, int nItemsLeft) {
        this.name = name;
        this.complete = complete;
        this.nSubItems = nSubItems;
        this.nItemsLeft = nItemsLeft;
    }

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean toggleComplete() {
        return complete = !complete;
    }

    public int getNSubItems() {
        return nSubItems;
    }

    public void setNSubItems(int nSubItems) {
        this.nSubItems = nSubItems;
    }

    public int getNItemsLeft() {
        return nItemsLeft;
    }

    public void setNItemsLeft(int nItemsLeft) {
        this.nItemsLeft = nItemsLeft;
    }

    public boolean hasSubItems() {
        return nSubItems > 0;
    }

}
