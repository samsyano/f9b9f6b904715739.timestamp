package com.example.samson.conty.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by SAMSON on 8/4/2017.
 */

public class TaskContentProvider extends ContentProvider {

    public static final int TASKS = 100;
    public static final int TASKS_WITH_ID = 101;

    public static final UriMatcher mUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS, TASKS);

        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#", TASKS_WITH_ID);

        return uriMatcher;
    }

    private TaskDbHelper dbHelper;
    /* onCreate() is where you should initialize anything you’ll need to setup
   your underlying data source.
   In this case, you’re working with a SQLite database, so you’ll need to
   initialize a DbHelper to gain access to it.
    */


    @Override
    public boolean onCreate() {
        // TODO (2) Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable
        dbHelper = new TaskDbHelper(getContext());

        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);

        Uri returnUri;
        switch (match) {
            case TASKS:
                Long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(uri, id);
                } else {
                    throw new SQLException("Failed to insert new row " + id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = mUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case TASKS_WITH_ID:
                /** NOT USED --selection = TaskContract.PATH_TASKS + "/?";
                 selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};--*/
                String id = uri.getPathSegments().get(1);
                selection = "_id=?";
                selectionArgs = new String[]{id};
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unkknown Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int delRow = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                delRow = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASKS_WITH_ID:
                selection = "_id=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                delRow = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return delRow;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        int taskUpdated = 0;

        switch (match) {
            case TASKS:
                taskUpdated = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASKS_WITH_ID:
                selection = "_id=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
//                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                taskUpdated = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if(taskUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return taskUpdated;
    }



    @Override
    public String getType(@NonNull Uri uri) {
        int match = mUriMatcher.match(uri);

        switch (match){
            case TASKS:
               return TaskContract.CONTENT_LIST_TYPE;
            case TASKS_WITH_ID:
                return TaskContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Not able to get Type");
        }
    }
}