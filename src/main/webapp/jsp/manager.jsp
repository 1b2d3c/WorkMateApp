<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>

<html lang="ja">
<head>
<meta charset="UTF-8">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=M+PLUS+Rounded+1c&family=Yusei+Magic&display=swap" rel="stylesheet">
<title>管理者ダッシュボード</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body class="managerpage">
	<header>
		<h1>管理者 <c:out value="${username}" /> さん</h1>
	</header>
	<nav>
		<a href="${pageContext.request.contextPath}/manager?action=view_attendance">勤怠管理</a>
		<a href="${pageContext.request.contextPath}/manager?action=view_users">ユーザー管理</a>
		<a href="${pageContext.request.contextPath}/manager?action=view_messages">連絡事項管理</a>
		<a href="${pageContext.request.contextPath}/logout">ログアウト</a>
	</nav>
	<div class="container">
		<div class="content">
			<c:choose>
				<c:when test="${page == 'attendance'}">
					<h2>勤怠管理</h2>
					<h3>勤怠履歴の追加/削除</h3>
					<div class="contentbox">
						<div class="form-section">
							<form action="manager" method="post">
								<input type="hidden" name="action" value="add_attendance">
								<input type="number" name="user_id" placeholder="ユーザーID" required>
								<input type="datetime-local" name="check_in" required>
								<input type="datetime-local" name="check_out">
								<input type="submit" value="追加">
							</form>
							<form action="manager" method="post" style="margin-top: 1em;">
								<input type="hidden" name="action" value="delete_attendance">
								<input type="number" name="attendance_id" placeholder="勤怠ID" required>
								<input type="submit" value="削除">
							</form>
						</div>
					</div>
					<h3>従業員全体の勤怠履歴</h3>
					<div class="contentbox">
					</div>
					<h4>詳細勤怠履歴</h4>
					<div class="contentbox">
						<div class="form-section">
							<form action="manager" method="get">
								<input type="hidden" name="action" value="user_attendance">
								<input type="text" name="user_id" placeholder="ユーザーIDを入力">
								<input type="submit" value="検索">
							</form>
						</div>
						<table>
							<thead>
								<tr>
									<th>ID</th>
									<th>ユーザーID</th>
									<th>出勤時刻</th>
									<th>退勤時刻</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${attendanceList}" var="attendance">
									<tr>
										<td><c:out value="${attendance.attendanceId}" /></td>
										<td><c:out value="${attendance.userId}" /></td>
										<td><c:out value="${attendance.checkInTime}" /></td>
										<td><c:out value="${attendance.checkOutTime != null ? attendance.checkOutTime : '-'}" /></td>
										<td>
											<form action="manager" method="post" style="display:inline;">
												<input type="hidden" name="action" value="delete_attendance">
												<input type="hidden" name="attendance_id" value="${attendance.attendanceId}">
												<input type="submit" value="削除">
											</form>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</c:when>
				<c:when test="${page == 'users'}">
					<h2>ユーザー管理</h2>
					<div class="form-section">
						<h3>ユーザー新規作成</h3>
						<form action="manager" method="post">
							<input type="hidden" name="action" value="add_user">
							<input type="text" name="username" placeholder="ユーザー名" required>
							<input type="password" name="password" placeholder="パスワード" required>
							<select name="role">
								<option value="manager">管理者</option>
								<option value="employee">従業員</option>
							</select>
							<label><input type="checkbox" name="enabled" value="true" checked> 有効</label>
							<input type="submit" value="作成">
						</form>
					</div>
					<h3>ロール管理</h3>
					<div class="contentbox">
						<div class="form-section">
							<form action="manager" method="post">
								<input type="hidden" name="action" value="add_role">
								<input type="text" name="rolename" placeholder="ロール名 (例: 営業部)" required>
								<select name="rolecategory">
									<option value="original">original</option>
									<option value="department">department</option>
									<option value="position">position</option>
									<option value="qualification">qualification</option>
								</select>
								<input type="submit" value="追加">
							</form>
						</div>
						<table>
							<thead>
								<tr>
									<th>ID</th>
									<th>ロール名</th>
									<th>カテゴリ</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${roles}" var="role">
									<tr>
										<td><c:out value="${role.roleId}" /></td>
										<td><a href="manager?action=role_users&role_id=${role.roleId}"><c:out value="${role.rolename}" /></a></td>
										<td><c:out value="${role.rolecategory}" /></td>
										<td>
											<form action="manager" method="post" style="display:inline;">
												<input type="hidden" name="action" value="delete_role">
												<input type="hidden" name="role_id" value="${role.roleId}">
												<input type="submit" value="削除">
											</form>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
					<h3>従業員詳細</h3>
					<h3>従業員一覧</h3>
					<table>
						<thead>
							<tr>
								<th>ID</th>
								<th>ユーザー名</th>
								<th>ロール</th>
								<th>有効</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${users}" var="user">
								<tr>
									<td><c:out value="${user.userId}" /></td>
									<td><c:out value="${user.username}" /></td>
									<td>
                                        <c:choose>
                                            <c:when test="${user.role == 'employee'}">従業員</c:when>
                                            <c:when test="${user.role == 'manager'}">管理者</c:when>
                                            <c:otherwise>不明</c:otherwise>
                                        </c:choose>
                                    </td>
									<td>
                                        <form id="updateUserForm_<c:out value="${user.userId}"/>" action="manager" method="post">
                                            <input type="hidden" name="action" value="update_user_enabled">
                                            <input type="hidden" name="user_id" value="<c:out value="${user.userId}"/>">
                                            <select name="enabled" onchange="submitUpdateForm(<c:out value="${user.userId}"/>)">
                                                <option value="true" <c:if test="${user.enabled}">selected</c:if>>有効</option>
                                                <option value="false" <c:if test="${!user.enabled}">selected</c:if>>無効</option>
                                            </select>
                                        </form>
                                    </td>
									<td>
										<form action="manager" method="post" style="display:inline;">
											<input type="hidden" name="action" value="delete_user">
											<input type="hidden" name="user_id" value="${user.userId}">
											<input type="submit" value="削除">
										</form>
										</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
					
				</c:when>
				<c:when test="${page == 'messages'}">
					<h2>連絡/告知事項管理</h2>
					<div class="form-section">
						<h3>連絡/告知事項の新規作成</h3>
						<form action="manager" method="post">
							<input type="hidden" name="action" value="add_message">
							<textarea name="message_text" placeholder="メッセージ" required></textarea>
							<select name="priority">
								<option value="high">高</option>
								<option value="normal">中</option>
								<option value="low">低</option>
							</select>
							<label>開始日時: <input type="datetime-local" name="start_datetime" required></label>
							<label>終了日時: <input type="datetime-local" name="end_datetime" required></label>
							<input type="submit" value="作成">
						</form>
					</div>
					<h3>連絡/告知事項一覧</h3>
					<table>
						<thead>
							<tr>
								<th>ID</th>
								<th>メッセージ</th>
								<th>優先度</th>
								<th>開始日時</th>
								<th>終了日時</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${messages}" var="message">
								<tr>
									<td><c:out value="${message.messageId}" /></td>
									<td><c:out value="${message.messageText}" /></td>
									<td>
                                        <c:choose>
                                            <c:when test="${message.priority == 'high'}">高</c:when>
                                            <c:when test="${message.priority == 'normal'}">中</c:when>
                                            <c:when test="${message.priority == 'low'}">低</c:when>
                                            <c:otherwise>不明</c:otherwise>
                                        </c:choose>
                                    </td>
									<td><c:out value="${message.startDatetime}" /></td>
									<td><c:out value="${message.endDatetime}" /></td>
									<td>
										<form action="manager" method="post" style="display:inline;">
											<input type="hidden" name="action" value="delete_message">
											<input type="hidden" name="message_id" value="${message.messageId}">
											<input type="submit" value="削除">
										</form>
										</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
					
				</c:when>
				<c:otherwise>
					<h2>ようこそ、管理者様</h2>
					<p>上のメニューから操作を選択してください。</p>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</body>
</html>