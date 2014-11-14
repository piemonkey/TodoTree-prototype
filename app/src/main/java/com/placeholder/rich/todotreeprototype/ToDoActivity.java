package com.placeholder.rich.todotreeprototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
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

import com.placeholder.rich.todotreeprototype.infrastructure.ListStore;
import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;

import java.util.ArrayList;

public class ToDoActivity extends Activity {

    private static final String KEY_LIST_BREADCRUMB = "ListBreadcrumb";

    private ListStore listStore;

    private ListTree list;
    private ArrayList<String> listBreadcrumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            listBreadcrumb = savedInstanceState.getStringArrayList(KEY_LIST_BREADCRUMB);
        } else if (getIntent() != null) {
            listBreadcrumb = getIntent().getStringArrayListExtra(KEY_LIST_BREADCRUMB);
        }
        if (listBreadcrumb == null) {
            listBreadcrumb = new ArrayList<String>();
        }
        listStore = new ListStore(this);

        setContentView(R.layout.activity_to_do);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String currentList;
        int size = listBreadcrumb.size();
        if (size > 0) {
            currentList = listBreadcrumb.get(size - 1);
        } else {
            currentList = null;
        }
        list = listStore.load(currentList);
        displayList();
        setUpNewItems();
    }

    void displayList() {
        final ListView listView = (ListView) findViewById(R.id.item_list);
        listView.setAdapter(new ArrayAdapter<Item>(
                getApplicationContext(), R.layout.list_item, list.getItems()) {
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }
                final TextView itemText = (TextView) convertView.findViewById(R.id.text_list_item);
                final Item item = getItem(position);
                itemText.setText(item.getName());
                if (item.isComplete()) {
                    itemText.setPaintFlags(itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                itemText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.toggleComplete();
                        listStore.save(list);
                        itemText.setPaintFlags(itemText.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                });
                Button button = (Button) convertView.findViewById(R.id.button_list_sublist);
                if (item.hasSubItems()) {
                    itemText.setText(itemText.getText() + " (" + item.getNItemsLeft() + "/"
                            + item.getNSubItems() + ")");
//                } else {
//                    button.setText(button.getText() + "+");
                }
                button.setOnClickListener(new View.OnClickListener() {
                    private EditText newItemName;

                    @Override
                    public void onClick(final View view) {
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
                                                    false, item.getName());
                                            item.setNItemsLeft(1);
                                            item.setNSubItems(1);
                                            listStore.save(list);
                                            openActivityForList(item, getContext());
                                        }
                                    });
                            newSubItemBuilder.setView(getLayoutInflater().inflate(
                                    R.layout.dialog_new_sub_item, null));
                            AlertDialog newSubItem = newSubItemBuilder.show();
                            newItemName = (EditText) newSubItem.findViewById(R.id.edit_text_new_sub);
                        }
                    }
                });

                return convertView;
            }
        });
    }

    private void openActivityForList(Item item, Context context) {
        Intent intent = new Intent(context, ToDoActivity.class);
        ArrayList<String> nextBreadcrumb = new ArrayList<String>(listBreadcrumb);
        nextBreadcrumb.add(item.getName());
        intent.putStringArrayListExtra(KEY_LIST_BREADCRUMB, nextBreadcrumb);
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
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(KEY_LIST_BREADCRUMB, listBreadcrumb);
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
