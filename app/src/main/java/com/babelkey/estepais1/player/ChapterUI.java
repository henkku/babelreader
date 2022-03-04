package com.babelkey.estepais1.player;

public class ChapterUI {
	private int chapterId;
	private String name;
	public ChapterUI(int chapterId, String name) {
		this.chapterId = chapterId;
		this.name = name;
	}
	public int getChapterId() {
		return chapterId;
	}
	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
