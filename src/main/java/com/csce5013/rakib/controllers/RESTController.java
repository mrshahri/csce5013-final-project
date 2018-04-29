package com.csce5013.rakib.controllers;

import com.csce5013.rakib.models.InstanceModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/webservices")
@RestController
public class RESTController {
    @Autowired
    private AWSManager awsManager;

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RESTController.class);

    @RequestMapping(value = "/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InstanceModel> getInstances() {

        // HACK ALERT: Couldn't fix Spring Boot AWSManager initialization, so using this hack
        awsManager.connectAWSIfNot();

        return awsManager.getListOfCreatedInstances();
    }
}
