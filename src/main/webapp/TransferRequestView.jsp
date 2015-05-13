<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="edu.rit.honors.gyfp.api.model.TransferRequest" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: regdoug
  Date: 5/13/15
  Time: 4:36 PM

  Displays outstanding transfer requests.  Backend is edu.rit.honors.gyfp.servlets.TransferRequestViewServlet
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Transfer Requests Outstanding | Gimme Your Files Please</title>

    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootswatch/3.2.0/cyborg/bootstrap.min.css" />
    <link rel="stylesheet" href="css/base.css" />

    <link rel="apple-touch-icon" sizes="57x57" href="<c:url value="/img/apple-icon-57x57.png"/>">
    <link rel="apple-touch-icon" sizes="60x60" href="<c:url value="/img/apple-icon-60x60.png"/>">
    <link rel="apple-touch-icon" sizes="72x72" href="<c:url value="/img/apple-icon-72x72.png"/>">
    <link rel="apple-touch-icon" sizes="76x76" href="<c:url value="/img/apple-icon-76x76.png"/>">
    <link rel="apple-touch-icon" sizes="114x114" href="<c:url value="/img/apple-icon-114x114.png"/>">
    <link rel="apple-touch-icon" sizes="120x120" href="<c:url value="/img/apple-icon-120x120.png"/>">
    <link rel="apple-touch-icon" sizes="144x144" href="<c:url value="/img/apple-icon-144x144.png"/>">
    <link rel="apple-touch-icon" sizes="152x152" href="<c:url value="/img/apple-icon-152x152.png"/>">
    <link rel="apple-touch-icon" sizes="180x180" href="<c:url value="/img/apple-icon-180x180.png"/>">
    <link rel="icon" type="image/png" sizes="192x192"  href="<c:url value="/img/android-icon-192x192.png"/>">
    <link rel="icon" type="image/png" sizes="32x32" href="<c:url value="/img/favicon-32x32.png"/>">
    <link rel="icon" type="image/png" sizes="96x96" href="<c:url value="/img/favicon-96x96.png"/>">
    <link rel="icon" type="image/png" sizes="16x16" href="<c:url value="/img/favicon-16x16.png"/>">
    <link rel="manifest" href="<c:url value="/img/manifest.json"/>">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="<c:url value="/img/ms-icon-144x144.png"/>">
    <meta name="theme-color" content="#ffffff">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <script type="application/javascript" src="js/third-party/angular/ui-utils/ui-utils-ieshiv.js"></script>
    <![endif]-->
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

            <img src="<c:url value="/img/android-icon-48x48.png"/>" alt="Gimme Your Files, Please Logo" class="logo" />
            <a class="navbar-brand" href="#">
                Gimme Your Files, Please!
            </a>
        </div>
        <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="/#/about">About</a></li>
            </ul>

            <div class="pull-right auth-container">
                <c:choose>
                <c:when test="${user ne null}">
                    <span>${user.email}</span>
                    <a href="${logoutURL}" class="btn btn-primary" type="button">
                        Log Out
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${loginURL}" class="btn btn-primary" type="button">
                        Log In
                    </a>
                </c:otherwise>
                </c:choose>
            </div>
        </div>
        <!--/.nav-collapse -->
    </div>
</div>

<div id="main" class="container">
<c:choose>
<c:when test="${validuser}">
    <h1>Transfer Requests</h1>
    <table class="table">
        <thead>
        <tr><th>Transfer Request ID</th><th>Requester</th><th>Recipient</th></tr>
        </thead>
        <tbody>
        <c:forEach items="${trlist}" var="tr">
            <tr>
                <td>${tr.id}</td>
                <td>${tr.requestingUser.name} (${tr.requestingUser.email})</td>
                <td>${tr.targetUser.name} (${tr.targetUser.email}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:when>
<c:otherwise>
    <h1>Not Authorized</h1>
    <p>You are not an authorized user. If you think this is a mistake, contact technology@honors.rit.edu</p>
</c:otherwise>
</c:choose>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" type="application/javascript"></script>
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js" type="application/javascript"></script>
</body>
</html>
