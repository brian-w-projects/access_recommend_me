package recommendation;

import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
import java.util.Base64;
import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.lang.reflect.Type;
//import com.google.gson.reflect.TypeToken;

public class API
{
	public String credentials;
	public static Gson gson = new Gson();

	
	public API(String login) throws IOException
	{
		String encoded_login = new String(Base64.getEncoder().encode(login.getBytes()));
		String token = gson.fromJson(sendGET("http://rec-me.herokuapp.com/api1/token/new", encoded_login), Token.class).toString() + ":";
		credentials = new String(Base64.getEncoder().encode(token.getBytes()));
	}

	public static void main(String[] args) throws IOException
	{
		if(args.length < 2)
		{
			System.out.println("Must enter username and password for authentication.");
			System.exit(1);
		}
		API ex = new API(args[0]+":"+args[1]);
//		Recs recs = new Recs(ex.sendGET("http://rec-me.herokuapp.com/api1/users/1/recs", ex.credentials));
		Comments comments = new Comments(ex.sendGET("http://rec-me.herokuapp.com/api1/users/1/comments", ex.credentials));
//		User user = gson.fromJson(ex.sendGET("http://rec-me.herokuapp.com/api1/users/1", ex.credentials), User.class);
//		System.out.println(recs);
		System.out.println(comments);
//		System.out.println(user);
		
	}
	
	private String sendGET(String url, String credentials) throws IOException
	{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic "+credentials);
		switch(con.getResponseCode()){
			case HttpURLConnection.HTTP_OK:
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while((inputLine = in.readLine()) != null)
					response.append(inputLine);
				in.close();
				return(response.toString());
			case HttpURLConnection.HTTP_FORBIDDEN:
				System.out.println("403 FORBIDDEN: You do not have permission to access this information.");
				break;
			case HttpURLConnection.HTTP_INTERNAL_ERROR:
				System.out.println("500 INTERNAL SERVER ERROR: There has been an error.");
				break;
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				System.out.println("401 UNAUTHORIZED: Please recheck login credentials.");
				break;
			case HttpURLConnection.HTTP_NOT_FOUND:
				System.out.println("404 NOT FOUND: Please check URL");
				break;
			case 429:
				System.out.println("429 TOO MANY REQUESTS: You may only make 15 requests every 15 minutes.");
				break;
			default:
				System.out.println("400 ERROR: There has been an error");
				break;
		}
		return(null);
	}
}