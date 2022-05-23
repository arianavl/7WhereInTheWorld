

import java.util.*;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.*;

/**
 * Inputs that still dont work
 * 12, w, 12, S
 * 
 * Still haven't done:
 * Degress and decimal minutes form
 * 
 * Fixed:
 * -12W, 12 - Not swapping
 * -12E, 12S - fixed (Cannot Convert because -12E is invalid)
 * 12 W 12
 * 12 12 W
 * 12 n 12 s
 * E 12 S 12
 * 12 12 12 12 12 12
 * 12 12 12 E 12 12 12 S
 * 
 * Degrees symbol = option shift 8
 * 
 */

/**
 * whereInTheWorld
 * Thinking of just making alot of regexs and if matches then converts into
 * Standard format 
 */
public class whereInTheWorld {
    private static final String FILE_START = "{\"type\":\"FeatureCollection\",\"features\":[";
    private static final String FILE_MIDDLE_START = "{\"type\":\"Feature\",\"properties\": {},\"geometry\": {\"type\": \"Point\",\"coordinates\": [";
    private static final String FILE_MIDDLE_END = "]}},";
    private static final String FILE_END = "]}}]}";
    private static final String STANDARD_LONG = "^-?([1-9]{1}[0-9]{1}|[0]{1,2}|[1-9]{1,2}|1[0-7][0-9]|180)([.][0-9]{6})$";
    private static final String STANDARD_LAT = "^-?([1-8]{1}[0-9]{1}|[0]{1,2}|90)([.][0-9]{6})$";
    private static String content = FILE_START;
    private static String validCoords = "";
    private static String errorMessage = "";


    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String[] latAndLong;
            String userInput = scan.nextLine();
            System.out.println();//debugging

            System.out.println("userInput: " + userInput); //Debugging
            errorMessage = "";
            userInput = userInput.replaceAll(",+", ","); // Replace multiple comma to single comma
            userInput = userInput.replaceAll(" +", " "); // Replace multiple comma to single comma
            userInput = userInput.replaceAll("-+", "-"); // Replace multiple comma to single comma

            int numSpaces = 0;
            for (int i = 0; i < userInput.length(); i++) {
                if (userInput.charAt(i) == ' ') {
                    numSpaces++;
                }
            }

            if ((userInput.contains("°") || userInput.contains("\'") || userInput.contains("″")
                    || userInput.contains("\"")) || numSpaces > 3) {
                latAndLong = seperateDegMinSec(userInput);
            } else {

                latAndLong = seperateIntoArray(userInput);
            }

            //Seperate into an array with Lat and Long in spereate spaces

            
            System.out.println("After Seperation Method: " + Arrays.toString(latAndLong)); //Debugging
            //Check to see if array seems correct
            if (errorMessage != "") {
                System.out.println(errorMessage + userInput);
                continue;
            }
            
            //System.out.println("1: " + Arrays.toString(latAndLong)); //Debugging
            
            // Check decimal Places
            for (int i = 0; i < latAndLong.length; i++) {
                //System.out.println("DECIMALS 8: " + latAndLong[i]); //Debugging
            
                String[] decimalArray = latAndLong[i].split("[.]");
                //System.out.println(Arrays.toString(decimalArray)); //Debugging
            
                if (decimalArray.length == 2) {
                    if (decimalArray[1].length() != 6) {
                        decimalArray[1] = checkDecimalAmount(decimalArray[1]);
                    }
                    latAndLong[i] = decimalArray[0] + "." + decimalArray[1];
                } else {
                    latAndLong[i] = decimalArray[0] + "." + "0".repeat(6);
                }
            
            }
            
            //Test once converted into standard form
            boolean latIsValid = Pattern.matches(STANDARD_LAT, latAndLong[0]);
            boolean longIsValid = Pattern.matches(STANDARD_LONG, latAndLong[1]);
            //If valid Write to geoJSON file
            if (latIsValid && longIsValid) {
                addToContent(latAndLong[0], latAndLong[1]);
                validCoords = Arrays.toString(latAndLong);
                System.out.println("Final: " + validCoords);
                writeToFile(content, "map1.geojson");
            } else {
                errorMessage = "Unable to Process: ";
                System.out.println("20: Lat and Long not valid" + Arrays.toString(latAndLong)); //Debugging
                System.out.println(errorMessage + userInput);
                continue;
            
            }
            
