package com.placeholder.rich.todotreeprototype.model;

import java.util.List;

public interface ItemList {

    public List<Item> getItems();

    public void deleteItem(Item toDelete);

    public boolean updateList(Item changedItem);

}
