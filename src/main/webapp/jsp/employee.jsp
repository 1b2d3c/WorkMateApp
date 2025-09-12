<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>

<html lang="ja">
<head>
<meta charset="UTF-8">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=M+PLUS+Rounded+1c&family=Yusei+Magic&display=swap" rel="stylesheet">
<title>従業員ダッシュボード</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body class="employeepage">
	<header>
		<h1>従業員ダッシュボード</h1>
		<nav>
			<a href="${pageContext.request.contextPath}/logout">ログアウト</a>
		</nav>
	</header>
	<div class="container">
		<div class="status-box <c:if test="${status eq '出勤中'}">active</c:if><c:if test="${status eq '退勤済み'}">inactive</c:if>">
<h3>現在のステータス: <c:out value="${status}"/></h3>
<form action="${pageContext.request.contextPath}/employee" method="post">
<c:if test="${status eq '退勤済み'}">
<input type="hidden" name="action" value="check_in">
<button type="submit" class="action-button check-in">出勤</button>
</c:if>
<c:if test="${status eq '出勤中'}">
<input type="hidden" name="action" value="check_out">
<button type="submit" class="action-button check-out">退勤</button>
</c:if>
</form>
</div>

    <div class="content">
        <h2>自身の勤怠履歴</h2>
        <table>
            <thead>
                <tr>
                    <th>日付</th>
                    <th>出勤時刻</th>
                    <th>退勤時刻</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${attendanceList}" var="attendance">
                    <tr>
                        <td><c:out value="${attendance.checkInTime.toLocalDate()}"/></td>
                        <td><c:out value="${attendance.checkInTime.toLocalTime()}"/></td>
                        <td><c:out value="${attendance.checkOutTime != null ? attendance.checkOutTime.toLocalTime() : '打刻なし'}"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <h2 style="margin-top: 2em;">連絡事項</h2>
        <div class="message-list">
            <c:forEach items="${messages}" var="message">
                <div class="message-item">
                    <h3><c:out value="${message.priority}"/>: <c:out value="${message.messageText}"/></h3>
                    <p>期間: <c:out value="${message.startDatetime}"/> - <c:out value="${message.endDatetime}"/></p>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

</body>
</html>