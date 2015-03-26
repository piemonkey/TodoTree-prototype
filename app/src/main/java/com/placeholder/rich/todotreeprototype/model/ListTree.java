package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ListTree implements ItemList {

    private static final UUID ID_ROOT = new UUID(0L, 0L);
    private static final ItemComparator ITEM_COMPARATOR = new ItemComparator();

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
        Collections.sort(this.items, ITEM_COMPARATOR);
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

    public void sort() {
        Collections.sort(items, ITEM_COMPARATOR);
    }

    @Override
    public boolean updateList(Item changedItem) {
        // TODO: Implement if has use case or remove if doesn't
        return false;
    }

    private static class ItemComparator implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            final int doneComp;
            if (item1.isComplete()) {
                doneComp = item2.isComplete() ? 0 : 1;
            } else {
                doneComp = item2.isComplete() ? -1 : 0;
            }
            return doneComp != 0 ? doneComp : item1.getName().compareTo(item2.getName());
        }
    }

}
