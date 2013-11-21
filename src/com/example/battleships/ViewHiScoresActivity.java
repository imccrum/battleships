package com.example.battleships;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.battleships.DbHandler;
import com.example.battleships.HiScore;

public class ViewHiScoresActivity extends Activity {

	private DbHandler mDatabase;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		setContentView(R.layout.activity_viewhiscores);
		setTitle("HI SCORES");
		mDatabase = new DbHandler(this);
		setupView();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}


	private void setupView() {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout hiScoresLayout = (LinearLayout) findViewById(R.id.hiScores_list);
		
		List<HiScore> allHiScores = mDatabase.getAllHiScores();
		for (int i = 0; i < allHiScores.size(); i++) {
			LinearLayout layout = (LinearLayout) inflater.inflate(
			R.layout.card_hiscore, null, true);
			TextView hiScoreName = (TextView) layout.findViewById(R.id.hiscore_name);
			hiScoreName.setText(allHiScores.get(i).getName() + ":");
			TextView hiScoreTime = (TextView) layout.findViewById(R.id.hiscore_time);
			hiScoreTime.setText(allHiScores.get(i).getTime() + " seconds");
			hiScoresLayout.addView(layout);
		}
	}
}
