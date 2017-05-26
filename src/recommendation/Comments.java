package recommendation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Comments extends APIElement{
	private Map<String, Comment> properties;
	public Gson gson = new Gson();
	
	public Comments(String json){
		properties = new HashMap<String, Comment>();
		addComments(json);
	}
	
	public void addComments(String json){
		Type type = new TypeToken<Map<String, Comment>>(){}.getType();
		Map<String, Comment> toAdd = gson.fromJson(json, type);
		if(toAdd != null)
			for(Map.Entry<String, Comment> entry : toAdd.entrySet())
				properties.put(entry.getKey(), entry.getValue());
	}
	
	public String headerRepr()
	{
		return("author_id,author_username,text,id,posted_on,timestamp");
	}
	
	public String repr()
	{
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Comment> entry : properties.entrySet())
			sb.append(entry.getValue().repr());
		return(sb.toString());
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Comment> entry : properties.entrySet())
		{
			sb.append(entry.getValue()+"\n\n");
		}
		return(sb.toString());
	}
	
	public class Comment{
		private int author_id;
		private String author_username;
		private String text;
		private int id;
		private int posted_on;
		private String timestamp;

		public String repr(){
			return(author_id+","+author_username+",\""+text+"\","+id+","+posted_on+",\""+timestamp+"\"");
		}
		
		public String toString(){
			return("Comment By " + author_username + "\n" + text);	
		}
	}
}