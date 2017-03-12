package sy.test.temp;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class Test7 {

	public static void main(String[] args) {
		Date d = new Date();
		System.out.println(d);
		System.out.println(DateUtils.addMilliseconds(d, -1000));
		System.out.println(DateUtils.addMilliseconds(d, 1000));
	}

}
