package com.example.battleships;

public class Square {

	int x;
	int y;
	int contents;
	int status;
	int guess;
	int orientation;

	public Square(int x, int y, int contents, int status, int guess, int orientation) {

		this.x = x;
		this.y = y;
		this.contents = contents;
		this.status = status;
		this.guess = guess;
		this.orientation = orientation;
	}
}
