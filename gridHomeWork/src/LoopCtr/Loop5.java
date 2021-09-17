package LoopCtr;
import java.util.Scanner;
public class Loop5 {

	public static void main(String[] args) {
          Scanner sc=new Scanner(System.in);
          System.out.println("Enter 1 for kilometer to mile");
          System.out.println("enter 2 for miles to kilometer");
          System.out.println("enter 3 for pounds to kilograms");
          System.out.println("enter 4 to exit");
          int op=sc.nextInt();
          switch(op) {
          case 1:
        	  System.out.println("kilometers          "+    "miles");
        		for(int i=10;i<110;i=i+10) {
        			double km=i;
        			double miles=km*0.621371;
        			
        			System.out.println(km  +"          "+  miles);
        		}
        	  break;
          case 2:
        	  System.out.println("miles          "+    "kilometers");
        		for(int i=10;i<110;i=i+10) {
        			int miles=i;
        			double km=i*1.609344;
        			System.out.println(miles  +"          "+  km);
        		}
          break;
          case 3:
        	  System.out.println("ponds          "+    "kilometers");
      		for(int i=10;i<110;i=i+10) {
      			double pond=i;
      			double km=pond*0.4536;
      			System.out.println(pond +"          "+  km);
      		}
          break;
          case 4:
        	  System.out.println("_");
        	  break;
        	  default :
        		  break;
          }

	}

}
