package com.placeholder.rich.todotreeprototype.model;

import java.util.UUID;

public class Item {
    private final UUID id;
    private String name;
    private boolean complete;
    private int nSubItems;
    private int nItemsLeft;
    private When when;

    public Item(String name) {
        this(UUID.randomUUID(), name, false, When.NA, 0, 0);
    }

    public Item(UUID id, String name, boolean complete, When when) {
        this(id, name, complete, when, 0, 0);
    }

    public Item(UUID id, String name, boolean complete, When when, int nSubItems, int nItemsLeft) {
        this.id = id;
        this.name = name;
        this.complete = complete;
        this.when = when;
        this.nSubItems = nSubItems;
        this.nItemsLeft = nItemsLeft;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean toggleComplete() {
        return complete = !complete;
    }

    public When getWhen() {
        return when;
    }

    public void doToday() {
        when = When.TODAY;
    }

    public void doTomorrow() {
        when = When.TOMORROW;
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
