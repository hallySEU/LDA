package monash.edu.hally.main;

import monash.edu.hally.util.FilesUtil;

public class Test {

	public static void main(String[] args) {
		
		String token="123,";
		token=FilesUtil.tokenModify(token);
		System.out.println(token);;
	}

}
