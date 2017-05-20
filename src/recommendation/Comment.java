package recommendation;

public class Comment {
	private int author_id;
	private String author_username;
	private String text;
	private int id;
	private int posted_on;
	private String timestamp;

	public Comment()
	{
		
	}
	
	public String toString()
	{
		return("Comment By " + author_username + "\n" + text);	
	}

}