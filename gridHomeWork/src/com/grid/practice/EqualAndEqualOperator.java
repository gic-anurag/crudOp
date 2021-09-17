package com.grid.practice;

public class EqualAndEqualOperator {

	
	int val;
	public EqualAndEqualOperator() {
		
	}
	
public float showDifferenct(float f) {
//		
		return 123.0f;
	}
	
	
	public int showDifferenct() {
		
		return 123;
	}
	
	
	public static void main(String arg[]) {
		
		EqualAndEqualOperator e1=new EqualAndEqualOperator();
			e1.val=10;
			EqualAndEqualOperator e2=new EqualAndEqualOperator();
			e2.val=10;
			String s1="ram" ;
			String s2=new String("ram");
			System.out.println("is object is equal in equal method "+e1.equals(e2));
			System.out.println("is  string is equal in equal method  "+s1.equals(s2));
			System.out.println("is object is equal in == operator "+(e1==e2));
			System.out.println("is  string is equal in == operator "+(s1==s2));
			
			
	}
	
	
}
