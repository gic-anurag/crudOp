package calculationAndDecision;
import java.util.Scanner;
public class IdealWeight {

	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		System.out.println("enter height in foot");
		int h=sc.nextInt();
		IdealWeight iw=new IdealWeight();
		iw.idWt(h);
	}
	
	
	
	
        public void idWt(int h) {
        	if(h==1) {
        		System.out.println("weight is 2.50 kg to 3kg");
        	}
        	else if(h==2) {
        		System.out.println("weight is 5kg to 8kg");

        	}
        	else if(h==3) {
        		System.out.println("weight is 9kg to 13kg");

        	}
        	else if(h==4) {
        		System.out.println("weight is 15kg to 20kg");

        	}
        	else if(h==5) {
        		System.out.println("weight is 20kg to 30kg");

        	}
        	else if(h==6) {
        		System.out.println("weight is 30kg to 70kg" );

        	}
        	else if(h==7) {
        		System.out.println("weight is 50kg to 100kg");

        	}
        	else if (h>7) {
        		System.out.println("defaults are alredy defined");
        	}
        }

}
