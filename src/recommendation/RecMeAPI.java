package recommendation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

public class RecMeAPI {
	public static String sendRequest(String method, String url, String credentials, String parameters){
		if(method.equals("GET")){
			try{
				String searchParameters = "?";
				while(parameters.length() != 0){
					int quote = parameters.indexOf("\"");
					int nextQuote = parameters.indexOf("\"", quote+1);
					searchParameters += "&" + parameters.substring(1,quote) + URLEncoder.encode(parameters.substring(quote+1,nextQuote), "UTF-8");
					parameters = parameters.substring(nextQuote+1);
				}
				return(sendRequest(method, url+searchParameters, credentials));
			}catch(Exception e){
				System.out.println(e);
				return(null);
			}	
		}else
			return(sendHelper(method, url, credentials, parameters));
	}

	public static String sendRequest(String method, String url, String credentials){
		try{
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("Authorization", "Basic "+credentials);
			return(httpResponse(con, url));
		}catch(Exception e){
			System.out.println(e);
			return(null);
		}
	}
	
	private static String sendHelper(String method, String url, String credentials, String parameters){
		try{
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod(method);
			con.setDoOutput(true);
			con.setRequestProperty("Authorization", "Basic "+credentials);
			con.setRequestProperty("Content-Type", "application/json");
			try{
				JSONObject all = new JSONObject();
				while(parameters.length() != 0){
					int quote = parameters.indexOf("\"");
					int nextQuote = parameters.indexOf("\"", quote+1);
					all.put(parameters.substring(1,quote-1), parameters.substring(quote+1,nextQuote));
					parameters = parameters.substring(nextQuote+1);
				}
				OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
				wr.write(all.toString());
				wr.flush();
				wr.close();
			}catch(Exception e){
				System.out.println(e);
			}
			return(httpResponse(con, url));
		}catch(Exception e){
			System.out.println(e);
			return(null);
		}
	}
	
	private static String httpResponse(HttpURLConnection con, String url) throws IOException{
		switch(con.getResponseCode()){
			case HttpURLConnection.HTTP_OK:
				BufferedReader bufferedIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input;
				StringBuffer response = new StringBuffer();
				while((input = bufferedIn.readLine()) != null)
					response.append(input);
				bufferedIn.close();
				return(response.toString());
			case HttpURLConnection.HTTP_CREATED:
				System.out.println(url+"\n201 Success!");
				break;
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
}
