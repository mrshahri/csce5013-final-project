package com.csce5013.rakib.controllers;

import com.csce5013.rakib.models.Config;
import com.csce5013.rakib.models.InstanceModel;
import com.csce5013.rakib.models.ResponseMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@RequestMapping("/webservices")
@RestController
public class RESTController {
    @Autowired
    private AWSManager awsManager;

    @Autowired
    private Config config;

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RESTController.class);

    @RequestMapping(value = "/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InstanceModel> getInstances() throws IOException {

        // HACK ALERT: Couldn't fix Spring Boot AWSManager initialization, so using this hack
        awsManager.connectAWSIfNot();
        List<InstanceModel> instances = awsManager.getListOfCreatedInstances();

        // Update status of machines from PiServer
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(config.getPiServerStatusesURL());
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
                JsonElement element = jsonObject.get(instance.getMachineId());
                if (element != null) {
                    instance.setMachineStatus(element.getAsString());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to fetch machine status {}", e);
        } finally {
            response.close();
        }
        return instances;
    }

    @RequestMapping(value = "/instances/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage updateMachineStatus(@PathVariable("id") String id,
                                               @RequestBody String action) throws IOException {
        // First close the EC2 instance
        awsManager.connectAWSIfNot();
        if ("ON".equals(action)) {
            awsManager.startInstance(id);
        } else if ("OFF".equals(action)) {
            awsManager.stopInstance(id);
        } else {
            throw new RuntimeException("Wrong message body");
        }

        // Update PiServer for new Machine Status
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut request = new HttpPut(config.getPiServerUpdateStatusURL()
                + awsManager.getMachineIdFromInstanceId(id));
        request.addHeader("accept", "application/json");
        request.setEntity(new StringEntity(action, "utf-8"));
        CloseableHttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResponse("Updated");
        return responseMessage;
    }
}
