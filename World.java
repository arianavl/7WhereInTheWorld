import java.util.*;

/**
 * Implementation of World
 * The main idea is reduce the forms of inputs.
 * Convert the form from 51° 28′ 38″ N, 51° 28′ 38″ E and the form 40 d 23 m 45 s N, 79 d 34 m 54 s W
 * to the form 40 36 45 N, 79 58 76 W. Then convert the form like 40 36 45 N, 79 58 76 W to the digital form。
 * The digital form is close to the standard form but maybe not in the correct length. For example 1.234567 -23.987654
 * or 1.2, -23.9.
 * Then convert them into standard from.
 * In this way, only need to deal with two cases, one is  standard form (or digital form) and
 * one is like 40 36 45 N, 79 58 76 W.
 * Finally, consider the invalid cases like a sentence or one string only combines many digits separately.
 *
 * @author Xiaoqian Yan ID: 6018373
 */
public class World {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String line = "";
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String res = line, arsterLat = "", areterLon = "";
            float latnum = 190, lonnum = 190;
            String latitude = "", longitude = "";
            // String latres = "", lonres = "";

            line = line.replaceAll(" +", " "); // Replace multiple spaces to one space
            line = line.replaceAll("\\++", ""); // Remove multiple positive sign
            line = line.replaceAll("-+", "-"); // Replace multiple negative sign to one

            if (isDigit(line)) {   // When  only digits in the line
                line = line.replaceAll(" ", ",");  // Replace single space to comma
                line = line.replaceAll(",+", ","); // Replace multiple comma to single comma
                String[] strline = line.split(",");
                if (strline.length != 2) {   // If the input is only one number or many number then print out.
                    System.out.println("Unable to process: " + res);
                    continue;
                } else {
                    latnum = Float.parseFloat(strline[0]);
                    if (Math.abs(latnum) <= 90) {  // Make sure the latitude is less than or equals 90.
                        lonnum = Float.parseFloat(strline[1]);
                    } else {
                        latnum = Float.parseFloat(strline[1]);
                        lonnum = Float.parseFloat(strline[0]);
                    }
                }

            } else if (checkCoordinate(line) || line.contains("d") || line.contains("°")) {
                // Convert all 51° 28′ 38″ N, 51° 28′ 38″ E form and 40 d 23 m 45 s N, 79 d 34 m 54 s W form
                // to 40 36 45 N, 79 58 76 W, form
                //line = line.replaceAll(" +", " "); // Replace multiple spaces to one space
                line = line.replaceAll("°+", "°"); // Replace multiple degrees to single
                line = line.replaceAll("′+", "′"); // Replace multiple minutes to single
                line = line.replaceAll("″+", "″"); // Replace multiple seconds to single
                line = line.replaceAll("d+", "d"); // Replace multiple degrees to single
                line = line.replaceAll("m+", "m"); // Replace multiple minutes to single
                line = line.replaceAll("s+", "s"); // Replace multiple seconds to single
                line = line.replaceAll("° ", ";"); // Replace degrees with one spaces to semicolon
                line = line.replaceAll("′ ", ";"); // Replace minutes with one spaces to semicolon
                line = line.replaceAll("″ ", ";"); // Replace seconds with one spaces to semicolon
                line = line.replaceAll(" d ", ";"); // Replace degrees with two spaces to semicolon
                line = line.replaceAll(" m ", ";"); // Replace minutes with two spaces to semicolon
                line = line.replaceAll(" s ", ";"); // Replace seconds with two spaces to semicolon

                line = line.toLowerCase();  // To lower case to reduce cases
                line = replaceCoorinate(line); // Add comma in case of the comma missing.
                line = line.replaceAll(";", " ");  // Replace semicolon to single spaces
                // line = line.replaceAll(",,", ","); // Replace comma just in case there is one in the line already and add again in front.

                line = line.replaceAll(" +", " "); // Replace multiple spaces to one space just in case
                line = line.replaceAll(", ", ","); // Remove the space after the comma.
                line = line.replaceAll(",+", ","); // Replace multiple comma to single comma
                // After this step, can make sure the latitude and longitude is separated by comma.
                // Degrees, minutes and seconds are separated by single space.

                // Split the coordinate by ","
                String[] strline = line.split(",");
                if (strline.length != 2 || !Character.isDigit(strline[0].charAt(0)) && strline[0].charAt(0) != '-' ||
                        !Character.isDigit(strline[1].charAt(0)) && strline[1].charAt(0) != '-') {  // If there are many words in one sentence or
                    System.out.println("Unable to process: " + res);
                    continue;
                }

                for (int i = 0; i < strline.length; i++) {
                    // If the form is 40 36 45 N 79 58 76 W convert to the standard form
                    if ((strline[i].contains("N") || strline[i].contains("S") || strline[i].contains("E") ||
                            strline[i].contains("W")) && strline[i].contains("-")) break;
                    if (strline[i].contains("N") || strline[i].contains("S")) {
                        latitude = strline[i];
                        if (latitude.contains("S")) latitude = "-" + latitude.substring(0, latitude.length() - 1);
                        else latitude = latitude.substring(0, latitude.length() - 1);
                        if (degreeInRange(latitude)) {  // If minutes and seconds less than or equals 60 just convert.
                            latnum = convertDMSToDD(latitude);
                        } else {                         // If minutes and seconds larger than 60 then asterisk.
                            int[] ars = uncertainPart(latitude);
                            latnum = convertDMSToDD(latitude);
                            arsterLat = isStandard(latnum);
                            if (ars.length == 2 && ars[0] != 0 && ars[1] != 0) arsterLat = arsterLat + " m* s*";
                            else if (ars[0] == 1) arsterLat = arsterLat + " m*";
                            else if (ars[1] == 2) arsterLat = arsterLat + " s*";
                        }

                    } else if (strline[i].contains("E") || strline[i].contains("W")) {
                        longitude = strline[i];
                        if (longitude.contains("W"))
                            longitude = "-" + longitude.substring(0, longitude.length() - 1);
                        else longitude = longitude.substring(0, longitude.length() - 1);
                        if (degreeInRange(longitude)) {
                            lonnum = convertDMSToDD(longitude);
                        } else {
                            int[] ars = uncertainPart(longitude);
                            lonnum = convertDMSToDD(longitude);
                            lonnum = warpLon(lonnum);
                            areterLon = isStandard(lonnum);
                            if (ars.length == 2 && ars[0] != 0 && ars[1] != 0) areterLon = areterLon + " m* s*";
                            else if (ars[0] == 1) areterLon = areterLon + " m*";
                            else if (ars[1] == 2) areterLon = areterLon + " s*";
                        }
                    }
                }

            } else {   // When the input is invalid then just print out, such as a sentence or a blank line
                System.out.println("Unable to process: " + res);
                continue;
            }

