/**
 * 
 */
package com.babelkey.estepais1.service;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;

import com.babelkey.estepais1.player.BabelPlayer;
import com.babelkey.estepais1.player.ChapterUI;
import com.babelkey.estepais1.player.CourseHelper;
import com.babelkey.estepais1.player.AudioHelper;
import com.babelkey.estepais1.player.R;
import com.babelkey.estepais1.player.SentenceUI;
import com.babelkey.estepais1.utils.PlayerState;

/**
 * @author Sapan
 */
public class BabelPlayerService extends Service implements OnCompletionListener, OnSharedPreferenceChangeListener {

	public static final String ACTION_PLAY = "PLAY";
	public static final String ACTION_TOGGLE = "TOGGLE";
	public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
	private AudioHelper audioHelper;
	private CourseHelper courseHelper;
    private int selectedChapter;
    
    private final Handler handler = new Handler(); 
    private Runnable pauseBeforeRunnable;
    private Runnable pauseAfterRunnable;
	private PlayerState playerState = PlayerState.stop;
	private Boolean repeatModeEnabled=false;
	private SharedPreferences sharedPrefs;

    private List<SentenceUI> sentenceUIList;
    private List<ChapterUI> chapterUIList;
	private int nextIndex = 0;
	private File tempFile;
	private int pauseBefore = 1;
	private int pauseAfter = 1;
    
    private PowerManager mgr;

	private	Builder builder;
	private Notification mNotification = null;
	private NotificationManagerCompat notificationManagerCompat;
	private PendingIntent activityPendingIntent;
	private PendingIntent playPendingIntent;
	private PendingIntent stopPendingIntent;
	private Intent activityIntent;
	private static final String NOTIFICATION_CHANNEL_ID = "NotificationChannelID";
	private static final String NOTIFICATION_CHANNEL_NAME = "BabelreaderNotificationChannel";
    public static final String BROADCAST_NEW_SENTENCE = "com.babelkey.estepais1.player.updatesentence";
    public static final String BROADCAST_NEW_CHAPTER = "com.babelkey.estepais1.player.updatechapter";
	public static final String BROADCAST_TOGGLE_STOP = "com.babelkey.estepais1.player.toggleplaybackstop";
	public static final String BROADCAST_TOGGLE_PLAY = "com.babelkey.estepais1.player.toggleplaybackplay";
	public static final String BROADCAST_AUDIO_NOT_FOUND = "com.babelkey.estepais1.player.audionotfound";
	Intent broadcastIntentSentence;
    Intent broadcastIntentChapter;
	Intent broadcastIntentToggleStop;
	Intent broadcastIntentTogglePlay;
	Intent broadcastIntentAudioNotFound;

	final int NOTIFICATION_ID = 1;
	private static BabelPlayerService sInstance = null;


	@SuppressLint("InvalidWakeLockTag")
	@Override
	public void onCreate() {
    	Log.e(getClass().getName(), "BabelPlayerService onCreate");
		sInstance = this;
		try{
        	tempFile = File.createTempFile("playingmedia", ".dat", getDir("playtemp", 0));
        }catch(Exception e){
        	Log.e(getClass().getName(), "unable to create temp file");       	
        }
		audioHelper = new AudioHelper(getApplicationContext(), tempFile);
        courseHelper = new CourseHelper(getApplicationContext());
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        chapterUIList = courseHelper.getChapterUIList();
        broadcastIntentSentence = new Intent(BROADCAST_NEW_SENTENCE);
        broadcastIntentChapter = new Intent(BROADCAST_NEW_CHAPTER);
		broadcastIntentToggleStop = new Intent(BROADCAST_TOGGLE_STOP);
		broadcastIntentTogglePlay = new Intent(BROADCAST_TOGGLE_PLAY);
		broadcastIntentAudioNotFound = new Intent(BROADCAST_AUDIO_NOT_FOUND);
		mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);

		builder = new Builder(this, NOTIFICATION_CHANNEL_ID);

		loadSelectedChapter();
        selectedChapter = 0;     
        
