<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>

<html lang="ja">
<head>
<meta charset="UTF-8">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=M+PLUS+Rounded+1c&family=Yusei+Magic&display=swap"
	rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
<title>WorkMate</title>
</head>
<body class="loginpage">
	<div class="login-container">
		<h1>WorkMate</h1>
		<form action="login" method="post">
			<input type="text" name="username" placeholder="ユーザー名" required>
			<input type="password" name="password" placeholder="パスワード" required>
			<input type="submit" value="login">
		</form>
		<% String error = (String) request.getAttribute("error"); %>
		<% if (error != null) { %>
			<p class="error-message"><%=error%></p>
		<% } %>
	</div>
</body>
</html>