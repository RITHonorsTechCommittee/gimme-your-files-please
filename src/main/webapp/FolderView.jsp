<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<jsp:include page="includes/Header.jsp" />
</head>
<body>

	<jsp:include page="includes/MainMenu.jsp" />


	<div id="preload-cover" class="cover">
		<div class="loadcontainer">
			<div class="spinner">
				<div class="bounce1"></div>
				<div class="bounce2"></div>
				<div class="bounce3"></div>
			</div>
			<h1>Initializing</h1>
		</div>
	</div>

    <div id="main" class="container">
		<div ng-view></div>
	</div>
	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" type="application/javascript"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js" type="application/javascript"></script>

	<script src="https://apis.google.com/js/client.js?onload=init" type="application/javascript"></script>
</body>
</html>