package com.csce5013.rakib.controllers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.csce5013.rakib.models.Config;
import com.csce5013.rakib.models.InstanceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

    public AmazonEC2 getEc2Client() {
        return ec2Client;
    }

    public AmazonEC2 connectAWSIfNot() {
        if (ec2Client == null) {
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
        }
        return ec2Client;
    }

    public String createInstance(String instanceName) {
        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(config.getAmiId())
                .withInstanceType(InstanceType.T1Micro)
                .withMaxCount(1)
                .withMinCount(1)
                .withSecurityGroups(config.getSecurityGroupName());
        RunInstancesResult run_response = ec2Client.runInstances(run_request);
        String instanceId = run_response.getReservation().getReservationId();
        Tag tag = new Tag()
                .withKey("Name")
                .withValue(instanceName);
        CreateTagsRequest tag_request = new CreateTagsRequest()
                .withTags(tag);
        CreateTagsResult tag_response = ec2Client.createTags(tag_request);
        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                instanceId, config.getAmiId());
        return instanceId;
    }

    public void startInstance(String instanceId) {
        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instanceId);
                    return request.getDryRunRequest();
                };
        DryRunResult dry_response = ec2Client.dryRun(dry_request);
        if (!dry_response.isSuccessful()) {
            System.out.printf(
                    "Failed dry run to start instance %s", instanceId);
            throw dry_response.getDryRunResponse();
        }
        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instanceId);
        ec2Client.startInstances(request);
        System.out.printf("Successfully started instance %s", instanceId);
    }

    public void stopInstance(String instanceId)
    {
        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instanceId);

                    return request.getDryRunRequest();
                };
        DryRunResult dry_response = ec2Client.dryRun(dry_request);
        if(!dry_response.isSuccessful()) {
            System.out.printf(
                    "Failed dry run to stop instance %s", instanceId);
            throw dry_response.getDryRunResponse();
        }
        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceId);
        ec2Client.stopInstances(request);
        System.out.printf("Successfully stop instance %s", instanceId);
    }

    public void rebootInstance(String instanceId) {
        RebootInstancesRequest request = new RebootInstancesRequest()
                .withInstanceIds(instanceId);
        RebootInstancesResult response = ec2Client.rebootInstances(request);
        System.out.printf("Successfully rebooted instance %s", instanceId);
    }

    public List<InstanceModel> getListOfCreatedInstances() {
        List<InstanceModel> models = new ArrayList<>();
        DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances();
        for (Reservation reservation : describeInstancesResult.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                InstanceModel model = new InstanceModel();
                model.setInstanceId(instance.getInstanceId());
                model.setInstanceStatus(instance.getState().getName());
                model.setMachineId(instance.getTags().get(0).withKey("Name").getValue());
                model.setMachineStatus("ON");
                models.add(model);
            }
        }
        return models;
    }
}
