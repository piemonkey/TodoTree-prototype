package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagList implements ItemList {

    private final When tag;
    private final List<Item> items;

    public TagList(When tag, List<Item> items) {
        this.tag = tag;
        this.items = new ArrayList<>(items);
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

    @Override
    public boolean updateList(Item changedItem) {
        final boolean changeMade;
        if (tag == changedItem.getWhen()) {
            if (items.contains(changedItem)) {
                changeMade = false;
            } else {
                items.add(changedItem);
                changeMade = true;
            }
        } else {
            if (items.contains(changedItem)) {
                items.remove(changedItem);
                changeMade = true;
            } else {
                changeMade = false;
            }
        }

        return changeMade;
    }
}
