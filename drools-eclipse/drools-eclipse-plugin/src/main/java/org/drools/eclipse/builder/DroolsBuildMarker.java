package org.drools.eclipse.builder;

public class DroolsBuildMarker {

	private String text;
	private int line = -1;
	private int offset = -1;
	private int length = -1;
	
	public DroolsBuildMarker(String text) {
		this.text = text;
	}
	
	public DroolsBuildMarker(String text, int line) {
		this.text = text;
		this.line = line;
	}

	public DroolsBuildMarker(String text, int offset, int length) {
		this.text = text;
		this.offset = offset;
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}

	public String getText() {
		return text;
	}
}
