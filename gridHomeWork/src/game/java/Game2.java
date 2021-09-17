package game.java;
import java.util.Scanner;
public class Game2 {
	static int a;
	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		System.out.println("This program is a computer game.");  
		System.out.println("Please, type inan integer in the range 1 ... 2147483646 : 17");
		a=sc.nextInt();
		System.out.println( "You typed in "+ a);
		a=a*a;
		int b=a+1;
		int c=b+1;
		System.out.print("my nos are "+  a);
		System.out.print(" "+ b);
		System.out.println(" "+ c);
		System.out.println("sry u lost the game");
		System.out.println("my nos are larger thaen urs");
		
	}

}
