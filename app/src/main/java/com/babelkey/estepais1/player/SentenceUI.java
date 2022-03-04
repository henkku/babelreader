package com.babelkey.estepais1.player;

public class SentenceUI {
	private String[] originalWords;
	private String[] translatedWords;
	private int sentenceID;
	private boolean isSelectedSentence;

	public SentenceUI() {
	}

	public String[] getOriginalWords() {
		return originalWords;
	}

	public void setOriginalWords(String[] originalWords) {
		this.originalWords = originalWords;
	}

	public String[] getTranslatedWords() {
		return translatedWords;
	}

	public void setTranslatedWords(String[] translatedWords) {
		this.translatedWords = translatedWords;
	}

	public int getSentenceID() {
		return sentenceID;
	}

	public void setSentenceID(int sentenceID) {
		this.sentenceID = sentenceID;
	}

	public boolean isSelectedSentence() {
		return isSelectedSentence;
	}

	public void setSelectedSentence(boolean isSelectedSentence) {
		this.isSelectedSentence = isSelectedSentence;
	}	

}
