package com.csce5013.rakib.controllers;

import com.csce5013.rakib.models.InstanceModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@RequestMapping("/webservices")
@RestController
public class RESTController {
    @Autowired
    private AWSManager awsManager;

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RESTController.class);

    @RequestMapping(value = "/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InstanceModel> getInstances() throws IOException {

        // HACK ALERT: Couldn't fix Spring Boot AWSManager initialization, so using this hack
        awsManager.connectAWSIfNot();
        List<InstanceModel> instances = awsManager.getListOfCreatedInstances();

        // Update status of machines from PiServer
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet("http://localhost:8080/statuses");
        getRequest.addHeader("accept", "application/json");
        CloseableHttpResponse response = httpClient.execute(getRequest);
        try {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            JsonReader reader = new JsonReader(br);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            for (InstanceModel instance : instances) {
                instance.setMachineStatus(jsonObject.get(instance.getMachineId()).getAsString());
            }
        } catch (IOException e) {
            logger.error("Failed to fetch machine status {}", e);
        } finally {
            response.close();
        }

        return instances;
    }
}
