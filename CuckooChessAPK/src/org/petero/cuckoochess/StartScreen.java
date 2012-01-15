package org.petero.cuckoochess;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartScreen extends Activity implements TextToSpeech.OnInitListener{


	Intent intent;
	private TextToSpeech tts;

	String arr[]={"New Game", "Continue Game","Instructions","Settings"};
	Button button[]=new Button[4];
	int current =3;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);

		
		tts = new TextToSpeech(this, this);
		tts.setLanguage(Locale.US); // Hard coded to US english
		
		button[0] = (Button)findViewById(R.id.newgame);
		button[0].setOnClickListener(new OnClickListener() {        	
			public void onClick(View v) {
				Intent StartGameIntent = new Intent(StartScreen.this,PlayOptions.class);
				startActivity(StartGameIntent);
			}
		});

		button[1] = (Button)findViewById(R.id.continuegame);
		button[1].setOnClickListener(new OnClickListener() {        	
			public void onClick(View v) {
				Intent ResumeGameIntent = new Intent(StartScreen.this,CuckooChess.class);
				Bundle b= new Bundle();
				b.putString("type","continue");
				ResumeGameIntent.putExtras(b);

				startActivity(ResumeGameIntent);
			}
		});

		button[2] = (Button)findViewById(R.id.instructions);
		button[2].setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent InstructionsIntent = new Intent(StartScreen.this,Instructions.class);
				startActivity(InstructionsIntent);
			}
		});

		button[3] = (Button)findViewById(R.id.settings);
		button[3].setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent OptionsIntent = new Intent(StartScreen.this,Preferences.class);
				startActivity(OptionsIntent);
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			//speak current
			decrement();
			tts.speak(arr[current]+"selected, Press the Power Button to Confirm", TextToSpeech.QUEUE_FLUSH, null);

		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			increment();
			tts.speak(arr[current]+"selected, Press the Power Button to Confirm", TextToSpeech.QUEUE_FLUSH, null);
		}
		else if (keyCode == KeyEvent.KEYCODE_POWER)
		{
			button[current].performClick();
		}

		return true;//super.onKeyDown(keyCode, event);
	}

	void increment()
	{
		current++;
		if(current>3)
			current=0;
	}

	void decrement()
	{
		current--;
		if(current<0)
			current=3;
	}

	@Override
	protected void onDestroy() {
		tts.shutdown();
		super.onDestroy();
	}


	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US); // Hard coded to US english
		tts.speak("Welcome to Chess Touch! Use the Volume Buttons to Cycle through the options", TextToSpeech.QUEUE_ADD, null);
	}

}
