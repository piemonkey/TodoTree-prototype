package com.placeholder.rich.todotreeprototype.infrastructure;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        //TODO
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
        private static final String COL_ID_TYPE = "INTEGER PRIMARY KEY";
        private static final String COL_NAME = "name";
        private static final String COL_NAME_TYPE = "TEXT NOT NULL";

        private static final String SQL_CREATE = String.format((Locale) null,
                "CREATE TABLE %s (%s %s, %s %s);",
                NAME,
                COL_ID, COL_ID_TYPE,
                COL_NAME, COL_NAME_TYPE);
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
