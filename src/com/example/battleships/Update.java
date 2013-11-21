package com.example.battleships;

public class Update {

	int adjPos;
	int adjCol;
	int adjRow;
	int length;
	int viewId;
	int oppId;
	boolean error;

	public Update(int adjPos, int adjCol, int adjRow, int length, int viewId, int oppId, boolean error) {

		this.adjPos = adjPos;
		this.adjCol = adjCol;
		this.adjRow = adjRow;
		this.length = length;
		this.viewId = viewId;
		this.oppId = oppId;
		this.error = error;
	}
}
