package recommendation;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RecMeGet
{
	public String credentials;
	public final String URLBASE = "http://rec-me.herokuapp.com/api1/";
	public static Gson gson = new Gson();
	public static Scanner in = new Scanner(System.in);
	public String fileName;
	
	public RecMeGet(String[] args) throws IOException{
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
			fileName = cmd.getOptionValue("write");

			if(cmd.hasOption("file")){
				BufferedReader br = new BufferedReader(new FileReader(new File(cmd.getOptionValue("file"))));
				System.out.println("Reading from file " + cmd.getOptionValue("file"));
				reader(br);
			}else{
				System.out.println("Reading from command line.");
				console();
			}
		}catch(ParseException e){
			new HelpFormatter().printHelp("Query rec-me.herokuapp.com's RESTful API",  options);
			System.exit(1);
		}catch(IOException e){
			System.out.println("Could not read from file.");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws IOException{
		RecMeGet ex = new RecMeGet(args);
	}
	
	public void reader(BufferedReader br) throws IOException{
		String input;
		while((input = br.readLine()) != null)
			inputIdentify(input.split(" "), true);
		br.close();
	}
	
	public void console() throws IOException{
		String input;
		System.out.print("Please select from 'recs ID', 'comments ID', 'users ID', 'search recs' or 'search comments'\nSelect 'done' to exit\n: ");
		while(!(input = in.nextLine()).equals("done")){
			inputIdentify(input.split(" "), false);
			System.out.print("Please select from 'recs ID', 'comments ID', 'users ID', 'search recs' or 'search comments'\nSelect 'done' to exit\n: ");
		}
		in.close();	
	}
	
	private void inputIdentify(String[] commands, boolean reader) throws IOException{
		 if(commands[0].equals("recs")){
			 for(String recNum : commands[1].split(","))
				 recsGET(recNum, reader);
		 }else if(commands[0].equals("comments")){
			 for(String comNum: commands[1].split(","))
				 commentsGET(comNum, reader);
		 }else if(commands[0].equals("users")){
			 for(String userNum: commands[1].split(","))
				 usersGET(userNum, reader);
		 }else
			 for(String searchPage: commands[3].split(","))
			 searchGET(commands[1],commands[2].split(",",-1), searchPage);
	}
	
	private void recsGET(String recNum, boolean reader) throws IOException{
		String URL = URLBASE+"recs/"+recNum;
		write(sendGET(URL, credentials), Recs.class);
		
		if(!reader){
			int comPage = 1;
			String input = "";
			do{
				if(input.equals("comments"))
					write(sendGET(URL+"/comments/page/"+(comPage++), credentials), Comments.class);
				System.out.print("Please select 'comments' or 'done'\n:");
			}while(!(input = in.nextLine()).equals("done"));
		}
	}
	
	private void commentsGET(String comNum, boolean reader) throws IOException{
		String URL = URLBASE+"comments/"+comNum;
		write(sendGET(URL, credentials), Comments.class);
	}
	
	private void usersGET(String userNum, boolean reader) throws IOException{
		String URL = URLBASE+"users/"+userNum;
		write(sendGET(URL, credentials), Users.class);
		
		if(!reader){
			String input = "";
			int recPage = 1;
			int comPage = 1;
			do{
				if(input.equals("recs"))
					write(sendGET(URL+"/recs/page/"+(recPage++), credentials), Recs.class);
				else if(input.equals("comments"))
					write(sendGET(URL+"/comments/page/"+(comPage++), credentials), Comments.class);
				else if(input.equals("following"))
					write(sendGET(URL+"/following", credentials), Followings.class);
				else if(input.equals("followers"))
					write(sendGET(URL+"/followed_by", credentials), Followings.class);
				System.out.print("Please select 'recs', 'comments', 'following', 'followers' or 'done'\n:");
			}while(!(input = in.nextLine()).equals("done"));
		}
	}
	
	private void searchGET(String searchType, String[] parameters, String searchPage) throws IOException{
		String URL = URLBASE+"search/"+searchType+"/page/";
		String searchParameters = "?";
		if(!parameters[0].equals(""))
			searchParameters += "user=" + URLEncoder.encode(parameters[0], "UTF-8");
		if(!parameters[1].equals(""))
			searchParameters += "&term=" + URLEncoder.encode(parameters[1], "UTF-8");
		if(!parameters[2].equals(""))
			searchParameters += "&date=" + URLEncoder.encode(parameters[2], "UTF-8");
		if(searchType.equals("recs"))
			write(sendGET(URL+searchPage+searchParameters, credentials), Recs.class);
		else
			write(sendGET(URL+searchPage+searchParameters, credentials), Comments.class);
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
				System.out.println(url + "\n403 FORBIDDEN: You do not have permission to access this information.");
				break;
			case HttpURLConnection.HTTP_INTERNAL_ERROR:
				System.out.println(url + "\n500 INTERNAL SERVER ERROR: There has been an error.");
				break;
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				System.out.println(url + "\n401 UNAUTHORIZED: Please recheck login credentials.");
				break;
			case HttpURLConnection.HTTP_NOT_FOUND:
				System.out.println(url + "\n404 NOT FOUND: Please check URL");
				break;
			case 429:
				System.out.println(url + "\n429 TOO MANY REQUESTS: You may only make 15 requests every 15 minutes.");
				break;
			default:
				System.out.println(url + "\n" + con.getResponseCode()+" ERROR: There has been an error");
				break;
		}
		return(null);
	}
	
	public void write(String content, Class<? extends APIElement> c){
		try{
			APIElement ele = c.getConstructor(String.class).newInstance(content);
			if(fileName != null){	
				File f = new File(fileName+c.getSimpleName()+".csv");
				if(!f.exists()){
					BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
					bw.write(ele.headerRepr());
					bw.newLine();
					bw.close();
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
				if(content != null)
					bw.write(ele.repr());
				bw.close();
			}else{
				System.out.println(ele);
			}
		}catch(IOException e){
			System.out.println("Could not write to file");
		}catch(Exception e){
			System.out.println(e);
		}
	}
}