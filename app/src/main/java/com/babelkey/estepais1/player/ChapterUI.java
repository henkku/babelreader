package com.babelkey.estepais1.player;

public class ChapterUI {
	private int chapterBegins;
	private int chapterEnds;
	private String description;
	public ChapterUI(int chapterBegins, int chapterEnds, String description) {
		this.chapterBegins = chapterBegins;
		this.chapterEnds = chapterEnds;
		this.description = description;
	}
	public int getChapterBegins() {
		return chapterBegins;
	}
	public void setChapterBegins(int chapterBegins) {
		this.chapterBegins = chapterBegins;
	}
	public int getChapterEnds() {
		return chapterEnds;
	}
	public void setChapterEnds(int chapterEnds) {
		this.chapterEnds = chapterEnds;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
