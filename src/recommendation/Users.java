package recommendation;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

public class Users extends APIElement{
	private Map<String, User> properties;
	public Gson gson = new Gson();
	
	public Users(String json){
		properties = new HashMap<String, User>();
		addUsers(json);
	}
	
	public void addUsers(String json){
		User toAdd = gson.fromJson(json, User.class);
		properties.put(toAdd.id+"", toAdd);
	}
	
	public String headerRepr(){
		return("about_me,comments,confirmed,display,followed_by_count,following_count,id,member_since,recs,username");
	}
	
	public String repr(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, User> entry : properties.entrySet())
			sb.append(entry.getValue().repr()+"\n");
		return(sb.toString());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, User> entry : properties.entrySet())
			sb.append(entry.getValue().toString());
		return(sb.toString());
	}
	
	public class User{
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
		
		public String repr(){
			return("\""+about_me+"\","+comments+","+confirmed+","+display+","+followed_by_count+","+following_count+","+id+",\""+member_since+"\","+recs+","+username);
		}
		
		public String toString(){
			return(username + " Recs: " + recs + " Comments: " + comments + " Following: " + following_count + " Followed By: " + followed_by_count +
					"\nAbout Me:\n" + about_me+"\n");
		}
	}
}