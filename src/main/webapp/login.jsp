<!DOCTYPE html>

<html lang="ja">
<head>
<meta charset="UTF-8">
<title>勤怠管理システム | ログイン</title>
<style>
body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f4f4f4; }
.login-container { background: white; padding: 2em; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; }
h1 { color: #333; margin-bottom: 1em; }
form { display: flex; flex-direction: column; }
input[type="text"], input[type="password"] { padding: 0.8em; margin-bottom: 1em; border: 1px solid #ddd; border-radius: 4px; }
input[type="submit"] { background-color: #007bff; color: white; padding: 0.8em; border: none; border-radius: 4px; cursor: pointer; transition: background-color 0.3s ease; }
input[type="submit"]:hover { background-color: #0056b3; }
.error-message { color: #dc3545; margin-top: 1em; }
</style>
</head>
<body>
<div class="login-container">
<h1>勤怠管理システム</h1>
<h2>ログイン</h2>
<form action="login" method="post">
<input type="text" name="username" placeholder="ユーザー名" required>
<input type="password" name="password" placeholder="パスワード" required>
<input type="submit" value="ログイン">
</form>
<% String error = (String) request.getAttribute("error"); %>
<% if (error != null) { %>
<p class="error-message"><%= error %></p>
<% } %>
</div>
</body>
</html>