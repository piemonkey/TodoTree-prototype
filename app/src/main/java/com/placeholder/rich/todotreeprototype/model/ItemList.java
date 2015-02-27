package com.placeholder.rich.todotreeprototype.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface ItemList {

    public List<Item> getItems();

    public void deleteItem(Item toDelete);

}
