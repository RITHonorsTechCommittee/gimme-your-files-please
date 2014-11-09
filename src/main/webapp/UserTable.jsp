<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<jsp:include page="includes/Header.jsp" />
    <style type="text/css">
    	div#main {
    		margin-top: 50px;
    	}
    	
    	.list-group-item {
    		padding: 5px 10px;
    	}
    	
    	.email-address-badge {
    		float: right;
    	}
    </style>

	<script type="application/javascript" src="js/third-party/angular/angular.js"></script>
	<script type="application/javascript" src="js/gyfp.js"></script>
</head>
<body>

	<jsp:include page="includes/MainMenu.jsp" />

    <div id="main" class="container">
		<h1><c:out value="${appName}" /></h1>
		
		<table class="table table-striped" ng-controller="FileListController as folder">
			<tr>
				<th> </th>
				<th>User</th>
				<th>Email</th>
				<th>Owner</th>
				<th>Writer</th>
				<th>Reader</th>
				<th>Actions</th>
			</tr>
			<tr ng-repeat="user in folder.users" ng-cloak>
				<td><input type="checkbox" ng-model="user.selected"/></td>
				<td ng-bind="user.name"></td>
				<td ng-bind="user.email"></td>
				<td ng-bind="user.files.owner.length"></td>
				<td ng-bind="user.files.reader.length"></td>
				<td ng-bind="user.files.writer.length"></td>
				<td>
					<div class="btn-group">
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-success btn-sm">Ask Nicely</a>
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-danger  btn-sm">Hostile Takeover</a>
						<a ng-if="user.files.reader.length + user.files.writer.length > 0" href="#" class="btn btn-default  btn-sm">Remove User</a>
					</div>
				</td>
			</tr>
		</table>
	</div>
	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" type="application/javascript"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js" type="application/javascript"></script>

	<script src="https://apis.google.com/js/client.js?onload=init" type="application/javascript"></script>
</body>
</html>