package com.rmkod.wixconnection.controllers;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/")
@CrossOrigin(originPatterns = { "http://localhost:*", "http://192.168.*" })
public class AuthController {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    static String APP_ID = "bc24c5d2-bcba-4be7-a562-5c5a631480d7";
    static String APP_SECRET = "c342857e-1f20-4587-8c40-dd4729288229";

    @PostMapping(value = "webhook-callback")
    public void webhookCallback(@RequestBody String token, @RequestHeader HashMap<Object, Object> headers) {
        System.out.println("webhook-callback");
        System.out.println(token);
        System.out.println(headers);
    }

    @PostMapping(value = "productchanged")
    public void productChanged(@RequestBody String token, @RequestHeader HashMap<Object, Object> headers) {
        System.out.println("productchanged");
        System.out.println(token);
        System.out.println(headers);
    }

    @GetMapping(value = "signup")
    public ResponseEntity<Object> signup(@RequestParam Map<String, String> params) {
        System.out.println("signup");
        System.out.println(params);

        String redirectUrl = "https://024de43f908d.ngrok.io/login";
        String token = params.get("token");
        String url = "https://www.wix.com/installer/install?token=" + token + "&appId=" + APP_ID + "&redirectUrl="
                + redirectUrl;

        System.out.println("redirecting to " + url);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @GetMapping(value = "login")
    public ResponseEntity<Object> login(@RequestParam Map<String, String> params) {
        System.out.println("login");
        System.out.println(params);
        String authorizationCode = params.get("code");
        System.out.println("authorizationCode = " + authorizationCode);
        Map<String, String> tokens = getTokensFromWix(authorizationCode);
        String refreshToken = tokens.get("refresh_token");
        String accessToken = tokens.get("access_token");

        System.out.println("=response=");
        System.out.println("refreshToken = " + refreshToken);
        System.out.println("accessToken = " + accessToken);

        getInstance(accessToken);

        String url = "https://www.wix.com/_api/site-apps/v1/site-apps/token-received??access_token=" + accessToken;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    public void getProducts(String refreshToken) {
        Map<String, String> tokens = getAccessToken(refreshToken);
        // const body = {
        // // *** PUT YOUR PARAMS HERE ***
        // };
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.wixapis.com/stores/v1/products/query"))
                .header("Authorization", tokens.get("access_token")).header("Content-Type", "application/json").build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public Map<String, String> getTokensFromWix(String authorizationCode) {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "authorization_code");
        data.put("client_id", APP_ID);
        data.put("client_secret", APP_SECRET);
        data.put("code", authorizationCode);
        Gson gson = new Gson();
        System.out.println("=payload=");
        System.out.println(gson.toJson(data));

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data)))
                .uri(URI.create("https://www.wix.com/oauth/access")).header("Content-Type", "application/json").build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Map<String, String> result = gson.fromJson(response.body(), new TypeToken<Map<String, String>>() {
            }.getType());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getInstance(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://dev.wix.com/api/v1/instance"))
                .header("Authorization", accessToken).build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("=getAccessToken=");
            System.out.println(response.statusCode());
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getAccessToken(String refreshToken) {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "refresh_token");
        data.put("client_id", APP_ID);
        data.put("client_secret", APP_SECRET);
        data.put("refresh_token", refreshToken);
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data)))
                .uri(URI.create("https://www.wix.com/oauth/access")).header("Content-Type", "application/json").build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("=getAccessToken=");
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Map<String, String> result = gson.fromJson(response.body(), new TypeToken<Map<String, String>>() {
            }.getType());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}