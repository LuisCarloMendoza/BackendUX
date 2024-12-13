package Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;

public class FirebaseInit {
    public static void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream("app/src/main/assets/projectux-q4-firebase-adminsdk-85x8w-fc87633b08.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase Initialized Successfully");
        } catch (Exception e) {
            System.err.println("Firebase Initialization Error: " + e.getMessage());
        }
    }
}
