<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="<c:url value="/resources/css/bootstrap.min.css"/>">

    <title>AWS Optimizer</title>
    <style>
        table {
            font-family: arial, sans-serif;
            border-collapse: collapse;
            width: 80%;
        }

        td, th {
            border: 1px solid #dddddd;
            text-align: left;
            padding: 8px;
        }

        tr:nth-child(even) {
            background-color: #dddddd;
        }
    </style>

    <script src="<c:url value="/resources/js/jquery-3.3.1.min.js"/>"></script>
    <script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>

    <script>
        var instance_id = '';
        var power_state = '';
        function startPrinting() {
            var parametersObj = {deviceId: "bukito", operationId: "startJob", parameters: []};
            var parameters = [];
            parameters.push({id: "material", name: "", type: "value", value: "PLA"})
            parameters.push({id: "quantity", name: "", type: "value", value: "1"})
            parameters.push({id: "objName", name: "", type: "value", value: 'Triangle'})
            parametersObj.parameters = parameters;
            var requestBody = JSON.stringify(parametersObj);
            $.ajax({
                type: 'POST',
                url: "${postUrl}",
                data: requestBody,
                success: function (data) {
                    alert('data: ' + data);
                },
                contentType: "application/json",
                dataType: 'json'
            });
        }

        function stopPrinting() {
            var parametersObj = {deviceId: "bukito", operationId: 'stopJob', parameters: []};
            var requestBody = JSON.stringify(parametersObj);
            $.ajax({
                type: 'POST',
                url: "${postUrl}",
                data: requestBody,
                success: function (data) {
                    alert('data: ' + data);
                },
                contentType: "application/json",
                dataType: 'json'
            });
        }

        function toggleMachinePower(instanceId, currentState) {
            instance_id = instanceId;
            power_state = 'ON';

            if (currentState === 'ON') {
                stopPrinting();
                console.log('Waiting to power off the machine for 2 minutes')
                power_state = 'OFF';
                setTimeout(powerMachine, 5000);
            } else {
                console.log('Waiting to power on the machine for 1 minutes')
                powerMachine();
                setTimeout(startPrinting, 6000);
            }
        }
        
        function powerMachine() {
            $.ajax({
                url: "${instancesUrl}" + instance_id,
                type: 'PUT',
                dataType: "json",
                data:power_state,
                success: function (data) {
                },
                complete: function (data) {
                    alert('Machine powered ' + power_state);
                },
                timeout: 200000  // two minutes
            });
        }
        
        function bringInstancesData() {
            $.ajax({
                url: "${instancesUrl}",
                dataType: "json",
                success: function (data) {
                },
                complete: function (data) {
                    instances = $.parseJSON(data.responseText);
                    bindTableHtml(instances);
                },
                timeout: 200000  // two minutes
            });
        }

        function bindTableHtml(instances) {
            var tableHtml = document.getElementById("instanceTable");
            tableHtml.innerHTML = "";
            tableHtml.innerHTML += "<tr>" +
                "<th>" + "Instance Id" + "</th>" +
                "<th>" + "Instance Status" + "</th>" +
                "<th>" + "Machine Id" + "</th>" +
                "<th>" + "Machine Status" + "</th>" +
                "<th>" + "Machine Action" + "</th>" +
                "</tr>";
            for (var i = 0; i < instances.length; ++i) {
                var instance = instances[i];
                tableHtml.innerHTML += "<tr>";
                var buttonHtml = "";
                if (instance['machineStatus'] === 'ON') {
                    buttonHtml = "<td><button type=\"button\" class=\"btn btn-warning\" onclick=\"toggleMachinePower('"
                        + instance['instanceId'] + "','" + instance['machineStatus'] + "')\">Stop Printing</button></td>";
                } else {
                    buttonHtml = "<td><button type=\"button\" class=\"btn btn-success\" onclick=\"toggleMachinePower('"
                        + instance['instanceId'] + "','" + instance['machineStatus'] + "')\">Start Printing</button></td>";
                }

                tableHtml.innerHTML += "<td>" + instance['instanceId'] + "</td>" +
                    "<td>" + instance['instanceStatus'] + "</td>" +
                    "<td>" + instance['machineId'] + "</td>" +
                    "<td>" + instance['machineStatus'] + "</td>" + buttonHtml;

                tableHtml.innerHTML += "</tr>";
            }
        }

        bringInstancesData();
        window.setInterval(bringInstancesData, 30000);
    </script>
</head>
<body>
<div class="container bg-faded">
    <h5 class="text-center">Table of AWS instances</h5>
    <div class="row">
        <div class="col text-center">
            <table id="instanceTable">
            </table>
        </div>
    </div>
</div>
</body>
</html>