package IOStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
public class FileOutputSreams {
  

public static void main(String[] args) throws IOException {
    
	try {
		FileOutputStream fo= new FileOutputStream("D:\\testout.txt");
		fo.write(65);
	    fo.close();
	    System.out.println("Success");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
   }
}
