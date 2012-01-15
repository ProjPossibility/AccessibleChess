/*
    CuckooChess - A java chess program.
    Copyright (C) 2011  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.petero.cuckoochess;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import guibase.ChessController;
import guibase.GUIInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import chess.ChessParseError;
import chess.Move;
import chess.Piece;
import chess.Position;
import chess.TextIO;
import chess.TextIO.readableForm;

import java.util.regex.*;

public class CuckooChess extends Activity implements GUIInterface, TextToSpeech.OnInitListener {
    ChessBoard cb;
    ChessController ctrl;
    Position pos;
    private TextToSpeech tts;
    boolean mShowThinking;
    int mTimeLimit;
    boolean playerWhite;
    static final int ttLogSize = 16; // Use 2^ttLogSize hash entries.
    
    TextView status;
    ScrollView moveListScroll;
    TextView moveList;
    TextView thinking;
    
    Vibrator vib;
    
    SharedPreferences settings;
    
    SmsManager sms;
    
	class SmsHandler extends Handler {
		   @Override  
		    public void handleMessage(Message message) {  
			    SmsMessage msg = (SmsMessage)message.obj;
			    String body = msg.getDisplayMessageBody();
			    
//			    Log.i("",msg.getDisplayMessageBody());
		    }  
	}

    private void readPrefs() {
        mShowThinking = settings.getBoolean("showThinking", false);
        String timeLimitStr = settings.getString("timeLimit", "5000");
        mTimeLimit = Integer.parseInt(timeLimitStr);
        playerWhite = settings.getBoolean("playerWhite", true);
        boolean boardFlipped = settings.getBoolean("boardFlipped", false);
        cb.setFlipped(boardFlipped);
        ctrl.setTimeLimit();
        String fontSizeStr = settings.getString("fontSize", "12");
        int fontSize = Integer.parseInt(fontSizeStr);
        status.setTextSize(fontSize);
        moveList.setTextSize(fontSize);
        thinking.setTextSize(fontSize);
        
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 1);
        pos = new Position();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                readPrefs();
                ctrl.setHumanWhite(playerWhite);
            }
        });
        
        sms = SmsManager.getDefault();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        SmsReceiver receiver = new SmsReceiver(new SmsHandler());
        this.registerReceiver(receiver, filter);
        
        setContentView(R.layout.main);
        status = (TextView)findViewById(R.id.status);
        moveListScroll = (ScrollView)findViewById(R.id.scrollView);
        moveList = (TextView)findViewById(R.id.moveList);
        thinking = (TextView)findViewById(R.id.thinking);
        cb = (ChessBoard)findViewById(R.id.chessboard);
        status.setFocusable(false);
        moveListScroll.setFocusable(false);
        moveList.setFocusable(false);
        thinking.setFocusable(false);
        ctrl = new ChessController(this, tts);
        ctrl.setThreadStackSize(32768);
        readPrefs();
        
        Typeface chessFont = Typeface.createFromAsset(getAssets(), "casefont.ttf");
        cb.setFont(chessFont);
        cb.setFocusable(true);
        cb.requestFocus();
        cb.setClickable(true);

        ctrl.newGame(playerWhite, false, false, ttLogSize, false);
        {
            String fen = "";
            String moves = "";
            String numUndo = "0";
            String tmp;
            if (savedInstanceState != null) {
                tmp = savedInstanceState.getString("startFEN");
                if (tmp != null) fen = tmp;
                tmp = savedInstanceState.getString("moves");
                if (tmp != null) moves = tmp;
                tmp = savedInstanceState.getString("numUndo");
                if (tmp != null) numUndo = tmp;
            } else {
                tmp = settings.getString("startFEN", null);
                if (tmp != null) fen = tmp;
                tmp = settings.getString("moves", null);
                if (tmp != null) moves = tmp;
                tmp = settings.getString("numUndo", null);
                if (tmp != null) numUndo = tmp;
            }
            List<String> posHistStr = new ArrayList<String>();
            posHistStr.add(fen);
            posHistStr.add(moves);
            posHistStr.add(numUndo);
            ctrl.setPosHistory(posHistStr);
        }
        ctrl.startGame();
        
        cb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	pos = new Position(ctrl.getGame().pos);
                if (ctrl.humansTurn() && (event.getAction() == MotionEvent.ACTION_UP)) 
                {                    	
                    int sq = cb.eventToSquare(event);
                    Move m = cb.mouseUp(sq);
                    if (m != null) {
                        Log.i("Helklo","Moved byaaa "+m.to);
                        if(ctrl.humanMove(m))
                        {
                        	vib.vibrate(500);
                        }
                        else
                        {
                        	cb.cancelMove();
                        }
                    }
                    return false;
                }
                else if (ctrl.humansTurn() && (event.getAction() == MotionEvent.ACTION_MOVE )) 
                {
                    int sq = cb.eventToSquare(event);
                    Move m = cb.mouseMoved(sq);
                    if(m!=null)
                    {
                    	if(m.selected)
                    	{
                    		if(ctrl.chkMove(m))
                    		{
                    			vib.vibrate(100);
                    			System.out.println("c");
                    	
                    			char sqX = (char) (pos.getX(sq) + 'a');
                            	char sqY = (char) (pos.getY(sq) + '1');
                            	 StringBuffer loca = new StringBuffer();
                            	loca.append(sqX);
                            	loca.append(sqY);

                        		tts.speak("Move to "+loca.toString(), TextToSpeech.QUEUE_FLUSH, null);

                    			// moving
                    		}
                    		else
                    		{
                        		tts.speak("Invalid Move", TextToSpeech.QUEUE_FLUSH, null);
                    			
                    		}
                    	}	
                    	else {
                        	char sqX = (char) (pos.getX(sq) + 'a');
                        	char sqY = (char) (pos.getY(sq) + '1');
                        	StringBuffer loca = new StringBuffer();
                        	loca.append(sqX);
                        	loca.append(sqY);

                    		tts.speak(loca.toString()+TextIO.pieceToName(pos.getPiece(sq)), TextToSpeech.QUEUE_FLUSH, null);
                    		vib.vibrate(100);
                    		Log.i("kjrlkjrkl","sftr svtopm movr");
                    		// what's there
                    	}
                    }
                    else
                    {
//                    	char sqX = (char) (pos.getX(sq) + 'a');
//                    	char sqY = (char) (pos.getY(sq) + '1');
//                    	StringBuffer loca = new StringBuffer();
//                    	loca.append(sqX);
//                    	loca.append(sqY);
//                    	
//                		tts.speak(loca.toString(), TextToSpeech.QUEUE_FLUSH, null);

                    }
                    //Log.i("Helklo","Moved by "+sq);
//                    if (m != null) {
//                        ctrl.humanMove(m);
//                    }
                    return false;
                }
                else if (ctrl.humansTurn() &&(event.getAction() == MotionEvent.ACTION_DOWN))
                {
                    int sq = cb.eventToSquare(event);
                    int m = cb.mouseDown(sq);
                    Log.i("hahaha","m:"+m);
                    if(m==1)
                    {
                    	vib.vibrate(100);
                    	System.out.println("e");
                		tts.speak(TextIO.pieceToName(pos.getPiece(sq)), TextToSpeech.QUEUE_FLUSH, null);
                    	// piece is selected
                    }
                    else if(m==2)
                    {
                    	char sqX = (char) (pos.getX(sq) + 'a');
                    	char sqY = (char) (pos.getY(sq) + '1');
                    	StringBuffer loca = new StringBuffer();
                    	loca.append(sqX);
                    	loca.append(sqY);

                		tts.speak(loca.toString() + " " + TextIO.pieceToName(pos.getPiece(sq)) + " selected", TextToSpeech.QUEUE_FLUSH, null);
                    	vib.vibrate(500);
//                    	try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
                    	vib.vibrate(100);
                    }
                                 	
                    //Log.i("Helklo","Moved by "+sq);
//                    if (m != null) {
//                        ctrl.humanMove(m);
//                    }
                    return false;
                }
                return false;
            }
        });
        
        cb.setOnTrackballListener(new ChessBoard.OnTrackballListener() {
            public void onTrackballEvent(MotionEvent event) {
                if (ctrl.humansTurn()) {
                    Move m = cb.handleTrackballEvent(event);
                    if (m != null) {
                        ctrl.humanMove(m);
                    }
                }
            }
        });
//        cb.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (!ctrl.computerThinking())
//                    showDialog(CLIPBOARD_DIALOG);
//                return true;
//            }
//        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<String> posHistStr = ctrl.getPosHistory();
        outState.putString("startFEN", posHistStr.get(0));
        outState.putString("moves", posHistStr.get(1));
        outState.putString("numUndo", posHistStr.get(2));
    }
    
    @Override
    protected void onPause() {
        List<String> posHistStr = ctrl.getPosHistory();
        Editor editor = settings.edit();
        editor.putString("startFEN", posHistStr.get(0));
        editor.putString("moves", posHistStr.get(1));
        editor.putString("numUndo", posHistStr.get(2));
        editor.commit();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ctrl.stopComputerThinking();
        tts.shutdown();
        super.onDestroy();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) 
		{

		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) 
		{

		}
		return super.onKeyDown(keyCode, event);
	}

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.item_new_game:
            ctrl.newGame(playerWhite, false, false, ttLogSize, false);
            ctrl.startGame();
            return true;
        case R.id.item_undo:
            ctrl.takeBackMove();
            return true;
        case R.id.item_redo:
            ctrl.redoMove();
            return true;
        case R.id.item_settings:
        {
            Intent i = new Intent(CuckooChess.this, Preferences.class);
            startActivityForResult(i, 0);
            return true;
        }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            readPrefs();
            ctrl.setHumanWhite(playerWhite);
        }
        //TODO
        if (requestCode == 1) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
                tts.setSpeechRate(10);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    // FIXME!!! Implement "edit board"
    // FIXME!!! Implement analysis mode

    @Override
    public void setPosition(Position pos) {
        cb.setPosition(pos);
        ctrl.setHumanWhite(playerWhite);
    }

    @Override
    public void setSelection(int sq) {
        cb.setSelection(sq);
    }

    @Override
    public void setStatusString(String str) {
        status.setText(str);
    }

    @Override
    public void setMoveListString(String str) {
        moveList.setText(str);
        moveListScroll.fullScroll(ScrollView.FOCUS_DOWN);
    }
    
    @Override
    public void setThinkingString(String str) {
        thinking.setText(str);
    }

    @Override
    public int timeLimit() {
        return mTimeLimit;
    }

    @Override
    public boolean randomMode() {
        return mTimeLimit == -1;
    }

    @Override
    public boolean showThinking() {
        return mShowThinking;
    }

    static final int PROMOTE_DIALOG = 0; 
    static final int CLIPBOARD_DIALOG = 1; 
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case PROMOTE_DIALOG: {
            final CharSequence[] items = {"Queen", "Rook", "Bishop", "Knight"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Promote pawn to?");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ctrl.reportPromotePiece(item);
                }
            });
            AlertDialog alert = builder.create();
            return alert;
        }
        case CLIPBOARD_DIALOG: {
            final CharSequence[] items = {"Copy Game", "Copy Position", "Paste"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Clipboard");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                    case 0: {
                        String pgn = ctrl.getPGN();
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(pgn);
                        break;
                    }
                    case 1: {
                        String fen = ctrl.getFEN() + "\n";
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(fen);
                        break;
                    }
                    case 2: {
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        if (clipboard.hasText()) {
                            String fenPgn = clipboard.getText().toString();
                            try {
                                ctrl.setFENOrPGN(fenPgn);
                            } catch (ChessParseError e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    }
                }
            });
            AlertDialog alert = builder.create();
            return alert;
        }
        }
        return null;
    }

    @Override
    public void requestPromotePiece() {
        runOnUIThread(new Runnable() {
            public void run() {
                showDialog(PROMOTE_DIALOG);
            }
        });
    }

    @Override
    public void reportInvalidMove(Move m) {
        String msg = String.format("Invalid move %s-%s", TextIO.squareToString(m.from), TextIO.squareToString(m.to));
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void runOnUIThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override
    public void onInit(int status) {
    	ctrl.setTTS(tts);
        tts.setLanguage(Locale.US); // Hard coded to US english
        tts.setSpeechRate((float)1.5);
        String hello = "Let's play chess!";
        tts.speak(hello, TextToSpeech.QUEUE_ADD, null);
    }
}
