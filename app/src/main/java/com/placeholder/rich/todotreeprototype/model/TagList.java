package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagList {

    private final When tag;
    private final List<Item> items;

    public TagList(When tag, List<Item> items) {
        this.tag = tag;
        this.items = new ArrayList<Item>(items);
    }

    public When getTag() {
        return tag;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void deleteItem(Item toDelete) {
        items.remove(toDelete);
    }

}
