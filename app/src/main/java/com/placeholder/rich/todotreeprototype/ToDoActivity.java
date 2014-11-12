package com.placeholder.rich.todotreeprototype;

import android.app.Activity;
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

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ToDoActivity extends Activity {

    private static final String FILENAME_TODO_TXT = "todo.txt";

    private static final String KEY_LIST_BREADCRUMB = "ListBreadcrumb";

    private static final String ROOT_PARENT_STRING = "¬ROOT¬";

    private ListTree list;
    private ArrayList<String> listBreadcrumb;
    private ArrayList<TodoTxtLine> unusedLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            listBreadcrumb = savedInstanceState.getStringArrayList(KEY_LIST_BREADCRUMB);
        }
        if (listBreadcrumb == null) {
            listBreadcrumb = new ArrayList<String>();
        }
        File saveFile = getFileStreamPath(FILENAME_TODO_TXT);
        if (!saveFile.exists()) {
            try {
                openFileOutput(FILENAME_TODO_TXT, MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

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
        list = loadList(currentList);
        displayList();
        setUpNewItems();
    }

    private ListTree loadList(String currentList) {
        ListTree list;
        try {
            final String parent = currentList != null ? currentList : ROOT_PARENT_STRING;
            FileInputStream fis = openFileInput(FILENAME_TODO_TXT);
            InputStreamReader irs = new InputStreamReader(fis);
            BufferedReader todoTxt = new BufferedReader(irs);
            String todo = todoTxt.readLine();
            List<Item> items = new ArrayList<Item>();
            unusedLines = new ArrayList<TodoTxtLine>();
            while (todo != null) {
                TodoTxtLine line = new TodoTxtLine(todo);
                if (line.getParent().equals(parent)) {
                    Item item = new Item(line.getName(), line.isComplete());
                    items.add(item);
                } else {
                    unusedLines.add(line);
                }
                todo = todoTxt.readLine();
            }
            todoTxt.close();
            irs.close();
            fis.close();
            if (currentList == null) {
                list = ListTree.rootList(items);
            } else {
                list = new ListTree(currentList, items);
            }
        } catch (FileNotFoundException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
        return list;
    }

    private void saveList(ListTree currentSave, List<TodoTxtLine> unused) {
        ArrayList<TodoTxtLine> usedLines = new ArrayList<TodoTxtLine>(currentSave.getItems().size());
        final String parent;
        if (currentSave.isRoot()) {
            parent = ROOT_PARENT_STRING;
        } else {
            parent = currentSave.getName();
        }
        for (Item item : currentSave.getItems()) {
            usedLines.add(new TodoTxtLine(item.getName(), parent, item.isComplete()));
        }
        try {
            FileOutputStream os = openFileOutput(FILENAME_TODO_TXT, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            for (TodoTxtLine line : usedLines) {
                bw.append(line.toLine());
                bw.newLine();
            }
            for (TodoTxtLine line : unused) {
                bw.append(line.toLine());
                bw.newLine();
            }
            bw.close();
            osw.close();
            os.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class TodoTxtLine {
        private final String parent;
        private final String name;
        private final boolean complete;

        TodoTxtLine(String line) {
            line = line.trim();
            if (line.charAt(0) == 'x' && line.charAt(1) == ' ') {
                complete = true;
                line = line.substring(2);
                //For full support deal with completion date
            } else {
                complete = false;
                //For full support deal with priority
            }
            //For full support deal with creation date
            //For full support deal with contexts
            //For full support deal with flexible ordering
            String[] chunks = line.split(" \\+");
            name = chunks[0];
            if (chunks.length > 1) {
                parent = chunks[1].replace("|¬", " ");
            } else {
                parent = null;
            }
        }

        TodoTxtLine(String name, String parent, boolean complete) {
            this.name = name;
            this.parent = parent.replace(" ", "|¬");
            this.complete = complete;
        }

        public String getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public boolean isComplete() {
            return complete;
        }

        public String toLine() {
            StringBuilder sb = new StringBuilder();
            if (complete) {
                sb.append("x ");
            }
            sb.append(name);
            if (parent != null) {
                sb.append(" +").append(parent);
            }
            return sb.toString();
        }
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
                        saveList(list, unusedLines);
                        itemText.setPaintFlags(itemText.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                });
                Button button = (Button) convertView.findViewById(R.id.button_list_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Button) view).setText(item.getName());
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
                saveList(list, unusedLines);
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
