import java.util.regex.*;


public class tester {

    private static final String STANDARD_WORD = "([a-zA-Z]{2,20})$";
    public static void main(String[] args) {
        System.out.println(Pattern.matches(STANDARD_WORD, "Dunedin"));
        System.out.println(Pattern.matches(STANDARD_WORD, "Bronw"));
        System.out.println(Pattern.matches(STANDARD_WORD, "19.02W"));
        System.out.println(Pattern.matches(STANDARD_WORD, "W"));
        System.out.println(Pattern.matches(STANDARD_WORD, "WW"));
        System.out.println(Pattern.matches(STANDARD_WORD, "W W"));
        System.out.println(Pattern.matches(STANDARD_WORD, "12.23 W"));

    }
}
