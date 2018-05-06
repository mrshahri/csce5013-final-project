package com.csce5013.rakib.controllers;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.FileNotFoundException;

/**
 * Created by Rakib on 5/25/2017.
 */
@Controller
public class ViewController {

    @Autowired
    private AWSManager awsManager;

    private static final String VIEW_INDEX = "index";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ViewController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(ModelMap model) throws FileNotFoundException {
        model.addAttribute("postUrl", "http://uaf132854.ddns.uark.edu:8100/app-ultimaker/operate-device");
        model.addAttribute("monitorUrl", "http://uaf132854.ddns.uark.edu:9002/virtualization-uark/monitor");
        model.addAttribute("instancesUrl", "http://uaf132854.ddns.uark.edu:10080/cpmc-aws/webservices/instances/");
        return VIEW_INDEX;
    }
}