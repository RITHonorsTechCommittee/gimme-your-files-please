<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

</head>
<body>

	<jsp:include page="includes/MainMenu.jsp" />

    <div id="main" class="container" ng-controller="FileListController" >
		<h1><c:out value="${appName}" /></h1>

		<h2 ng-show="!loaded_users">Loading...</h2>
		<table class="table table-striped" ng-cloak>
			<tr>
				<th><input type="checkbox" ng-model="selectAll" ui-indeterminate="isSelectAllIndeterminate()" ng-click="toggleSelectAll()"/> </th>
				<th>User</th>
				<th>Email</th>
				<th>Owner</th>
				<th>Writer</th>
				<th>Reader</th>
				<th>Actions</th>
			</tr>
			<tr ng-repeat="user in users" ng-cloak>
				<td><input type="checkbox" ng-model="user.selected"/></td>
				<td ng-bind="user.name"></td>
				<td ng-bind="user.email"></td>
				<td ng-bind="user.files.owner.length" popover="{{user.files.owner}}"></td>
				<td ng-bind="user.files.reader.length" popover="{{user.files.reader}}"></td>
				<td ng-bind="user.files.writer.length" popover="{{user.files.writer}}"></td>
				<td>
					<div class="btn-group">
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-success btn-sm" ng-click="ask(user)">Ask Nicely</a>
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-danger  btn-sm" ng-click="force(user)">Hostile Takeover</a>
						<a ng-if="user.files.reader.length + user.files.writer.length > 0" href="#" class="btn btn-default  btn-sm" ng-click="revoke(user)">Remove User</a>
					</div>
				</td>
			</tr>
		</table>
		<div>
			With selected...
			<div class="btn-group">
				<a href="#" ng-class="{'disabled': !isOwnerSelected()}" class="btn btn-success btn-sm" ng-click="askAll()">Ask Nicely</a>
				<a href="#" ng-class="{'disabled': !isOwnerSelected()}" class="btn btn-danger  btn-sm" ng-click="forceAll()">Hostile Takeover</a>
				<a href="#" ng-class="{'disabled': !isReadWriteSelected()}" class="btn btn-default  btn-sm" ng-click="revokeAll()">Remove User</a>
			</div>
		</div>
	</div>
	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" type="application/javascript"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js" type="application/javascript"></script>

	<script src="https://apis.google.com/js/client.js?onload=init" type="application/javascript"></script>
</body>
</html>