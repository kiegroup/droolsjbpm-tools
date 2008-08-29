package org.guvnor.tools.views.model;

/**
 * A model for resource history data. 
 * @author jgraham
 *
 */
public class ResourceHistoryEntry {
	private String revision;
	private String date;
	private String author;
	private String comment;
	
	public ResourceHistoryEntry(String revision, String date,
			                   String author, String comment) {
		this.revision = revision;
		this.date = date;
		this.author = author;
		this.comment = comment;
	}
	
	public String getRevision() {
		return revision != null?revision:""; //$NON-NLS-1$
	}
	public String getDate() {
		return date != null?date:""; //$NON-NLS-1$
	}
	public String getAuthor() {
		return author != null?author:""; //$NON-NLS-1$
	}
	public String getComment() {
		return comment != null?comment:""; //$NON-NLS-1$
	}
}
