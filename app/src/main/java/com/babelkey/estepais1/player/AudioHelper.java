package com.babelkey.estepais1.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.PowerManager;
import android.util.Log;

public class AudioHelper implements OnErrorListener {
	private DBHelper dbHelper;
	private Cursor audioCursor;
	private MediaPlayer mp;
	private boolean isPlayerInitialized = false;
	private File tempFile;
	
	public AudioHelper(Context context, File tempFile){
    	Log.e(getClass().getName(), "Entering AudioHelper constructor()");
/********* Prepare DataBase Helper ********/
    	this.tempFile = tempFile;
	    dbHelper = new DBHelper(context);
	    initDB();
	    mp = new MediaPlayer();
	    mp.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
	}
	

    public String playSentence(int id){
    	Log.e(getClass().getName(), "playSentence: "+ id);
    	byte[] audio = null;
    	File bufferedFile;
 	   	audioCursor = dbHelper.fetchSentenceAudio(id);
 	   	audioCursor.moveToFirst();
        if(audioCursor.isAfterLast() == false){
        	audio = audioCursor.getBlob(0);
            try{

 //        	   bufferedFile = File.createTempFile("playingMedia", ".dat");
 //        	   FileOutputStream out2 = getBaseContext().openFileOutput("")
            	
            	FileOutputStream out = new FileOutputStream(tempFile);
        	       out.write(audio, 0, audio.length);
        	       out.flush();
        	       out.close();
            }catch (Exception e){
     	 		throw new Error("Unable to write Blob to FileOutputStream ", e);    	   
            }
        }
        else{
	    	Log.e(getClass().getName(), "Playback complete, stopping!!!");
        	return "no more audio files in DB";
        }
        audioCursor.deactivate();
    	try {
    		setDataSource(tempFile);
       	    mp.prepare();
       	    isPlayerInitialized=true;
    	    mp.start();
    	} catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer. ", e);
        	return "could not initialize the MediaPlayer";
    	}
    	return "playback started";	
    }
	    
    private void setDataSource(File mediaFile) throws IOException {
    	Log.e(getClass().getName(), "Entering setDataSource()");
    	mp.reset();
		FileInputStream fis = new FileInputStream(mediaFile);
		mp.setDataSource(fis.getFD());
    }

    private void initDB(){
    	Log.e(getClass().getName(), "Entering initDB()");
        try {	        	 
        	dbHelper.createDataBase();	 
	 	} catch (IOException ioe) {		 
	 		throw new Error("Unable to create database "+ioe);		 
	 	}		 	
	 	try {		 		 
	 		dbHelper.openDataBase();		 
	 	}catch(SQLException sqle){		 
	 		throw sqle;		 
	 	}		 	
    }

    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
    	Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
    	if (mediaPlayer != null) {
    		mediaPlayer.stop();
    	    mediaPlayer.release();
    	}
    	return false;
	}

    public void onDestroy(){
		Log.e(getClass().getName(), "AudioHelper onDestroy");
    	dbHelper.close();
    	
    }


	public MediaPlayer getMp() {
		return mp;
	}

	public void setMp(MediaPlayer mp) {
		this.mp = mp;
	}
	
	public void releaseMediaPlayer(){
		if(mp.isPlaying()){
			this.mp.stop();
		}
	    this.mp.release();
	}

	public boolean isPlayerInitialized() {
		return isPlayerInitialized;
	}
	
	public boolean isPlaying(){
		return mp.isPlaying();
	}

	public void resetMediaPlayer() {
		mp.reset();
		isPlayerInitialized=false;
	}
	
	public void stopMediaPlayer(){
		mp.stop();
		isPlayerInitialized=false;		
	}
	
	public void startMediaPlayer(){
		mp.start();
	}
	
	public void pauseMediaPlayer(){
		mp.pause();
	}
}
