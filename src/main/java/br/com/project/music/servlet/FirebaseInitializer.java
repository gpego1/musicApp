package br.com.project.music.servlet;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {
    public static void initializeApp() throws IOException {
        String filePath = System.getenv("HOMEPATH") + "/Downloads/fiec2024-projeto.json";
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(
                        new FileInputStream(filePath)))

                .build();
        FirebaseApp.initializeApp(options);
    }
}


