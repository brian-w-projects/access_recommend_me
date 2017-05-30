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
				if(input.split(" ")[0].equals("GET"))
					inputIdentifyGET(input.substring(input.indexOf(" ")+1));
				else
					inputIdentifyPPD(input);
			br.close();
		}catch(IOException e){
			System.out.println("IO Exception: " + e);
		}catch(ClassNotFoundException e){
			System.out.println("Class Not Found Exception: " + e);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void console(){
		String input;
		System.out.print("Please enter a command\nSelect 'done' to exit\n: ");
		while(!(input = in.nextLine()).equals("done")){
			try{
				if(input.split(" ")[0].equals("GET"))
					inputIdentifyGET(input.substring(input.indexOf(" ")+1));
				else
					inputIdentifyPPD(input);
				System.out.print("Please enter a command\nSelect 'done' to exit\n: ");
			}catch(IOException e){
				System.out.println("IO Exception: " + e);
			}catch(ClassNotFoundException e){
				System.out.println("Class Not Found Exception: " + e);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		in.close();	
	}
	
	private void inputIdentifyPPD(String command){
		Pattern r = Pattern.compile("(\\w+) (\\w+) ?(\\d+)?( \\w+=.+)?$");
		Matcher m = r.matcher(command);
		String URL = URLBASE;
		if(m.find()){
			if(m.group(1).equals("DELETE")){
				URL += m.group(2)+"/"+m.group(3);
				RecMeAPI.sendRequest(m.group(1), URL, credentials);
			}else if(m.group(1).equals("POST")){
				if(m.group(2).equals("recs")){
					URL += m.group(2);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4));
				}else if(m.group(2).equals("comments")){
					URL += "recs/"+m.group(3)+"/"+m.group(2);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4));
				}else{
					URL += m.group(2) + "/" + m.group(3);
					RecMeAPI.sendRequest(m.group(1), URL, credentials);
				}
			}else if(m.group(1).equals("PUT")){
				URL += m.group(2);
				if(m.group(3) != null)
					URL += "/"+m.group(3);
					RecMeAPI.sendRequest(m.group(1), URL, credentials, m.group(4));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void inputIdentifyGET(String command) throws IOException, ClassNotFoundException, Exception{
		Pattern r = Pattern.compile("(\\w+) (\\d+(,\\d+)*) ?(\\w+)? ?(\\d+(,\\d+)*)?");
		Matcher m = r.matcher(command);
		String URL = "";
		if(m.find()){
			for(String numOne : m.group(2).split(",")){
				URL = URLBASE + m.group(1) + "/" + numOne;
				if(m.group(4) != null){
					String className = "recommendation."+m.group(4).substring(0,1).toUpperCase()+m.group(4).substring(1);
					URL += "/"+m.group(4);
					if(m.group(5) != null){
						URL += "/";
						for(String numTwo : m.group(5).split(","))
							write(RecMeAPI.sendRequest("GET", URL+"page/"+numTwo, credentials), (Class<? extends APIElement>) Class.forName(className));
					}else{
						if(m.group(4).startsWith("f"))
							className = "recommendation.Followings";
						write(RecMeAPI.sendRequest("GET", URL, credentials), (Class<? extends APIElement>) Class.forName(className));
					}
				}else{
					String className = "recommendation."+m.group(1).substring(0,1).toUpperCase()+m.group(1).substring(1);
					write(RecMeAPI.sendRequest("GET", URL, credentials), (Class<? extends APIElement>) Class.forName(className));
				}
			}
		}else{
			Pattern s = Pattern.compile("(\\w+) (\\w+)( .+) ?([\\d,]+)?$");
			m = s.matcher(command);
			if(m.find())
				if(m.group(4) != null)
					for(String page: m.group(4).split(","))
						searchGet(m.group(2), m.group(3), page);
				else
					searchGet(m.group(2), m.group(3), "1");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void searchGet(String searchType, String parameters, String searchPage) throws IOException, ClassNotFoundException, Exception{
		String URL = URLBASE+"search/"+searchType+"/page/"+searchPage;
		String searchParameters = "?";
		while(parameters.length() != 0){
			int quote = parameters.indexOf("\"");
			int nextQuote = parameters.indexOf("\"", quote+1);
			searchParameters += "&" + parameters.substring(1,quote) + URLEncoder.encode(parameters.substring(quote+1,nextQuote), "UTF-8");
			parameters = parameters.substring(nextQuote+1);
		}
		String className = "recommendation."+searchType.substring(0,1).toUpperCase()+searchType.substring(1);
		write(RecMeAPI.sendRequest("GET", URL+searchParameters, credentials), (Class<? extends APIElement>) Class.forName(className));
	}
	
	public void write(String content, Class<? extends APIElement> c) throws IOException, Exception{
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
	}
}