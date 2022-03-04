package com.babelkey.estepais1.player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private final Context context;
	private SQLiteDatabase database;
	private DBHelper dbManager;
	private static String DB_NAME = "estepais1.db";
	private static String DB_PATH;

	
	public DBHelper(Context context){
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = context.getDatabasePath(DB_NAME).getPath();

	}
	
    public void createDataBase() throws IOException{   	 
    	Log.e(getClass().getName(), "Entering createDataBase()");
    	boolean dbExist = checkDataBase();
    	Log.e(getClass().getName(), "dbExist: "+dbExist);
    	if(dbExist){
    		//do nothing - database already exist
    	}else{ 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
        	try { 
    			copyDataBase();
 
    		} catch (IOException e) { 
        		throw new Error("Error copying database, e:"+e);
        	}
    	}
 
    }
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
    	Log.e(getClass().getName(), "Entering checkDataBase");
    	SQLiteDatabase checkDB = null; 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS); 
    	}catch(Exception e){
			checkDB = null;
    	}
    	if(checkDB != null){ 
    		checkDB.close(); 
    	} 
    	return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
    	Log.e(getClass().getName(), "Entering copyDataBase()");
    	//Open your local db as the input stream
    	InputStream myInput = context.getAssets().open(DB_NAME);
		Log.e(getClass().getName(), "DEBUG 1");
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME; 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[10240];
    	int length;
    	while ((length = myInput.read(buffer))>0){
//			Log.e(getClass().getName(), "DEBUG 3");
    		myOutput.write(buffer, 0, length);
    	}


		Log.e(getClass().getName(), "DEBUG 2");
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();

    }
    public void openDataBase() throws SQLException{
    	Log.e(getClass().getName(), "Entering openDataBase()");
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	database = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
 
    }
 
    @Override
	public synchronized void close() {
		Log.e(getClass().getName(), "DBHelper close");
		try{
    	    if(database != null)
    	    	database.close();
 
    	    super.close();
		}catch(Exception e){
			Log.e(getClass().getName(), "unable to close Database");			
		}
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
	
	public Cursor fetchSentences(ChapterUI chapterUI){
//		String sql = "SELECT SentenceID, Column1, Column2 FROM Words WHERE SentenceID >= "+ chapterUI.getChapterBegins() +" AND SentenceID <="+ chapterUI.getChapterEnds() +" ORDER BY SentenceID,RowPosition ASC";
		String sql = "SELECT SentenceID, Column1, Column2 FROM Words WHERE Chapter = "+ chapterUI.getChapterId() +" ORDER BY SentenceID,RowPosition ASC";
	  	Log.e(getClass().getName(), "fetchSentences SQL: "+sql);		
		return database.rawQuery(sql, new String [] {});
	}
	
	public Cursor fetchChapters(){
		return database.rawQuery("SELECT ID, Description FROM Chapters ORDER BY ID", new String [] {});
	}

	// Use this method when the audio is played from the SQLite database
	public Cursor fetchSentenceAudio(int id){
		return database.rawQuery("SELECT Audio FROM blobs WHERE SentenceID ="+id, new String [] {});
	} 
 
} 
    
