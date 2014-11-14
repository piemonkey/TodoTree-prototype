package com.placeholder.rich.todotreeprototype.infrastructure;

import android.content.Context;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListStore {

    private static final String ROOT_PARENT_STRING = "¬ROOT¬";
    private static final String FILENAME_TODO_TXT = "todo.txt";

    private ArrayList<TodoTxtLine> unusedLines = new ArrayList<TodoTxtLine>();
    private Context context;

    public ListStore(Context context) {
        this.context = context;
        File saveFile = context.getFileStreamPath(FILENAME_TODO_TXT);
        if (!saveFile.exists()) {
            try {
                context.openFileOutput(FILENAME_TODO_TXT, context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save(ListTree currentSave) {
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
            FileOutputStream os = context.openFileOutput(FILENAME_TODO_TXT, context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            for (TodoTxtLine line : usedLines) {
                bw.append(line.toLine());
                bw.newLine();
            }
            for (TodoTxtLine line : unusedLines) {
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

    public ListTree load(String currentList) {
        List<TodoTxtLine> lines = new ArrayList<TodoTxtLine>();
        try {
            FileInputStream fis = context.openFileInput(FILENAME_TODO_TXT);
            InputStreamReader irs = new InputStreamReader(fis);
            BufferedReader todoTxt = new BufferedReader(irs);
            String todo = todoTxt.readLine();
            while (todo != null) {
                lines.add(new TodoTxtLine(todo));
                todo = todoTxt.readLine();
            }
            todoTxt.close();
            irs.close();
            fis.close();
        } catch (FileNotFoundException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
        // Work out sub items
        Map<String, Integer> subItemsCount = new HashMap<String, Integer>();
        Map<String, Integer> itemsRemainCount = new HashMap<String, Integer>();
        for (TodoTxtLine line : lines) {
            String parent = line.getParent();
            if (parent != null) {
                if (subItemsCount.containsKey(parent)) {
                    subItemsCount.put(parent, subItemsCount.get(parent) + 1);
                    if (!line.isComplete()) {
                        itemsRemainCount.put(parent, itemsRemainCount.get(parent) + 1);
                    }
                } else {
                    subItemsCount.put(parent, 1);
                    if (line.isComplete()) {
                        itemsRemainCount.put(parent, 0);
                    } else {
                        itemsRemainCount.put(parent, 1);
                    }
                }
            }
        }
        final String parent = currentList != null ? currentList : ROOT_PARENT_STRING;
        List<Item> items = new ArrayList<Item>();
        Set<String> itemNames = new HashSet<String>();
        unusedLines = new ArrayList<TodoTxtLine>();
        for (TodoTxtLine line : lines) {
            if (line.getParent().equals(parent)) {
                String name = line.getName();
                final Item item;
                if (subItemsCount.containsKey(name)) {
                    item = new Item(name,
                            line.isComplete(),
                            subItemsCount.get(name),
                            itemsRemainCount.get(name));
                } else {
                    item = new Item(name, line.isComplete());
                }
                items.add(item);
                itemNames.add(name);
            } else {
                unusedLines.add(line);
            }
        }
        final ListTree list;
        if (currentList == null) {
            list = ListTree.rootList(items);
        } else {
            list = new ListTree(currentList, items);
        }
        return list;
    }

    public void addEntry(String name, boolean completed, String parent) {
        unusedLines.add(new TodoTxtLine(name, parent, completed));
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

}
