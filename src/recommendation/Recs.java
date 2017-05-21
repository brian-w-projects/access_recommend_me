package recommendation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Recs{
	private Map<String, Rec> properties;
	public Gson gson = new Gson();
	
	public Recs(String json){
		properties = new HashMap<String, Rec>();
		addRecs(json); 
	}
	
	public void addRecs(String json){
		Type type = new TypeToken<Map<String, Rec>>(){}.getType();
		Map<String, Rec> toAdd = gson.fromJson(json, type);
		if(toAdd != null)
			for(Map.Entry<String, Rec> entry : toAdd.entrySet())
				properties.put(entry.getKey(), entry.getValue());
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Rec> entry : properties.entrySet())
		{
			sb.append(entry.getValue()+"\n");
		}
		return(sb.toString());
	}
}