package IOStream;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class FileOuString {
  public static void main(String[] args) throws IOException {
	  FileOutputStream fs=new FileOutputStream("D:\\fileString.txt");
	  String s="my name is anurag";
	  byte[] b=s.getBytes();
	  fs.write(b);
	  fs.close();
	 for (int i = 0; i < b.length; i++) {
		System.out.print(b[i]);
	}
	  System.out.println("ho gyaa");

  }
}
