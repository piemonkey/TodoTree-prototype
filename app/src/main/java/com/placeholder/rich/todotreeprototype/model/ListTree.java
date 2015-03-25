package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ListTree implements ItemList {

    private static final UUID ID_ROOT = new UUID(0L, 0L);

    private final UUID id;
    private final String name;
    private final List<Item> items;

    public ListTree(UUID id, String name, List<Item> items) {
        if (ID_ROOT.equals(id)) {
            this.name = null;
        } else {
            this.name = name;
        }
        this.id = id;
        this.items = new ArrayList<>(items);
    }

    public static ListTree rootList(List<Item> items) {
        return new ListTree(ID_ROOT, null, items);
    }

    public static UUID getRootId() {
        return ID_ROOT;
    }

    public boolean isRoot() {
        return ID_ROOT.equals(id);
    }

    @Override
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Item newItem) {
        items.add(newItem);
    }

    @Override
    public void deleteItem(Item toDelete) {
        items.remove(toDelete);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean updateList(Item changedItem) {
        // TODO: Implement if has use case or remove if doesn't
        return false;
    }
}
