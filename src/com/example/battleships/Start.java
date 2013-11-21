package com.example.battleships;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Start extends Activity implements MyDialogFragmentListener {

	private DbHandler mDatabase;
	private Chronometer chronometer;
	private Square[][] gameBoard;
	private ArrayList<Boat> boats;
	private Counter[] counters;
	private GridView gridView;
	private int gridIndex;
	private int gridSize;
	private int viewSize;
	private int blockSize;
	private int difficulty;
	private boolean timerOn;
	private boolean solved;
	private long savedTime;
	private long finishTime;
	private int rank;
	private int scoreboardSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && "savedtime" != null) {
			savedTime = savedInstanceState.getLong("savedtime");
		}
		if (savedInstanceState != null && "gameboard" != null) {
			gameBoard = (Square[][]) savedInstanceState.getSerializable("gameboard");
		}
		if (savedInstanceState != null && "timeron" != null) {
			timerOn = savedInstanceState.getBoolean("timeron");
		}
		if (savedInstanceState != null && "solved" != null) {
			solved = savedInstanceState.getBoolean("solved");
		}
		if (savedInstanceState != null && "counters" != null) {
			counters = (Counter[]) savedInstanceState.getSerializable("counters");
		}
		if (savedInstanceState != null && "finishtime" != null) {
			finishTime = savedInstanceState.getLong("finishtime");
		}

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		gridIndex = extras.getInt("grid");
		difficulty = extras.getInt("difficulty");

		LinearLayout getDimensions = new LinearLayout(this);
		getDimensions.setOrientation(LinearLayout.VERTICAL);
		getDimensions.post(new Runnable()

		// HELPER METHOD TO GET THE DIMENSIONS FOR THE GRID LAYOUT

				{
					@Override
					public void run() {

						Rect rect = new Rect();
						Window window = getWindow();
						window.getDecorView().getWindowVisibleDisplayFrame(rect);
						int statusBarHeight = rect.top;
						int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
						int titleBarHeight = contentViewTop - statusBarHeight;
						System.out.println("TitleBarHeight: " + titleBarHeight + ", StatusBarHeight: " + statusBarHeight);
						int menuHeight = titleBarHeight + statusBarHeight;
						try {
							game(menuHeight, savedTime, gridIndex, difficulty);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

						}
					}
				});
	}

	// ================================================================================
	// SET UP THE VIEW
	// ================================================================================

	public void game(int menuHeight, long savedTime, int gridIndex, int difficulty) throws IOException {

		setContentView(R.layout.activity_start);
		chronometer = (Chronometer) findViewById(R.id.chronometer);

		if (timerOn == true) {

			chronometer.setBase(SystemClock.elapsedRealtime() + savedTime);
			chronometer.start();
		}

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		switch (gridIndex) {

		case 0:
			viewSize = 7;
			gridSize = 6;
			break;
		case 1:
			viewSize = 9;
			gridSize = 8;
			break;

		case 2:
			viewSize = 11;
			gridSize = 10;
			break;
		default:
			viewSize = 7;
			gridSize = 6;
		}

		int screenSize;

		if (height > width) {

			blockSize = ((width - gridSize) / viewSize) - 1;
			screenSize = width;

		} else {

			blockSize = (((height - menuHeight - gridSize) / viewSize)) - 1;
			screenSize = height - menuHeight;

		}

		// DON'T CHECK IF RUNNING

		if (timerOn == false) {

			ArrayList<Boat> boats = readFile();
			gameBoard = setUpBoard(boats);
			counters = getCounters();

		}

		// THIS FIXES THE GRID VIEW FOR HORIZONTAL LAYOUTS

		gridView = (GridView) findViewById(R.id.gridview);

		if (height < width) {

			LayoutParams params = gridView.getLayoutParams();
			params.width = screenSize;

		}

		gridView.setNumColumns(viewSize);
		gridView.setAdapter(new ImageAdapter(this, blockSize, viewSize, gridSize, gameBoard, counters));

		// ================================================================================
		// GRID EVENT LISTENER
		// ================================================================================

		final Button clearButton = (Button) findViewById(R.id.button1);
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				clearBoard();
				gridView = (GridView) findViewById(R.id.gridview);
				gridView.setAdapter(new ImageAdapter(getBaseContext(), blockSize, viewSize, gridSize, gameBoard, counters));

			}
		});

		final Button resignButton = (Button) findViewById(R.id.button2);
		resignButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				replay();
			}
		});
		
		
		
		// ================================================================================
		// GRID EVENT LISTENER
		// ================================================================================

		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				// CONVERT THE GRID POSITION TO A COORDINATE

				int gridRow = viewSize - 2 - (position / viewSize);
				int gridColumn = position - ((position / viewSize) * viewSize);

				if (timerOn == true || solved == true) {
					
					
					gameBoard[gridColumn][gridRow].status = updateSquare(parent, v, gameBoard[gridColumn][gridRow].status, gridRow, gridColumn, position);

					solved = updateCounters(parent, gridColumn, gridRow);

					if (solved) {

						// RECORD THE USERS FINISH TIME

						chronometer.stop();
						timerOn = false;
						finishTime = chronometer.getBase() - SystemClock.elapsedRealtime();
						finishTime = finishTime / (-1000);

						// CHECK THE DATABASE TO SEE WHERE THE USER RANKS

						mDatabase = new DbHandler(getBaseContext());
						scoreboardSize = 10;

						List<HiScore> allHiScores = mDatabase.getAllHiScores();

						rank = allHiScores.size();

						for (int i = 0; i < allHiScores.size(); i++) {

							int temp = Integer.parseInt(allHiScores.get(i).getTime());

							if (finishTime < temp) {

								rank = i;
								break;
							}
						}

						// REDIRECT THE USER TO THE APPROPRIATE FRAGMENT

						if (rank < scoreboardSize) {

							DialogFragment newFragment = new HiScoreFragment();
							newFragment.show(getFragmentManager(), "hiscore");
							
						}

						else {

							DialogFragment otherFragment = new SolvedFragment();
							otherFragment.show(getFragmentManager(), "solved");
						}

					}

				}

				// FIRST CLICK - REVEAL SQUARES AND SET THE TIMER

				else {

					revealSection(parent, v, gridColumn, gridRow, position);
					TextView message = (TextView) findViewById(R.id.play_message);
					message.setText("FIND\nTHE\nBOATS");
					chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();
					timerOn = true;

				}
			}
		});
	}

	// ================================================================================
	// DIALOG FRAGMENTS
	// ================================================================================

	// FOR HIGH SCORES

	public static class HiScoreFragment extends DialogFragment {

		public HiScoreFragment() {
		}

		String inputName;
		EditText editText;
		View v;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
			v = getActivity().getLayoutInflater().inflate(R.layout.dialogue_getname, null);
			builder.setTitle(R.string.new_hi_score).setView(v)
	
			
			.setNegativeButton(R.string.replay, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {

					MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();
				}
			})

			.setPositiveButton(R.string.submit_score, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {

					MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();

					editText = (EditText) v.findViewById(R.id.getname);
					activity.onReturnValue(editText.getText().toString().toUpperCase());
				}
			});

			if (savedInstanceState != null) {
				inputName = savedInstanceState.getString("inputname");
			}

			// PRELOAD TEXT IF IT HAS ALREADY BEEN INPUTTED

			if (inputName != null) {
				editText = (EditText) v.findViewById(R.id.getname);
				editText.setText(inputName);
			}

			return builder.create();
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {

			// SAVE THE INPUT ON SCREEN REORIENTATION

			editText = (EditText) v.findViewById(R.id.getname);
			outState.putString("inputname", editText.getText().toString());

		}

	}

	// NOT A HIGH SCORE

	public static class SolvedFragment extends DialogFragment {

		public SolvedFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.congratulations)
			

			.setNegativeButton(R.string.replay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

					MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();

					activity.replay();

				}
			})

			.setPositiveButton(R.string.see_scores, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

					MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();

					activity.viewScores();

				}
			});

			return builder.create();
		}

	}

	// ================================================================================
	// EVENT LISTENERS FOR DIALOG FRAGMENTS
	// ================================================================================

	// PROCESS HISCORE AND ADD IT TO THE DATABASE

	@Override
	public void onReturnValue(String inputName) {

		mDatabase = new DbHandler(getBaseContext());
		List<HiScore> allHiScores = mDatabase.getAllHiScores();

		if (inputName.length() < 1) {
			inputName = "PLAYER ONE";
		}

		String timeString = String.valueOf(finishTime);

		if (rank == allHiScores.size()) {

			mDatabase.addHiScore(new HiScore(inputName, timeString));
		}

		else {

			for (int i = rank; i < allHiScores.size(); i++) {

				if (i == rank) {

					HiScore hiScore = new HiScore(rank + 1, inputName, timeString);
					mDatabase.updateHiScore(hiScore);
				}

				if (i == allHiScores.size() - 1) {

					if (i < scoreboardSize - 1) {

						mDatabase.addHiScore(allHiScores.get(i));
					}

				} else {

					allHiScores.get(i).incrementId();
					mDatabase.updateHiScore(allHiScores.get(i));
				}
			}
		}

		mDatabase.close();

		Intent intent = new Intent(getBaseContext(), ViewHiScoresActivity.class);
		finish();
		startActivity(intent);
	}

	// MOVE TO VIEW HISCORES ACTIVITY

	@Override
	public void viewScores() {

		Intent intent = new Intent(getBaseContext(), ViewHiScoresActivity.class);
		finish();
		startActivity(intent);

	}

	// RELOAD THE CURRENT ACTIVITY

	@Override
	public void replay() {

		finish();
		startActivity(getIntent());

	}
	

	// METHOD TO PROCESS USER INPUT DEPENDING ON STATUS OF ADJACENT SQUARES

	public int updateSquare(AdapterView<?> parent, View v, int index, int gridRow, int gridColumn, int position) {

		// THIS ARRAYLIST WILL BE POPULATE IF ANY OF THE ADJACENT SQUARES
		// CONTAIN SHIPS

		ArrayList<Update> updates = new ArrayList<Update>();

		if (gridColumn > 0) {

			int counter = 1;

			if ((gameBoard[gridColumn - counter][gridRow].status == 2 || gameBoard[gridColumn - counter][gridRow].status == 3) && gameBoard[gridColumn - counter][gridRow].guess != 6) {

				int length = 1;
				boolean isLine = true;
				Update update = new Update(-1, -1, 0, length, 5, 4, false);
				counter++;

				while (isLine == true && gridColumn - counter >= 0) {

					if ((gameBoard[gridColumn - counter][gridRow].status == 2 || gameBoard[gridColumn - counter][gridRow].status == 3) && gameBoard[gridColumn - counter][gridRow].guess != 6) {
						length++;
						counter++;
					} else {
						isLine = false;
					}
				}

				update.length = length;
				updates.add(update);

			}
		}

		if (gridColumn < gridSize - 1) {

			int counter = 1;

			if ((gameBoard[gridColumn + counter][gridRow].status == 2 || gameBoard[gridColumn + counter][gridRow].status == 3) && gameBoard[gridColumn + counter][gridRow].guess != 6) { // block

				int length = 1;
				boolean isLine = true;
				Update update = new Update(1, 1, 0, length, 4, 5, false);
				counter++;

				while (isLine == true && gridColumn + counter <= gridSize - 1) {

					if ((gameBoard[gridColumn + counter][gridRow].status == 2 || gameBoard[gridColumn + counter][gridRow].status == 3) && gameBoard[gridColumn + counter][gridRow].guess != 6) {
						length++;
						counter++;
					} else {
						isLine = false;
					}
				}

				update.length = length;
				updates.add(update);

			}
		}

		if (gridRow > 0) {

			int counter = 1;

			if ((gameBoard[gridColumn][gridRow - counter].status == 2 || gameBoard[gridColumn][gridRow - counter].status == 3) && gameBoard[gridColumn][gridRow - counter].guess != 6) {

				int length = 1;
				boolean isLine = true;
				Update update = new Update(viewSize, 0, -1, length, 3, 1, false);
				counter++;

				while (isLine == true && gridRow - counter >= 0) {

					if ((gameBoard[gridColumn][gridRow - counter].status == 2 || gameBoard[gridColumn][gridRow - counter].status == 3) && gameBoard[gridColumn][gridRow - counter].guess != 6) {
						length++;
						counter++;
					} else {
						isLine = false;
					}
				}

				update.length = length;
				updates.add(update);

			}

		}

		if (gridRow < gridSize - 1) {

			int counter = 1;

			if ((gameBoard[gridColumn][gridRow + counter].status == 2 || gameBoard[gridColumn][gridRow + counter].status == 3) && gameBoard[gridColumn][gridRow + counter].guess != 6) {

				int length = 1;
				boolean isLine = true;
				Update update = new Update(-viewSize, 0, 1, length, 1, 3, false);
				counter++;

				while (isLine == true && gridRow + counter <= gridSize - 1) {

					if ((gameBoard[gridColumn][gridRow + counter].status == 2 || gameBoard[gridColumn][gridRow + counter].status == 3) && gameBoard[gridColumn][gridRow + counter].guess != 6) {
						length++;
						counter++;
					} else {
						isLine = false;
					}
				}

				update.length = length;
				updates.add(update);

			}

		}

		// WITH DETAILS OF THE ADJACENT SQUARES NOW PROCESS THE MOVE

		switch (index) {

		// IF SEA BLANK CONVERT TO SEA

		case 0:

			ImageView seaView = (ImageView) v;
			seaView.setImageResource(R.drawable.sea);
			gameBoard[gridColumn][gridRow].guess = 7;
			return 1;

		case 1:

			// SQUARE HAS NO NEIGHBOURING SHIPS

			if (updates.size() == 0) {

				ImageView view = (ImageView) v; // first update original
												// position
				view.setImageResource(R.drawable.single);
				gameBoard[gridColumn][gridRow].guess = 0;
				return 2;

			}

			// IF TWO NEIGHBOURING SHIPS

			else if (updates.size() >= 2) {

				// IF IN A ROW ALLOW TO JOIN

				if (updates.size() == 2 && updates.get(0).length + updates.get(1).length <= 3 && updates.get(0).viewId == updates.get(1).oppId) {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.mid);
					gameBoard[gridColumn][gridRow].guess = 2;

					for (int k = 0; k < 2; k++) {

						if (gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].status != 3) {

							if (gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].guess != 0 && gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].guess != updates.get(k).viewId && gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].contents != 2 && gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].contents != updates.get(k).oppId) {

								// ERROR

								ImageView errView = (ImageView) v;
								errView.setImageResource(R.drawable.error);
								gameBoard[gridColumn][gridRow].guess = 6;
								updates.get(k).error = true;

								// UPDATE ADJACEN SQUARES TO REFLECT THE ERROR

								if (k == 1 && updates.get(0).length + updates.get(1).length == 2) {

									View fillView = parent.getChildAt(position - (updates.get(k).adjPos));
									ImageView fV = (ImageView) fillView;
									fV.setImageResource(R.drawable.single);
									gameBoard[gridColumn - (updates.get(k).adjCol)][gridRow - (updates.get(k).adjRow)].guess = 2;

								}

								if (updates.get(0).length + updates.get(1).length > 2) {

									View fillView = parent.getChildAt(position - (updates.get(k).adjPos));
									ImageView fV = (ImageView) fillView;

									switch (updates.get(k).oppId) {

									case 1:
										fV.setImageResource(R.drawable.bottom);
										break;

									case 3:
										fV.setImageResource(R.drawable.top);
										break;

									case 4:
										fV.setImageResource(R.drawable.left);
										break;

									case 5:
										fV.setImageResource(R.drawable.right);
										break;
									}

									gameBoard[gridColumn - updates.get(k).adjCol][gridRow - updates.get(k).adjRow].guess = updates.get(k).oppId;

								}

								return 2;

							}

							// NOW UPDATE NEIGHBOURS

							if (updates.get(k).length == 1) {

								View fillView = parent.getChildAt(position + (updates.get(k).adjPos));
								ImageView fV = (ImageView) fillView;

								switch (updates.get(k).oppId) {

								case 1:
									fV.setImageResource(R.drawable.bottom);
									break;

								case 3:
									fV.setImageResource(R.drawable.top);
									break;

								case 4:
									fV.setImageResource(R.drawable.left);
									break;

								case 5:
									fV.setImageResource(R.drawable.right);
									break;
								}

								gameBoard[gridColumn + updates.get(k).adjCol][gridRow + updates.get(k).adjRow].guess = updates.get(k).oppId;

							}

							else {

								View fillView = parent.getChildAt(position + (updates.get(k).adjPos));
								ImageView fV = (ImageView) fillView;
								fV.setImageResource(R.drawable.mid);
								gameBoard[gridColumn + (updates.get(k).adjCol)][gridRow + (updates.get(k).adjRow)].guess = 2;

							}
						}
					}

					return 2;

				}

				else {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.error);
					gameBoard[gridColumn][gridRow].guess = 6;
					return 2;

				}
			}

			// NOW IF ONLY ONE NEIGHBOUR

			for (int i = 0; i < updates.size(); i++) {

				// ERROR - CANNOT JOIN

				if (gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess != 0 && gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess != updates.get(i).viewId) {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.error);
					gameBoard[gridColumn][gridRow].guess = 6;
					return 2;

				}

				// ERROR - TOO LONG

				if (updates.get(i).length > 3) {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.error);
					gameBoard[gridColumn][gridRow].guess = 6;
					return 2;

				}

				// ERROR - REVEALED SQUARE

				if (gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].status == 3 && (updates.get(i).adjRow == gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].orientation || -(updates.get(i).adjRow) == gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].orientation)) {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.error);
					gameBoard[gridColumn][gridRow].guess = 6;
					return 2;

				}

				// ERROR - REVEALED SQUARE WRONG ROTATION

				if (gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].status == 3 && gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].orientation == 2) {

					ImageView view = (ImageView) v;
					view.setImageResource(R.drawable.error);
					gameBoard[gridColumn][gridRow].guess = 6;
					return 2;

				}

				// OTHERWISE UPDATE

				ImageView view = (ImageView) v;

				switch (updates.get(i).viewId) {

				case 1:
					view.setImageResource(R.drawable.bottom);
					break;

				case 3:
					view.setImageResource(R.drawable.top);
					break;

				case 4:
					view.setImageResource(R.drawable.left);
					break;

				case 5:
					view.setImageResource(R.drawable.right);
					break;

				}

				gameBoard[gridColumn][gridRow].guess = updates.get(i).viewId;

				// THEN DO SUBSEQUENT SQUARES

				for (int j = 0; j < updates.get(i).length; j++) {

					// DON'T UPDATE IF ALREADY REVEALED

					if (gameBoard[gridColumn + ((j + 1) * updates.get(i).adjCol)][gridRow + ((j + 1) * updates.get(i).adjRow)].status != 3) {

						// ONLY ONE SQUARE LEFT TO UPDATE

						if (j == updates.get(i).length - 1) {

							View endView = parent.getChildAt(position + (updates.get(i).adjPos * (j + 1)));
							ImageView eV = (ImageView) endView;

							switch (updates.get(i).oppId) {

							case 1:
								eV.setImageResource(R.drawable.bottom);
								break;

							case 3:
								eV.setImageResource(R.drawable.top);
								break;

							case 4:
								eV.setImageResource(R.drawable.left);
								break;

							case 5:
								eV.setImageResource(R.drawable.right);
								break;
							}

							gameBoard[gridColumn + ((j + 1) * updates.get(i).adjCol)][gridRow + ((j + 1) * updates.get(i).adjRow)].guess = updates.get(i).oppId;

						}

						else {

							View midView = parent.getChildAt(position + (updates.get(i).adjPos * (j + 1)));
							ImageView mV = (ImageView) midView;
							mV.setImageResource(R.drawable.mid);
							gameBoard[gridColumn + ((j + 1) * updates.get(i).adjCol)][gridRow + ((j + 1) * updates.get(i).adjRow)].guess = 2;

						}
					}

				}

			}

			return 2;

			// DELETE

		case 2:

			// FIRST DELETE TOUCH

			ImageView view = (ImageView) v;
			view.setImageDrawable(null);

			for (int i = 0; i < updates.size(); i++) {

				// DON'T UPDATE IF REVEALED

				if (gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].status != 3) {

					if (gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess == 2 || gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess == updates.get(i).viewId || gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess == updates.get(i).oppId) {

						// DON'T UPDATE IF ERROR

						if (gameBoard[gridColumn][gridRow].guess != 6) {

							// SINGLE SQUARE

							if (updates.get(i).length == 1) {

								View overView = parent.getChildAt(position + (updates.get(i).adjPos));
								ImageView oV = (ImageView) overView;
								oV.setImageResource(R.drawable.single);
								gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess = 0;

							}

							// MORE THAN ONE SQUARE

							else {

								View overView = parent.getChildAt(position + (updates.get(i).adjPos));
								ImageView oV = (ImageView) overView;

								switch (updates.get(i).viewId) {

								case 1:
									oV.setImageResource(R.drawable.bottom);
									break;

								case 3:
									oV.setImageResource(R.drawable.top);
									break;

								case 4:
									oV.setImageResource(R.drawable.left);
									break;

								case 5:
									oV.setImageResource(R.drawable.right);
									break;
								}
								gameBoard[gridColumn][gridRow].guess = 7;
								gameBoard[gridColumn + updates.get(i).adjCol][gridRow + updates.get(i).adjRow].guess = updates.get(i).viewId;

							}
						}
					}
				}
			}

			return 0;

		}

		return index;
	}

	// ================================================================================
	// COUNT
	// ================================================================================

	public boolean updateCounters(AdapterView<?> parent, int gridColumn, int gridRow) {

		// GET COUNT FOR COLUMNS AND ROWS CHANGE TEXT COLOR IF GUESSES EXCEEDS
		// SHIPS

		int resultCounter = 0;

		for (int i = 0; i < gridSize; i++) {

			int lineCountX = 0;
			int lineCountY = 0;

			int countX = 0;
			int countY = 0;

			for (int j = 0; j < gridSize; j++) {

				if (gameBoard[i][j].status == 3 || gameBoard[i][j].status == 2) {

					countX++;
				}

				if (gameBoard[j][i].status == 3 || gameBoard[j][i].status == 2) {

					countY++;
				}
			}

			lineCountX += countX;
			lineCountY += countY;

			View textView = (TextView) parent.getChildAt(viewSize * (viewSize - 1) + i);
			TextView tV = (TextView) textView;

			if (lineCountX > counters[i].colTotal && counters[i].colColor == 0) {

				tV.setTextColor(getResources().getColor(R.color.red));
				counters[i].colColor = 1;
			}

			if (lineCountX <= counters[i].colTotal && counters[i].colColor == 1) {

				tV.setTextColor(getResources().getColor(R.color.black));
				counters[i].colColor = 0;
			}

			View yView = (TextView) parent.getChildAt((viewSize * (viewSize - 1 - i)) - 1);
			TextView yV = (TextView) yView;

			if (lineCountY > counters[i].rowTotal && counters[i].rowColor == 0) {

				yV.setTextColor(getResources().getColor(R.color.red));
				counters[i].rowColor = 1;

			}

			if (lineCountY <= counters[i].rowTotal && counters[i].rowColor == 1) {

				yV.setTextColor(getResources().getColor(R.color.black));
				counters[i].rowColor = 0;

			}

			// CHECK THE RESULT!

			if (lineCountX == counters[i].colTotal && lineCountY == counters[i].rowTotal) {

				resultCounter++;

			}

		}

		if (resultCounter == gridSize) {

			return true;

		}

		else
			return false;
	}

	public void revealSection(AdapterView<?> parent, View v, int gridColumn, int gridRow, int position) {

		switch (difficulty) {

		// REVEAL 4X4 - IN CORNER CASES OR EDGES REVEAL OPPOSITE SIDE OF TOUCH

		case 0:

			gameBoard[gridColumn][gridRow].status = reveal(v, gameBoard[gridColumn][gridRow].contents);

			if (gridColumn == gridSize - 1) {

				View v1 = parent.getChildAt(position - 1);
				gameBoard[gridColumn - 1][gridRow].status = reveal(v1, gameBoard[gridColumn - 1][gridRow].contents);

				if (gridRow == gridSize - 1) {

					View v2 = parent.getChildAt(position + viewSize);
					gameBoard[gridColumn][gridRow - 1].status = reveal(v2, gameBoard[gridColumn][gridRow - 1].contents);

					View v3 = parent.getChildAt(position + viewSize - 1);
					gameBoard[gridColumn - 1][gridRow - 1].status = reveal(v3, gameBoard[gridColumn - 1][gridRow - 1].contents);

				} else {

					View v2 = parent.getChildAt(position - viewSize);
					gameBoard[gridColumn][gridRow + 1].status = reveal(v2, gameBoard[gridColumn][gridRow + 1].contents);

					View v3 = parent.getChildAt(position - viewSize - 1);
					gameBoard[gridColumn - 1][gridRow + 1].status = reveal(v3, gameBoard[gridColumn - 1][gridRow + 1].contents);

				}

			}

			else if (gridRow == gridSize - 1 && gridColumn < gridSize - 1) {

				View v1 = parent.getChildAt(position + 1);
				gameBoard[gridColumn + 1][gridRow].status = reveal(v1, gameBoard[gridColumn + 1][gridRow].contents);

				View v2 = parent.getChildAt(position + viewSize);
				gameBoard[gridColumn][gridRow - 1].status = reveal(v2, gameBoard[gridColumn][gridRow - 1].contents);

				View v3 = parent.getChildAt(position + viewSize + 1);
				gameBoard[gridColumn + 1][gridRow - 1].status = reveal(v3, gameBoard[gridColumn + 1][gridRow - 1].contents);

			}

			else {

				View v1 = parent.getChildAt(position + 1);
				gameBoard[gridColumn + 1][gridRow].status = reveal(v1, gameBoard[gridColumn + 1][gridRow].contents);

				View v2 = parent.getChildAt(position - viewSize);
				gameBoard[gridColumn][gridRow + 1].status = reveal(v2, gameBoard[gridColumn][gridRow + 1].contents);

				View v3 = parent.getChildAt(position - viewSize + 1);
				gameBoard[gridColumn + 1][gridRow + 1].status = reveal(v3, gameBoard[gridColumn + 1][gridRow + 1].contents);

			}

			break;

		case 1:

			// REVEAL 2X2 - AT EDGES REVEAL OPPOSITE SIDE OF TOUCH

			gameBoard[gridColumn][gridRow].status = reveal(v, gameBoard[gridColumn][gridRow].contents);

			if (gridColumn == gridSize - 1) {

				View nextV = parent.getChildAt(position - 1);
				gameBoard[gridColumn - 1][gridRow].status = reveal(nextV, gameBoard[gridColumn - 1][gridRow].contents);

			} else {

				View nextV = parent.getChildAt(position + 1);
				gameBoard[gridColumn + 1][gridRow].status = reveal(nextV, gameBoard[gridColumn + 1][gridRow].contents);

			}

			break;

		case 2:

			// REVEAL 1X1

			gameBoard[gridColumn][gridRow].status = reveal(v, gameBoard[gridColumn][gridRow].contents);

		}
	}

	// ================================================================================
	// REVEAL
	// ================================================================================

	public int reveal(View v, int index) {

		ImageView imageView = (ImageView) v;

		switch (index) {

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
			return 4;
		}

		// DISPABLE CLICKING ON ALL THE SQARES THAT HAVE BEEN REVEALED

		imageView.setOnClickListener(null);
		return 3;
	}

	// ================================================================================
	// READ FILE
	// ================================================================================

	public ArrayList<Boat> readFile() throws IOException {

		ArrayList<Boat> boats = new ArrayList<Boat>();

		try {

			InputStream stream = getResources().openRawResource(R.raw.r10);

			if (stream != null) {

				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

				String l;

				int counter = 0;

				String USER_INPUT = "Board 1";

				while ((l = reader.readLine()) != null) {

					if (counter == 0) {
						if (l != USER_INPUT) {
							// throw an exception
						}
					}

					else if (counter == 1) {

						int x = Integer.parseInt(l);

						if (x != gridSize) {
							// throw an exception
						}
					}

					else {

						Boat boat = new Boat(0, 0, 0, 0);
						int arrayCounter = 0;

						for (int i = 0; i < l.length(); i++) {

							char c = l.charAt(i);

							if (c != ':' && c != ',') {

								int temp = Character.getNumericValue(c);

								if (arrayCounter == 0) {

									boat.length = temp;
								}

								if (arrayCounter == 1) {

									boat.x = temp;
								}

								if (arrayCounter == 2) {

									boat.y = temp;
								}

								if (arrayCounter == 3) {

									if (temp == boat.x) {

										boat.orientation = 0;

									} else {

										boat.orientation = 1;
									}
								}

								arrayCounter++;
							}
						}

						// THEN FINALLY ADD TO BOATS

						boats.add(boat);
					}
					// INCREMENT FOT THE WHILE LOOP

					counter++;
				}
				// CLOSE THE FILE

				stream.close();
			}

		} catch (java.io.FileNotFoundException e) {

			// FILE DOESN'T EXIST
		}

		return boats;

	}

	// ================================================================================
	// BOARD SETUP
	// ================================================================================

	public Square[][] setUpBoard(ArrayList<Boat> boats) {

		// INITIALISE A 2D ARRAY OF SQUARES

		gameBoard = new Square[gridSize][gridSize];

		for (int i = 0; i < gridSize; i++) {

			for (int j = 0; j < gridSize; j++) {

				gameBoard[i][j] = new Square(i, j, 0, 0, 0, 0);

			}
		}

		// THEN POPULATE IT WITH THE SHIPS IN BOATS

		for (int i = 0; i < gridSize; i++) {

			for (int j = 0; j < gridSize; j++) {

				for (int k = 0; k < boats.size(); k++) {

					if (i == boats.get(k).x && j == boats.get(k).y) {

						for (int l = 0; l < boats.get(k).length; l++) {

							if (boats.get(k).orientation == 0) {

								if (boats.get(k).length == 1) {

									gameBoard[i][j + l].contents = 0;
									gameBoard[i][j + l].orientation = 2;

								}

								else {

									gameBoard[i][j + l].orientation = 0;

									// VERTICAL

									if (l == 0) {

										gameBoard[i][j].contents = 1; // BOTTOM

									} else if (l == (boats.get(k).length - 1)) {

										gameBoard[i][j + l].contents = 3; // TOP

									} else {

										gameBoard[i][j + l].contents = 2; // MIDDLE

									}

								}

							}

							// HORIZONTAL

							else {

								gameBoard[i + l][j].orientation = 1;

								if (boats.get(k).length == 1) {

									gameBoard[i + l][j].contents = 0; // single.png

								}

								else {

									if (l == 0) {

										gameBoard[i + l][j].contents = 4;

									} else if (l == (boats.get(k).length - 1)) {

										gameBoard[i + l][j].contents = 5;

									} else {

										gameBoard[i + l][j].contents = 2;
									}
								}
							}
						}

						break;

					} else {

						// THE REST IS SEA

						if (gameBoard[i][j].contents < 1) {

							gameBoard[i][j].contents = 6;

						}
					}
				}
			}
		}

		return gameBoard;
	}

	public Counter[] getCounters() {

		Counter[] counters = new Counter[gridSize];

		for (int i = 0; i < gridSize; i++) {

			int xCount = 0;
			int yCount = 0;

			counters[i] = new Counter(0, 0, 0, 0);

			for (int j = 0; j < gridSize; j++) {

				if (gameBoard[i][j].contents != 6) {

					xCount++;
				}

				if (gameBoard[j][i].contents != 6) {

					yCount++;
				}
			}

			counters[i].colTotal = xCount;
			counters[i].rowTotal = yCount;
		}

		return counters;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPause() {
		super.onPause();
		savedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
		chronometer.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		// SAVE EVERYTHING FOR ROTATIONS

		savedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
		savedInstanceState.putSerializable("boats", boats);
		savedInstanceState.putSerializable("gameboard", gameBoard);
		savedInstanceState.putLong("finishtime", finishTime);
		savedInstanceState.putSerializable("counters", counters);
		savedInstanceState.putBoolean("timeron", timerOn);
		savedInstanceState.putBoolean("solved", solved);
		savedInstanceState.putLong("savedtime", savedTime);
	}

	public void clearBoard() {

		for (int i = 0; i < gridSize; i++) {

			for (int j = 0; j < gridSize; j++) {

				if (gameBoard[i][j].status != 3 && gameBoard[i][j].status != 4) {

					gameBoard[i][j].status = 0;
					gameBoard[i][j].guess = 7;

				}
			}
		}
	}

	public void disableBoard() {

		for (int i = 0; i < gridSize; i++) {

			for (int j = 0; j < gridSize; j++) {

				if (gameBoard[i][j].status != 3 && gameBoard[i][j].status != 4) {

					gameBoard[i][j].status = 0;
					gameBoard[i][j].guess = 7;

				}
			}
		}
	}
}
