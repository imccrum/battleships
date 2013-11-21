package com.example.battleships;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends Activity {

	private Spinner spinner1, spinner2, spinner3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		spinner1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gridSizes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);
		spinner1.setOnItemSelectedListener(new SpinnerActivity());

		
		spinner2 = (Spinner) findViewById(R.id.spinner2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.boards, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
		spinner2.setOnItemSelectedListener(new SpinnerActivity());		

		
		spinner3 = (Spinner) findViewById(R.id.spinner3);
		adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner3.setAdapter(adapter);
		spinner3.setOnItemSelectedListener(new SpinnerActivity());
		
		
	}

	public void sendMessage(View view) {

		int grid = spinner1.getSelectedItemPosition();
		int boardNo = spinner2.getSelectedItemPosition();
		int difficulty = spinner3.getSelectedItemPosition();

		Intent intent = new Intent(this, Start.class);
		intent.putExtra("grid", grid);
		intent.putExtra("boardno", boardNo);
		intent.putExtra("difficulty", difficulty);
		startActivity(intent);

	}

	public class SpinnerActivity implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}
}
