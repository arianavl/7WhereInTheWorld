import java.util.*;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.*;


/**
 * One that wont work
 * 12 12 12 12Dunedin
 */
public class WhereInTheWorld2 {
    private static final String FILE_START = "{\"type\":\"FeatureCollection\",\"features\":[";
    private static final String FILE_MIDDLE_START = "{\"type\":\"Feature\",\"properties\": {},\"geometry\": {\"type\": \"Point\",\"coordinates\": [";
    private static final String FILE_MIDDLE_END = "]}},";
    private static final String FILE_END = "]}}]}";
    private static final String STANDARD_LONG = "^-?([1-9]{1}[0-9]{1}|[0]{1,2}|[1-9]{1,2}|1[0-7][0-9]|180)([.][0-9]{6})$";
    private static final String STANDARD_LAT = "^-?([1-8]{1}[0-9]{1}|[0-9]{1,2}|90)([.][0-9]{6})$";
    private static final String STANDARD_WORD = "([a-zA-Z]{2,20})$";
    private static String content = FILE_START;
    private static String validCoords = "";
    private static String errorMessage = "";
    private static int nums, letters, total;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String[] latAndLong;
            String userInputOriginal = scan.nextLine();
            System.out.println("\n" + userInputOriginal); //debugging
            String userInput = userInputOriginal;

            //Get rid of all extra labels and convert compass lables into N, S, E, W
            // Seperated with spaces
            userInput = replaceNESWCoorinate(userInput);
            System.out.println(userInput); //debugging

            userInput = userInput.replaceAll(",", " "); // Replace multiple comma to single comma
            userInput = userInput.replaceAll(" +", " "); // Replace multiple comma to single comma

            // Split into an array
            String[] userInputArray = userInput.split(" ");

            System.out.println(Arrays.toString(userInputArray)); //debugging

            //Total
            total = userInputArray.length;

            //counts amount of letters and nums
            countLettersAndNums(userInputArray);

            //Convert different systems into lat and long
            latAndLong = convert(userInputArray);

            System.out.println(Arrays.toString(latAndLong)); //debugging


            //Check to see if array seems correct
            if (errorMessage != "") {
                System.out.println(errorMessage + userInputOriginal);
                continue;
            } else if (latAndLong.length == 0) {
                System.out.println("Unable to process-1: " + userInputOriginal);
                continue;
            } else if (latAndLong[0] == null) {
                System.out.println("Unable to process-1: " + userInputOriginal);
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
                errorMessage = "Unable to process2: ";
                System.out.println(errorMessage + userInputOriginal);
                continue;
            }
            boolean latIsValid = Pattern.matches(STANDARD_LAT, latAndLong[0]);
            boolean longIsValid = Pattern.matches(STANDARD_LONG, latAndLong[1]);
            //If valid Write to geoJSON file
            if (latIsValid && longIsValid) {
                addToContent(latAndLong[0], latAndLong[1]);
                validCoords = Arrays.toString(latAndLong);
                System.out.println("Final: " + validCoords);
                writeToFile(content, "map1.geojson");
            } else {
                errorMessage = "Unable to Process3: ";
                System.out.println("20: Lat and Long not valid" + Arrays.toString(latAndLong)); //Debugging
                System.out.println(errorMessage + userInputOriginal);
                continue;

            }

        }
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
            
        }

        return null;
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
                    errorMessage = "Unable to process4: ";
                    return userInput;
                }
            } catch (NumberFormatException ex) {
                errorMessage = "Unable to process5: ";
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
    public static String [] decMinAndSecConvert(String [] userInput) {
        double lat = 0.0;
        String [] latAndLong = new String[2];
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
                    errorMessage = "Unable to process6: ";
                    return userInput;
                }
            } catch (NumberFormatException e) {
                errorMessage = "Unable to process7: ";
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
    public static void countLettersAndNums(String [] userInputArray) {
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
        if (userInput.contains("north"))
            userInput = userInput.replaceAll("north", " N ");
        if (userInput.contains("south"))
            userInput = userInput.replaceAll("south", " S ");
        if (userInput.contains("east"))
            userInput = userInput.replaceAll("east", " E ");
        if (userInput.contains("west"))
            userInput = userInput.replaceAll("west", " W ");

        if (userInput.contains("North"))
            userInput = userInput.replaceAll("North", " N ");
        if (userInput.contains("South"))
            userInput = userInput.replaceAll("South", " S ");
        if (userInput.contains("East"))
            userInput = userInput.replaceAll("East", " E ");
        if (userInput.contains("West"))
            userInput = userInput.replaceAll("West", " W ");

        String[] temp = userInput.split(" ");
        for (String string : temp) {
            if (Pattern.matches(STANDARD_WORD, string)) {
                userInput = userInput.replaceAll(string, "");

            }
        }

        if (userInput.contains("n"))
            userInput = userInput.replaceAll("n", " N ");
        if (userInput.contains("s"))
            userInput = userInput.replaceAll("s", " S ");
        if (userInput.contains("e"))
            userInput = userInput.replaceAll("e", " E ");
        if (userInput.contains("w"))
            userInput = userInput.replaceAll("w", " W ");

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
    
}
