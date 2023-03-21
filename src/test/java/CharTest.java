import org.junit.jupiter.api.Test;

public class CharTest {

    @Test
    public void listChars () {
        /*
        for (char c = 991; c < 2000; c++) {
            System.out.println((int) c + ": " + c);
	}
         */
	/*
	for (char c = 48; c <= 57; c++) {
	    System.out.print(c);
	}
	for (char c = 65; c <= 90; c++) {
	    System.out.print(c);
	}
	for (char c = 97; c <= 122; c++) {
	    System.out.print(c);
	}
	for (char c = 192; c <= 451; c++) {
	    System.out.print(c);
	}
 	*/
	/*
	for (char c = 913; c < 930; c++) {
		System.out.print(c);
    	}
	for (char c = 931; c <= 991; c++) {
	    System.out.print(c);
	}
	 */
	for ( char c = 992; c <= 1154; c++ ) {
	    System.out.println("case '" + c + "':");
	}
	for ( char c = 1162; c <= 1327; c++ ) {
	    System.out.println("case '" + c + "':");
	}
	for ( char c = 1329; c <= 1365; c++ ) {
	    System.out.println("case '" + c + "':");
	}
	for ( char c = 1377; c <= 1415; c++ ) {
	    System.out.println("case '" + c + "':");
	}
    }

}
