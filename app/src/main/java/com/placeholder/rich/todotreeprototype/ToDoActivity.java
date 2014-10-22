package com.placeholder.rich.todotreeprototype;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToDoActivity extends Activity {

    private static final String KEY_CURRENT_LIST = "CurrentList";
    private static final String KEY_PARENT_ITEM = "ParentItem";
    private static final String KEY_HACK_ROOT_NAME = "RootName";
    private static final String KEY_HACK_NAME_PRE = "HackName-";
    private static final String KEY_HACK_COMPLETE_PRE = "HackComplete-";

    private Bundle savedState;
    private ListTree list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;
        setContentView(R.layout.activity_to_do);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String currentList;
        if (savedState == null) {
            list = ListTree.rootList(Arrays.asList(new Item("Item 1"), new Item("Item 2")));
        } else {
            if (savedState.containsKey(KEY_CURRENT_LIST)) {
                currentList = savedState.getString(KEY_CURRENT_LIST);
            } else {
                savedState.putString(KEY_CURRENT_LIST, KEY_HACK_ROOT_NAME);
                currentList = KEY_HACK_ROOT_NAME;
            }
            list = loadList(savedState, currentList);
        }
        displayList();
        setUpNewItems();
    }

    private ListTree loadList(Bundle savedState, String currentList) {
        final String nameKeyStub = KEY_HACK_NAME_PRE + currentList;
        final String completeKeyStub = KEY_HACK_COMPLETE_PRE + currentList;
        int i = 0;
        String nameKey = nameKeyStub + i;
        String completeKey = completeKeyStub + i;
        List<Item> items = new ArrayList<Item>();
        while (savedState.containsKey(nameKey)) {
            items.add(new Item(savedState.getString(nameKey), savedState.getBoolean(completeKey)));
            i++;
            nameKey = nameKeyStub + i;
            completeKey = completeKeyStub + i;
        }
        ListTree list;
        if (savedState.containsKey(KEY_PARENT_ITEM)) {
            list = new ListTree(currentList, new Item(savedState.getString(KEY_PARENT_ITEM)), items);
        } else {
            list = ListTree.rootList(items);
        }
        return list;
    }

    void displayList() {
        ListView listView = (ListView) findViewById(R.id.item_list);
        listView.setAdapter(new ArrayAdapter<Item>(
                getApplicationContext(), R.layout.list_item, list.getItems()) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }
                TextView itemText = (TextView) convertView.findViewById(R.id.text_list_item);
                itemText.setText(getItem(position).getName());
                Button button = (Button) convertView.findViewById(R.id.button_list_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Button) view).setText(getItem(position).getName());
                    }
                });

                return convertView;
            }
        });
    }

    void setUpNewItems() {
        final EditText itemText = (EditText) findViewById(R.id.new_item_text);
        Button addItem = (Button) findViewById(R.id.add_button);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.addItem(new Item(itemText.getText().toString()));
                itemText.getText().clear();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedState = savedInstanceState;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (savedState == null) {
            savedState = new Bundle();
        }
        saveList(savedState);

        outState.putAll(savedState);
        super.onSaveInstanceState(outState);
    }

    private void saveList(Bundle saveBundle) {
        final String currentList;
        if (list.isRoot()) {
            currentList = KEY_HACK_ROOT_NAME;
            saveBundle.remove(KEY_PARENT_ITEM);
        } else {
            currentList = list.getName();
            saveBundle.putString(KEY_PARENT_ITEM, list.getParent().getName());
        }
        saveBundle.putString(KEY_CURRENT_LIST, currentList);

        final String nameKeyStub = KEY_HACK_NAME_PRE + currentList;
        final String completeKeyStub = KEY_HACK_COMPLETE_PRE + currentList;
        String nameKey;
        String completeKey;
        Item item;
        List<Item> items = list.getItems();
        for (int i = 0; i < items.size(); i++) {
            nameKey = nameKeyStub + i;
            completeKey = completeKeyStub + i;
            item = items.get(i);
            saveBundle.putString(nameKey, item.getName());
            saveBundle.putBoolean(completeKey, item.isComplete());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.to_do, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
