package recommendation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject; 



public class Following {
	private int count;
	private Map<String, String> properties;
	public Gson gson = new Gson();
	
	public Following(String json){
		properties = new HashMap<String, String>();
		addFollowing(json);
	}
	
	public void addFollowing(String json){
		JsonParser p = new JsonParser();
		JsonObject jsonTree = (JsonObject) p.parse(json);
		
		count = jsonTree.get("count").getAsInt();
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> toAdd = gson.fromJson(jsonTree.get("follow_info"), type);
		for(Map.Entry<String, String> entry : toAdd.entrySet())
			properties.put(entry.getKey(),  entry.getValue());
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Count: " + count + "\n");
		for(Map.Entry<String, String> entry : properties.entrySet())
		{
			sb.append(entry.getKey() + " " + entry.getValue()+"\n");
		}
		return(sb.toString());
	
	}
}