            if (lonnum < -180 || lonnum > 180) lonnum = warpLon(lonnum);
            if (standardLat(latnum) && standardLon(lonnum) && latnum != 190 && lonnum != 190 &&
                    arsterLat.equals("") && areterLon.equals("")) {
                System.out.println(isStandard(latnum) + ", " + isStandard(lonnum));
            } else if (!arsterLat.equals("") && !areterLon.equals("")) {
                System.out.println(arsterLat + ", " + areterLon);
            } else if (!arsterLat.equals("")) {
                System.out.println(arsterLat + ", " + isStandard(lonnum));
            } else if (!areterLon.equals("")) {
                System.out.println(isStandard(latnum) + ", " + areterLon);
            } else System.out.println("Unable to process: " + res);
        }

    }

    /**
     * Method for check the line contains the coordinate in different form.
     *
     * @param line The line read from scanner.
     * @return true if the line contains the coordinate in different form, else return false.
     */
    private static boolean checkCoordinate(String line) {

        if (line.contains("N") || line.contains("S") || line.contains("E") || line.contains("W") ||
                line.contains("n") || line.contains("s") || line.contains("e") || line.contains("w") ||
                line.contains("North") || line.contains("South") || line.contains("East") || line.contains("West") ||
                line.contains("north") || line.contains("south") || line.contains("east") || line.contains("west"))
            return true;
        return false;
    }

    /**
     * Method for replace the coordinate from different forms to one form.
     *
     * @param line The line read from scanner.
     * @return the line after replaced.
     */
    private static String replaceCoorinate(String line) {
        if (line.contains("north")) line = line.replaceAll("north", "N,");
        if (line.contains("south")) line = line.replaceAll("south", "S,");
        if (line.contains("east")) line = line.replaceAll("east", "E,");
        if (line.contains("west")) line = line.replaceAll("west", "W,");

        if (line.contains("n")) line = line.replaceAll("n", "N,");
        if (line.contains("s")) line = line.replaceAll("s", "S,");
        if (line.contains("e")) line = line.replaceAll("e", "E,");
        if (line.contains("w")) line = line.replaceAll("w", "W,");

        return line;
    }

    /**
     * Method for warpping around longitude
     *
     * @param lonnum The float longitude.
     * @return The longitude in range.
     */
    private static float warpLon(float lonnum) {
        if (lonnum < -180) {
            while (lonnum < -180) {
                lonnum += 360;
            }
        } else if (lonnum > 180) {
            while (lonnum > 180) {
                lonnum -= 360;
            }
        }
        return lonnum;
    }

    /**
     * Method for check minutes and seconds not greater than 60.
     *
     * @param str The string can be latitude or longitude.
     * @return true if minutes and seconds not greater than 60, else return false.
     */
    private static int[] uncertainPart(String str) {
        String[] strs = str.split(" ");
        int[] res = new int[strs.length - 1];
        float[] degree = {0, 0, 0};
        for (int i = 1; i < strs.length; i++) {
            degree[i] = Float.parseFloat(strs[i]);
            if (degree[i] > 60) res[i - 1] = i;
        }
        return res;
    }

    /**
     * Method for check minutes and seconds not greater than 60.
     *
     * @param str The string can be latitude or longitude.
     * @return true if minutes and seconds not greater than 60, else return false.
     */
    private static boolean degreeInRange(String str) {
        String[] strs = str.split(" ");
        float[] degree = {0, 0, 0};
        for (int i = 0; i < strs.length; i++) {
            degree[i] = Float.parseFloat(strs[i]);
        }
        return !(degree[1] > 60) && !(degree[2] > 60);
    }

    /**
     * Method for convert degrees, minutes and seconds to decimal degrees format.
     *
     * @param str The string can be latitude or longitude.
     * @return The decimal degrees format.
     */
    private static float convertDMSToDD(String str) {
        String[] strs = str.split(" ");
        float[] degree = {0, 0, 0};
        for (int i = 0; i < strs.length; i++) {
            degree[i] = Float.parseFloat(strs[i]);
        }
        return (float) (Math.signum(degree[0]) * (Math.abs(degree[0]) + (degree[1] / 60.0) + (degree[2] / 3600.0)));
    }

    /**
     * Method for check if the line is in words or sentence.
     *
     * @param line The line read from scanner.
     * @return true if the line is in alphabets.
     */
    /*public static boolean isAlpha(String line) {
        return line.matches("[a-zA-Z\\p{Punct}]+");
    }*/

    /**
     * Method for check if the line only consist in digits.
     *
     * @param line The line read from scanner.
     * @return true if the line consist in digits.
     */
    public static boolean isDigit(String line) {
        return line.matches("[0-9-,. ]+");
    }

    /**
     * Method for determine latitude number is valid.
     *
     * @param latnum The latitude number.
     * @return true if valid, else false.
     */
    private static boolean standardLat(float latnum) {
        return Math.abs(latnum) <= 90;
    }

    /**
     * Method for determine longitude number is valid.
     *
     * @param lonnum The longitude number.
     * @return true if valid, else false.
     */
    private static boolean standardLon(float lonnum) {
        return Math.abs(lonnum) <= 180;
    }

    /**
     * Method for convert the number to the standard form.
     *
     * @param num The number which needs to convert.
     * @return The standard form in string.
     */
    private static String isStandard(float num) {
        String temp = String.valueOf(num);
        int len = 0;
        if (num >= 0 && num < 10) len = 8;
        else if ((num >= 10 && num < 100) || (num < 0 && num > -10)) len = 9;
        else if (num >= 100 || (num <= -10 && num > -100)) len = 10;
        else if (num <= -100) len = 11;
        char[] digits = new char[len];
        for (int i = 0; i < len; i++) {
            if (i < temp.length()) digits[i] = temp.charAt(i);
            else digits[i] = '0';
        }
        StringBuffer res = new StringBuffer();
        for (char d : digits) {
            res.append(d);
        }
        return res.toString();
    }
}