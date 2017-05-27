package recommendation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Followings extends APIElement{
	private Map<String, Following> properties;
	public Gson gson = new Gson();
	
	public Followings(String json){
		properties = new HashMap<String, Following>();
		addFollowings(json);
	}
	
	public void addFollowings(String json){
		Following toAdd = new Following(json);
		properties.put(toAdd.id+"", toAdd);
	}
	
	public String headerRepr(){
		return("user,id,date");
	}
	
	public String repr(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Following> entry : properties.entrySet())
			sb.append(entry.getValue().repr()+"\n");
		return(sb.toString());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Following> entry : properties.entrySet())
			sb.append(entry.getValue().toString());
		return(sb.toString());
	}
	
	public class Following {
		private int count;
		private int id;
		private Map<String, String> prop;
		
		public Following(String json){
			prop = new HashMap<String, String>();
			addFollowing(json);
		}
		
		public void addFollowing(String json){
			JsonParser p = new JsonParser();
			JsonObject jsonTree = (JsonObject) p.parse(json);
			
			count = jsonTree.get("count").getAsInt();
			id = jsonTree.get("id").getAsInt();
			Type type = new TypeToken<Map<String, String>>(){}.getType();
			Map<String, String> toAdd = gson.fromJson(jsonTree.get("follow_info"), type);
			for(Map.Entry<String, String> entry : toAdd.entrySet())
				prop.put(entry.getKey(),  entry.getValue());
		}
		
		public String repr(){
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, String> entry : prop.entrySet()){
				sb.append(id+","+entry.getKey() + ",\"" + entry.getValue()+"\"\n");
			}
			return(sb.toString());
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Count: " + count + "\n");
			for(Map.Entry<String, String> entry : prop.entrySet()){
				sb.append(entry.getKey() + " " + entry.getValue()+"\n");
			}
			sb.append("\n");
			return(sb.toString());
		}
	}
}