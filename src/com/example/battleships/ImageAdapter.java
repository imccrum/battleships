package com.example.battleships;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private int blockSize;
	private int viewSize;
	private int gridSize;
	private Square[][] gameBoard;
	private Counter[] counters;

	public ImageAdapter(Context c, int blockSize, int viewSize, int gridSize, Square[][] gameBoard, Counter[] counters) {

		mContext = c;
		this.blockSize = blockSize;
		this.viewSize = viewSize;
		this.gridSize = gridSize;
		this.gameBoard = gameBoard;
		this.counters = counters;
	}

	public int getCount() {
		return viewSize * viewSize;
	}

	public Object getItem(int position) {
		return 0;
	}

	public long getItemId(int position) {
		return 0;
	}

	// CREATE AN IMAGE VIEW OR TEXT VIEW DEPENDING ON POSITION

	public View getView(int position, View convertView, ViewGroup parent) {

		TextView textView = null;
		ImageView imageView;

		int gridRow = gridSize - 1 - (position / viewSize);
		int gridColumn = (position % viewSize);

		imageView = new ImageView(mContext);
		imageView.setLayoutParams(new GridView.LayoutParams(blockSize, blockSize));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		if (gridRow == -1 || gridColumn == gridSize) {

			textView = new TextView(mContext);
			textView.setLayoutParams(new GridView.LayoutParams(blockSize, blockSize));
			textView.setTextSize(2, 30 - viewSize);
			textView.setGravity(0x11);
			textView.setBackgroundResource(R.color.white);
			textView.setClickable(false);

			if (position == (viewSize * viewSize) - 1) {

				textView.setText("");
			}

			else {

				int counterValue;

				if (gridRow >= 0) {

					counterValue = counters[gridRow].rowTotal;

					if (counters[gridRow].rowColor == 1) {

						textView.setTextColor(mContext.getResources().getColor(R.color.red));
					}
					else {

						textView.setTextColor(mContext.getResources().getColor(R.color.black));
					}
				}
				else {

					counterValue = counters[gridColumn].colTotal;

					if (counters[gridColumn].rowColor == 1) {

						textView.setTextColor(mContext.getResources().getColor(R.color.red));
					}

					else {

						textView.setTextColor(mContext.getResources().getColor(R.color.black));
					}
				}

				textView.setText(Integer.toString(counterValue));

				return textView;
			}
			
			// NOT A COUNTER GET AN IMAGE
			
		} else { 

			switch (gameBoard[gridColumn][gridRow].status) {

			case 0:

				imageView.setImageDrawable(null);
				break;

			case 1:
				imageView.setImageResource(R.drawable.sea);
				break;

			case 2:

				switch (gameBoard[gridColumn][gridRow].guess) {

				case 0:
					imageView.setImageResource(R.drawable.single);
					break;

				case 1:
					imageView.setImageResource(R.drawable.bottom);
					break;

				case 2:
					imageView.setImageResource(R.drawable.mid);
					break;

				case 3:
					imageView.setImageResource(R.drawable.top);
					break;

				case 4:
					imageView.setImageResource(R.drawable.left);
					break;

				case 5:
					imageView.setImageResource(R.drawable.right);
					break;
				case 6:
					imageView.setImageResource(R.drawable.error);
					break;
				}

				break;

			case 3:
			case 4:

				switch (gameBoard[gridColumn][gridRow].contents) {

				case 0:
					imageView.setImageResource(R.drawable.single);
					break;

				case 1:
					imageView.setImageResource(R.drawable.bottom);
					break;

				case 2:
					imageView.setImageResource(R.drawable.mid);
					break;

				case 3:
					imageView.setImageResource(R.drawable.top);
					break;

				case 4:
					imageView.setImageResource(R.drawable.left);
					break;

				case 5:
					imageView.setImageResource(R.drawable.right);
					break;

				case 6:
					imageView.setImageResource(R.drawable.sea);
					break;
				}

				imageView.setOnClickListener(null);
				break;
			}
		}

		imageView.setBackgroundResource(R.color.white);
		return imageView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		if ((position + 1) % viewSize == 0 || position > ((viewSize) * (viewSize - 1)) - 1) {
			return false;
		} else {
			return true;
		}
	}
}
