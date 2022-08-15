package user;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;
import com.dpg7.main.State;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class LoginUser {
    private String userID;
    private String password;

    public void handleLogin() {
        Boolean areInputsValid = handleUserInputs();

        if (areInputsValid) {
            UserProfile userProfile = getUserProfile();

            // check if user doesn't exists in database
            if (userProfile == null) {
                // TODO :: JAY :: LOG THIS INFORMATION
                System.out.println("userId: " + userID + " is is not registered in the system.");
                return;
            }

            String hashedPassword = UserUtils.getSHA256EncodedString(password);
            // check if password is incorrect
            if (!hashedPassword.equals(userProfile.getHashedPassword())) {
                // TODO :: JAY :: LOG THIS INFORMATION
                System.out.println("Incorrect password.");
                return;
            }

            Random rand = new Random();
            // Generates a random number within rang [0 - 2]
            int randomQuestionNumber = rand.nextInt(3);

            String securityQuestion = "";
            String securityAnswer = "";
            switch (randomQuestionNumber) {
                case 0:
                    securityQuestion = UserUtils.getSecurityQuestion1();
                    securityAnswer = userProfile.getSecurityAnswer1();
                    break;
                case 1:
                    securityQuestion = UserUtils.getSecurityQuestion2();
                    securityAnswer = userProfile.getSecurityAnswer2();
                    break;
                case 2:
                    securityQuestion = UserUtils.getSecurityQuestion3();
                    securityAnswer = userProfile.getSecurityAnswer3();
                    break;

                default:
                    securityQuestion = UserUtils.getSecurityQuestion1();
                    securityAnswer = userProfile.getSecurityAnswer1();
                    break;
            }

            final Scanner sc = new Scanner(System.in);

            System.out.print(securityQuestion + " : ");
            String userEnteredAnswer = sc.nextLine();
            System.out.println();

            // Check is answer is incorrect
            if (!userEnteredAnswer.equals(securityAnswer)) {
                // TODO :: JAY :: LOG THIS INFORMATION
                System.out.println("Incorrect answer.");
                return;
            }

            State.getInstance().setIsUserLoggedIn(true);
            State.getInstance().setLoggedInUser(userProfile);

            // TODO :: JAY :: LOG THIS INFORMATION
            System.out.println("Login successful. Welcome " + State.getInstance().getLoggedInUser().getUserID());
        }
    }

    private UserProfile getUserProfile() {
        ArrayList<UserProfile> userProfileArrayLists = UserUtils.readUserProfile();

        int size = userProfileArrayLists.size();
        for (int i = 0; i < size; i++) {

            // Check if userId entered by the user is present in USER_PROFILE.txt
            if (userProfileArrayLists.get(i).getUserID().equals(userID)) {
                return userProfileArrayLists.get(i);
            }
        }

        return null;
    }

    private Boolean handleUserInputs() {

        final Scanner sc = new Scanner(System.in);

        System.out.print("Enter userId: ");
        userID = sc.nextLine();

        if (userID.isEmpty() || userID.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid userID: " + userID);
            return false;
        }

        System.out.print("Enter password: ");
        password = sc.nextLine();

        if (password.isEmpty() || password.trim().isEmpty()) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Invalid password");
            return false;
        }

        return true;
    }
}
