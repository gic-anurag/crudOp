package jsonExample;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;







public class Day8b {
	

	    public static void main(String[] args) throws Exception {
	    	try {
	    	InputStream in=new FileInputStream("D:\\prac\\InputJson (2).json");
			int i=0;
			String s="";
			while ((i=in.read())!=-1) {
				System.out.print((char)i);
				s+=(char)i;
				
			}

			//  String jsonStr = "{\"Technologies\" : [\"Java\", \"Scala\", \"Python\"]}";
		      ObjectMapper mapper = new ObjectMapper();
		      String fetch=null;
		      JsonNode node=mapper.readTree(s);
		      System.out.println(node);
		      ArrayNode arrayNode = (ArrayNode) mapper.readTree(s).get("data");
		      if(arrayNode.isArray()) {
		         for(JsonNode jsonNode : arrayNode) {
		            System.out.println("123::: "+jsonNode.get("taskSpecs"));
		            fetch=jsonNode.get("taskSpecs").toString();
		            
		           
		         }
		     
	String objJson="";
	objJson+="\"multiEngineResults\": {\"suspectResult\": \"\",\"cbvutvi4vResult\": \" \", \"wellknownResult\": \" \",\"uniqueResult\": \"\"}";
	JsonNode node2=new ObjectMapper().readTree(objJson);
	
	

				   
				//JSONParser parser = new JSONParser();  
				//JSONObject json = (JSONObject) parser.parse(objJson); 
			         ObjectMapper mapper1 = new ObjectMapper();
					 ObjectWriter writer = mapper1.writer(new DefaultPrettyPrinter());
					 writer.writeValue(new File("D:\\prac\\objJson.json"), node2);
					 System.out.println("JSON string write to a file successfully");
				   
				System.out.println("12357::: "+node2);
			
		
	    	
		      }	
	    	in.close();
	    }catch (Exception e) {
			e.printStackTrace();
		}

	}
}


