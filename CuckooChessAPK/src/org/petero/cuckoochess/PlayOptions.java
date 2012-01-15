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

public class PlayOptions extends Activity implements TextToSpeech.OnInitListener{


	Intent intent;
	private TextToSpeech tts;

	String arr[]={"Play Versus Computer","Play a Local Multiplayer Game", "Play a Multiplayer Game over sms"};
	Button button[]=new Button[3];
	int current =2;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);

		
		tts = new TextToSpeech(this, this);
		tts.setLanguage(Locale.US); // Hard coded to US english
		
		
		button[0] = (Button)findViewById(R.id.VScomputer);
		button[0].setOnClickListener(new OnClickListener() {        	
			public void onClick(View v) {
				Intent StartGameIntent = new Intent(PlayOptions.this,CuckooChess.class);
				Bundle b= new Bundle();
				b.putString("type","computer");
				StartGameIntent.putExtras(b);
				startActivity(StartGameIntent);
			}
		});

		button[1] = (Button)findViewById(R.id.LM);
		button[1].setOnClickListener(new OnClickListener() {        	
			public void onClick(View v) {
				Intent ResumeGameIntent = new Intent(PlayOptions.this,CuckooChess.class);
				Bundle b= new Bundle();
				b.putString("type","lm");
				ResumeGameIntent.putExtras(b);

				startActivity(ResumeGameIntent);
			}
		});

		button[2] = (Button)findViewById(R.id.SMS);
		button[2].setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent InstructionsIntent = new Intent(PlayOptions.this,CuckooChess.class);
				Bundle b= new Bundle();
				b.putString("type","sms");
				InstructionsIntent.putExtras(b);
				
				startActivity(InstructionsIntent);
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
		if(current>2)
			current=0;
	}

	void decrement()
	{
		current--;
		if(current<0)
			current=2;
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
