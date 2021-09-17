package LoopCtr;

public class Loop1 {
public static void main(String[] args) {
	System.out.println("miles          "+    "kilometers");
	for(int i=10;i<110;i=i+10) {
		int miles=i;
		double km=i*1.609344;
		System.out.println(miles  +"          "+  km);
	}
	m2();
}
public static void m2() {
	int j=0;
	System.out.println("miles          "+    "kilometers");

	while(j<110) {
		int miles=j;
		double km=j*1.609344;
		System.out.println(miles  +"          "+  km);
        j=j+10;
	}
}
}
