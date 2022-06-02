package WhereInTheWorldFinal;

import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.*;


/**
 * Class that takes a input from a user and converts it into standard Latitude and longatude Form
 * If able.
 * It then writes the valid coordinates into a geoJSON file
 */
public class WhereInTheWorld2 {
    private static final String FILE_START = "{\"type\":\"FeatureCollection\",\"features\":[";
    private static final String FILE_MIDDLE_START = "{\"type\":\"Feature\",\"properties\": {},\"geometry\": {\"type\": \"Point\",\"coordinates\": [";
    private static final String FILE_MIDDLE_END = "]}},";
    private static final String FILE_END = "]}}]}";
    private static final String STANDARD_LONG = "^-?([1-9]{1}[0-9]{1}|[0]{1,2}|[1-9]{1,2}|1[0-7][0-9]|180)([.][0-9]{6})$";
    private static final String STANDARD_LAT = "^-?([1-8]{1}[0-9]{1}|[0-9]{1,2}|90)([.][0-9]{6})$";
    private static final String STANDARD_WORD = "([a-zA-Z,]{2,20})$";
    private static String content = FILE_START;
    //private static String validCoords = "";
    private static String errorMessage = "";
    private static int nums, letters, total;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String[] latAndLong;
            String userInputOriginal = scan.nextLine();
            //System.out.println("\n" + userInputOriginal); //debugging
            String userInput = userInputOriginal;
            errorMessage = "";
            nums = 0;
            letters = 0;
            total = 0;


            userInput = userInput.replaceAll(",+", ","); // Replace multiple comma to single comma

            // If there is more than one ',' change them all into spaces
            if (userInput.indexOf(",") != 1) {
                int moreThanOneComer = userInput.indexOf(",", userInput.indexOf(",") + 1);
                if (moreThanOneComer != -1) {
                    userInput = userInput.replaceAll(",", " "); // Replace multiple comma to single comma
                }
            }

            userInput = userInput.replaceAll(", ", ","); // Replace multiple comma to single comma
            userInput = userInput.replaceAll("[°\'\"″]", "");

            //Get rid of all extra labels and convert compass lables into N, S, E, W
            // Seperated with spaces

            if (hasLetters(userInput) > 0) {
                userInput = replaceNESWCoorinate(userInput);
                if (userInput == "" || userInput == ",") {
                    errorMessage = "Unable to process: ";
                    System.out.println(errorMessage + userInputOriginal);
                    continue;
                }

            }
            
            //Check to see if array seems correct
            if (errorMessage != "") {
                System.out.println(errorMessage + userInputOriginal);
                continue;
            }
            //System.out.println("1: " + userInput); //debugging

            // Error checking
            if (userInput.length() == 0 || userInput.length() == 1) {
                System.out.println("Unable to process: " + userInputOriginal);

                continue;
            }

            userInput = userInput.replaceAll(",", " "); // Replace multiple comma to single comma
            userInput = userInput.replaceAll(" +", " "); // Replace multiple comma to single comma

            //Get rid of leading spaces
            userInput = removeLeadingSpaces(userInput);

            // Split into an array
            String[] userInputArray = userInput.split(" ");
            if (userInputArray.length < 2) {
                System.out.println("Unable to process: " + userInputOriginal);

                continue;
            }

            //System.out.println("2: " + Arrays.toString(userInputArray)); //debugging

            //Total
            total = userInputArray.length;

            //counts amount of letters and nums
            countLettersAndNums(userInputArray);

            if (nums % 2 != 0) {
                errorMessage = "Unable to process: ";
                System.out.println(errorMessage + userInputOriginal);
                continue;
            }

            //Convert different systems into lat and long
            latAndLong = convert(userInputArray);

            try {
                // Get rid of multiple '-'
                for (int i = 0; i < latAndLong.length; i++) {
                latAndLong[i] = latAndLong[i].replaceAll("-+", "-"); // Replace multiple comma to single comma
            }
            } catch (NullPointerException e) {
                //TODO: handle exception
                errorMessage = "Unable to process: ";
                System.out.println(errorMessage + userInputOriginal);
                continue;
            }

            

