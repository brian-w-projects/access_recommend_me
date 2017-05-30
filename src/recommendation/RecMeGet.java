package recommendation;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class RecMeGet
{
	public String credentials;
	public final String URLBASE = "http://rec-me.herokuapp.com/api1";
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
			String token = new Gson().fromJson(RecMeAPI.sendRequest("GET", URLBASE + "/token/new", encoded_login), Token.class) + ":";
			credentials = new String(Base64.getEncoder().encode(token.getBytes()));
			fileName = cmd.getOptionValue("write");

			if(cmd.hasOption("file")){
				BufferedReader br = new BufferedReader(new FileReader(new File(cmd.getOptionValue("file"))));
				System.out.println("Reading from file " + cmd.getOptionValue("file"));
				reader(br);
			}else{
				System.out.println("Reading from command line.");
				Scanner in = new Scanner(System.in);
				console(in);
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
		String input = "";
		try{
			while((input = br.readLine()) != null)
				inputIdentify(input);
			br.close();
		}catch(Exception e){
			System.out.println("Error in search command: " + input);
		}
	}
	
	public void console(Scanner in){
		String input = "";
		System.out.print("Please enter a command\nSelect 'done' to exit\n: ");
		while(!(input = in.nextLine()).equals("done")){
			try{
				inputIdentify(input);
			}catch(Exception e){
				System.out.println("Error in search command: " + input);
			}
			System.out.print("Please enter a command\nSelect 'done' to exit\n: ");
		}
		in.close();	
	}
	
	private void inputIdentify(String command) throws ClassNotFoundException, IOException, Exception{
        Pattern r = Pattern.compile("^(\\w+) (\\w+) ([\\d,]+)? ?(\\w+)? ?([\\w,]+)? ?(-p .+)?$");
        Matcher m = r.matcher(command);
        ArrayList<String> elements = new ArrayList<String>();
        if(m.find()){
            for(int i=1; i <=  m.groupCount(); i++)
                elements.add(m.group(i));
        }
        createURL(elements, 1, URLBASE, "");
    }

    @SuppressWarnings("unchecked")
	private void createURL(ArrayList<String> elements, int step, String URL, String cs) throws ClassNotFoundException, IOException, Exception{
        if(step == elements.size()){
        	String requestInfo = RecMeAPI.sendRequest(elements.get(0), URL, credentials);
            if(elements.get(0).equals("GET")){
            	if(cs.startsWith("f"))
    				cs = "recommendation.Followings";
                else
                	cs = "recommendation."+cs.substring(0,1).toUpperCase()+cs.substring(1);
            	write(requestInfo, (Class<? extends APIElement>) Class.forName(cs));
            }
        	return;
        }
        String process = elements.get(step);
        if(process == null || process.equals(""))
            createURL(elements, step+1, URL, cs);
        else if(process.startsWith("-p")){
        	String requestInfo = RecMeAPI.sendRequest(elements.get(0), URL, credentials, process.substring(2));
        	if(elements.get(0).equals("GET")){
        		cs = "recommendation."+cs.substring(0,1).toUpperCase()+cs.substring(1);
        		write(requestInfo, (Class<? extends APIElement>) Class.forName(cs));
        	}
            return;
        }
        else if(StringUtils.isNumeric(process.substring(0,1))){
            for(String num : process.split(",")){
                createURL(elements, step+1, URL+"/"+num, cs);
            }
        }else{
            createURL(elements, step+1, URL+"/"+process, elements.get(step));
        }
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