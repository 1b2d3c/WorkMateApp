<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
		<h1>従業員 <c:out value="${username}" /> さん</h1>
		<nav>
			<a href="${pageContext.request.contextPath}/logout">logout</a>
		</nav>
	</header>
	<c:if test="${!enabled}">
		<div class="disabled-message">
			<p>無効なアカウントでログインしています。勤怠操作はできません。</p>
		</div>
	</c:if>
	<div class="container">
		<c:if test="${enabled}">
			<div class="status-box <c:if test="${status eq '勤務中'}">active</c:if><c:if test="${status eq '退勤済み'}">inactive</c:if>">
				<h3>現在のステータス:<c:out value="${status}" /></h3>
				<form action="${pageContext.request.contextPath}/employee" method="post">
					<c:if test="${status eq '退勤済み'}">
						<input type="hidden" name="action" value="check_in">
						<button type="submit" class="action-button check-in">出勤</button>
					</c:if>
					<c:if test="${status eq '勤務中'}">
						<input type="hidden" name="action" value="check_out">
						<button type="submit" class="action-button check-out">退勤</button>
					</c:if>
				</form>
			</div>
		</c:if>

		<div class="content">
			<h2>Messages</h2>
			<div class="contentbox">
				<div class="message-list">
					<c:forEach items="${messages}" var="message">
						<div class="message-item">
							<h3>
								<c:choose>
									<c:when test="${message.priority == 'high'}">高</c:when>
									<c:when test="${message.priority == 'normal'}">中</c:when>
									<c:when test="${message.priority == 'low'}">低</c:when>
									<c:otherwise>不明</c:otherwise>
								</c:choose>
								:
								<c:out value="${message.messageText}" />
							</h3>
							<p class="message-period">
								表示期間:
								<c:out value="${message.startDatetime}" />
								-
								<c:out value="${message.endDatetime}" />
							</p>
						</div>
					</c:forEach>
					<c:if test="${empty messages}">
                        <li>現在、表示期間内の連絡事項はありません。</li>
                    </c:if>
				</div>
			</div>
			<h2>Status</h2>
			<div class="contentbox">
			    <p>総労働時間: <strong><c:out value="${totalWorkingTime}" /></strong></p>
                <h2>月次レポート</h2>
                    <div class="monthly-report">
                        <form action="${pageContext.request.contextPath}/employee" method="get">
                            <input type="hidden" name="action" value="monthly_summary">
                            <select name="year">
                                <c:forEach var="y" begin="2020" end="2025">
                                    <option value="${y}" <c:if test="${selectedYear == y}">selected</c:if>>${y}年</option>
                                </c:forEach>
                            </select>
                            <select name="month">
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" <c:if test="${selectedMonth == m}">selected</c:if>>${m}月</option>
                                </c:forEach>
                            </select>
                            <input type="submit" value="表示">
                        </form>
                        <c:if test="${not empty selectedYear}">
		                    <h3><c:out value="${selectedYear}" />年 <c:out value="${selectedMonth}" />月の集計</h3>
		                    <p>総労働時間: <strong><c:out value="${monthlyTotalTime}" /></strong></p>
		                    <p>出勤回数: <strong><c:out value="${checkInCount}" />回</strong></p>
		                </c:if>
                    </div>
			</div>
			<h2>Work Records</h2>
			<div class="contentbox">
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
								<td><c:out value="${attendance.checkInTime.toLocalDate()}" /></td>
								<td><c:out value="${attendance.checkInTime.toLocalTime()}" /></td>
								<td><c:out value="${attendance.checkOutTime != null ? attendance.checkOutTime.toLocalTime() : '勤務中'}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>

</body>
</html>