package recommendation;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecMeGet
{
	public String credentials;
	public final String URLBASE = "http://rec-me.herokuapp.com/api1/";
	public static Scanner in = new Scanner(System.in);
	public String fileName;
	
	public RecMeGet(String[] args){
		Options options = new Options();
		options.addOption(Option.builder("f").longOpt("file").argName("file").hasArg().desc("Read queries from specified file. (optional)").build());
		options.addOption(Option.builder("w").longOpt("write").argName("write").hasArg().desc("Print data to specified file. (optional)").build());
		options.addOption(Option.builder("l").longOpt("login").argName("login").hasArg().desc("Username:Password (required)").required().build());
		options.addOption(Option.builder("h").longOpt("help").desc("Display this information").build());
		
		try{
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options,  args);
			
			String encoded_login = new String(Base64.getEncoder().encode(cmd.getOptionValue("l").getBytes()));
			String token = new Gson().fromJson(RecMeAPI.sendRequest("GET", URLBASE + "token/new", encoded_login), Token.class) + ":";
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

	public static void main(String[] args){
		RecMeGet ex = new RecMeGet(args);
	}
	
	public void reader(BufferedReader br){
		try{
			String input;
			while((input = br.readLine()) != null)
				inputIdentify(input);
			br.close();
		}catch(Exception e){
			System.out.println(e);
			System.exit(1);
		}
	}
	
	public void console(){
		String input;
		System.out.print("Please select a 'recs', 'comments', 'users', or 'search' command\nSelect 'done' to exit\n: ");
		while(!(input = in.nextLine()).equals("done")){
			try{
				inputIdentify(input);
			}
			catch(Exception e){
				System.out.println(e);
			}
			System.out.print("Please select a 'recs', 'comments', 'users', or 'search' command\nSelect 'done' to exit\n: ");
		}
		in.close();	
	}
	
	@SuppressWarnings("unchecked")
	private void inputIdentify(String command) throws ClassNotFoundException{
		Pattern r = Pattern.compile("(\\w+) (\\d+(,\\d+)*) ?(\\w+)? ?(\\d+(,\\d+)*)?");
		Matcher m = r.matcher(command);
		String URL = "";
		if(m.find()){
			for(String numOne : m.group(2).split(",")){
				URL = URLBASE + m.group(1) + "/" + numOne;
				System.out.println(URL);
				if(m.group(4) != null){
					String className = "recommendation."+m.group(4).substring(0,1).toUpperCase()+m.group(4).substring(1);
					URL += "/"+m.group(4);
					if(m.group(5) != null){
						URL += "/";
						for(String numTwo : m.group(5).split(","))
							write(RecMeAPI.sendRequest("GET", URL+"page/"+numTwo, credentials), (Class<? extends APIElement>) Class.forName(className));
					}else
						if(m.group(4).startsWith("f"))
							className = "recommendation.Followings";
						write(RecMeAPI.sendRequest("GET", URL, credentials), (Class<? extends APIElement>) Class.forName(className));
				}else{
					String className = "recommendation."+m.group(1).substring(0,1).toUpperCase()+m.group(1).substring(1);
					write(RecMeAPI.sendRequest("GET", URL, credentials), (Class<? extends APIElement>) Class.forName(className));
				}
			}
		}else{
			Pattern s = Pattern.compile("(\\w+) (\\w+) ([\\w,/ ]+) ?([\\w,]+)?");
			m = s.matcher(command);
			if(m.find())
				System.out.println(m.group(0));
				System.out.println(m.group(1));
				System.out.println(m.group(2));
				System.out.println(m.group(3));
				System.out.println(m.group(4));
				if(m.group(4) != null)
					for(String page: m.group(4).split(","))
						searchGet(m.group(2), m.group(3).split(",",-1), page);
				else
					searchGet(m.group(2), m.group(3).split(",",-1), "1");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void searchGet(String searchType, String[] parameters, String searchPage){
		try{
			String URL = URLBASE+"search/"+searchType+"/page/"+searchPage;
			String searchParameters = "?";
			if(!parameters[0].equals(""))
				searchParameters += "user=" + URLEncoder.encode(parameters[0], "UTF-8");
			if(!parameters[1].equals(""))
				searchParameters += "&term=" + URLEncoder.encode(parameters[1], "UTF-8");
			if(!parameters[2].equals(""))
				searchParameters += "&date=" + URLEncoder.encode(parameters[2], "UTF-8");
			String className = "recommendation."+searchType.substring(0,1).toUpperCase()+searchType.substring(1);
			write(RecMeAPI.sendRequest("GET", URL+searchParameters, credentials), (Class<? extends APIElement>) Class.forName(className));
		}catch(IOException e){
			System.out.println(e);
		}catch(ClassNotFoundException e){
			System.out.println(e);
		}
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
					bw.flush();
					bw.close();
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
				if(content != null)
					bw.write(ele.repr());
				bw.flush();
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