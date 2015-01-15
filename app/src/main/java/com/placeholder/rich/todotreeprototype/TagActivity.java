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
import com.placeholder.rich.todotreeprototype.infrastructure.ListStoreSQLite;
import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.ArrayList;
import java.util.UUID;

public class TagActivity extends Activity {

    public static final String KEY_WHEN = "WhenList";

    private ListStore listStore;

    private TagList list;
    private When when;

    private BaseAdapter todoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            when = When.valueOf(getIntent().getStringExtra(KEY_WHEN));
        } else if (savedInstanceState != null) {
            when = When.valueOf(savedInstanceState.getString(KEY_WHEN));
        } else {
            when = When.TODAY;
        }

        listStore = new ListStoreSQLite(this);

        setContentView(R.layout.activity_tag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        list = listStore.loadTagged(when);
        displayList();
        setUpWhenHeader();
    }

    private void setUpWhenHeader() {
        final Button lists = (Button) findViewById(R.id.back_button);
        lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagActivity.this.finish();
            }
        });
    }

    void displayList() {
        final ListView listView = (ListView) findViewById(R.id.item_list);
        todoListAdapter = new ArrayAdapter<Item>(
                getApplicationContext(), R.layout.tag_list_item, list.getItems()) {
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.tag_list_item, parent, false);
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
                                        String name = newName.getText().toString();
                                        if (!name.isEmpty()) {
                                            item.setName(name);
                                            listStore.save(list);
                                            todoListAdapter.notifyDataSetChanged();
                                        }
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
                                listStore.delete(item, item.getParent());
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
        listStore.saveUpdatedCompleteness(item, item.getParent());
        itemText.setPaintFlags(itemText.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_WHEN, when.name());
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
