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

		.spinner {
			margin: 30px auto;
			width: 70px;
			text-align: center;
		}

		.spinner > div {
			width: 18px;
			height: 18px;
			background-color: #0099CC;

			border-radius: 100%;
			display: inline-block;
			-webkit-animation: bouncedelay 1.4s infinite ease-in-out;
			animation: bouncedelay 1.4s infinite ease-in-out;
			/* Prevent first frame from flickering when animation starts */
			-webkit-animation-fill-mode: both;
			animation-fill-mode: both;
		}

		.spinner .bounce1 {
			-webkit-animation-delay: -0.32s;
			animation-delay: -0.32s;
		}

		.spinner .bounce2 {
			-webkit-animation-delay: -0.16s;
			animation-delay: -0.16s;
		}

		@-webkit-keyframes bouncedelay {
			0%, 80%, 100% { -webkit-transform: scale(0.0) }
			40% { -webkit-transform: scale(1.0) }
		}

		@keyframes bouncedelay {
			0%, 80%, 100% {
				transform: scale(0.0);
				-webkit-transform: scale(0.0);
			} 40% {
				  transform: scale(1.0);
				  -webkit-transform: scale(1.0);
			  }
		}
    </style>

</head>
<body>

	<jsp:include page="includes/MainMenu.jsp" />

    <div id="main" class="container" ng-controller="FileListController" >
		<div class="modal fade" id="loadingModalDialog">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title">{{modal.title}}</h4>
					</div>
					<div class="modal-body">
						<p>{{modal.body}}</p>
						<div ng-show="modal.indeterminate">
							<div class="spinner">
								<div class="bounce1"></div>
								<div class="bounce2"></div>
								<div class="bounce3"></div>
							</div>
						</div>
						<div ng-show="!modal.indeterminate">
							<progressbar max="modal.maxValue" value="modal.progress"><span style="color:black; white-space:nowrap;">{{modal.progress}} / {{modal.maxValue}}</span></progressbar>
						</div>
					</div>
				</div>
			</div>
		</div>
		<h1><c:out value="${appName}" /></h1>

		<h2 ng-show="!loaded_users">Loading...</h2>
		<a ng-click="refresh()">Refresh</a>
		<table class="table table-striped" ng-cloak>
			<tr>
				<th><input type="checkbox" ng-model="selectAll" ui-indeterminate="isSelectAllIndeterminate()" ng-click="toggleSelectAll()"/> </th>
				<th>User</th>
				<th>Email</th>
				<th>Owner</th>
				<th>Reader</th>
				<th>Writer</th>
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
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-success btn-sm" ng-click="ask(user)"><span class="glyphicon glyphicon-transfer"></span></a>
						<a ng-if="user.files.owner.length > 0" href="#" class="btn btn-danger  btn-sm" ng-click="force(user)"><span class="glyphicon glyphicon-exclamation-sign"></span></a>
						<a ng-if="user.files.reader.length > 0" href="#" class="btn btn-default  btn-sm" ng-click="revoke('reader', user)"><span class="glyphicon glyphicon-eye-open"></span></a>
						<a ng-if="user.files.writer.length > 0" href="#" class="btn btn-default  btn-sm" ng-click="revoke('writer', user)"><span class="glyphicon glyphicon-pencil"></span></a>
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