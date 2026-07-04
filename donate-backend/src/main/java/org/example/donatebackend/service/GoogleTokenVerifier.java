package org.example.donatebackend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenVerifier {

    private final String CLIENT_ID = "146265469090-j2las3054h4cusc27umf2t7uppg48sdf.apps.googleusercontent.com";

    public GoogleIdToken.Payload verify(String idToken) throws Exception{
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(Collections.singleton(CLIENT_ID))
        .build();

        GoogleIdToken token = verifier.verify(idToken);
        if (token == null) {
            throw new Exception("Token verification failed");
        }
        return token.getPayload();
    }
}
