package recommendation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Comments {
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
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Comment> entry : properties.entrySet())
		{
			sb.append(entry.getValue()+"\n");
		}
		return(sb.toString());
	}
}