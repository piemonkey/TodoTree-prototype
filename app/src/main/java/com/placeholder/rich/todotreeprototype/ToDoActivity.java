package com.placeholder.rich.todotreeprototype;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.ArrayList;
import java.util.UUID;

public class ToDoActivity extends AbstractListActivity {

    private static final String KEY_LIST_BREADCRUMB = "ListBreadcrumb";

    private ListTree list;
    private ArrayList<UUID> listBreadcrumb;

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
            listBreadcrumb = new ArrayList<>(idStrings.length);
            for (String idString : idStrings) {
                listBreadcrumb.add(UUID.fromString(idString));
            }
        } else {
            listBreadcrumb = new ArrayList<>();
        }

        setContentView(R.layout.activity_to_do);
    }

    @Override
    protected void onStart() {
        int size = listBreadcrumb.size();
        if (size > 0) {
            final UUID currentId = listBreadcrumb.get(size - 1);
            list = listStore.load(currentId);
        } else {
            list = listStore.loadRoot();
        }
        setUpNewItems();
        super.onStart();
    }

    @Override
    protected void changeActionbarText() {
        if(!list.isRoot()) {
            setTitle(list.getName());
        }
    }

    @Override
    protected void setUpWhenHeader() {
        final Button today = (Button) findViewById(R.id.today_button);
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTagActivity(When.TODAY, ToDoActivity.this);
            }
        });
    }

    @Override
    protected ItemList getList() {
        return list;
    }

    private void startTagActivity(When when, Context context) {
        Intent intent = new Intent(context, TagActivity.class);
        intent.putExtra(TagActivity.KEY_WHEN, when.name());
        startActivity(intent);
    }

    @Override
    protected BaseAdapter setUpAdapterForList(final ListView listView) {
        return new ArrayAdapter<Item>(this, R.layout.list_item, list.getItems()) {
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
                final Button todayButton = (Button) convertView.findViewById(R.id.button_list_today);
                setWhenButtonBackground(todayButton, item);
                todayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickTodayButton(item, todayButton);
                    }
                });
                Button subListButton = (Button) convertView.findViewById(R.id.button_list_sublist);
                subListButton.setOnClickListener(new View.OnClickListener() {
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
                                            String name = newItemName.getText().toString();
                                            if (!name.isEmpty()) {
                                                listStore.addItem(new Item(name, item.getId()));
                                                openActivityForList(item, getContext());
                                            }
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
    protected void setWhenButtonBackground(Button whenButton, Item item) {
        if (item.getWhen() == When.NA) {
            whenButton.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.ic_when_tag_inactive));
        } else if (item.getWhen() == When.TODAY) {
            whenButton.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.ic_when_tag_active));
        }
    }

    @Override
    protected void openActivityForList(Item item, Context context) {
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
                String name = itemText.getText().toString();
                if (!name.isEmpty()) {
                    Item newItem = new Item(name, list.getId());
                    list.addItem(newItem);
                    listStore.addItem(newItem);
                    itemText.getText().clear();
                    todoListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        int numberOfCrumbs = listBreadcrumb.size();
        String[] crumbs = new String[numberOfCrumbs];
        for (int i = 0; i < numberOfCrumbs; i++) {
            crumbs[i] = listBreadcrumb.get(i).toString();
        }
        outState.putStringArray(KEY_LIST_BREADCRUMB, crumbs);
        super.onSaveInstanceState(outState);
    }

}
