package com.babelkey.estepais1.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import com.babelkey.estepais1.utils.PlayerState;
import com.babelkey.estepais1.service.BabelPlayerService;

public class BabelPlayer extends Activity implements OnSharedPreferenceChangeListener {
	
    private ImageButton playButton;
//    private ImageButton previousButton;
//    private ImageButton nextButton;
    private ImageButton repeatButton;  
    private ImageButton hideOriginalButton;
    private ImageButton hideTranslationButton;
//    private Button chapterMenuButton;
    private TextView sentenceCounter;
    private SeekBar sentenceSelector;
    
    private boolean isLargeScreen = false;
    private BabelApplication babelApplication;
    
    private int dialogSelection;
//    private SentenceViewAdapter sentenceAdapter;
    private Context context;
	private Boolean hideTranslation=false;
	private Boolean hideOriginal=false;
	private ViewSwitcher switcher;
	private LayoutInflater inflater;
	private Drawable playDrawable;
	private Drawable pauseDrawable;
	private SharedPreferences sharedPrefs;
	
	int lastAction = 0;
    private ScrollView sentenceViewHolder;
    private SentenceView sentenceView;
    private Intent broadcastIntent;
    private Animation animatePrevious;
    private Animation animateNext;
    
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;   

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.e(getClass().getName(), "BabelPlayer onCreate");
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        babelApplication = (BabelApplication)getApplication();

        setViewMode(context);
    	this.setContentView(R.layout.babelapp_layout);

//    	this.setContentView(R.layout.babelapp_layout);
 //   	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	switcher = (ViewSwitcher) findViewById(R.id.screenSwitcher);
        switcher.showNext();   	
        new showStartUpScreen().execute();
	    sentenceViewHolder = (ScrollView) findViewById(R.id.sentenceViewHolder);
//        audioHelper = new AudioHelper(this);    
    	Log.e(getClass().getName(), "BabelPlayer 2");
        broadcastIntent = new Intent(this, BabelPlayerService.class);
        
    	playButton = (ImageButton) findViewById(R.id.playbutton);
    	repeatButton =  (ImageButton) findViewById(R.id.repeatbutton);
		repeatButton.setBackgroundResource(android.R.drawable.btn_default);
//    	previousButton = (ImageButton) findViewById(R.id.previousbutton);
//    	nextButton = (ImageButton) findViewById(R.id.nextbutton);
//    	chapterMenuButton = (Button) findViewById(R.id.chaptermenubutton);
    	sentenceSelector = (SeekBar) findViewById(R.id.sentenceselector);
    	
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        playDrawable = getResources().getDrawable(R.drawable.play);
        pauseDrawable = getResources().getDrawable(R.drawable.pause);
        animatePrevious = AnimationUtils.loadAnimation(this, R.anim.in_animation);
        animateNext = AnimationUtils.loadAnimation(this, R.anim.out_animation);
        gestureDetector = new GestureDetector(new MyGestureDetector());

        hideOriginalButton = (ImageButton) findViewById(R.id.hideoriginalbutton);
        hideTranslationButton = (ImageButton) findViewById(R.id.hidetranslationbutton);
        sentenceCounter = (TextView) findViewById(R.id.sentencecounter);
       
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        switcher.showNext();
        
