package Fixes.UselessObjects;

public class Bad {
	public static void main(String[] args) {
		MyClass obj = new MyClass("Aravind", 20);
		
		for(int i = 0;i < 10;i++) {
			//Some stuff
		}
		
		System.out.println(obj.getClass());
		
		for(int i = 0;i < 10;i++) {
			//some stuff
		}
		
		System.out.println(obj.getClass());
	}
}


