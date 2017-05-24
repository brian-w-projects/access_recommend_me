package recommendation;

import java.io.BufferedReader;
import java.util.Scanner;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
	public final String URLBASE = "http://rec-me.herokuapp.com/api1/";
	public static Gson gson = new Gson();
	public static Scanner in = new Scanner(System.in);

	
	public API(String login) throws IOException{
		String encoded_login = new String(Base64.getEncoder().encode(login.getBytes()));
		String token = gson.fromJson(sendGET(URLBASE + "token/new", encoded_login), Token.class).toString() + ":";
		credentials = new String(Base64.getEncoder().encode(token.getBytes()));
	}

	public static void main(String[] args) throws IOException{
		if(args.length < 2){
			System.out.println("Must enter username and password for authentication.");
			System.exit(1);
		}
		API ex = new API(args[0]+":"+args[1]);
		ex.console();
	}
	
	public void console() throws IOException{
		String input = "";
		String num = "";
		do{
			if(StringUtils.isNumeric(num = input.substring(input.indexOf(" ")+1))){
				if(input.contains("recs"))
						recsConsole(num);
				else if(input.contains("comments"))
					commentsConsole(num);
				else if(input.contains("users"))
					usersConsole(num);
			}else
				if(input.contains("search recs") || input.contains("search comments"))
					searchConsole(input.substring(input.indexOf(" ")+1));
			System.out.println("Format input as 'command info'");
			System.out.println("Please select from 'recs ID', 'comments ID', 'users ID', 'search recs' or 'search comments'");
			System.out.println("Select 'done' to exit");
			System.out.print(": ");
		}while(!(input = in.nextLine()).equals("done"));
		in.close();	
	}
	
	private void searchConsole(String searchType) throws IOException{
		String parameter = "";
		String searchParameters = "?";
		System.out.print("User: ");
		parameter = in.nextLine();
		if(!parameter.equals(""))
			searchParameters += "user=" + URLEncoder.encode(parameter, "UTF-8");
		System.out.print("Term: ");
		parameter = in.nextLine();
		if(!parameter.equals(""))
			searchParameters += "&term=" + URLEncoder.encode(parameter, "UTF-8");
		System.out.print("Date (MM/DD/YYYY): ");
		parameter = in.nextLine();
		if(!parameter.equals(""))
			searchParameters += "&date=" + URLEncoder.encode(parameter, "UTF-8");
		
		String URL = URLBASE+"search/"+searchType+"/page/";
		int page = 1;
		String input = "";
		do{
			if(searchType.equals("recs"))
				System.out.println(new Recs(sendGET(URL+(page++)+searchParameters, credentials)));
			else
				System.out.println(new Comments(sendGET(URL+(page++)+searchParameters, credentials)));
			System.out.println("Please select from 'more' or 'done'");
			System.out.print(":");
		}while(!(input = in.nextLine()).equals("done"));
	}
	
	private void usersConsole(String userNum) throws IOException{
		String URL = URLBASE+"users/"+userNum;
		System.out.println(gson.fromJson(sendGET(URL, credentials), User.class));
		
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
			System.out.print("Please select 'recs', 'comments', 'following', 'followers' or 'done'\n:");
		}while(!(input = in.nextLine()).equals("done"));
	}
	
	private void commentsConsole(String comNum) throws IOException{
		String URL = URLBASE+"comments/"+comNum;
		System.out.println(new Comments(sendGET(URL, credentials)));
	}
	
	private void recsConsole(String recNum) throws IOException{
		String URL = URLBASE+"recs/"+recNum;
		System.out.println(new Recs(sendGET(URL, credentials)));
		
		String input = "";
		int comPage = 1;
		do{
			if(input.equals("comments"))
				System.out.println(new Comments(sendGET(URL+"/comments/page/"+(comPage++), credentials)));
			System.out.print("Please select 'comments' or 'done'\n:");
		}while(!(input = in.nextLine()).equals("done"));
	}
	
	private String sendGET(String url, String credentials) throws IOException{
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic "+credentials);
		switch(con.getResponseCode()){
			case HttpURLConnection.HTTP_OK:
				BufferedReader bufferedIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				StringBuffer response = new StringBuffer();
				while((input = bufferedIn.readLine()) != null)
					response.append(input);
				bufferedIn.close();
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
				System.out.println(con.getResponseCode()+" ERROR: There has been an error");
				break;
		}
		return("");
	}
}