        sentenceViewHolder.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event))
					return true;
				else
					return false;
			}
		});
        
        playButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		togglePlayback("toggle");
        	}
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		toggleRepeat();
        	}
        });
        hideOriginalButton.setBackgroundColor(Color.DKGRAY);
        hideOriginalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.e(getClass().getName(), "hideOriginalButton pressed");
            	hideOriginal = !hideOriginal;
            	updateViewedSentence();
            }
        });
        hideTranslationButton.setBackgroundColor(Color.DKGRAY);
        hideTranslationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.e(getClass().getName(), "hideTranslationButton pressed");
            	hideTranslation = !hideTranslation;
            	updateViewedSentence();
            }
        });
        animatePrevious.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
            	Log.e(getClass().getName(), "animatePrevious onAnimationEnd FIRED!"	);	
            	updateViewedSentence();		
			}
			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {}
        });
        animateNext.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
            	Log.e(getClass().getName(), "animateNext onAnimationEnd FIRED!"	);	
			}
			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {}
        });

		Intent serviceIntent = new Intent(context, BabelPlayerService.class);
		serviceIntent.setAction(BabelPlayerService.ACTION_PLAY);
		ContextCompat.startForegroundService(context, serviceIntent);
	//	startService(serviceIntent);

		try{
        	sentenceSelector.setMax((BabelPlayerService.getInstance().getSentenceUIListSize()));
            sentenceSelector.setProgress(BabelPlayerService.getInstance().getIndex());	
            if(BabelPlayerService.getInstance().getPlayerState().equals(PlayerState.play)){
	    		playButton.setImageDrawable(pauseDrawable); 
				playButton.setBackgroundColor(Color.DKGRAY);
            } else if (BabelPlayerService.getInstance().getPlayerState().equals(PlayerState.pause)){
            	playButton.setImageDrawable(playDrawable);
            	playButton.setBackgroundColor(Color.DKGRAY);
            }
            if(BabelPlayerService.getInstance().isRepeat()){
            	repeatButton.setBackgroundColor(Color.DKGRAY);
				Log.e(getClass().getName(), "onCreate isRepeat = true");

			} else{
				Log.e(getClass().getName(), "onCreate isRepeat = false");
				repeatButton.setBackgroundResource(android.R.drawable.btn_default);
			}
		}catch(Exception e){
	    	Log.e(getClass().getName(), "Unable to get BabelPlayerService instance");
	        sentenceSelector.setMax(100);
	        sentenceSelector.setProgress(1);
		}
        sentenceSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	public void onStopTrackingTouch(SeekBar arg0) {}
	        public void onStartTrackingTouch(SeekBar arg0) {}
	        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	        	BabelPlayerService.getInstance().setIndex(arg1);            		
	    		updateViewedSentence();    	
	        }
	    });
    }
    
    public boolean isLargeScreen(Context context) {
    	return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    
    public void setViewMode(Context context){
        isLargeScreen = isLargeScreen(context);
		if (isLargeScreen){
	    	Log.e(getClass().getName(), "BabelPlayer isLargeScreen = true");
	    	 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    	 babelApplication.setLargeScreen(true);
	    	 babelApplication.setTextSizeDefault(35);
	    	 babelApplication.setTextSizeMax(60);
		}
		else{
	    	Log.e(getClass().getName(), "BabelPlayer isLargeScreen = false");
	    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
	    	 babelApplication.setLargeScreen(false);
	    	 babelApplication.setTextSizeDefault(23);
	    	 babelApplication.setTextSizeMax(35);
		}

    }

	/****** BabelPlayer Core Functions  *******/
    
    private BroadcastReceiver sentenceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	updateViewedSentence();       
        }
    };   
    
    private BroadcastReceiver chapterUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	sentenceSelector.setMax((BabelPlayerService.getInstance().getSentenceUIListSize()));
            sentenceSelector.setProgress(BabelPlayerService.getInstance().getIndex());      
        }
    };

	private BroadcastReceiver toggleStopBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(getClass().getName(), "stop Broadcast Received");
			togglePlayback("stop");
		}
	};

	private BroadcastReceiver togglePlaybackBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(getClass().getName(), "toggle Broadcast Received");
			togglePlayback("toggle");
		}
	};

	private BroadcastReceiver audioNotFoundBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(getClass().getName(), "audioNotFound Broadcast Received");
			togglePlayback("stop");
			createEOFDialog();
		}
	};
    
    public void updateViewedSentence(){
    	try{
	    sentenceView = new SentenceView(context, BabelPlayerService.getInstance().getCurrentSentence(), inflater, hideOriginal, hideTranslation);
	    sentenceViewHolder.removeAllViews();
	    sentenceViewHolder.addView(sentenceView);
	    sentenceCounter.setText((BabelPlayerService.getInstance().getIndex()+1)+" / "+(BabelPlayerService.getInstance().getSentenceUIListSize()));
	    sentenceSelector.setProgress(BabelPlayerService.getInstance().getIndex());
		} catch(Exception e){
			Log.e(getClass().getName(), "updateViewedSentence, Exception caught");
		}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
		Log.e(getClass().getName(), "BabelPlayer onConfigurationChanged");  
        setViewMode(context);
    }
    
    @Override
	protected void onDestroy() {
		Log.e(getClass().getName(), "BabelPlayer onDestroy");
		BabelPlayerService.getInstance().onDestroy();
		new Thread(new Runnable() {
			public void run() {
				stopService(new Intent("PLAY"));
			}
		}).start();

		super.onDestroy();
		this.finishActivity(1);
		finish();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e(getClass().getName(), "BabelPlayer ON RESUME!!!");
		try{
			BabelPlayerService.getInstance().setPauseBefore(sharedPrefs.getInt("pauseBeforeSentences", 2));
			BabelPlayerService.getInstance().setPauseAfter(sharedPrefs.getInt("pauseAfterSentences", 2));
		}catch(Exception e){
			Log.e(getClass().getName(), "Unable to get BabelPlayerService instance onResume");
		}
		super.onResume();
		updateViewedSentence();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
        registerReceiver(sentenceUpdateBroadcastReceiver, new IntentFilter(BabelPlayerService.BROADCAST_NEW_SENTENCE));
		registerReceiver(chapterUpdateBroadcastReceiver, new IntentFilter(BabelPlayerService.BROADCAST_NEW_CHAPTER));
		registerReceiver(toggleStopBroadcastReceiver, new IntentFilter(BabelPlayerService.BROADCAST_TOGGLE_STOP));
		registerReceiver(togglePlaybackBroadcastReceiver, new IntentFilter(BabelPlayerService.BROADCAST_TOGGLE_PLAY));
		registerReceiver(audioNotFoundBroadcastReceiver, new IntentFilter(BabelPlayerService.BROADCAST_AUDIO_NOT_FOUND));
		Log.e(getClass().getName(), "BabelPlayer ON START!!!");

		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.e(getClass().getName(), "BabelPlayer ON STOP!!!");
		// TODO Auto-generated method stub
        unregisterReceiver(sentenceUpdateBroadcastReceiver);
		unregisterReceiver(chapterUpdateBroadcastReceiver);
		unregisterReceiver(toggleStopBroadcastReceiver);
		unregisterReceiver(togglePlaybackBroadcastReceiver);
		unregisterReceiver(audioNotFoundBroadcastReceiver);
		BabelPlayerService.getInstance().setPauseBefore(1);
        BabelPlayerService.getInstance().setPauseAfter(1);
		super.onStop();
	}

	@Override
	protected void onPause() {
		Log.e(getClass().getName(), "BabelPlayer ON PAUSE!!!");
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	public void onBackPressed() {
		Log.e(getClass().getName(), "BabelPlayer onBackPressed");

		// TODO Auto-generated method stub
		super.onBackPressed();
		onDestroy();
		
	}



	private class showStartUpScreen extends AsyncTask {
		@Override /* Background Task is Done */
		protected void onPostExecute(Object result) {
			
			switcher.showPrevious();
		}
		@Override
		protected Object doInBackground(Object... params) {
	    	Log.e(getClass().getName(), "Entering showStartUpScreen ");
			try {
				Thread.sleep(3000); //only to demonstrate
				publishProgress(1);
				Thread.sleep(6000); //only to demonstrate
				publishProgress(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	Log.e(getClass().getName(), "Returning from showStartUpScreen ");
			return null;
		}

		
		 @Override
		protected void onProgressUpdate(Object... values) {
			int value =  (Integer)values[0];
			Log.e(getClass().getName(), "onProgressUpdate value: "+value);
			if(value == 1){
				ImageView loadingImage = (ImageView) findViewById(R.id.loadingImage);
/*				Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.alpha_fade_out);
				loadingImage.startAnimation(fadeOut);
*/
				RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
				relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				LinearLayout loadingTextLayer= (LinearLayout) inflater.inflate(R.layout.loadingtext_layer, null, true);
				RelativeLayout loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
				loadingScreen.addView(loadingTextLayer, relativeLayoutParams);
				loadingImage.setAlpha(100);
				
			}
			else if (value == 2){
//				loadingScreen.bringChildToFront(loadingTextLayer);				
			}
			/*			TextView titleText = new TextView(context);
			titleText.setText("BABELKEY PLAYER");    
			titleText.setTextSize(new Float(25));
			titleText.setTextColor(Color.parseColor("#00ccff"));
			loadingView.addView(titleText, relativeLayoutParams);
			loadingView.bringChildToFront(titleText);
*/
		}	
		
		
	}
    
    /****** Handle Touch events  *******/
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
            int action = ev.getAction();
/*            if (!editModeEnabled) {
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                    	return super.dispatchTouchEvent(ev);
                    }else{
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return true;
                    }
            }
*/
            return super.dispatchTouchEvent(ev);
    }
    
    private void toggleRepeat(){
		boolean repeatModeEnabled = BabelPlayerService.getInstance().toggleRepeat();
		if(repeatModeEnabled){ repeatButton.setBackgroundColor(Color.DKGRAY); }
		else{ repeatButton.setBackgroundResource(android.R.drawable.btn_default); };
    }
    
    private void togglePlayback(String toggle){
    	
    	if(toggle.equals("toggle")){
			Log.e(getClass().getName(), "BabelPlayer togglePlayback with string: "+toggle);
            BabelPlayerService.getInstance().setPauseBefore(sharedPrefs.getInt("pauseBeforeSentences", 2));
            BabelPlayerService.getInstance().setPauseAfter(sharedPrefs.getInt("pauseAfterSentences", 2));
	    	Boolean isPlaying = BabelPlayerService.getInstance().togglePlayback(toggle);
	    	if(isPlaying){
				Log.e(getClass().getName(), "Trying to set pauseDrawable");
	    		playButton.setImageDrawable(pauseDrawable); 
				playButton.setBackgroundColor(Color.DKGRAY);
	    	}else{
	        	playButton.setImageDrawable(playDrawable);
				playButton.setBackgroundResource(android.R.drawable.btn_default);
	    	}

    	}
    	else if (toggle.equals("stop")){
			Log.e(getClass().getName(), "BabelPlayer togglePlayback with string: "+toggle);
			Boolean isPlaying = BabelPlayerService.getInstance().togglePlayback(toggle);
    		playButton.setBackgroundResource(android.R.drawable.btn_default);
    		playButton.setImageDrawable(playDrawable); 
    	}
    	
    }
 
    class MyGestureDetector extends SimpleOnGestureListener {
    	@Override
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	// int delta = 0;
	    	if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	    	return false;
	    	else{
		    	try { 
		    	// right to left swipe
			    	if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			        	Log.e(getClass().getName(), "nextButton pressed");
			        	boolean isNext = BabelPlayerService.getInstance().next();
			        	if(isNext){      
			            	togglePlayback("stop");
			            	updateViewedSentence();		
			        		sentenceViewHolder.startAnimation(animateNext);
			        	}
			    	//left to right swipe
			    	} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			        	Log.e(getClass().getName(), "previousButton pressed");
			        	boolean isPrevious = BabelPlayerService.getInstance().previous();
			        	if(isPrevious){      
			            	togglePlayback("stop");
			        		sentenceViewHolder.startAnimation(animatePrevious);
			        		            		
			        	}
			
			    	} 
		    	} catch (Exception e) {
			    	Log.e(getClass().getName(), "Exception in SWIPE ");
		    	}
	    	return true;
	    	}
    	} 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
	    switch (item.getItemId()) {
	       case 1: {
		    	Log.e(getClass().getName(), "MenuItem Settings selected ");
		    	this.startActivityForResult(new Intent(this, BabelPreferenceActivity.class), 1);
	    	   break;
	       }
	       case 2: {
		    	Log.e(getClass().getName(), "MenuItem Help selected ");
		    	createHelpDialog();
	   	   break;
	      }
	       case 3: {
		    	Log.e(getClass().getName(), "MenuItem Feedback selected ");
		    	createFeedbackDialog();
	   	   break;
	      }
	       case 4: {
		    	Log.e(getClass().getName(), "MenuItem About selected ");
		    	createAboutDialog();
	   	   break;
	      }
	       case 5: {
		    	Log.e(getClass().getName(), "MenuItem Chapter selection selected ");
            	togglePlayback("pause");
                createChapterSelectionDialog();
	   	   break;
	      }
	    }
	    return true;
    }
    
    /****** Create Menus and Dialogs  *******/


	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		updateViewedSentence();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    super.onCreateOptionsMenu(menu);
    togglePlayback("stop");

    menu.add(1, 1, 0, R.string.menu_settings)
    .setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(1, 2, 0, R.string.menu_help)
    .setIcon(android.R.drawable.ic_menu_help);
    menu.add(1, 3, 0, R.string.menu_feedback)
    .setIcon(android.R.drawable.ic_menu_send);
    menu.add(1, 4, 0, R.string.menu_about)
    .setIcon(android.R.drawable.ic_menu_info_details);
    menu.add(1, 5, 0, R.string.menu_chapters)
    .setIcon(android.R.drawable.ic_menu_agenda);
    
    return true;
    }
    
    private void createChapterSelectionDialog(){
    	AlertDialog chapterSelectionDialog;
    	List<String> strings = new ArrayList<String>();
		for(ChapterUI tempChapterUI : BabelPlayerService.getInstance().getChapterUIList()){
			strings.add(tempChapterUI.getDescription());
		}   	
  
    	final CharSequence[] items = strings.toArray(new String[strings.size()]);

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select Chapter");
    	builder.setSingleChoiceItems(items, BabelPlayerService.getInstance().getSelectedChapter(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Log.e(getClass().getName(), "singleChoiseItem selected: " + items[item] + " index: " + item);
				dialogSelection = item;

			}
		});
    	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				togglePlayback("stop");
				BabelPlayerService.getInstance().setSelectedChapter(dialogSelection);
				BabelPlayerService.getInstance().loadSelectedChapter();
			}
		});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
			}
		});
    	
    	chapterSelectionDialog = builder.create();
    	chapterSelectionDialog.show();
    }

	private void createEOFDialog(){
		Log.e(getClass().getName(), "create EOF dialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog eofDialog;

		builder.setTitle("Exception: Audio file for sentence not found!");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
			}
		});

		builder.setIcon(R.drawable.babelicon);


		eofDialog = builder.create();
		eofDialog.show();
	}

    private void createHelpDialog(){
		Log.e(getClass().getName(), "create help dialog");
		AlertDialog.Builder builder;
		AlertDialog helpDialog;
    	View helpDialogLayout = inflater.inflate(R.layout.help_dialog_root,(ViewGroup) findViewById(R.id.helpdialog_root));
	
    	builder = new AlertDialog.Builder(this);
    	builder.setView(helpDialogLayout);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		  public void onClick(DialogInterface dialog, int item) {
    		    dialog.dismiss();
    		  }
    		});

		builder.setTitle("Welcome to Babelkey Player!");
		builder.setIcon(R.drawable.babelicon);
		

    	helpDialog = builder.create();
    	helpDialog.show();
    }
    
    private void createAboutDialog(){
		Log.e(getClass().getName(), "create about dialog");
		AlertDialog.Builder builder;
		AlertDialog helpDialog;
    	View helpDialogLayout = inflater.inflate(R.layout.about_dialog,(ViewGroup) findViewById(R.id.about_dialog));
	
    	builder = new AlertDialog.Builder(this);
    	builder.setView(helpDialogLayout);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		  public void onClick(DialogInterface dialog, int item) {
    		    dialog.dismiss();
    		  }
    		});

		builder.setTitle("About Babelkey");
		builder.setIcon(R.drawable.babelicon);
		

    	helpDialog = builder.create();
    	helpDialog.show();
    }

    private void createFeedbackDialog(){
		Log.e(getClass().getName(), "create Feedback dialog");  
		final Dialog feedbackDialog = new Dialog(this);
		feedbackDialog.setContentView(R.layout.feedback_layout);
		feedbackDialog.setTitle(R.string.feedback_title);

		final EditText nameField = (EditText) feedbackDialog.findViewById(R.id.EditTextName);
		final EditText emailField = (EditText) feedbackDialog.findViewById(R.id.EditTextEmail);
		final EditText bodyField = (EditText) feedbackDialog.findViewById(R.id.EditTextFeedbackBody);
		final Spinner feedbackSpinner = (Spinner) feedbackDialog.findViewById(R.id.SpinnerFeedbackType);
		final CheckBox responseCheckbox = (CheckBox) feedbackDialog.findViewById(R.id.CheckBoxResponse);
		Button sendFeedback = (Button)feedbackDialog.findViewById(R.id.ButtonSendFeedback);
		sendFeedback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.e(getClass().getName(), "Feedback SEND button pressed"); 
            	PackageInfo pinfo;
            	int versionNumber = 0;
            	String versionName = "null";
				try {
					pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
					versionNumber = pinfo.versionCode;
					versionName = pinfo.versionName;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		String name = nameField.getText().toString();
        		String email = emailField.getText().toString();
        		String body = bodyField.getText().toString()
        			+ "\n\n\n\n"
        			+ "Player version number: " + versionNumber + "\n" 
        			+ "Player version name: " + versionName + "\n"
        			+ "Course name: " + getResources().getString(R.string.course_name);
        		String feedbackType = feedbackSpinner.getSelectedItem().toString();
        		boolean bRequiresResponse = responseCheckbox.isChecked();
        		String subject = feedbackType+" from Babelkey user" + ((bRequiresResponse) ? ", response requested" : "");

            	feedbackDialog.dismiss();
            	
            	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);            	
            	emailIntent.setType("plain/text");
            	String aEmailList[] = { "feedback@babelkey.com" };
//            	String aEmailCCList[] = { "user3@fakehost.com","user4@fakehost.com"};
            	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
            	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            	
            	startActivity(Intent.createChooser(emailIntent, "Send your email in:"));
            }
        });
		
		feedbackDialog.show();
    }

}