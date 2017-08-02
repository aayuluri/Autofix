package Fixes.SwitchIsDefaultCaseLastCheck;

public class Bad {

    private static int count = 0;

    public static void main(String[] args) {
        System.out.println(count);
    }

	public void setcount(int count) {
		this.count = count;
	}

	public int getcount() {
		return count;
	}
}