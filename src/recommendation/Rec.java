package recommendation;

public class Rec
{
	private String author;
	private int author_id;
	private int comment_count;
	private int id;
	private String text;
	private String timestamp;
	private String title;
	
	public Rec()
	{
	
	}
	
	public String toString()
	{
		return("Title: " + title + "\nBy " + author + "\n" + text);
	}
}