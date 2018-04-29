package com.csce5013.rakib.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Created by Rakib on 4/28/2018.
 */
public class Test {
    @Autowired
    private static ApplicationContext appContext;

    public static void main(String[] args) {
        AWSManager awsManager = (AWSManager) appContext.getBean("awsManager");
        System.out.println("Lets see");
    }
}
