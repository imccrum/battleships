package com.example.battleships;

public class HiScore {
	
	private int mId;
	private String mName;
	private String mTime;
	
	public HiScore() {	
	}
	
	public HiScore(int id, String name, String time) {
		
		mId = id;
		mName = name;
		mTime = time;
	}
	
	public HiScore(String name, String time) {
		
		mName = name;
		mTime = time;
	}
	
	public void setID(int id) {
		mId = id;
	}
	
	public void incrementId() {
		mId++;
	}
	
	public int getID() {
		return mId;
	}

	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setTime(String time) {
		mTime = time;
	}
	
	public String getTime() {
		return mTime;
	}	
}
