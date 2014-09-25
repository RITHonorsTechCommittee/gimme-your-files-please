<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	
	<title>Give me Your Files (Please)</title>
	
	<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootswatch/3.2.0/cyborg/bootstrap.min.css" />
	
	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
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

    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Gimme Your Files Please</a>
        </div>
        <div class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>

    <div id="main" class="container">
		<h1>Gimme Your Files Please!</h1>
		
		<div class="row">
			<c:forEach items="${users}" var="user">
				<div class="col-xs-6">
					<div class="panel panel-primary">
					  <div class="panel-heading">
					    <h3 class="panel-title">${user.name} <span class="badge email-address-badge">${user.email}</span></h3>
					  </div>
					  <div class="panel-body">
				  		<div class="container-fluid">
				  			<div class="row">
				  				<div class="col-xs-8">
								    <ul class="list-group">
									  <li class="list-group-item">
									    <span class="badge">${user.filesOwner}</span>
									    Files Owned
									  </li>
									  <li class="list-group-item">
									    <span class="badge">${user.filesWriter}</span>
									    Files with Write Permissions
									  </li>
									  <li class="list-group-item">
									    <span class="badge">${user.filesReader}</span>
									    Files with Read Permissions
									  </li>
									</ul>
								</div>
								<div class="col-xs-4">
									<div class="btn-group-vertical">
									  <a href="#" class="btn btn-success btn-sm">Ask Nicely</a>
									  <a href="#" class="btn btn-danger  btn-sm">Hostile Takeover</a>
									  <a href="#" class="btn btn-default  btn-sm">Remove User</a>
									</div>
								</div>
							</div>
						</div>
					  </div>
					</div>
				</div>
			</c:forEach>
		</div>
	</div>
	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</body>
</html>