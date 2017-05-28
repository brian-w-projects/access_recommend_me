package recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;

public class RecMePPD {
	public String credentials;
	public final String URLBASE = "http://rec-me.herokuapp.com/api1/";
	public static Scanner in = new Scanner(System.in);
	
	public RecMePPD(String[] args) throws IOException{
		Options options = new Options();
		options.addOption(Option.builder("f").longOpt("file").argName("file").hasArg().desc("Read queries from specified file. (optional)").build());
		options.addOption(Option.builder("l").longOpt("login").argName("login").hasArg().desc("Username:Password (required)").required().build());
		options.addOption(Option.builder("h").longOpt("help").desc("Display this information").build());
		
		try{
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options,  args);
			
			String encoded_login = new String(Base64.getEncoder().encode(cmd.getOptionValue("l").getBytes()));
			String token =  new Gson().fromJson(RecMeAPI.sendRequest("GET", URLBASE + "token/new", encoded_login), Token.class).toString() + ":";
			credentials = new String(Base64.getEncoder().encode(token.getBytes()));

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
		RecMePPD ex = new RecMePPD(args);
	}
	
	public void console(){
		String input;
		System.out.print("Please select a 'recs', 'comments', 'users', or 'follow' command\nSelect 'done' to exit\n: ");
		while(!(input = in.nextLine()).equals("done")){
			inputIdentify(input);
			System.out.print("Please select a 'recs', 'comments', 'users', or 'follow' command\nSelect 'done' to exit\n: ");
		}
		in.close();	
	}
	
	private void inputIdentify(String command){
		Pattern r = Pattern.compile("(\\w+) (\\w+) ?(\\d+)? ?(.+)?");
		Matcher m = r.matcher(command);
		String URL = URLBASE;
		if(m.find()){
			if(m.group(1).equals("DELETE")){
				URL += m.group(2)+"/"+m.group(3);
				RecMeAPI.sendRequest(m.group(1), URL, credentials);
			}else if(m.group(1).equals("POST")){
				if(m.group(2).equals("recs")){
					URL += m.group(2);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4).split("~"));
				}else if(m.group(2).equals("comments")){
					URL += "recs/"+m.group(3)+"/"+m.group(2);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4).split("~"));
				}else{
					URL += m.group(2) + "/" + m.group(3);
					RecMeAPI.sendRequest(m.group(1), URL, credentials);
				}
			}else if(m.group(1).equals("PUT")){
				URL += m.group(2);
				if(m.group(3) != null)
					URL += "/"+m.group(3);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4).split("~"));
			}
		}
	}
	
	public void reader(BufferedReader br){
		
	}
}
















