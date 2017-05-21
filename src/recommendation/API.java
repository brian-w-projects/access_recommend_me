package recommendation;

import java.io.BufferedReader;
import java.util.Scanner;
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
import org.apache.commons.lang3.StringUtils;
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
	public static Scanner in = new Scanner(System.in);

	
	public API(String login) throws IOException{
		String encoded_login = new String(Base64.getEncoder().encode(login.getBytes()));
		String token = gson.fromJson(sendGET("http://rec-me.herokuapp.com/api1/token/new", encoded_login), Token.class).toString() + ":";
		credentials = new String(Base64.getEncoder().encode(token.getBytes()));
	}

	public static void main(String[] args) throws IOException{
		if(args.length < 2){
			System.out.println("Must enter username and password for authentication.");
			System.exit(1);
		}
		API ex = new API(args[0]+":"+args[1]);
		String input = "";
		do{
			if(!StringUtils.isNumeric(input.substring(input.indexOf(" ")+1)))
				System.out.println("Please format input as 'command number'");
			else if(input.indexOf("recs") != -1)
				ex.recsConsole(input.substring(input.indexOf(" ")+1));
			else if(input.indexOf("comments") != -1)
				ex.commentsConsole(input.substring(input.indexOf(" ")+1));
			else if(input.indexOf("users") != -1)
				ex.usersConsole(input.substring(input.indexOf(" ")+1));
//			else if(input.indexOf("search") != -1)
//				ex.searchConsole();
			System.out.println("Please select from 'recs', 'comments', 'users' and an ID number");
			System.out.println("Select 'done' to exit");
			System.out.print(": ");
		}while(!(input = in.nextLine()).equals("done"));
		in.close();	
	}
	
//	Recs recs = new Recs(ex.sendGET("http://rec-me.herokuapp.com/api1/recs/2", ex.credentials));
//	System.out.println(recs);
	
//	Comments comments = new Comments(ex.sendGET("http://rec-me.herokuapp.com/api1/recs/2/comments/page/2", ex.credentials));
//	System.out.println(comments);
	
//	User user = gson.fromJson(ex.sendGET("http://rec-me.herokuapp.com/api1/users/1", ex.credentials), User.class);
//	System.out.println(user);
	
//	Following following = new Following(ex.sendGET("http://rec-me.herokuapp.com/api1/users/2/followed_by", ex.credentials));
//	System.out.println(following);
	
	private void usersConsole(String userNum) throws IOException{
		String URL = "http://rec-me.herokuapp.com/api1/users/"+userNum;
		User user = gson.fromJson(sendGET(URL, credentials), User.class);
		if(user.equals(""))
			return;
		System.out.println(user);
		
		String input = "";
		int recPage = 1;
		int comPage = 1;
		do{
			if(input.equals("recs"))
				System.out.println(new Recs(sendGET(URL+"/recs/page/"+(recPage++), credentials)));
			else if(input.equals("comments"))
				System.out.println(new Comments(sendGET(URL+"/comments/page/"+(comPage++), credentials)));
			else if(input.equals("following"))
				System.out.println(new Following(sendGET(URL+"/following", credentials)));
			else if(input.equals("followers"))
				System.out.println(new Following(sendGET(URL+"/followed_by", credentials)));
			System.out.println("Please select 'recs', 'comments', 'following', 'followers' or 'done'");
			System.out.print(":");
		}while(!(input = in.nextLine()).equals("done"));
	}
	
	private void commentsConsole(String comNum) throws IOException{
		Comments comments = new Comments(sendGET("http://rec-me.herokuapp.com/api1/comments/"+comNum, credentials));
		if(comments.equals(""))
			return;
		System.out.println(comments);
	}
	
	private void recsConsole(String recNum) throws IOException{
		String URL = "http://rec-me.herokuapp.com/api1/recs/"+recNum;
		Recs recs = new Recs(sendGET(URL, credentials));
		if(recs.equals(""))
			return;
		System.out.println(recs);
		
		String input = "";
		int page = 1;
		do{
			if(input.equals("comments"))
				System.out.println(new Comments(sendGET(URL+"/comments/page/"+(page++), credentials)));
			System.out.println("Please select 'comments' or 'done'");
			System.out.print(":");
		}while(!(input = in.nextLine()).equals("done"));
	}
	
	private String sendGET(String url, String credentials) throws IOException{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic "+credentials);
		switch(con.getResponseCode()){
			case HttpURLConnection.HTTP_OK:
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				StringBuffer response = new StringBuffer();
				while((input = in.readLine()) != null)
					response.append(input);
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
		return("");
	}
}