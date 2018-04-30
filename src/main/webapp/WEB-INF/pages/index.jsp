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

    <!-- Optional JavaScript -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <%--<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>--%>
    <script src="<c:url value="/resources/js/jquery-3.3.1.min.js"/>"></script>
    <script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>

    <script>
        
        function toggleMachinePower(instanceId, currentState) {
            var nextState = 'ON';
            if (currentState === 'ON') {
                nextState = 'OFF';
            }
            $.ajax({
                url: "http://localhost:10080/app-opt-cloud/webservices/instances/" + instanceId,
                type: 'PUT',
                dataType: "json",
                data:nextState,
                success: function (data) {
                },
                complete: function (data) {
                    alert(data.responseText);
                },
                timeout: 200000  // two minutes
            });
        }
        
        function bringInstancesData() {
            $.ajax({
                url: "http://localhost:10080/app-opt-cloud/webservices/instances",
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
                var instance = instances[0];
                tableHtml.innerHTML += "<tr>";
                var buttonHtml = "";
                if (instance['machineStatus'] === 'ON') {
                    buttonHtml = "<td><button type=\"button\" class=\"btn btn-warning\" onclick=\"toggleMachinePower('"
                        + instance['instanceId'] + "','" + instance['machineStatus'] + "')\">Power OFF</button></td>";
                } else {
                    buttonHtml = "<td><button type=\"button\" class=\"btn btn-success\" onclick=\"toggleMachinePower('"
                        + instance['instanceId'] + "','" + instance['machineStatus'] + "')\">Power ON</button></td>";
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