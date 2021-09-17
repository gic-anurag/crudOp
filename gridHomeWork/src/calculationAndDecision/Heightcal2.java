package calculationAndDecision;
import java.util.Scanner;
public class Heightcal2 {
	
	
	  
		public static void main(String[] args) {
			 double foot,inch;
			Scanner sc= new Scanner(System.in);
			System.out.println("enter foot and inches");
			foot=sc.nextDouble();
			inch=sc.nextDouble();
			foot=foot*30.48;
			inch=inch*2.54;
			
			System.out.println(foot+inch +"cm");
		}
	
	
	
	
	
	
	
	
	
	
}
