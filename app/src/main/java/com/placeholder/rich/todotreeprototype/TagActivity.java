package com.placeholder.rich.todotreeprototype;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ItemList;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

public class TagActivity extends AbstractListActivity {

    public static final String KEY_WHEN = "WhenList";

    private TagList list;
    private When when;

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

        setContentView(R.layout.activity_tag);
    }

    @Override
    protected void onStart() {
        list = listStore.loadTagged(when);

        super.onStart();
    }

    @Override
    protected void changeActionbarText() {
        setTitle("Today");
    }

    @Override
    protected void setUpWhenHeader() {
        final Button lists = (Button) findViewById(R.id.back_button);
        lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagActivity.this.finish();
            }
        });
    }

    @Override
    protected ItemList getList() {
        return list;
    }

    @Override
    protected BaseAdapter setUpAdapterForList(final ListView listView) {
        return new ArrayAdapter<Item>(this, R.layout.tag_list_item, list.getItems()) {
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
                } else {
                    itemText.setPaintFlags(itemText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
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
                final Button whenButton = (Button) convertView.findViewById(R.id.button_list_today);
                setWhenButtonBackground(whenButton, item);
                whenButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickTodayButton(item, whenButton);
                        list.deleteItem(item);
                        todoListAdapter.notifyDataSetChanged();
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
                        newName.setText(item.getName());
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
                                listStore.delete(item);
                                todoListAdapter.notifyDataSetChanged();
                            }
                        });
                        itemDeleteBuilder.show();
                    }
                });

                return convertView;
            }
        };
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_WHEN, when.name());
        super.onSaveInstanceState(outState);
    }

}
