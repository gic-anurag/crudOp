package ArraysEx;

public class Arrays1 {
	public static void main(String[] args) {
	int[] ar= {13,15,18,24,65,98,25,24,34,78};
	int[] arr=new int[ar.length];
	for(int i=0;i<ar.length;i++) {
	System.out.print(" "+ ar[i]);
	arr[i]=ar[i];
	}
	}
}