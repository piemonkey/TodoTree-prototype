package com.placeholder.rich.todotreeprototype.infrastructure;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.UUID;

public interface ListStore {
    void save(ListTree currentSave);

    void save(TagList currentSave);

    ListTree load(UUID currentId);

    TagList loadTagged(When tag);

    ListTree loadRoot();

    void addEntry(String name, boolean completed, When when, UUID parentId, String parent);

    void addItem(Item item, UUID parent);

    void saveUpdatedCompleteness(Item item, UUID parent);

    void delete(Item item, UUID parent);
}
