package com.placeholder.rich.todotreeprototype.infrastructure;

import android.content.Context;
import android.util.Log;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ListStoreFile implements ListStore {

    private static final String LOG_TAG = "ListStore";

    private static final String ROOT_PARENT_STRING = "¬ROOT¬";
    private static final String ID_TAG = " id:";
    private static final String PARENT_TAG = " parent:";
    private static final String WHEN_TAG = " when:";
    private static final String FILENAME_TODO_TXT = "todo.txt";

    private static final String FILENAME_BACKUP_FORMAT = "todo.txt.backup-v1-%c-%03d";
    private static final int BACKUP_MIN = 0;
    private static final int BACKUP_MAX = 200;

    private ArrayList<TodoTxtLine> unusedLines = new ArrayList<TodoTxtLine>();
    private Context context;

    public ListStoreFile(Context context) {
        this.context = context;
        String[] files = context.fileList();
        Arrays.sort(files);
        if (files.length == 0 || !FILENAME_TODO_TXT.equals(files[0])) {
            if (files.length == 0) {
                Log.i(LOG_TAG, "Store file does not exist, creating new one. No files found at all");
            } else {
                Log.i(LOG_TAG, "Store file does not exist, creating new one. Only found: " + files[0]);
            }
            try {
                context.openFileOutput(FILENAME_TODO_TXT, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            String backupFileName = figureOutNextBackup(files);
            Log.d(LOG_TAG, "Store file found, backing up to " + backupFileName);
            try {
                FileInputStream fis = context.openFileInput(FILENAME_TODO_TXT);
                InputStreamReader irs = new InputStreamReader(fis);
                BufferedReader todoTxt = new BufferedReader(irs);
                String todo = todoTxt.readLine();
                ArrayList<String> lines = new ArrayList<String>();
                while (todo != null) {
                    lines.add(todo);
                    todo = todoTxt.readLine();
                }
                todoTxt.close();
                irs.close();
                fis.close();
                FileOutputStream os = context.openFileOutput(backupFileName, Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                for (String line : lines) {
                    bw.append(line);
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
    }

    private String figureOutNextBackup(String[] files) {
        final String newName;
        final String toDelete;
        if (files.length == 1) {
            newName = String.format((Locale) null, FILENAME_BACKUP_FORMAT, 'b', BACKUP_MIN);
        } else {
            String[] highestParts = files[files.length - 1].split("-", 4);
            if (highestParts[2].equals("a")) {
                // Only happens when all 'b's have been deleted
                newName = String.format((Locale) null, FILENAME_BACKUP_FORMAT, 'b', BACKUP_MIN);
                toDelete = String.format((Locale) null, FILENAME_BACKUP_FORMAT, 'a', BACKUP_MAX);
            } else {
                int nextB = Integer.parseInt(highestParts[3]) + 1;
                if (nextB > BACKUP_MAX) {
                    // Just found max so need to find lowest 'a' to find next filename
                    String[] lowestParts = files[1].split("-", 4);
                    if (lowestParts[2].equals("b")) {
                        // Have just finished counting up the 'b's so move on to 'a's
                        newName = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                                'a', BACKUP_MAX);
                        toDelete = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                                'b', BACKUP_MIN);
                    } else {
                        // Next 'a' is one lower than current 'a' and delete inverse 'b'
                        int nextA = Integer.parseInt(lowestParts[3]) - 1;
                        newName = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                                'a', nextA);
                        toDelete = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                                'b', BACKUP_MAX - nextA);
                    }
                } else {
                    // Must be counting up the 'b's so pick the next
                    newName = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                            'b', nextB);
                    toDelete = String.format((Locale) null, FILENAME_BACKUP_FORMAT,
                            'a', BACKUP_MAX - nextB);
                }
            }
            if (context.deleteFile(toDelete)) {
                Log.d(LOG_TAG, "Old backup file deleted: " + toDelete);
            } else {
                Log.d(LOG_TAG, "Backup file not found for deletion, tried to find " + toDelete);
            }
        }
        return newName;
    }

    @Override
    public void save(ListTree currentSave) {
        ArrayList<TodoTxtLine> usedLines = new ArrayList<TodoTxtLine>(currentSave.getItems().size());
        final String parent;
        if (currentSave.isRoot()) {
            parent = ROOT_PARENT_STRING;
        } else {
            parent = currentSave.getName();
        }
        for (Item item : currentSave.getItems()) {
            usedLines.add(new TodoTxtLine(item.getId(), item.getName(), item.isComplete(),
                    item.getWhen(), currentSave.getId(), parent));
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

    @Override
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

    @Override
    public TagList loadTagged(final When tag) {
        List<TodoTxtLine> lines = getLinesFromFile();

        List<Item> tagged = new ArrayList<Item>();
        for (TodoTxtLine line : lines) {
            if (line.hasTag(tag)) {
                tagged.add(line.toItem());
            }
        }

        return new TagList(tag, tagged);
    }

    @Override
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

    @Override
    public void addEntry(String name, boolean completed, When when, UUID parentId, String parent) {
        unusedLines.add(new TodoTxtLine(UUID.randomUUID(), name, completed, when, parentId, parent));
    }

    private class TodoTxtLine {
        private final UUID id;
        private final String name;
        private final boolean complete;
        private final When when;
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
            String[] whenParts = parts[1].split(WHEN_TAG);
            when = When.valueOf(whenParts[1]);
            String[] idParts = whenParts[0].split(PARENT_TAG);
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

        TodoTxtLine(UUID id, String name, boolean complete, When when, UUID parentId, String parent) {
            this.id = id;
            this.complete = complete;
            this.name = name;
            this.when = when;
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

        public boolean hasTag(When tag) {
            return when == tag;
        }

        public Item toItem() {
            return new Item(id, name, complete, when);
        }

        public Item toItem(int subItemsCount, int itemsRemainCount) {
            return new Item(id, name, complete, when, subItemsCount, itemsRemainCount);
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
            sb.append(WHEN_TAG);
            sb.append(when.toString());
            return sb.toString();
        }
    }

}
