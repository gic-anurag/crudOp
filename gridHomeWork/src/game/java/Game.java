package game.java;
import java.util.Scanner;
public class Game {
	int a;
	
	public static void main(String[] args) {
		Game g=new Game();
		g.doubleValue();
		g.miniLarge();
		
	}
	public void doubleValue() {
		Scanner sc=new Scanner(System.in);
		System.out.println("enter a no");
		a=sc.nextInt();
		a=a*a;
		System.out.println("double"+  a);
	}
	public void miniLarge() {
		Scanner sc=new Scanner(System.in);
		System.out.println("enter a no");
		a=sc.nextInt();
		a=a+1;
		System.out.println("miniValue"+  a);
	}

}