            //System.out.println("3: " + Arrays.toString(latAndLong)); //debugging

            //Check to see if array seems correct
            if (errorMessage != "") {
                System.out.println(errorMessage + userInputOriginal);
                continue;
            } else if (latAndLong.length == 0) {
                System.out.println("Unable to process: " + userInputOriginal);
                continue;
            } else if (latAndLong[0] == null) {
                System.out.println("Unable to process: " + userInputOriginal);
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
            if (latAndLong.length != 2) {
                errorMessage = "Unable to process: ";
                System.out.println(errorMessage + userInputOriginal);
                continue;
            }
            boolean latIsValid = Pattern.matches(STANDARD_LAT, latAndLong[0]);
            boolean longIsValid = Pattern.matches(STANDARD_LONG, latAndLong[1]);
            //If valid Write to geoJSON file
            if (latIsValid && longIsValid) {
                addToContent(latAndLong[0], latAndLong[1]);
                //validCoords = Arrays.toString(latAndLong);
                //System.out.println("Final: " + validCoords);
                writeToFile(content, "map.geojson");
            } else {
                errorMessage = "Unable to Process: ";
                //System.out.println("20: Lat and Long not valid" + Arrays.toString(latAndLong)); //Debugging
                System.out.println(errorMessage + userInputOriginal);
                continue;

            }

        }
        scan.close();
    }

    /**
     * Method that convert values into valid lat and long if possible
     * @param userInput the array to be converted
     * @return lat and long
     */
    private static String[] convert(String[] userInput) {
        // Determine what method to use to convert

        //Hass no letters
        if (letters == 0) {
            if (total == 2) { //Already in Lat and long
                return userInput;
            } else if (total == 4) { //In decimals and minutes
                return decAndMinsConvert(userInput);
            } else if (total == 6) { // In decimals, minutes, and seconds
                return decMinAndSecConvert(userInput);
            } else { // worng amount of values
                errorMessage = "Unable to process: ";
                return null;
            }
        } else if (letters > 0) {
            //userInput = checkOrderOfCompass(userInput);
            if (nums == 2) {
                return compasslatLongConvert(userInput);
            } else if (nums == 4) {
                return compassDegMinConvert(userInput);
            } else if (nums == 6) {
                return compassDegMinSecConvert(userInput);
            } else { // worng amount of values
                errorMessage = "Unable to process: ";
                return null;
            }
        }

        errorMessage = "Unable to Process: ";
        return null;

    }

    /**
     * Method to convert lat and long with compass lable into lat and long
     * @param userInput Array to be converted
     * @return converted array lat and Long
     */
    public static String[] compasslatLongConvert(String[] userInput) {
        String[] latAndLong = new String[2];

        if (letters == 2) { // replace both with "-"
            int z = 0;
            for (int i = 0; i < userInput.length; i++) {
                if (!Character.isLetter(userInput[i].charAt(0))) {
                    latAndLong[z] = "-" + userInput[i];
                    z++;
                }
            }
            return latAndLong;

        }

        // Find index of char and the char
        char ch = 0;
        int j = 1;
        for (int i = 0; i < userInput.length; i++) {
            if (Character.isLetter(userInput[i].charAt(0))) {
                ch = userInput[i].charAt(0);
                j = i;
            }
        }

        // Replace appropriate one with "-"
        int y = 0;
        if (ch == 'S') {
            for (int i = 0; i < 3; i++) {
                if (i != j) {
                    if (y == 0) {
                        latAndLong[y] = "-" + userInput[i];

                    } else {
                        latAndLong[y] = userInput[i];
                    }
                    y++;

                }
            }
        }

        y = 0;
        if (ch == 'W') {
            for (int i = 0; i < 3; i++) {
                if (i != j) {
                    if (y == 1) {
                        latAndLong[y] = "-" + userInput[i];
                    } else {
                        latAndLong[y] = userInput[i];
                    }
                    y++;
                }
            }
        }
        return latAndLong;
    }

    /**
    * Method to convert DegMin with compass lable into lat and long
    * @param userInput array to be converted
    * @return the converted array
    */
    public static String[] compassDegMinConvert(String[] userInput) {
        String[] latAndLongBefore = new String[4];

        //If both need to be negative
        if (letters == 2) {
            int z = 0;
            for (int i = 0; i < userInput.length; i++) {
                if (!Character.isLetter(userInput[i].charAt(0))) {
                    latAndLongBefore[z] = "-" + userInput[i];
                    latAndLongBefore[z + 1] = userInput[i + 1];
                    z += 2;
                    i += 2;
                }
            }
            //System.out.println("11: " + Arrays.toString(latAndLongBefore)); //debugging

            return decAndMinsConvert(latAndLongBefore);
        }

        // If only one is negative
        char ch = 0;
        int j = 1;
        for (int i = 0; i < userInput.length; i++) {
            if (Character.isLetter(userInput[i].charAt(0))) {
                ch = userInput[i].charAt(0);
                j = i; // char index
            }
        }

        try {
            int y = 0;
            if (ch == 'S') {
                for (int i = 0; i < total; i++) { //For every value in userArray
                    if (i != j) { //If index is not where char is
                        if (y == 0) { // If its the first index hence the S or N coordinate
                            latAndLongBefore[y] = "-" + userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                        } else {
                            latAndLongBefore[y] = userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                        }
                        y += 2;
                        i++;
                    }
                }
            }

            y = 0;
            if (ch == 'W') {
                for (int i = 0; i < total; i++) { //For every value in userArray
                    if (i != j) { //If index is not where char is
                        if (y == 0) { // If its the first index hence the S or N coordinate
                            latAndLongBefore[y] = userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                        } else {
                            latAndLongBefore[y] = "-" + userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                        }
                        y += 2;
                        i++;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            errorMessage = "Unable to process: ";
            return null;
        }

        //System.out.println("10: " + Arrays.toString(latAndLongBefore)); //debugging
        return decAndMinsConvert(latAndLongBefore);
    }

    /**
    * Method to convert DegMinSec with compass lable into lat and long
    * @param userInput array to be converted
    * @return the converted array
    */
    public static String[] compassDegMinSecConvert(String[] userInput) {
        String[] latAndLongBefore = new String[6];

        // Get rid of extra '-'
        for (int i = 0; i < userInput.length; i++) {
            userInput[i] = userInput[i].replaceAll("-+", "-"); // Replace multiple comma to single comma
        }


        //If both need to be negative
        if (letters == 2) {
            int z = 0;
            for (int i = 0; i < userInput.length; i++) {
                if (!Character.isLetter(userInput[i].charAt(0))) {
                    latAndLongBefore[z] = "-" + userInput[i];
                    latAndLongBefore[z + 1] = userInput[i + 1];
                    latAndLongBefore[z + 2] = userInput[i + 2];
                    z += 3;
                    i += 3;
                }
            }
            //System.out.println("11: " + Arrays.toString(latAndLongBefore)); //debugging

            return decMinAndSecConvert(latAndLongBefore);
        }

        // If only one is negative
        char ch = 0;
        int j = 1;
        for (int i = 0; i < userInput.length; i++) {
            if (Character.isLetter(userInput[i].charAt(0))) {
                ch = userInput[i].charAt(0);
                j = i; // char index
            }
        }

        try {
            int y = 0;
            if (ch == 'S') {
                for (int i = 0; i < total; i++) { //For every value in userArray
                    if (i != j) { //If index is not where char is
                        if (y == 0) { // If its the first index hence the S or N coordinate
                            latAndLongBefore[y] = "-" + userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                            latAndLongBefore[y + 2] = userInput[i + 2];
                        } else {
                            latAndLongBefore[y] = userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                            latAndLongBefore[y + 2] = userInput[i + 2];
                        }
                        y += 3;
                        i += 2;
                    }
                }
            }

            y = 0;
            if (ch == 'W') {
                for (int i = 0; i < total; i++) { //For every value in userArray
                    if (i != j) { //If index is not where char is
                        if (y == 0) { // If its the first index hence the S or N coordinate
                            latAndLongBefore[y] = userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                            latAndLongBefore[y + 2] = userInput[i + 2];
                        } else {
                            latAndLongBefore[y] = "-" + userInput[i];
                            latAndLongBefore[y + 1] = userInput[i + 1];
                            latAndLongBefore[y + 2] = userInput[i + 2];
                        }
                        y += 3;
                        i += 2;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            errorMessage = "Unable to process: ";
            return null;
        }

        //System.out.println("19: " + Arrays.toString(latAndLongBefore)); //debugging
        return decMinAndSecConvert(latAndLongBefore);
    }

    /**
     * Method to convert decimal and minutes into lat and long
     * @param userInput dec and mins to be converted
     * @return lat and long
     */
    public static String[] decAndMinsConvert(String[] userInput) {

        String[] latAndLong = new String[2];
        double lat = 0.0;
        int j = 0;

        // Get rid of extra '-'
        for (int i = 0; i < userInput.length; i++) {
            userInput[i] = userInput[i].replaceAll("-+", "-"); // Replace multiple comma to single comma
        }


        for (int i = 0; i < userInput.length; i += 2) {
            try {
                Double deg = Double.parseDouble(userInput[i]);
                Double min = Double.parseDouble(userInput[i + 1]);

                //Check values are valid
                if (min < 60 && deg < 90) {
                    //Trying to catch case if deg is negative - currently in process
                    if (deg < 0) {
                        deg = Double.parseDouble(userInput[i].substring(1));
                        lat = (deg + min / 60); //Convert
                        String latSt = "-" + Double.toString(lat);//Add negative to end
                        lat = Double.parseDouble(latSt);
                    } else {
                        lat = (deg + min / 60);
                    }
                } else {
                    errorMessage = "Unable to process: ";
                    return userInput;
                }
            } catch (NumberFormatException ex) {
                //System.out.println("latAndLong: " + Arrays.toString(latAndLong) + " userInput: " + Arrays.toString(userInput));  //debugging
                errorMessage = "Unable to process: ";
                return userInput;
            }

            latAndLong[j] = Double.toString(lat); //Add converted value to array
            //System.out.println("Hiiii " + Arrays.toString(latAndLong)); //debugging
            j++;
        }
        return latAndLong;
    }

    /**
     * Converts decimals minutes and seconds into lat and Long
     * @param userInput the values to be converted
     * @return the converted values
     */
    public static String[] decMinAndSecConvert(String[] userInput) {
        double lat = 0.0;
        String[] latAndLong = new String[2];
        int j = 0;
        for (int i = 0; i < userInput.length; i += 3) {
            try {
                Double deg = Double.parseDouble(userInput[i]);
                Double min = Double.parseDouble(userInput[i + 1]);
                Double sec = Double.parseDouble(userInput[i + 2]);

                //Check values are valid
                if (min < 60 && sec < 60) {

                    //Trying to catch case if deg is negative - currently in process
                    if (deg < 0) {
                        deg = Double.parseDouble(userInput[i].substring(1));
                        lat = (deg + min / 60 + sec / 3600); //Convert
                        String latSt = "-" + Double.toString(lat);//Add negative to end
                        lat = Double.parseDouble(latSt);
                    } else {

                        lat = (deg + min / 60 + sec / 3600); //Convert
                    }
                } else {
                    errorMessage = "Unable to process: ";
                    return userInput;
                }
            } catch (NumberFormatException e) {
                errorMessage = "Unable to process: ";
                return userInput;
            }
            latAndLong[j] = Double.toString(lat);
            j++;
        }
        return latAndLong;
    }

    /**
     * Method that counts the amount of letters and numbers in array
     * @param userInputArray The array to be counted
     */
    public static void countLettersAndNums(String[] userInputArray) {
        for (String string : userInputArray) {
            if (Character.isLetter(string.charAt(0))) {
                letters++;
            } else {
                nums++;
            }
        }
        return;
    }

    /**
     * Method for replace the coordinate from different forms to one form.
     *
     * @param userInput The line read from scanner.
     * @return the line after replaced.
     */
    private static String replaceNESWCoorinate(String userInput) {
        int n = 0, s = 0, w = 0, e = 0;
        if (userInput.contains("north")) {
            userInput = userInput.replaceAll("north", " N ");
        }
        if (userInput.contains("south")) {
            userInput = userInput.replaceAll("south", " S ");
        }
        if (userInput.contains("east")) {
            userInput = userInput.replaceAll("east", " E ");
        }
        if (userInput.contains("west")) {
            userInput = userInput.replaceAll("west", " W ");
        }

        if (userInput.contains("North")) {
            userInput = userInput.replaceAll("North", " N ");
        }
        if (userInput.contains("South")) {
            userInput = userInput.replaceAll("South", " S ");
        }
        if (userInput.contains("East")) {
            userInput = userInput.replaceAll("East", " E ");
        }
        if (userInput.contains("West")) {
            userInput = userInput.replaceAll("West", " W ");
        }

        // Remove labels
        userInput = userInput.replaceAll(STANDARD_WORD, ""); // Replace multiple comma to single comma
        //System.out.println("After replace standardWord: " + userInput); //Debugging
        
        //System.out.println("after getting rid of strings: " + userInput);  //debugging
        
        if (userInput.isEmpty()) {
            errorMessage = "Unable to process: ";
            return userInput;
        }
        
        if (userInput.contains("n")) {
            userInput = userInput.replaceAll("n", " N ");
        }
        if (userInput.contains("s")) {
            userInput = userInput.replaceAll("s", " S ");
        }
        if (userInput.contains("e")) {
            userInput = userInput.replaceAll("e", " E ");
        }
        if (userInput.contains("w")) {
            userInput = userInput.replaceAll("w", " W ");
        }
        
        //System.out.println("Before checkOrderOfNSEW: " + userInput); //debugging
        
        userInput = checkOrderOfNSEW(userInput);
        
        // Checks to see whether still valid
        if (errorMessage != "") {
            return userInput;
        }
        
        
        
        //System.out.println("After checkOrderOfNSEW: " + userInput); // debugging
        
        if (userInput.contains("N")) {
            n++;
            int index = userInput.indexOf("N", userInput.indexOf('N') + 1);
            if (index != -1) {
                n++;
            }
            userInput = userInput.replaceAll("N", "");
        }
        if (userInput.contains("S")) {
            s++;
            int index = userInput.indexOf("S", userInput.indexOf('S')+ 1);
            if (index != -1) {
                s++;
            }
            userInput = userInput.replaceAll("S", " S ");
        }
        if (userInput.contains("E")) {
            e++;
            int index = userInput.indexOf("E", userInput.indexOf('E')+ 1);
            if (index != -1) {
                e++;
            }
            //System.out.println("index: " + e);  //debugging
            userInput = userInput.replaceAll("E", "");
        }
        if (userInput.contains("W")) {
            w++;
            int index = userInput.indexOf("W", userInput.indexOf('W')+ 1);
            if (index != -1) {
                w++;
            }
            userInput = userInput.replaceAll("W", " W ");
        }
        
        //Checks NSEW are valid combinations - isnt working
        if (n > 1 || s > 1 || e > 1 || w > 1) {
            errorMessage = "Unable to process: ";
            return userInput;
        } else if ((n == 1 && s == 1) || (w == 1 && e == 1)) {
            errorMessage = "Unable to process: ";
            return userInput;
        }

        return userInput;
    }
    
    
    /**
    * Method that checks and corrects the order of NSEW
    * @param userInput
    * @return
    */
    public static String checkOrderOfNSEW(String userInput) {

        String[] twoD = { "", "" };
        // prep
        userInput = userInput.replaceAll(" +", " "); // Replace multiple comma to single comma
        userInput = removeLeadingSpaces(userInput); //Remove leading spaces

        if (userInput.contains(",")) {
            String[] userInputTemp = userInput.split(",");
            if (userInputTemp.length == 2) {
                twoD = checkOrderOfLatAndLong(userInputTemp);
                userInput = twoD[0] + " " + twoD[1];
                return userInput;
            } else {
                userInput = userInput.replaceAll(",", " ");
            }
            
        }
        //System.out.println("checkOrderOfNSEW prep: " + userInput);  //debugging

        //Trying to check oreder of compass and swap if need be
        String[] userInputTemp = userInput.split(" ");

        //System.out.println("After temp: " + Arrays.toString(userInputTemp));  //debugging

        int y = userInputTemp.length / 2;
        if (userInputTemp.length % 2 != 0) { // add char to nums
            String[] temp = new String[userInputTemp.length - 1];
            int z = 0;
            for (int i = 0; i < userInputTemp.length; i++) {
                if (Character.isLetter(userInputTemp[i].charAt(0))) {
                    if (i == 0) {
                        temp[z] = userInputTemp[i].charAt(0) + " " + userInputTemp[i + 1];
                        z++;
                        i++;
                    } else if (i == userInputTemp.length / 2) {
                        temp[z - 1] = userInputTemp[i - 1] + " " + userInputTemp[i].charAt(0);
                        //z++;
                    } else if (i == userInputTemp.length -1) {
                        temp[z - 1] += userInputTemp[i].charAt(0);
                        z++;
                        i++;
                    }
                } else {
                    temp[z] = userInputTemp[i];
                    z++;
                }
            }

            userInputTemp = temp;
            //System.out.println("After temp: " + Arrays.toString(userInputTemp));  //debugging

        }
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < userInputTemp.length / 2; i++) {
                if (j == 1) {
                    twoD[j] += userInputTemp[y] + " ";
                    y++;
                } else {

                    twoD[j] += userInputTemp[i] + " ";
                }
            }
        }
        //System.out.println("twoD 1: " + Arrays.toString(twoD)); //debugging
        twoD = checkOrderOfLatAndLong(twoD);

        userInput = twoD[0] + " " + twoD[1];

        return userInput;
    }

    /**
     * Method to check whether lat and long are in the correct order by looking
     * at whether it contains E, S, W, N
     * @param latAndLong the array to be checked
     * @return an Array in the correct order
     */

    public static String[] checkOrderOfLatAndLong(String[] latAndLong) {
        String[] validLatLong = new String[2];

        //Check to see if there is a negative and a lable that doesnt match on same value
            if (latAndLong[0].contains("-") && (latAndLong[0].contains("N") || latAndLong[0].contains("E"))) {
                errorMessage = "Unable to process: ";
                return latAndLong;
            } else if (latAndLong[1].contains("-") && (latAndLong[1].contains("N") || latAndLong[1].contains("E"))) {
                
            }

        for (int j = 0; j < latAndLong.length; j++) {
            
            if ((latAndLong[j].contains("N") || latAndLong[j].contains("S")) && j != 0) {
                validLatLong = swapCoords(latAndLong);
                //System.out.println("swapped coords");//Debugging
                //System.out.println("24: " + Arrays.toString(validLatLong)); //Debugging
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
     * Recursive method to remove any leading spaces
     * @param userInput string to remove leading spaces
     * @return the same string without leading spaces
     */
    public static String removeLeadingSpaces(String userInput) {
        if (userInput.charAt(0) == ' ') {
            userInput = userInput.substring(1);
            removeLeadingSpaces(userInput);
        }
        return userInput;
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

}
