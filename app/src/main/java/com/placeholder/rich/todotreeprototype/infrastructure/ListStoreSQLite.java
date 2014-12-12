package com.placeholder.rich.todotreeprototype.infrastructure;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.ListTree;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

import java.util.Locale;
import java.util.UUID;

public class ListStoreSQLite implements ListStore {

    private final TodoDbHelper todoDbHelper;
    private final SQLiteDatabase todoDb;

    public ListStoreSQLite(Context context) {
        todoDbHelper = new TodoDbHelper(context);
        todoDb = todoDbHelper.getWritableDatabase();
    }

    @Override
    public void save(ListTree currentSave) {
        for (Item item : currentSave.getItems()) {
            todoDb.insertWithOnConflict(EntryTable.NAME, null, prepForDB(item, currentSave.getId()),
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private ContentValues prepForDB(Item item, UUID parent) {
        ContentValues values = new ContentValues();
        values.put(EntryTable.COL_ID, item.getId().toString());
        values.put(EntryTable.COL_NAME, item.getName());
        values.put(EntryTable.COL_COMPLETE, item.isComplete());
        values.put(EntryTable.COL_PARENT, parent.toString());
        values.put(EntryTable.COL_ITEMS_LEFT, item.getNItemsLeft());
        values.put(EntryTable.COL_SUB_ITEMS, item.getNSubItems());
        values.put(EntryTable.COL_WHEN, item.getWhen().name());

        return values;
    }

    @Override
    public ListTree load(UUID currentId) {
        //TODO
        return null;
    }

    @Override
    public TagList loadTagged(When tag) {
        //TODO
        return null;
    }

    @Override
    public ListTree loadRoot() {
        //TODO
        return null;
    }

    @Override
    public void addEntry(String name, boolean completed, When when, UUID parentId, String parent) {
        //TODO
    }

    private static final class EntryTable {
        private static final String NAME = "todo_entry";
        private static final String COL_ID = "id";
        private static final String COL_ID_TYPE = "TEXT PRIMARY KEY";
        private static final String COL_NAME = "name";
        private static final String COL_NAME_TYPE = "TEXT NOT NULL";
        private static final String COL_COMPLETE = "complete";
        private static final String COL_COMPLETE_TYPE = "BOOLEAN NOT NULL DEFAULT FALSE";
        private static final String COL_PARENT = "parent";
        private static final String COL_PARENT_TYPE = "TEXT";
        private static final String COL_SUB_ITEMS = "sub_items";
        private static final String COL_SUB_ITEMS_TYPE = "INTEGER NOT NULL DEFAULT 0";
        private static final String COL_ITEMS_LEFT = "items_left";
        private static final String COL_ITEMS_LEFT_TYPE = "INTEGER NOT NULL DEFAULT 0";
        private static final String COL_WHEN = "when_tag";
        private static final String COL_WHEN_TYPE = "TEXT NOT NULL";
        private static final String CONSTRAINT_PARENT_ID =
                "FOREIGN KEY (parent) REFERENCES todo_entry (id)";

        private static final String SQL_CREATE = String.format((Locale) null,
                "CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s, %s %s, %s %s, %s %s, %s);",
                NAME,
                COL_ID, COL_ID_TYPE,
                COL_NAME, COL_NAME_TYPE,
                COL_COMPLETE, COL_COMPLETE_TYPE,
                COL_PARENT, COL_PARENT_TYPE,
                COL_SUB_ITEMS, COL_SUB_ITEMS_TYPE,
                COL_ITEMS_LEFT, COL_ITEMS_LEFT_TYPE,
                COL_WHEN, COL_WHEN_TYPE,
                CONSTRAINT_PARENT_ID);
    }

    private class TodoDbHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "todo";
        private static final int DB_VERSION = 1;

        private TodoDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(EntryTable.SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
