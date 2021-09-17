package IOStream;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectMaprDemo {
public static void main(String[] args) throws IOException {
   
	FileOutputStream fs=new FileOutputStream("D:\\fileString.txt");
	ObjectMapper mapper = new ObjectMapper();//it is use to convert any type in jason object..
	    ObjectNode on = mapper.createObjectNode();
	    
	    
}
}
