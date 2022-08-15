package user;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RegisterUser {

    private String userID;
    private String password;
    private String hashedPassword;
    private String securityAnswer1;
    private String securityAnswer2;
    private String securityAnswer3;

    public void handleRegistration() {
        Boolean areInputsValid = handleUserInputs();

        if (areInputsValid) {
            // check if user exists in database
            if (checkIfUserExists()) {
                // TODO :: JAY :: LOG THIS INFORMATION
                System.out.println("UserId already exists.");
                return;
            }

            hashedPassword = UserUtils.getSHA256EncodedString(password);

            String separator = Globals.GET_SEPARATOR();
            String userProfileContent = userID + separator + hashedPassword + separator + securityAnswer1 + separator + securityAnswer2 + separator + securityAnswer3 + "\n";
            try {
                UserUtils.writeUserProfile(userProfileContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean checkIfUserExists() {
        ArrayList<UserProfile> userProfileArrayLists = UserUtils.readUserProfile();

        int size = userProfileArrayLists.size();
        for (int i = 0; i < size; i++) {
            if (userProfileArrayLists.get(i).getUserID().equals(userID)) {
                return true;
            }
        }

        return false;
    }

    private Boolean handleUserInputs() {

        final Scanner sc = new Scanner(System.in);

        System.out.print("Enter userId: ");
        userID = sc.nextLine();
        System.out.println();

        if (userID.isEmpty() || userID.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid input for userID");
            return false;
        }

        System.out.print("Enter password: ");
        password = sc.nextLine();
        System.out.println();

        if (password.isEmpty() || password.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid input for password");
            return false;
        }

        System.out.println(UserUtils.getSecurityQuestion1());
        securityAnswer1 = sc.nextLine();
        System.out.println();

        if (securityAnswer1.isEmpty() || securityAnswer1.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid input for answer");
            return false;
        }

        System.out.println(UserUtils.getSecurityQuestion2());
        securityAnswer2 = sc.nextLine();
        System.out.println();

        if (securityAnswer2.isEmpty() || securityAnswer2.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid input for answer");
            return false;
        }

        System.out.println(UserUtils.getSecurityQuestion3());
        securityAnswer3 = sc.nextLine();
        System.out.println();

        if (securityAnswer3.isEmpty() || securityAnswer3.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid input for answer");
            return false;
        }

        return true;
    }
}
