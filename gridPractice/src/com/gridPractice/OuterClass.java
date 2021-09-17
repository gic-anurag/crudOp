package com.gridPractice;

public class OuterClass {
 String name;
	
	public OuterClass(String name) {
		this.name=name;
	}
	
	class InnerClass{
		
		
		public  void discloseSecret() {
			//OuterClass outer=new OuterClass("Anurag");
			
			if(name=="Anurag")
			System.out.println("Welcome to you in my Secrete");
		else 
			System.out.println("Sorry you are not Allow !!!!");
		}
	}
	
	public static void main(String args[]) {
		
		//OuterClass.InnerClass.discloseSecret();
		OuterClass.InnerClass outer=new OuterClass("Anurag").new InnerClass();
		outer.discloseSecret();
		
		
	}

}
