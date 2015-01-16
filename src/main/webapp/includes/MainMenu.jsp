<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>

<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="#"><c:out value="${appName}" /></a>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<li class="active"><a href="#">Home</a></li>
				<li><a href="#/about">About</a></li>
				<li><a href="#contact">Contact</a></li>
			</ul>

			<div class="pull-right auth-container" ng-controller="AuthenticationController">
				<span ng-bind="{{displayName}}"></span>
				<button ng-show="!authenticated" class="btn btn-primary" type="button" ng-click="authenticate()" ladda="operationRunning" data-style="expand-right">
					Login
				</button>
				<button ng-show="authenticated" class="btn btn-primary" type="button" ng-click="deauthenticate()" ladda="operationRunning" data-style="expand-right">
					Log Out {{email}}
				</button>
			</div>

		</div>
		<!--/.nav-collapse -->
	</div>
</div>