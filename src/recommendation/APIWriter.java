package recommendation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class APIWriter {
	public final String fileName;
	
	public APIWriter(String fileName){
		this.fileName = fileName;
	}
	
	
	
	public boolean writeToFile(String content, Class<? extends APIElement> c){
		try{
			APIElement ele = c.getConstructor(String.class).newInstance(content);
			File f = new File(fileName+c.getSimpleName()+".csv");
			if(!f.exists()){
				f.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				bw.write(ele.headerRepr()+"\n");
				bw.close();
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.write(ele.repr()+"\n");
			bw.close();
		}catch(IOException e){
			System.out.println("Could not write to file");
		}catch(Exception e){
			System.out.println(e);
		}
		return(false);
	}
	
}
