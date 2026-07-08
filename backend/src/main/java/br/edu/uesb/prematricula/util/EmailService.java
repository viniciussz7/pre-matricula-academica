package br.edu.uesb.prematricula.util;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendFirstAccessToken(String email, String token) {

        System.out.println("============================");
        System.out.println("FIRST ACCESS EMAIL");
        System.out.println("To: " + email);
        System.out.println("Token: " + token);
        System.out.println("============================");
        
    }
}
