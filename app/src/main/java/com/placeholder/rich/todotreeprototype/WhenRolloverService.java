package com.placeholder.rich.todotreeprototype;

import android.app.IntentService;
import android.content.Intent;

import com.placeholder.rich.todotreeprototype.infrastructure.ListStore;
import com.placeholder.rich.todotreeprototype.infrastructure.ListStoreSQLite;
import com.placeholder.rich.todotreeprototype.model.Item;
import com.placeholder.rich.todotreeprototype.model.TagList;
import com.placeholder.rich.todotreeprototype.model.When;

public class WhenRolloverService extends IntentService {
    public static final String ACTION_ROLLOVER_DAY =
            "com.placeholder.rich.todotreeprototype.action.ROLLOVER_DAY";
    public static final int REQUEST_ROLLOVER_SCHEDULE = 1;

    private ListStore listStore;

    public WhenRolloverService() {
        super("WhenRolloverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listStore = new ListStoreSQLite(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ROLLOVER_DAY.equals(action)) {
                TagList tomorrow = listStore.loadTagged(When.TOMORROW);
                for (Item item : tomorrow.getItems()) {
                    item.doToday();
                    listStore.save(item);
                }
            }
        }
    }

}
