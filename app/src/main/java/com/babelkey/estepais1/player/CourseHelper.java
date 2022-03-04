package com.babelkey.estepais1.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class CourseHelper {
	private List<SentenceUI> sentenceUIList;
	private List<ChapterUI> chapterUIList;
	private SentenceUI sentenceUI = null;
	private List<String> originalWordsList = null;
	private List<String> translatedWordsList = null;
	private DBHelper dbHelper;

	public CourseHelper(Context context) {
	    dbHelper = new DBHelper(context);
	    initDB();
	    
	    /********* Build chapter list ********/
	    Cursor chapterCursor = dbHelper.fetchChapters();
	    chapterCursor.moveToFirst();
	    int chapterCursorSize = chapterCursor.getCount();
	    chapterUIList = new ArrayList<ChapterUI> (chapterCursorSize);
    	Log.e(getClass().getName(), "chapterCursorSize: "+chapterCursorSize);
	    while(chapterCursor.isAfterLast() == false){
	    	ChapterUI chapterUI = new ChapterUI(chapterCursor.getInt(0), chapterCursor.getString(1));
	    	Log.e(getClass().getName(), "create chapterUI for chapterName: "+chapterUI.getName());
	    	Log.e(getClass().getName(), "create chapterUI for chapter ID: "+chapterUI.getChapterId());
	    	chapterUIList.add(chapterUI);
	    	chapterCursor.moveToNext();
	    }
	}

	public void createSentenceList(ChapterUI chapterUI){
		/********* Build sentence List ********/	    
	    Cursor sentenceCursor = dbHelper.fetchSentences(chapterUI);
	    sentenceCursor.moveToFirst();
	    int sentenceCursorSize = sentenceCursor.getCount();
	    sentenceUI = null;

	    sentenceUIList = new ArrayList<SentenceUI> (sentenceCursorSize);
    	Log.e(getClass().getName(), "sentenceCursorSize: "+sentenceCursorSize);
	    sentenceCursor.moveToFirst();
//	    Log.e(getClass().getName(), "build sentenceUI loop start");
        int currentID = 0;
	    while(sentenceCursor.isAfterLast() == false){
//	    	Log.e(getClass().getName(), "sentenceID to add to sentenceUIList: "+sentenceCursor.getInt(0));
	    	if(currentID != sentenceCursor.getInt(0)){
//    	    	Log.e(getClass().getName(), "if("+currentID+" != "+sentenceCursor.getInt(0)+")");
	    		if(null != sentenceUI){
//					Log.e(getClass().getName(), "if(null != sentenceUI) add sentenceUI to sentenceUIList ");
	    	    	sentenceUI.setOriginalWords(originalWordsList.toArray(new String[originalWordsList.size()]));
	    			sentenceUI.setTranslatedWords(translatedWordsList.toArray(new String[translatedWordsList.size()]));
	    			sentenceUI.setSentenceID(currentID);
//					Log.e(getClass().getName(), "currentID: " + currentID);
	    			sentenceUIList.add(sentenceUI);
	    		}
	    		sentenceUI = new SentenceUI();
	    		originalWordsList = new ArrayList<String>();
	    		translatedWordsList = new ArrayList<String>();
//	   	    	Log.e(getClass().getName(), "else {} getString(1): "+sentenceCursor.getString(1)+" ,getString(2): "+sentenceCursor.getString(2));
	    		originalWordsList.add(sentenceCursor.getString(1));
	    		translatedWordsList.add(sentenceCursor.getString(2));
	    		currentID = sentenceCursor.getInt(0);
	    	}
	    	else{
//    	    	Log.e(getClass().getName(), "else {} getString(1): "+sentenceCursor.getString(1)+" ,getString(2): "+sentenceCursor.getString(2));
	    		originalWordsList.add(sentenceCursor.getString(1));
	    		translatedWordsList.add(sentenceCursor.getString(2));
	    	}
	    	sentenceCursor.moveToNext();
	    }
 //   	Log.e(getClass().getName(), "build sentenceUI loop exit");
		if(null != sentenceUI){
//			Log.e(getClass().getName(), "if(null != sentenceUI) add LAST sentenceUI to sentenceUIList ");
			sentenceUI.setOriginalWords(originalWordsList.toArray(new String[originalWordsList.size()]));
			sentenceUI.setTranslatedWords(translatedWordsList.toArray(new String[translatedWordsList.size()]));
			sentenceUI.setSentenceID(currentID);
//			Log.e(getClass().getName(), "currentID: " + currentID);
			sentenceUIList.add(sentenceUI);
		}
	    sentenceCursor.close();

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
	

    
	public List<SentenceUI> getSentenceUIList() {
		return sentenceUIList;
	}

	public void setSentenceUIList(List<SentenceUI> sentenceUIList) {
		this.sentenceUIList = sentenceUIList;
	}

	public List<ChapterUI> getChapterUIList() {
		return chapterUIList;
	}

	public void setChapterUIList(List<ChapterUI> chapterUIList) {
		this.chapterUIList = chapterUIList;
	}

}
