package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListTree {
    private final String name;
    private final List<Item> items;
    private final Item parent;

    public ListTree(String name, Item parent, List<Item> items) {
        this.name = name;
        this.parent = parent;
        this.items = new ArrayList<Item>(items);
    }

    public static ListTree rootList(List<Item> items) {
        return new ListTree(null, null, items);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Item newItem) {
        items.add(newItem);
    }

    public String getName() {
        return name;
    }

    public Item getParent() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }
}