            //Code sees if valid lat and long if values are swapped
            /*else {
            // Check to see if coords were the wrong way around
            latIsValid = Pattern.matches(STANDARD_LONG, latAndLong[0]);
            longIsValid = Pattern.matches(STANDARD_LAT, latAndLong[1]);
            
            if (latIsValid && longIsValid) {
                latAndLong = swapCoords(latAndLong);
                validCoords = Arrays.toString(latAndLong);
                System.out.println(validCoords);
                writeToFile(content, "map1.geojson");
            
            }*/

            //System.out.println("lat: " + latIsValid);//Debugging
            //System.out.println("long: " + longIsValid);//Debugging

        }

        scan.close();

    }
    
    /**
     * Method to split Degrees, minutes and seconds into array
     * Representing lat and long
     * @param userInput The line read from user
     * @return an array of lat and long
     */
    private static String[] seperateDegMinSec(String userInput) {
        userInput = userInput.replaceAll("[°\'\"″]", "");
        userInput = userInput.replaceAll(",", "");
        String[] degMinSec = userInput.split(" ");
        String[] latAndLong = new String[2];
        Double lat = 0.0;
        int j = 0;

        //Calculations to convert to lat and long
        if (degMinSec.length == 6) {
            for (int i = 0; i < degMinSec.length; i += 3) {
                try {
                    Double deg = Double.parseDouble(degMinSec[i]);
                    Double min = Double.parseDouble(degMinSec[i + 1]);
                    Double sec = Double.parseDouble(degMinSec[i + 2]);

                    //Check values are valid
                    if (min < 60 && sec < 60) {
                        lat = (deg + min / 60 + sec / 3600);
                    } else {
                        errorMessage = "Unable to process: ";
                        return degMinSec;
                    }
                } catch (NumberFormatException e) {
                    errorMessage = "Unable to process: ";
                    return degMinSec;
                }

                latAndLong[j] = lat.toString(); //Add converted value to array
                //System.out.println("Hiiii " + Arrays.toString(latAndLong)); //debugging
                j++;
            }
        } else if (degMinSec.length == 8 && hasLetters(userInput) > 0) {
            int i = 0;
            int c = -1;
            String compass = "";
            if (Character.isLetter(userInput.charAt(0))) {
                c = 0;
                i = 1;
                compass += degMinSec[0];
            }
            while (i < degMinSec.length) {
                try {
                    Double deg = Double.parseDouble(degMinSec[i]);
                    Double min = Double.parseDouble(degMinSec[i + 1]);
                    Double sec = Double.parseDouble(degMinSec[i + 2]);

                    if (c == -1) {
                        c = 3;
                        compass = (degMinSec[c]);
                        c += 4;
                    } else {

                        compass = (degMinSec[c]);
                        c += 4;
                    }

                    //Check values are valid
                    if (min < 60 && sec < 60) {
                        lat = (deg + min / 60 + sec / 3600);
                    } else {
                        errorMessage = "Unable to process: ";
                        return degMinSec;
                    }
                } catch (NumberFormatException e) {
                    errorMessage = "Unable to process: ";
                    return degMinSec;
                }

                latAndLong[j] = lat.toString() + compass; //Add converted value to array + compass
                //System.out.println("Hiiii " + Arrays.toString(latAndLong)); //debugging
                j++;
                i += 4;
            }

            for (int z = 0; z < latAndLong.length; z++) {
                    latAndLong[z] = replaceNESWCoorinate(latAndLong[z]);
                }

                //Check order of lat and long
                latAndLong = checkOrderOfLatAndLong(latAndLong);

                //Convert E, S, W, N's
                for (int z = 0; z < latAndLong.length; z++) {
                    latAndLong[z] = convertNESWCoorinate(latAndLong[z]);
                    if (latAndLong[z] == null) {
                        errorMessage = "Unable to process: ";
                    }
                }
            
            
        } else {
            errorMessage = "Unable to process: ";
            return degMinSec;

        }



        return latAndLong;
    }

    /**
     * Method to split user input into an array of length 2
     * repesenting the latitude and the longatude
     *
     * @param userInput The line read from scanner.
     * @return an array of lat and long
     */
    private static String[] seperateIntoArray(String userInput) {
        //If has ',' split using that
        if (userInput.contains(",")) {
            //userInput = userInput.replaceAll(",+", ","); // Replace multiple comma to single comma

            // If it contains spaces, get rid of them
            if (userInput.contains(" ")) {
                userInput = userInput.replaceAll(" ", "");
            }
            String[] latAndLong = userInput.split(",");//Split into array
            
            //Looks for '-' - don't know why
            /*
            for (int i = 0; i < latAndLong.length; i++) {
                if ((latAndLong[i].substring(0, 1)).equals("-")) {
                    System.out.println(latAndLong[i]);
                } else {
                    
                }
            }
            */

            //Convert to E, S, W, N's
            for (int i = 0; i < latAndLong.length; i++) {
                latAndLong[i] = replaceNESWCoorinate(latAndLong[i]);
            }

            //Check order of lat and long
            latAndLong = checkOrderOfLatAndLong(latAndLong);
            //System.out.println("3: " + Arrays.toString(latAndLong)); //Debugging

            //Convert E, S, W, N's
            for (int i = 0; i < latAndLong.length; i++) {
                latAndLong[i] = convertNESWCoorinate(latAndLong[i]);
                if (latAndLong[i] == null) {
                    errorMessage = "Unable to process: ";
                }
            }

            return latAndLong;
            //If there is no ',' but there are spaces
        } else if (userInput.contains(" ")) {
            //If it also has letter e.g E, N, east, south etc.
            userInput = replaceNESWCoorinate(userInput);
            int amountOfLetters = hasLetters(userInput);
            if (amountOfLetters != 0) {

                String[] latAndLongAndCompass = userInput.split(" ");
                if (latAndLongAndCompass.length > 4) {
                    System.out.println("More then length 4 : " + Arrays.toString(latAndLongAndCompass)); //debugging
                    return latAndLongAndCompass;
                }
                System.out.println("5: " + Arrays.toString(latAndLongAndCompass)); //debugging
                String[] latAndLongCompassValid = new String[amountOfLetters + 2];
                int count = 0;
                //Get rid of extra spaces
                for (int i = 0; i < latAndLongAndCompass.length; i++) {
                    if (!latAndLongAndCompass[i].isBlank()) {
                        //System.out.println(latAndLongAndCompass[i]);
                        //Convert E, S, W, N's
                        //String value = replaceNESWCoorinate(latAndLongAndCompass[i]);

                        latAndLongCompassValid[count] = latAndLongAndCompass[i];
                        count++;
                    }
                }
                System.out.println("Compass before: " + Arrays.toString(latAndLongCompassValid));//debugging



                String[] latAndLong = new String[2];
                int coordsIndex = 0;
                int count2 = 0;
                System.out.println("latAndLong Before: " + Arrays.toString(latAndLong));//debugging
                for (int i = 0; i < latAndLongCompassValid.length; i++) {
                    //System.out.println(i); //debugging
                    //System.out.println(latAndLongCompassValid.length); //debugging

                    if (Character.isLetter(latAndLongCompassValid[i].charAt(0))) {
                        if (i == 0) { //If compass letters are before digit
                            coordsIndex = 1;
                        }
                        char compass = latAndLongCompassValid[i].charAt(0);
                        latAndLong[count2] = latAndLongCompassValid[coordsIndex] + compass;
                        count2++;
                    } else {
                        coordsIndex = i;
                    }
                }
                if (latAndLong[1] == null) {
                    latAndLong[1] = latAndLongCompassValid[coordsIndex];
                }
                System.out.println("7: " + Arrays.toString(latAndLong)); //Debugging

                //Check order of lat and long
                latAndLong = checkOrderOfLatAndLong(latAndLong);

                //Convert E, S, W, N's
                for (int i = 0; i < latAndLong.length; i++) {
                    latAndLong[i] = convertNESWCoorinate(latAndLong[i]);
                    if (latAndLong[i] == null) {
                        errorMessage = "Unable to process: ";
                    }
                }

                return latAndLong;

            //If it has spaces and only digits
        } else {
                
            // gets rid of leading spaces
                int index = 0;
                for (int i = 0; i < userInput.length(); i++) {
                    if (Character.isLetterOrDigit(userInput.charAt(i))) {
                        index = i;
                        i = userInput.length();
                    }
                }
                String userInputWithoutLeadingSpaces = userInput.substring(index);

                String[] latAndLong = userInputWithoutLeadingSpaces.split(" ");


                //String[] latAndLong = userInputWithoutLeadingSpaces.split(" ", 2);
                //System.out.println(latAndLong[1]);
                latAndLong[1] = latAndLong[1].replaceAll(" ", "");
                //System.out.println(latAndLong[1]);
                //Convert to E, S, W, N's
                for (int i = 0; i < latAndLong.length; i++) {
                    latAndLong[i] = replaceNESWCoorinate(latAndLong[i]);
                }

                //Check order of lat and long
                latAndLong = checkOrderOfLatAndLong(latAndLong);

                //Convert E, S, W, N's
                for (int i = 0; i < latAndLong.length; i++) {
                    latAndLong[i] = convertNESWCoorinate(latAndLong[i]);
                    if (latAndLong[i] == null) {
                        errorMessage = "Unable to process: ";
                    }
                }
                return latAndLong;
            }

        }
        String[] latAndLong = {};
        return latAndLong;
    }
    

    /**
     * Method to check whether lat and long are in the correct order by looking
     * at whether it contains E, S, W, N
     * @param latAndLong the array to be checked
     * @return an Array in the correct order
     */
    public static String[] checkOrderOfLatAndLong(String[] latAndLong) {
        String[] validLatLong = new String[2];
        int n = 0;
        int s = 0;
        int w = 0;
        int e = 0;
        for (int i = 0; i < latAndLong.length; i++) {
            if (latAndLong[i].contains("N")) {
                n++;
            } else if (latAndLong[i].contains("E")) {
                e++;
            } else if (latAndLong[i].contains("S")) {
                s++;
            } else if (latAndLong[i].contains("W")) {
                w++;
            }
        }
        if (n > 1 || s > 1 || e > 1 || w > 1) {
            errorMessage = "Unable to process: ";
            return latAndLong;
        } else if ((n == 1 && s == 1) || (w == 1 && e == 1)) {
            errorMessage = "Unable to process: ";
            return latAndLong;
        }
        for (int j = 0; j < latAndLong.length; j++) {

            if ((latAndLong[j].contains("N") || latAndLong[j].contains("S")) && j != 0) {
                validLatLong = swapCoords(latAndLong);
                //System.out.println("swapped coords");//Debugging
                //System.out.println(Arrays.toString(validLatLong)); //Debugging
                return validLatLong;

            } else if ((latAndLong[j].contains("W") || latAndLong[j].contains("E")) && j != 1) {
                validLatLong = swapCoords(latAndLong);
                //System.out.println("swapped coords");//Debugging
                //System.out.println("4: " + Arrays.toString(validLatLong)); //Debugging
                return validLatLong;

            } else {
                validLatLong = latAndLong;
                //return validLatLong;
            }
        }
    
        return validLatLong;
    }


    /**
     * Method to change N, E, S, W into digits
     *
     * @param userInput The line read from scanner.
     * @return the line after replaced.
     */
    private static String convertNESWCoorinate(String userInput) {
        //userInput = replaceNESWCoorinate(userInput);
        if (userInput.contains("N")) {
            if (userInput.contains("-")) {
                return null;
            }
            userInput = userInput.replaceAll("N", "");
            //System.out.println("N: " + userInput);//debugging
        }
        if (userInput.contains("S")) {
            userInput = userInput.replaceAll("S", "");
            userInput = "-" + userInput;
            //System.out.println("S: " + userInput);//debugging
        }
        if (userInput.contains("E")) {
            if (userInput.contains("-")) {
                return null;
            }
            userInput = userInput.replaceAll("E", "");
            //System.out.println("E: " + userInput);//debugging
        }
        if (userInput.contains("W")) {
            userInput = userInput.replaceAll("W", "");
            userInput = "-" + userInput;

            //System.out.println("W: " + userInput);//debugging
        }

        return userInput;
    }

    /**
     * Method for replace the coordinate from different forms to one form.
     *
     * @param userInput The line read from scanner.
     * @return the line after replaced.
     */
    private static String replaceNESWCoorinate(String userInput) {
        if (userInput.contains("north"))
            userInput = userInput.replaceAll("north", "N");
        if (userInput.contains("south"))
            userInput = userInput.replaceAll("south", "S");
        if (userInput.contains("east"))
            userInput = userInput.replaceAll("east", "E");
        if (userInput.contains("west"))
            userInput = userInput.replaceAll("west", "W");

        if (userInput.contains("North"))
            userInput = userInput.replaceAll("North", "N");
        if (userInput.contains("South"))
            userInput = userInput.replaceAll("South", "S");
        if (userInput.contains("East"))
            userInput = userInput.replaceAll("East", "E");
        if (userInput.contains("West"))
            userInput = userInput.replaceAll("West", "W");

        if (userInput.contains("n"))
            userInput = userInput.replaceAll("n", "N");
        if (userInput.contains("s"))
            userInput = userInput.replaceAll("s", "S");
        if (userInput.contains("e"))
            userInput = userInput.replaceAll("e", "E");
        if (userInput.contains("w"))
            userInput = userInput.replaceAll("w", "W");

        return userInput;
    }


    /**
     * Metjod to change the amount of decimals to 6 places
     * @param decimal the string to be changed
     * @return the correct amount of decimals
     */
    public static String checkDecimalAmount(String decimal) {
        int correct = 6;
        int current = decimal.length();
        int amountToAdd = correct - current;
        if (amountToAdd > 0) {
            decimal = decimal + "0".repeat(amountToAdd);
        }
        //DecimalFormat f = new DecimalFormat("000000");
        //decimal = f.format(Integer.parseInt(decimal));
        decimal = decimal.substring(0, 6);
        //System.out.println(decimal);
        return decimal;
    }

    /**
     * Adds valid Latitude and Longatude to content string
     * @param validLong
     * @param validLat
     */
    public static void addToContent(String validLong, String validLat) {
        if (content.charAt(content.length() - 1) != '[') {
            content = content + FILE_MIDDLE_END;
        }
        content = content + FILE_MIDDLE_START + validLong + ", " + validLat;
    }

    /**
     * Method that writes the new content into the file with file Name and 
     * Adds geoJSON formatting to beggining and end.
     * @param newContent
     * @param fileName
     */
    public static void writeToFile(String newContent, String fileName) {
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(newContent);
            myWriter.write(FILE_END);
            myWriter.close();
        } catch (Exception e) {
        }
    }


    /**
     * Method to check if any of the charaters in String input are letters
     * @param input String
     * @return whether it has letters
     */
    public static int hasLetters(String input) {
        int amountOfLetters = 0;
        for (int i = 0; i < input.length(); i++) {
            if (Character.isLetter(input.charAt(i))) {
                amountOfLetters++;
            }
        }
        return amountOfLetters;
    }


    /**
     * Method to swap the coordinates around if longatude was put first
     * @param coords the array to be swapped
     * @return the swapped array
     */
    public static String[] swapCoords(String[] coords) {
        String[] swappedCoords = new String[2];
        swappedCoords[0] = coords[1];
        swappedCoords[1] = coords[0];
        return swappedCoords;
    }
}

