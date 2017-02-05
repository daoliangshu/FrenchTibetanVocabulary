package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daoliangshu on 1/28/17.
 * Class containing the db connection,
 * provides methods to get data from db.
 */

public class DBHelper extends SQLiteOpenHelper {

    public final static String COL_ID = "_id";
    public final static String COL_WORD = "word";
    public final static String COL_TRANS = "trans";
    public final static String COL_PHON = "phonetic";
    public final static String COL_LESSON = "lesson";
    public final static String COL_OGG = "sound_ogg";
    public final static String TB_BASIC = "basic_dic";
    private final static String TB_SOUND = "sound_corr";
    private final static String COL_SOUND = "sound";
    private final static String COL_LETTER = "letter";

    private SQLiteDatabase myDB;
    private static String DB_PATH;
    private static final String DB_NAME = "fr_tb_dic.db";
    private final Context myContext;

    public DBHelper(Context context) throws SQLException {
        super(context, DB_NAME, null, 3);
        myContext = context;
        DB_PATH = myContext.getFilesDir().getPath();
        openDB();
    }

    private void openDB() throws SQLException {
        String path = DB_PATH + "/dic_librefra.db";
        File dbFile = new File(path);
        if (!dbFile.exists()) {
            try {
                copyDB(dbFile);
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            myDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

            Log.i("DB", "Opend db succesfully !!");

        } else {
            Log.i("ERR", "File not found");
            System.exit(-1);
        }
    }

    public void copyDB(File dbFile) throws IOException {
        //Open local db as input stream
        Log.e("Err0", "Could not open a stream");
        InputStream input = myContext.getAssets().open(DB_NAME);
        //Path to the new created empty db
        Log.e("Info", "Opening:" + dbFile.toString());


        OutputStream output = new FileOutputStream(dbFile);
        Log.e("Err", "Could not open a stream");

        //transfer bytes from inputfile to outputfile
        byte[] buffer = new byte[1024];
        Log.e("Err3", "Could not open a stream");
        while (input.read(buffer) > 0) {
            output.write(buffer);
            System.out.println(buffer.toString());
        }
        Log.i("CopyDB", "OK");
        //close
        output.flush();
        output.close();
        input.close();
    }

    public synchronized void close() {
        if (myDB != null) myDB.close();
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public ArrayList<HashMap<String, String>> getTrans(String colWord) {
        return getTransByStartWith(colWord, 0, 0);
    }


    private boolean entryExist(String table, String colName, String value) {
        try {
            String q = "SELECT _id" +
                    " FROM '" + table + "' WHERE " +
                    colName + " LIKE '" + value + "'";
            Cursor c = myDB.rawQuery(q, null);
            Log.i("query", q);
            Log.i("count", String.valueOf(c.getCount()));
            if (c.moveToFirst()) {
                c.close();
                return true;
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public byte[] getLetterAudioBytes(String letterOrPhon) {
        String q = "SELECT " + COL_SOUND + " FROM " + TB_SOUND +
                " WHERE <my_col_name>=\"" + letterOrPhon + "\";";
        if (entryExist(TB_SOUND, COL_PHON, letterOrPhon)) {
            q = q.replace("<my_col_name>", COL_PHON);
        } else if (entryExist(TB_SOUND, COL_LETTER, letterOrPhon)) {
            q = q.replace("<my_col_name>", COL_LETTER);
        } else return null;

        Cursor c = myDB.rawQuery(q, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        } else {
            byte[] res = c.getBlob(0);
            c.close();
            return res;
        }
    }

    public byte[] getAudioBytes(int wordInt) {
        Cursor c = myDB.rawQuery("SELECT " +
                        COL_OGG +
                        " FROM " + TB_BASIC + " WHERE _id=" + wordInt,
                null);
        if (!c.moveToFirst()) return null;
        byte[] res = c.getBlob(0);
        c.close();
        return res;
    }

    public ArrayList<HashMap<String, String>> getTransByStartWith(String colWord,
                                                                  int mode,
                                                                  int lessonIndex) {
        String query = "SELECT " +
                COL_ID + ", " +
                COL_TRANS + ", " + COL_PHON + ", " + COL_WORD +
                " FROM " + TB_BASIC;
        if (mode == 1)
            query += " WHERE " + COL_WORD + " LIKE '" + colWord + "%' ";
        else
            query += " WHERE " + COL_WORD + "=`" + colWord;
        if (lessonIndex > 0)
            query += " AND " + COL_LESSON + "=" + lessonIndex;
        query += ";";
        Cursor c = myDB.rawQuery(query, null);
        ArrayList<HashMap<String, String>> resList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> res = new HashMap<>();
            res.put(COL_ID, c.getString(0));
            res.put(COL_TRANS, c.getString(1));
            res.put(COL_PHON, c.getString(2));
            res.put(COL_WORD, c.getString(3));
            resList.add(res);
        }
        c.close();
        return resList;
    }


}
