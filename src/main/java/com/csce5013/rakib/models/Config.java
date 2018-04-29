package com.csce5013.rakib.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Rakib on 2/22/2016.
 */
public class Config {

    private String accessKey;
    private String secretKey;
    private String region;
    private String securityGroupName;
    private String securityKeyName;

    public Config() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("config.json").getFile());
            JsonReader reader = new JsonReader(new FileReader(file));
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            this.accessKey = jsonObject.get("access_key").getAsString();
            this.secretKey = jsonObject.get("secret_key").getAsString();
            this.region = jsonObject.get("region").getAsString();
            this.securityGroupName = jsonObject.get("security_group").getAsString();
            this.securityKeyName = jsonObject.get("security_key").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public String getSecurityKeyName() {
        return securityKeyName;
    }

/*
    // Test config
    public static void main(String[] args) {
        Config config = new Config();
        System.out.println(config.getAccessKey());
    }
*/
}
