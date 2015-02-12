package kr.co.nexon.todoplus.Helper;


/**
 * Created by raintype on 2015-01-20.
 * http://mainia.tistory.com/718
 */
import java.text.*;
import java.util.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.*;
import kr.co.nexon.todoplus.Entity.*;

public class DBContactHelper extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "todoManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "tasks";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PERIOD = "period";
    private static final String KEY_DATE_TYPE= "dateType";
    private static final String KEY_IMPORTANT = "important";
    private static final String KEY_SECRET = "secret";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_USE_YN = "useYN";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public DBContactHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DATE_TYPE + " INTEGER,"
                + KEY_PERIOD + " DATETIME,"
                + KEY_IMPORTANT + " BOOLEAN,"
                + KEY_SECRET + " BOOLEAN,"
                + KEY_COMPLETED + " BOOLEAN,"
                + KEY_USE_YN + " BOOLEAN"
                +")";

        Log.i("DB", CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    public long addTask(TaskInfo taskInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, taskInfo.getName());

        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String period = formatter.format(taskInfo.getPeriod().getTime());
        values.put(KEY_PERIOD, period);
        values.put(KEY_DATE_TYPE, taskInfo.getDateType());
        values.put(KEY_IMPORTANT, taskInfo.getImportant());
        values.put(KEY_SECRET, taskInfo.getSecret());
        values.put(KEY_COMPLETED, taskInfo.getCompleted());
        values.put(KEY_USE_YN, taskInfo.getUseYN());

        // Inserting Row
        long resultValue = db.insert(TABLE_CONTACTS, null, values);

        db.close(); // Closing database connection

        return  resultValue;
    }

    public ArrayList<TaskInfo> getAllTask() {
        ArrayList<TaskInfo> taskInfoArrayList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT "
                + KEY_ID + ", "
                + KEY_NAME + ", "
                + KEY_PERIOD + ", "
                + KEY_DATE_TYPE + ", "
                + KEY_IMPORTANT + ", "
                + KEY_SECRET + ", "
                + KEY_COMPLETED
                +" FROM " + TABLE_CONTACTS + " WHERE " + KEY_USE_YN
                + " order by " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    taskInfoArrayList.add(getTaskInfo(cursor));
                } catch (Exception ex){

                }
            } while (cursor.moveToNext());
        }

        return taskInfoArrayList;
    }

    public ArrayList<TaskInfo> getLockScreenTask() {
        ArrayList<TaskInfo> taskInfoArrayList = new ArrayList<TaskInfo>();

        // Select All Query
        String selectQuery = "SELECT "
                + KEY_ID + ", "
                + KEY_NAME + ", "
                + KEY_PERIOD + ", "
                + KEY_DATE_TYPE + ", "
                + KEY_IMPORTANT + ", "
                + KEY_SECRET + ", "
                + KEY_COMPLETED
                +" FROM " + TABLE_CONTACTS
                + " WHERE " + KEY_USE_YN
                + " AND NOT " + KEY_SECRET + " AND NOT " + KEY_COMPLETED
                + " order by " + KEY_IMPORTANT + " DESC, " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    taskInfoArrayList.add(getTaskInfo(cursor));
                } catch (Exception ex) {

                }
            } while (cursor.moveToNext());
        }

        return taskInfoArrayList;
    }

    public TaskInfo getTask(int id)  {
        String selectQuery = "SELECT "
                + KEY_ID + ", "
                + KEY_NAME + ", "
                + KEY_PERIOD + ", "
                + KEY_DATE_TYPE + ", "
                + KEY_IMPORTANT + ", "
                + KEY_SECRET + ", "
                + KEY_COMPLETED
                +" FROM " + TABLE_CONTACTS + " WHERE " + KEY_ID + " = " + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null)
            cursor.moveToFirst();

        TaskInfo taskinfo = null;
        try {
            taskinfo = getTaskInfo(cursor);
        } catch (Exception ex) {
            // Todo : Exception
            Log.e("DBContactHelper.getTask", ex.getMessage());
        }

        return taskinfo;
    }

    public int updateTaskInfo(TaskInfo taskInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, taskInfo.getName());

        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String period = formatter.format(taskInfo.getPeriod().getTime());

        values.put(KEY_PERIOD, period);
        values.put(KEY_DATE_TYPE, taskInfo.getDateType());
        values.put(KEY_IMPORTANT, taskInfo.getImportant());
        values.put(KEY_SECRET, taskInfo.getSecret());
        values.put(KEY_COMPLETED, taskInfo.getCompleted());
        values.put(KEY_USE_YN, taskInfo.getUseYN());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(taskInfo.getId()) });
    }

    private TaskInfo getTaskInfo(Cursor cursor) throws Exception {
        TaskInfo taskInfo = new TaskInfo();

        taskInfo.setId(Integer.parseInt(cursor.getString(0)));
        taskInfo.setName(cursor.getString(1));

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date tempDate = format.parse(cursor.getString(2));
        Calendar cal = Calendar.getInstance();
        cal.setTime((tempDate));
        taskInfo.setPeriod(cal);

        taskInfo.setDateType(cursor.getInt(3));
        taskInfo.setImportant(cursor.getInt(4) == 1);
        taskInfo.setSecret(cursor.getInt(5) == 1);
        taskInfo.setCompleted(cursor.getInt(6) == 1);

        return  taskInfo;
    }

    public void removeAllCompletedTask() {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_USE_YN, false);

        // updating row
        db.update(TABLE_CONTACTS, values, KEY_COMPLETED, null);
    }
}
