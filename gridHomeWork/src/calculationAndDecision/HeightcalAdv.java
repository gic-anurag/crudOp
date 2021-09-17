package calculationAndDecision;
import java.util.Scanner;
public class HeightcalAdv {

	   double foot,inch,cm;
		
	   
	   public void inCm() {
			Scanner sc= new Scanner(System.in);
			System.out.println("enter foot and inches");
			foot=sc.nextDouble();
			inch=sc.nextDouble();
			foot=foot*30.48;
			inch=inch*2.54;
			System.out.println("foot="+ foot +"CM");
			System.out.println("inch="+ inch +"CM");
		}
		
		
		
		public void inFoot() {
			
			Scanner sc= new Scanner(System.in);
			System.out.println("enter in CM");
			 cm=sc.nextDouble();
			 foot=cm/30.48;
			  System.out.println(foot);
			  }
		
		
		
		public static void main(String[] args) {
			HeightCalc hc=new HeightCalc();
			Scanner sc=new Scanner(System.in);
			System.out.println("press 1 for cm to feet and inch ##### press 2 for feet and inch to cm");
			int option=sc.nextInt();
			switch(option){
				case 1:
					 hc.inFoot();
					 break;
				case 2:
		        	hc.inCm();
		        	break;
		        	default :
		        		System.out.println("invalid option");
		        		break;

			}
		}
}
	
	

