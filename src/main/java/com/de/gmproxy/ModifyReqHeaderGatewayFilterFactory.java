package com.de.gmproxy;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

@Component
public class ModifyReqHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ModifyReqHeaderGatewayFilterFactory.Config> {
    FirebaseApp firebaseApp;
    FirebaseAuth firebaseAuth;
    private final String AUTHORIZATION = "Authorization";
    private final String GM_AUDIENCE = "gm_audience";
    private final String ORIGINAL_REQUESTER = "Original-Requester";

    public ModifyReqHeaderGatewayFilterFactory() {
        super(Config.class);
        try {
            firebaseApp = FirebaseApp.initializeApp();
            firebaseAuth = FirebaseAuth.getInstance();
            // System.out.println("firebaseAuth: " + firebaseAuth);
        } catch (Exception e) {
            System.out.println("firebase constructor error: " + e.getMessage());
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authValue = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
            String email = getCallerEmail(authValue);
            String myIdToken = getIdToken();
            ServerHttpRequest request = exchange.getRequest().mutate().header(ORIGINAL_REQUESTER, email)
                    .header(AUTHORIZATION, "Bearer " + myIdToken).build();
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private String getCallerEmail(String authValue) {
        if (authValue == null) {
            return null;
        }
        String[] parts = authValue.split(" ");
        if (parts.length != 2) {
            return null;
        }
        String token = parts[1];
        // System.out.println("This is the token: " + token);
        try {
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
            // System.out.println("firebase token: " + firebaseToken);
            String email = firebaseToken.getEmail();
            System.out.println("This is the email: " + email);
            return email;
        } catch (Exception e) {
            System.out.println("Get caller email exception: " + e.getMessage());
            return null;
        }
    }

    private String getIdToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            String idToken = ((ServiceAccountCredentials) credentials).idTokenWithAudience(GM_AUDIENCE, null)
                    .getTokenValue();
            // System.out.println("idToken: " + idToken);
            return idToken;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static class Config {
    }
}