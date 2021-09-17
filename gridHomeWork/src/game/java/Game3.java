package game.java;

import java.util.Scanner;

public class Game3 {

	static int a;
	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		System.out.println("This program is a computer game.");  
		System.out.println("Please, type inan integer in the range 1 ... 2147483646 : 17");
		a=sc.nextInt();
		System.out.println( "You typed in "+ a);
		a=a/2;
		int b=a/2;
		int c=b/2;
		System.out.print("my nos are "+  a);
		System.out.print(" "+ b);
		System.out.println(" "+ c);
		System.out.println("u won the game");
		System.out.println("my nos are smaller thaen urs");
		
	}
	
}
