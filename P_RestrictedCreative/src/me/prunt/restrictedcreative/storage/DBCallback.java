package me.prunt.restrictedcreative.storage;

import java.sql.ResultSet;

public interface DBCallback {
    public void onQueryDone(ResultSet rs, long start);
}