        pauseBeforeRunnable = new Runnable() {
        	public void run() {
				Log.e(getClass().getName(), "pauseBefore Timer Complete");
				if(playerState.equals(PlayerState.delayed)){
			    	playNextIndex();	
				}
			}
		};
        pauseAfterRunnable = new Runnable() {
        	public void run() {
				Log.e(getClass().getName(), "pauseAfter Timer Complete");
				if(!repeatModeEnabled){
			    	nextIndex++;								
				}
				if(playerState.equals(PlayerState.delayed)){
			        updateUISentence();	
			        handler.postDelayed(pauseBeforeRunnable, (pauseBefore*1000));
				}
			}
		};

        activityIntent = new Intent(this, BabelPlayer.class);
        activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE );
        playPendingIntent = PendingIntent.getBroadcast(this, 0, broadcastIntentTogglePlay, 0);
//        stopPendingIntent = PendingIntent.getBroadcast(this, 0, broadcastIntentToggleStop, 0);
		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
		bigTextStyle.bigText("Service started");
		builder.setStyle(bigTextStyle);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.drawable.babelicon);
		builder.setPriority(Notification.PRIORITY_DEFAULT);
		builder.setContentIntent(activityPendingIntent);
		builder.addAction(R.drawable.play, "play", playPendingIntent);
		mNotification = builder.build();

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			Log.e(getClass().getName(), "Android version above 26 found, creating notification channel");
			NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);

			startForeground(NOTIFICATION_ID, mNotification);
		} else {
			Log.e(getClass().getName(), "Android version below 26 found, startForeground without notification channel");
			startForeground(NOTIFICATION_ID, mNotification);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

/***********   Service handling methods ***************/

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.e(getClass().getName(), "BabelPlayerService onStartCommand()");
		if (intent.getAction().equals(ACTION_PLAY)) {
		}

		return START_STICKY;
	}

	void updateNotification(String text) {
		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
		bigTextStyle.bigText(text);
		builder.setStyle(bigTextStyle);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.drawable.babelicon);
		builder.setPriority(Notification.PRIORITY_DEFAULT);
		mNotification = builder.build();
		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
		notificationManagerCompat.notify(NOTIFICATION_ID, mNotification);
	}


    @Override
    public void onDestroy(){
		Log.e(getClass().getName(), "BabelPlayerService onDestroy");
    	playerState = PlayerState.exiting;
    	audioHelper.onDestroy();
    	handler.removeCallbacks(pauseBeforeRunnable);
    	handler.removeCallbacks(pauseAfterRunnable);
    	super.onDestroy();

    }

	public static BabelPlayerService getInstance() {
		return sInstance;
	}


	/***********   MediaPlayer Related methods ***************/	

	public void onCompletion(MediaPlayer mp) {
    	Log.e(getClass().getName(), "BabelPlayerService OnCompletionListener onCompletion fired!!!");
    	if(!playerState.equals(PlayerState.exiting)){
    		playerState = PlayerState.delayed;
    	}
        handler.postDelayed(pauseAfterRunnable, (pauseAfter*1000));

	}

    public boolean toggleRepeat(){
		repeatModeEnabled = !repeatModeEnabled;
		return repeatModeEnabled;
    }
    
    public boolean isRepeat(){
    	return repeatModeEnabled;
    }

	public MediaPlayer getMediaPlayer() {
		return audioHelper.getMp();
	}

	private void playNextIndex(){	
    	Log.e(getClass().getName(), "BabelPlayerService entering playNextIndex with nextIndex"+nextIndex);
		if(nextIndex <= (sentenceUIList.size()-1)){
			Log.e(getClass().getName(), "playing next index");
	    	String result = audioHelper.playSentence(((SentenceUI)sentenceUIList.get(nextIndex)).getSentenceID());
			if(result.equals("no more audio files in DB")){
				Log.e(getClass().getName(), result);
				sendBroadcast(broadcastIntentAudioNotFound);
			}
        }else{
			if(chapterUIList.size()>selectedChapter+1){
				this.playerState = PlayerState.stop;
				selectedChapter++;
				loadSelectedChapter();
				sendBroadcast(broadcastIntentTogglePlay);
			}
			else {
				this.playerState = PlayerState.stop;
				Log.e(getClass().getName(), "Playback complete, stopping!!!");
				sendBroadcast(broadcastIntentToggleStop);
	//			audioHelper.releaseMediaPlayer();
				nextIndex--;
			}
        }

	}
	
	public boolean togglePlayback(String toggleAction){
		// toggleAction : stop / pause / toggle / 
		Log.e(getClass().getName(), "BabelPlayerService togglePlayback()");
		try{
	    	if(toggleAction.equals("stop")){
	           	Log.e(getClass().getName(), "togglePlayback.equals(stop)");
	    		if(!playerState.equals(PlayerState.stop)){
		    		this.playerState = PlayerState.stop;
	    			this.updateNotification("stopped!");
//		    		audioHelper.getMp().stop();
					audioHelper.resetMediaPlayer();
	    		}
	    	}
	    	else if(toggleAction.equals("toggle")){
	           	Log.e(getClass().getName(), "togglePlayback.equals(toggle)");
	           	if(playerState.equals(PlayerState.stop) || playerState.equals(PlayerState.pause)){
	           		if(playerState.equals(PlayerState.pause)){
		               	Log.e(getClass().getName(), "player initialized, resuming playback");
		    			audioHelper.startMediaPlayer();
	           		}
	           		else{
		               	Log.e(getClass().getName(), "player NOT initialized, starting playback");
			           	playNextIndex();	           			
	           		}
	    			this.updateNotification("playing!");
					playerState = PlayerState.play;	           			
	           	}
	           	else if(playerState.equals(PlayerState.play) || playerState.equals(PlayerState.delayed)){
	           		if(audioHelper.isPlaying()){
		               	Log.e(getClass().getName(), "pausing player");
			    		playerState = PlayerState.pause;
		    			audioHelper.pauseMediaPlayer();
		    			this.updateNotification("paused!");
	           		}	    
	           		else{
		               	Log.e(getClass().getName(), "MediaPlayer already stopped, setting playerState to stop.");
			    		playerState = PlayerState.stop;	
		    			this.updateNotification("stopped!");
	           		}
	           	}
		
	       	}
	    	
		} catch(Exception e){
			Log.e(getClass().getName(), "togglePlayback, Exception caught");
			Log.e(getClass().getName(), "Exception: "+e);
		}
		return (playerState.equals(PlayerState.play));
	}

	/***********   UI Related methods ***************/	
	
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		//    	loadSelectedChapter();
	}
	
    public void loadSelectedChapter(){
    	Log.e(getClass().getName(), "Entering loadSelectedChapter() with selectedChapter: "+selectedChapter);
    	audioHelper.resetMediaPlayer();
    	courseHelper.createSentenceList(chapterUIList.get(selectedChapter));
	    sentenceUIList = courseHelper.getSentenceUIList();
		Log.e(getClass().getName(), "sentenceUIList loaded, size"+sentenceUIList.size());

		audioHelper.getMp().setOnCompletionListener((OnCompletionListener) this);
		nextIndex=0;
        updateUISentence();
        sendBroadcast(broadcastIntentChapter);
    }

    private void updateUISentence(){
    	Log.e(getClass().getName(), "Entering updateUI ");
    	sendBroadcast(broadcastIntentSentence);
    }
    
	public PlayerState getPlayerState(){
		return this.playerState;
	}
	
	public SentenceUI getCurrentSentence(){
		return sentenceUIList.get(nextIndex);
	}
	
	public List<ChapterUI> getChapterUIList(){
		return chapterUIList;
	}
	
	public List<SentenceUI> getSentenceUIList(){
		return sentenceUIList;
	}
	public int getIndex(){
		return nextIndex;
	}
	public void setIndex(int index){
		nextIndex = index;
	}
	
	public int getSentenceUIListSize(){
		return sentenceUIList.size();
	}
	
	public boolean next(){
		if(nextIndex < (sentenceUIList.size()-1)){
    		nextIndex++;	
    		return true;
		}
		else return false;
	}
	
	public boolean previous(){
		if(nextIndex > 0){
			nextIndex--;	
    		return true;
		}
		else return false;
	}

	public int getSelectedChapter(){
		return this.selectedChapter;
	}
	public void setSelectedChapter(int selection){ this.selectedChapter = selection;}

	public int getPauseBefore() {
		return pauseBefore;
	}

	public void setPauseBefore(int pauseBefore) {
		this.pauseBefore = pauseBefore;
	}

	public int getPauseAfter() {
		return pauseAfter;
	}

	public void setPauseAfter(int pauseAfter) {
		this.pauseAfter = pauseAfter;
	}

	
}
