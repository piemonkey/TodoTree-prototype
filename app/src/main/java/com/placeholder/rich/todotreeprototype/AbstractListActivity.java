package com.placeholder.rich.todotreeprototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.placeholder.rich.todotreeprototype.infrastructure.ListStore;
import com.placeholder.rich.todotreeprototype.infrastructure.ListStoreSQLite;
import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ItemList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListActivity extends Activity {

    protected ListStore listStore;

    protected BaseAdapter todoListAdapter;

    protected abstract void changeActionbarText();

    protected abstract void setUpWhenHeader();

    protected abstract ItemList getList();

    protected abstract BaseAdapter setUpAdapterForList(final ListView listView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listStore = new ListStoreSQLite(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        changeActionbarText();
        setUpWhenHeader();
        displayList();
    }

    private void displayList() {
        final ListView listView = (ListView) findViewById(R.id.item_list);
        listView.requestFocus();
        todoListAdapter = setUpAdapterForList(listView);
        listView.setAdapter(todoListAdapter);
    }

    protected void setWhenButtonBackground(Button whenButton, Item item){}

    protected void onClickTodayButton(Item item, Button button) {
        if (item.hasSubItems()) {
            Toast.makeText(this, "Feature coming soon...", Toast.LENGTH_SHORT).show();
        } else {
            if (item.getWhen() == When.NA) {
                item.doToday();
            } else {
                item.dontDoNow();
            }
            listStore.save(item);
            setWhenButtonBackground(button, item);
        }
    }

    protected void onClickTodoText(Item item, TextView itemText) {
        if (item.hasSubItems()) {
            openActivityForList(item, this);
        } else {
            item.toggleComplete();
            listStore.saveUpdatedCompleteness(item);
            itemText.setPaintFlags(itemText.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    protected void openActivityForList(Item item, Context context) {
        if (item.hasSubItems() && item.getWhen() != When.NA) {
            item.dontDoNow();
            listStore.save(item);
        }
        //TODO: Need to figure out a way to launch sub item lists in today view
        throw new UnsupportedOperationException("Somehow a sub-itemed thing got into today list");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.to_do, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_clear_completed) {
            showClearCompetedDialog();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showClearCompetedDialog() {
        AlertDialog.Builder clearDialogBuilder = new AlertDialog.Builder(this);
        clearDialogBuilder.setTitle("Deleting completed items");
        clearDialogBuilder.setMessage("Are you sure?");
        clearDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteCompletedItems();
            }
        });
        clearDialogBuilder.show();
    }

    private void deleteCompletedItems() {
        List<Item> toDelete = new ArrayList<>();
        for (Item item : getList().getItems()) {
            if (item.isComplete() && !item.hasSubItems()) {
                toDelete.add(item);
            }
        }
        for (Item item : toDelete) {
            getList().deleteItem(item);
            listStore.delete(item);
        }
        todoListAdapter.notifyDataSetChanged();
    }

}
