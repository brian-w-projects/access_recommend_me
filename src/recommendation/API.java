package recommendation;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
//import java.io.InputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
//import java.io.OutputStream;
//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
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

	
	public API(String[] args) throws IOException{
		Options options = new Options();
		options.addOption(Option.builder("f").longOpt("file").argName("file").hasArg().desc("Read queries from specified file. (optional)").build());
		options.addOption(Option.builder("w").longOpt("write").argName("write").hasArg().desc("Print data to specified file. (optional)").build());
		options.addOption(Option.builder("l").longOpt("login").argName("login").hasArg().desc("Username:Password (required)").required().build());
		options.addOption(Option.builder("h").longOpt("help").desc("Display this information").build());
		
		try{
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options,  args);
			
			String encoded_login = new String(Base64.getEncoder().encode(cmd.getOptionValue("l").getBytes()));
			String token = gson.fromJson(sendGET(URLBASE + "token/new", encoded_login), Token.class).toString() + ":";
			credentials = new String(Base64.getEncoder().encode(token.getBytes()));
			
			if(cmd.hasOption("file")){
				BufferedReader br = new BufferedReader(new FileReader(new File(cmd.getOptionValue("file"))));
				System.out.println("Reading from file " + cmd.getOptionValue("file"));
				reader(br);
			}else{
				System.out.println("Reading from command line.");
				console();
			}
			
//			if(cmd.hasOption("write")){
//				System.out.println("Write to file " + cmd.getOptionValue("write"));
//			}else
//				System.out.println("Writing to command line");

			
		}catch(ParseException e){
			System.out.println(e);
			new HelpFormatter().printHelp("Query rec-me.herokuapp.com's RESTful API",  options);
			System.exit(1);
		}catch(IOException e){
			System.out.println(e);
			System.exit(1);
		}
	}

	public static void main(String[] args) throws IOException{
		API ex = new API(args);
	}
	
	public void reader(BufferedReader br) throws IOException{
		String input;
		while((input = br.readLine()) != null){
			String[] s = input.split(" ");
			 if(s[0].equals("get")){
				 if(s[1].equals("recs")){
					 for(String recNum : s[2].split(","))
						 recsFile(recNum, s.length == 4);
				 }else if(s[1].equals("comments")){
					 for(String comNum: s[2].split(","))
						 commentsConsole(comNum);
				 }else{
					 for(String userNum: s[2].split(","))
						 userFile(userNum);
				 }
			 }else if(s[0].equals("search")){
				 searchFile(s[1],s[2].split(",",-1));
			 }
		}
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
	
	private void searchFile(String searchType, String[] parameters) throws IOException{
		String URL = URLBASE+"search/"+searchType+"/page/";
		String searchParameters = "?";
		if(!parameters[0].equals(""))
			searchParameters += "user=" + parameters[0];
		if(!parameters[1].equals(""))
			searchParameters += "&term=" + parameters[1];
		if(!parameters[2].equals(""))
			searchParameters += "&date=" + parameters[2];
		int page = 1;
		System.out.println(searchParameters);
		if(searchType.equals("recs")){
			Recs display;
			while(!(display = new Recs(sendGET(URL+(page++)+searchParameters, credentials))).toString().equals(""))
				System.out.println(display);
		}else{
			Comments display;
			while(!(display = new Comments(sendGET(URL+(page++)+searchParameters, credentials))).toString().equals(""))
				System.out.println(display);
		}
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
	
	private void userFile(String userNum) throws IOException{
		String URL = URLBASE+"users/"+userNum;
		System.out.println(gson.fromJson(sendGET(URL, credentials), User.class));
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
	
	private void recsFile(String recNum, boolean coms) throws IOException{
		String URL = URLBASE+"recs/"+recNum;
		System.out.println(new Recs(sendGET(URL, credentials)));
		
		int comPage = 1;
		Comments comments;
		while(coms && !(comments = new Comments(sendGET(URL+"/comments/page/"+(comPage++), credentials))).toString().equals("")){
			System.out.println(comments);
		}
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