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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ListStore {

    private static final String ROOT_PARENT_STRING = "¬ROOT¬";
    private static final String ID_TAG = " id:";
    private static final String PARENT_TAG = " parent:";
    private static final String FILENAME_TODO_TXT = "todo.txt";

    private ArrayList<TodoTxtLine> unusedLines = new ArrayList<TodoTxtLine>();
    private Context context;

    public ListStore(Context context) {
        this.context = context;
        File saveFile = context.getFileStreamPath(FILENAME_TODO_TXT);
        if (!saveFile.exists()) {
            try {
                context.openFileOutput(FILENAME_TODO_TXT, Context.MODE_PRIVATE);
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
            usedLines.add(new TodoTxtLine
                    (item.getId(), item.getName(), item.isComplete(), currentSave.getId(), parent));
        }
        try {
            FileOutputStream os = context.openFileOutput(FILENAME_TODO_TXT, Context.MODE_PRIVATE);
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

    public ListTree load(final UUID currentId) {
        List<TodoTxtLine> lines = getLinesFromFile();

        Item current = null;
        Map<UUID, Integer> subItemsCount = new HashMap<UUID, Integer>();
        Map<UUID, Integer> itemsRemainCount = new HashMap<UUID, Integer>();
        for (TodoTxtLine line : lines) {
            if (currentId.equals(line.getId())) {
                current = line.toItem();
            } else {
                incrementCounts(subItemsCount, itemsRemainCount, line);
            }
        }
        List<Item> items = new ArrayList<Item>();
        unusedLines = new ArrayList<TodoTxtLine>();
        fillItemsAndUnusedLines
                (currentId, lines, subItemsCount, itemsRemainCount, items, unusedLines);

        return new ListTree(currentId, current.getName(), items);
    }

    public ListTree loadRoot() {
        List<TodoTxtLine> lines = getLinesFromFile();

        Map<UUID, Integer> subItemsCount = new HashMap<UUID, Integer>();
        Map<UUID, Integer> itemsRemainCount = new HashMap<UUID, Integer>();
        for (TodoTxtLine line : lines) {
            incrementCounts(subItemsCount, itemsRemainCount, line);
        }

        List<Item> items = new ArrayList<Item>();
        unusedLines = new ArrayList<TodoTxtLine>();
        fillItemsAndUnusedLines
                (ListTree.getRootId(), lines, subItemsCount, itemsRemainCount, items, unusedLines);

        return new ListTree(ListTree.getRootId(), null, items);
    }

    private List<TodoTxtLine> getLinesFromFile() {
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
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private void incrementCounts(Map<UUID, Integer> subItemsCount,
                                 Map<UUID, Integer> itemsRemainCount,
                                 TodoTxtLine line) {
        UUID parentId = line.parentId;
        if (subItemsCount.containsKey(parentId)) {
            subItemsCount.put(parentId, subItemsCount.get(parentId) + 1);
            if (!line.isComplete()) {
                itemsRemainCount.put(parentId, itemsRemainCount.get(parentId) + 1);
            }
        } else {
            subItemsCount.put(parentId, 1);
            if (line.isComplete()) {
                itemsRemainCount.put(parentId, 0);
            } else {
                itemsRemainCount.put(parentId, 1);
            }
        }
    }

    private void fillItemsAndUnusedLines(UUID currentId,
                                         List<TodoTxtLine> lines,
                                         Map<UUID, Integer> subItemsCount,
                                         Map<UUID, Integer> itemsRemainCount,
                                         List<Item> items,
                                         List<TodoTxtLine> unused) {
        for (TodoTxtLine line : lines) {
            if (line.hasParent(currentId)) {
                UUID id = line.getId();
                final Item item;
                if (subItemsCount.containsKey(id)) {
                    item = line.toItem(subItemsCount.get(id), itemsRemainCount.get(id));
                } else {
                    item = line.toItem();
                }
                items.add(item);
            } else {
                unused.add(line);
            }
        }
    }

    public void addEntry(String name, boolean completed, UUID parentId, String parent) {
        unusedLines.add(new TodoTxtLine(UUID.randomUUID(), name, completed, parentId, parent));
    }

    private class TodoTxtLine {
        private final UUID id;
        private final String name;
        private final boolean complete;
        private final UUID parentId;
        private final String parent;

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
            String[] parts = line.split(ID_TAG);
            line = parts[0];
            String[] idParts = parts[1].split(PARENT_TAG);
            id = UUID.fromString(idParts[0]);
            parentId = UUID.fromString(idParts[1]);

            String[] chunks = line.split(" \\+");
            name = chunks[0];
            if (chunks.length > 1) {
                parent = chunks[1].replace("|¬", " ");
            } else {
                parent = null;
            }
        }

        TodoTxtLine(UUID id, String name, boolean complete, UUID parentId, String parent) {
            this.id = id;
            this.complete = complete;
            this.name = name;
            this.parentId = parentId;
            this.parent = parent.replace(" ", "|¬");
        }

        public UUID getId() {
            return id;
        }

        public boolean isComplete() {
            return complete;
        }

        public boolean hasParent(UUID parentId) {
            return this.parentId.equals(parentId);
        }

        public Item toItem() {
            return new Item(id, name, complete);
        }

        public Item toItem(int subItemsCount, int itemsRemainCount) {
            return new Item(id, name, complete, subItemsCount, itemsRemainCount);
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
            sb.append(ID_TAG);
            sb.append(id);
            sb.append(PARENT_TAG);
            sb.append(parentId);
            return sb.toString();
        }
    }

}
