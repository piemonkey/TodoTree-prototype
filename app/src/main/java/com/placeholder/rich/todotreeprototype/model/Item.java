package com.placeholder.rich.todotreeprototype.model;

import java.util.Comparator;
import java.util.UUID;

public class Item {

    static final ItemComparator COMPARATOR = new ItemComparator();

    private final UUID id;
    private String name;
    private boolean complete;
    private final UUID parent;
    private int nSubItems;
    private int nItemsLeft;
    private When when;

    public Item(String name, UUID parent) {
        this(UUID.randomUUID(), name, false, parent, When.NA, 0, 0);
    }

    public Item(UUID id, String name, boolean complete, UUID parent, When when, int nSubItems,
                int nItemsLeft) {
        this.id = id;
        this.name = name;
        this.complete = complete;
        this.parent = parent;
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

    public UUID getParent() {
        return parent;
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

    public void dontDoNow() {
        when = When.NA;
    }

    public int getNSubItems() {
        return nSubItems;
    }

    public int getNItemsLeft() {
        return nItemsLeft;
    }

    public boolean hasSubItems() {
        return nSubItems > 0;
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
