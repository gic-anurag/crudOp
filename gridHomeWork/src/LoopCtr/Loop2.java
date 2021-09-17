package LoopCtr;

public class Loop2 {
public static void main(String[] args) {
	System.out.println("kilometers          "+    "miles");
	for(int i=10;i<110;i=i+10) {
		double km=i;
		double miles=km*0.621371;
		
		System.out.println(km  +"          "+  miles);
	}
	m2();
}
public static void m2() {
	int i=10;
	System.out.println("miles          "+    "kilometers");
	do {
		double km=i;
		double miles=km*0.621371;
		
		System.out.println(km  +"          "+  miles);
		i=i+10;
	}while( i<110);
}
}
