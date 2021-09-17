package stset;
import java.util.Scanner;

import stpojo.Pojos;
public class Setter  {
	public static void main(String[] args) {
      Pojos po=new Pojos();

	Scanner sc=new Scanner(System.in);
    		  System.out.println("enter roll no");
       int roll=sc.nextInt();
      po.setRoll(roll);
      System.out.println("enter name of student");
      String name=sc.next();
      po.setName(name);
      System.out.println("roll="+ po.getRoll() +"name="+ po.getName());
}
}
