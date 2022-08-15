package user;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class UserUtils {
    private static String SECURITY_QUESTION_1 = "What city were you born in?";
    private static String SECURITY_QUESTION_2 = "What primary school did you attend?";
    private static String SECURITY_QUESTION_3 = "What was your childhood nickname?";

    public static String getSecurityQuestion1() {
        return SECURITY_QUESTION_1;
    }

    public static String getSecurityQuestion2() {
        return SECURITY_QUESTION_2;
    }

    public static String getSecurityQuestion3() {
        return SECURITY_QUESTION_3;
    }

    public static String getSHA256EncodedString(String password) {
        String encoded = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            encoded = Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encoded;
    }

    public static void writeUserProfile(String userProfileContent) throws IOException {
        File userProfileFile = new File(Globals.GET_USER_PROFILE_FILE());
        FileWriter fileWriter = new FileWriter(userProfileFile, true);
        fileWriter.write(userProfileContent);
        fileWriter.close();

        new FileProcessor(true).replaceFileInRemote(Globals.GET_USER_PROFILE_FILE());
    }

    public static ArrayList<UserProfile> readUserProfile() {
        File userProfileFile = new File(Globals.GET_USER_PROFILE_FILE());

        ArrayList<UserProfile> userProfileArrayList = new ArrayList<>();

        Scanner sc = null;
        try {
            sc = new Scanner(userProfileFile);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] splittedLine = line.split(Globals.GET_SEPARATOR());

                if (line != null && line.length() > 0 && (splittedLine.length > 0)) {
                    UserProfile userProfile = new UserProfile();

                    userProfile.setUserID(splittedLine[0]);
                    userProfile.setHashedPassword(splittedLine[1]);
                    userProfile.setSecurityAnswer1(splittedLine[2]);
                    userProfile.setSecurityAnswer2(splittedLine[3]);
                    userProfile.setSecurityAnswer3(splittedLine[4]);

                    userProfileArrayList.add(userProfile);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }

        return userProfileArrayList;
    }
}
