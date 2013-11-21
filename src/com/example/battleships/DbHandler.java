package com.example.battleships;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.battleships.HiScore;

public class DbHandler extends SQLiteOpenHelper {

	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "hiScores";
	private static final String TABLE_HISCORES = "scores";

	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_HISCORE = "hiscore";

	public DbHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_HISCORES + "("
				+ KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT,"
				+ KEY_HISCORE + " TEXT" + ")";
		db.execSQL(CREATE_POSTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISCORES);
		onCreate(db);
	}

	public void addHiScore(HiScore hiScore) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, hiScore.getName());
		values.put(KEY_HISCORE, hiScore.getTime());
		db.insert(TABLE_HISCORES, null, values);
		db.close();
	}

	public HiScore getHiScore(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_HISCORES, new String[] { KEY_ID, KEY_NAME,
				KEY_HISCORE, }, KEY_ID + "=?", new String[] { String.valueOf(id) },
				null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		HiScore hiScore = new HiScore(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2));

		return hiScore;
	}

	public List<HiScore> getAllHiScores() {
		List<HiScore> postsList = new ArrayList<HiScore>();
		String selectQuery = "SELECT * FROM " + TABLE_HISCORES;
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HiScore hiScore = new HiScore();
				hiScore.setID(Integer.parseInt(cursor.getString(0)));
				hiScore.setName(cursor.getString(1));
				hiScore.setTime(cursor.getString(2));
				postsList.add(hiScore);
			} while (cursor.moveToNext());
		}

		return postsList;
	}
	
	public void deleteHiScore(HiScore hiScore) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_HISCORES, KEY_ID + "=?",
				new String[] { String.valueOf(hiScore.getID()) });
	}

	public int updateHiScore(HiScore hiScore) {
		
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, hiScore.getName());
		values.put(KEY_HISCORE, hiScore.getTime());
		return db.update(TABLE_HISCORES, values, KEY_ID + "=?", new String[] { String.valueOf(hiScore.getID()) });
	}
}
