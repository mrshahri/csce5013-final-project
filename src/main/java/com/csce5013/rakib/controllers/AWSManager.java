package com.csce5013.rakib.controllers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.csce5013.rakib.models.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Created by Rakib on 4/28/2018.
 */
@Service
public class AWSManager {
    @Autowired
    private Config config;

    private AmazonEC2 ec2Client;

    public AWSManager() {
    }

    public AmazonEC2 getEc2Client() throws FileNotFoundException {
        if (ec2Client == null) {
            ec2Client = connectAWS();
        }
        return ec2Client;
    }

    private AmazonEC2 connectAWS() throws FileNotFoundException {
        // Set up the client
        AWSCredentials credentials = new BasicAWSCredentials(
                config.getAccessKey(),
                config.getSecretKey()
        );

        ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1).build();

        IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
        IpPermission ipPermission = new IpPermission()
                .withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
                .withIpProtocol("tcp")
                .withFromPort(80)
                .withToPort(80);
        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
                new AuthorizeSecurityGroupIngressRequest()
                        .withGroupName(config.getSecurityGroupName())
                        .withIpPermissions(ipPermission);
        ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

/*
        //        if ("".equals(properties.getProperty("key"))) {
        ImportKeyPairResult
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName(config.getSecurityKeyName());
        CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
        System.out.println("key=" + createKeyPairResult.getKeyPair().getKeyMaterial());
//            properties.setProperty("key", keyMaterial);
        File file = new File("cpmc-aws-key.pem");
        PrintWriter print = new PrintWriter(file);
        print.print(createKeyPairResult.getKeyPair().getKeyMaterial());
        print.close();
//        } else {
//
//        }
*/

        return ec2Client;
    }

    public void startInstance(String yourInstanceId, AmazonEC2 ec2Client) {
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(yourInstanceId);
        ec2Client.startInstances(startInstancesRequest);
    }

    public void stopInstance(String yourInstanceId, AmazonEC2 ec2Client) {
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(yourInstanceId);
        ec2Client.stopInstances(stopInstancesRequest);
    }

    public RebootInstancesResult rebootInstance(String yourInstanceId, AmazonEC2 ec2Client) {
        RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(yourInstanceId);
        RebootInstancesResult rebootInstancesRequest = ec2Client.rebootInstances(request);
        return rebootInstancesRequest;
    }
}
