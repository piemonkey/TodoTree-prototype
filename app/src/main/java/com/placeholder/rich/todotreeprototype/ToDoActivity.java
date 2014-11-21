package com.placeholder.rich.todotreeprototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.placeholder.rich.todotreeprototype.infrastructure.ListStore;
import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;

import java.util.ArrayList;
import java.util.UUID;

public class ToDoActivity extends Activity {

    private static final String KEY_LIST_BREADCRUMB = "ListBreadcrumb";

    private ListStore listStore;

    private ListTree list;
    private ArrayList<UUID> listBreadcrumb;

    private BaseAdapter todoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] idStrings = null;
        if (savedInstanceState != null) {
            idStrings = savedInstanceState.getStringArray(KEY_LIST_BREADCRUMB);
        } else if (getIntent() != null) {
            idStrings = getIntent().getStringArrayExtra(KEY_LIST_BREADCRUMB);
        }
        if (idStrings != null) {
            listBreadcrumb = new ArrayList<UUID>(idStrings.length);
            for (String idString : idStrings) {
                listBreadcrumb.add(UUID.fromString(idString));
            }
        } else {
            listBreadcrumb = new ArrayList<UUID>();
        }
        listStore = new ListStore(this);

        setContentView(R.layout.activity_to_do);
    }

    @Override
    protected void onStart() {
        super.onStart();

        int size = listBreadcrumb.size();
        if (size > 0) {
            final UUID currentId = listBreadcrumb.get(size - 1);
            list = listStore.load(currentId);
        } else {
            list = listStore.loadRoot();
        }
        displayList();
        setUpNewItems();
    }

    void displayList() {
        final ListView listView = (ListView) findViewById(R.id.item_list);
        todoListAdapter = new ArrayAdapter<Item>(
                getApplicationContext(), R.layout.list_item, list.getItems()) {
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }
                return setupItemView(position, convertView);
            }

            private View setupItemView(int position, View convertView) {
                final TextView itemText = (TextView) convertView.findViewById(R.id.text_list_item);
                final Item item = getItem(position);
                itemText.setText(item.getName());
                if (item.isComplete()) {
                    itemText.setPaintFlags(itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                itemText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickTodoText(item, itemText);
                    }
                });
                if (item.hasSubItems()) {
                    itemText.setText(itemText.getText() + " (" + item.getNItemsLeft() + "/"
                            + item.getNSubItems() + ")");
                }
                Button button = (Button) convertView.findViewById(R.id.button_list_sublist);
                button.setOnClickListener(new View.OnClickListener() {
                    private EditText newItemName;

                    @Override
                    public void onClick(final View view) {
                        onClickNewSubItem();
                    }

                    private void onClickNewSubItem() {
                        if (item.hasSubItems()) {
                            openActivityForList(item, getContext());
                        } else {
                            AlertDialog.Builder newSubItemBuilder =
                                    new AlertDialog.Builder(listView.getContext());
                            newSubItemBuilder.setPositiveButton
                                    ("Done", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            listStore.addEntry(newItemName.getText().toString(),
                                                    false, item.getId(), item.getName());
                                            item.setNItemsLeft(1);
                                            item.setNSubItems(1);
                                            listStore.save(list);
                                            openActivityForList(item, getContext());
                                        }
                                    });
                            newSubItemBuilder.setView(getLayoutInflater().inflate(
                                    R.layout.dialog_text_entry_alert, listView, false));
                            AlertDialog newSubItem = newSubItemBuilder.show();
                            newItemName = (EditText) newSubItem.findViewById(R.id.edit_text_alert);
                            newItemName.setHint("New sub-item...");
                        }
                    }
                });

                convertView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    private EditText newName;

                    @Override
                    public void onCreateContextMenu(ContextMenu menu,
                                                    View view,
                                                    ContextMenu.ContextMenuInfo info) {
                        createTodoContextMenu(menu);
                    }

                    private void createTodoContextMenu(ContextMenu menu) {
                        menu.setHeaderTitle(item.getName());
                        menu.add("edit").setOnMenuItemClickListener(
                                new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                onMenuItemClickTodoEdit();

                                return true;
                            }
                        });
                        menu.add("delete").setOnMenuItemClickListener(
                                new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                onMenuItemClickTodoDelete();

                                return true;
                            }
                        });
                    }

                    private void onMenuItemClickTodoEdit() {
                        AlertDialog.Builder itemEditBuilder =
                                new AlertDialog.Builder(listView.getContext());
                        itemEditBuilder.setPositiveButton
                                ("Done", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        item.setName(newName.getText().toString());
                                        listStore.save(list);
                                        todoListAdapter.notifyDataSetChanged();
                                    }
                                });
                        itemEditBuilder.setView(getLayoutInflater().inflate(
                                R.layout.dialog_text_entry_alert, listView, false));
                        AlertDialog newSubItem = itemEditBuilder.show();
                        newName = (EditText) newSubItem.findViewById(R.id.edit_text_alert);
                        newName.setHint("New name...");
                    }

                    private void onMenuItemClickTodoDelete() {
                        AlertDialog.Builder itemDeleteBuilder =
                                new AlertDialog.Builder(listView.getContext());
                        itemDeleteBuilder.setTitle(item.getName());
                        itemDeleteBuilder.setMessage("Are you sure you want to delete?");
                        itemDeleteBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                list.deleteItem(item);
                                listStore.save(list);
                                todoListAdapter.notifyDataSetChanged();
                            }
                        });
                        itemDeleteBuilder.show();
                    }
                });

                return convertView;
            }
        };
        listView.setAdapter(todoListAdapter);
    }

    private void onClickTodoText(Item item, TextView itemText) {
        item.toggleComplete();
        listStore.save(list);
        itemText.setPaintFlags(itemText.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void openActivityForList(Item item, Context context) {
        Intent intent = new Intent(context, ToDoActivity.class);
        int numberOfCrumbs = listBreadcrumb.size();
        String[] nextCrumbs = new String[numberOfCrumbs + 1];
        for (int i = 0; i < numberOfCrumbs; i++) {
            nextCrumbs[i] = listBreadcrumb.get(i).toString();
        }
        nextCrumbs[numberOfCrumbs] = item.getId().toString();
        intent.putExtra(KEY_LIST_BREADCRUMB, nextCrumbs);
        startActivity(intent);
    }

    private void setUpNewItems() {
        final EditText itemText = (EditText) findViewById(R.id.new_item_text);
        Button addItem = (Button) findViewById(R.id.add_button);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.addItem(new Item(itemText.getText().toString()));
                itemText.getText().clear();
                listStore.save(list);
                todoListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int numberOfCrumbs = listBreadcrumb.size();
        String[] crumbs = new String[numberOfCrumbs];
        for (int i = 0; i < numberOfCrumbs; i++) {
            crumbs[i] = listBreadcrumb.get(i).toString();
        }
        outState.putStringArray(KEY_LIST_BREADCRUMB, crumbs);
        super.onSaveInstanceState(outState);
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
