package com.placeholder.rich.todotreeprototype.infrastructure;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.UUID;

public interface ListStore {
    void save(ListTree currentSave);

    void save(TagList currentSave);

    void save(Item item);

    ListTree load(UUID currentId);

    TagList loadTagged(When tag);

    ListTree loadRoot();

    void addItem(Item item);

    void saveUpdatedCompleteness(Item item);

    void delete(Item item);
}
