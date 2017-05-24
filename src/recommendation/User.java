package recommendation;

public class User {
	private String about_me;
	private int comments;
	private boolean confirmed;
	private int display;
	private int followed_by_count;
	private int following_count;
	private int id;
	private String member_since;
	private String recs;
	private String username;
	
	
	public User(String json){	
	}
	
	public String toString(){
		return(username + " Recs: " + recs + " Comments: " + comments + " Following: " + following_count + " Followed By: " + followed_by_count +
				"\nAbout Me:\n" + about_me+"\n");
	}
}
