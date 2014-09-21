<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<jsp:include page="includes/Header.jsp"></jsp:include>
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

	<jsp:include page="includes/MainMenu.jsp"></jsp:include>

    <div id="main" class="container">
		<h1><c:out value="${appName}" /></h1>
		
		<table class="table table-striped">
			<tr>
				<th> </th>
				<th>User</th>
				<th>Email</th>
				<th>Owner</th>
				<th>Writer</th>
				<th>Reader</th>
				<th>Actions</th>
			</tr>
			<c:forEach items="${users}" var="user">
				<tr>
					<td><input type="checkbox" /></td>
					<td><c:out value="${user.name}" /></td>
					<td><c:out value="${user.email}" /></td>
					<td><c:out value="${user.filesOwner}" /></td>
					<td><c:out value="${user.filesWriter}" /></td>
					<td><c:out value="${user.filesReader}" /></td>
					<td>
						<div class="btn-group">
							<c:if test="${user.filesOwner > 0}">
								<a href="#" class="btn btn-success btn-sm">Ask Nicely</a>
								<a href="#" class="btn btn-danger  btn-sm">Hostile Takeover</a>
							</c:if>
							<c:if test="${user.filesReader > 0 || user.filesWriter > 0}">
								<a href="#" class="btn btn-default  btn-sm">Remove User</a>
							</c:if>
						</div>
					</td>
				</tr>
			</c:forEach>
		</table>
			
	</div>
	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</body>
</html>