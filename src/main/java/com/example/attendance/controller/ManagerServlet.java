package com.example.attendance.controller; 
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.MessageDAO;
import com.example.attendance.dao.RoleDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.Message;
import com.example.attendance.dto.Role;
import com.example.attendance.dto.User;

@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    
    private boolean isUserEnabled(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
			return false;
		}

		User user = (User) session.getAttribute("user");
		if (!user.isEnabled()) {
			session.invalidate();
			response.sendRedirect(request.getContextPath() + "/login.jsp?error=account_disabled");
			return false;
		}
		return true;
	}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        User user = (User) session.getAttribute("user");
        request.setAttribute("username", user.getUsername());
        
        if (!user.isEnabled()) {
            request.setAttribute("disabledMessage", "無効なアカウントでログイン中です。データの追加・削除・更新操作はできません。");
        }
        
        String action = request.getParameter("action");
        if (action == null) {
            action = "dashboard"; // Ensure action is never null
        }
        String page = "dashboard"; // Default page

        switch (action) {
            case "view_attendance":
                page = "attendance";
                List<Attendance> allAttendance = attendanceDAO.getAllAttendance();
                request.setAttribute("attendanceList", allAttendance);
                break;
            case "user_attendance":
                String userIdParam = request.getParameter("user_id");
                List<Attendance> attendanceList;

                if (userIdParam == null || userIdParam.isEmpty()) {
                    // 検索欄が空の場合は、全ての勤怠履歴を取得
                    attendanceList = attendanceDAO.getAllAttendance();
                } else {
                    // ユーザーIDが入力された場合は、そのIDの勤怠履歴を取得
                    try {
                        int userId = Integer.parseInt(userIdParam);
                        attendanceList = attendanceDAO.getAttendanceByUserId(userId);
                    } catch (NumberFormatException e) {
                        // 無効なIDが入力された場合
                        System.err.println("Invalid user ID format: " + userIdParam);
                        attendanceList = new ArrayList<>(); // 空のリストを返す
                        request.setAttribute("errorMessage", "無効なユーザーIDです。");
                    }
                }

                request.setAttribute("attendanceList", attendanceList);
                page = "attendance";
                break;
            case "view_users":
                page = "users";
                List<User> allUsers = userDAO.getAllUsers();
                request.setAttribute("users", allUsers);
                break;
            case "view_messages":
                page = "messages";
                List<Message> allMessages = messageDAO.getAllMessages();
                String filter = request.getParameter("filter");
                
                List<Message> filteredMessages;
                LocalDateTime now = LocalDateTime.now();
                
                if ("active".equals(filter)) {
                    filteredMessages = allMessages.stream()
                        .filter(m -> !now.isBefore(m.getStartDatetime()) && !now.isAfter(m.getEndDatetime()))
                        .collect(Collectors.toList());
                } else if ("inactive".equals(filter)) {
                    filteredMessages = allMessages.stream()
                        .filter(m -> now.isBefore(m.getStartDatetime()) || now.isAfter(m.getEndDatetime()))
                        .collect(Collectors.toList());
                } else {
                    filteredMessages = allMessages; // "all" または null の場合
                }
                
                Collections.sort(filteredMessages, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        int p1 = getPriorityValue(m1.getPriority());
                        int p2 = getPriorityValue(m2.getPriority());
                        return Integer.compare(p2, p1);
                    }

                    private int getPriorityValue(String priority) {
                        switch (priority) {
                            case "high": return 3;
                            case "normal": return 2;
                            case "low": return 1;
                            default: return 0;
                        }
                    }
                });
                request.setAttribute("messages", filteredMessages);
                break;
            default:
                // If action is unknown, default to dashboard
                page = "dashboard";
                break;
            case "view_roles": // 追加：ロール一覧表示
                page = "roles";
                try {
                    List<Role> allRoles = roleDAO.getAllRoles();
                    request.setAttribute("roles", allRoles);
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "ロール情報の取得中にエラーが発生しました。");
                }
                break;
            case "role_users": // 追加：ロールごとのユーザー一覧表示
                page = "users";
                String roleIdParam = request.getParameter("role_id");
                List<User> users;
                
                if (roleIdParam == null || roleIdParam.isEmpty()) {
                    // ロールIDが空の場合は全てのユーザーを取得
                    users = userDAO.getAllUsers();
                } else {
                    try {
                        int roleId = Integer.parseInt(roleIdParam);
                        // UserDAOにロールIDでユーザーを取得するメソッドが必要
                        // userDAO.getUsersByRoleId(roleId) のようなメソッドを仮定
                        // ユーザーの役割情報を取得するロジックが未実装のため、一旦全ユーザーを取得して絞り込み
                        users = userDAO.getUsersByRoleId(roleId);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid role ID format: " + roleIdParam);
                        users = userDAO.getAllUsers(); // 無効な場合は全ユーザーを表示
                        request.setAttribute("errorMessage", "無効なロールIDです。全てのユーザーを表示します。");
                    } catch (Exception e) {
                    	e.printStackTrace();
                    	users = userDAO.getAllUsers();
                    	request.setAttribute("errorMessage", "ユーザー情報の取得中にエラーが発生しました。全てのユーザーを表示します。");
                    }
                }
                request.setAttribute("users", users);
                break;
        }
        request.setAttribute("page", page);
        System.out.println("ManagerServlet: Final page attribute set to: " + page); // Debugging log
        request.getRequestDispatcher("/jsp/manager.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        
        // 無効なユーザーはPOSTリクエストをすべて拒否
        if (!user.isEnabled()) {
            response.sendRedirect(request.getContextPath() + "/manager?error=permission_denied");
            return;
        }
    	
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/manager");
            return;
        }

        switch (action) {
            case "add_attendance":
                // 勤怠記録の追加ロジック
                int userId = Integer.parseInt(request.getParameter("user_id"));
                LocalDateTime checkIn = LocalDateTime.parse(request.getParameter("check_in"));
                LocalDateTime checkOut = null;
                if (request.getParameter("check_out") != null && !request.getParameter("check_out").isEmpty()) {
                    checkOut = LocalDateTime.parse(request.getParameter("check_out"));
                }
                Attendance attendance = new Attendance(userId, checkIn, checkOut);
                attendanceDAO.insertAttendance(attendance);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_attendance");
                break;
            case "delete_attendance":
                // 勤怠記録の削除ロジック
                int attendanceId = Integer.parseInt(request.getParameter("attendance_id"));
                attendanceDAO.deleteAttendance(attendanceId);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_attendance");
                break;
            case "add_user":
                // ユーザー新規作成ロジック
                // パスワードは実際の環境ではハッシュ化が必要です
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String role = request.getParameter("role");
                boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
                User newUser = new User(username, password, role, enabled);
                userDAO.insertUser(newUser);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_users");
                break;
            case "delete_user":
                // ユーザー削除ロジック
                int deleteUserId = Integer.parseInt(request.getParameter("user_id"));
                userDAO.deleteUser(deleteUserId);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_users");
                break;
            case "update_user_enabled":
                try {
                    int updateUserId = Integer.parseInt(request.getParameter("user_id"));
                    boolean updateEnabled = Boolean.parseBoolean(request.getParameter("enabled"));
                    userDAO.updateUserEnabled(updateUserId, updateEnabled);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid user ID or enabled value format.");
                }
                response.sendRedirect(request.getContextPath() + "/manager?action=view_users");
                break;
            case "add_message":
                // 連絡/告知事項の追加ロジック
                String messageText = request.getParameter("message_text");
                String priority = request.getParameter("priority");
                LocalDateTime start = LocalDateTime.parse(request.getParameter("start_datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                LocalDateTime end = null;
                if (request.getParameter("end_datetime") != null && !request.getParameter("end_datetime").isEmpty()) {
                    end = LocalDateTime.parse(request.getParameter("end_datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                }
                Message newMessage = new Message(0, messageText, priority, start, end);
                boolean insertSuccess = messageDAO.insertMessage(newMessage);
                System.out.println("ManagerServlet: messageDAO.insertMessage success: " + insertSuccess); // Debugging log
                response.sendRedirect(request.getContextPath() + "/manager?action=view_messages");
                break;
            case "delete_message":
                // メッセージ削除ロジック
                int deleteMessageId = Integer.parseInt(request.getParameter("message_id"));
                messageDAO.deleteMessage(deleteMessageId);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_messages");
                break;
            case "edit_message":
                // 連絡/告知事項の編集ロジック
                int messageId = Integer.parseInt(request.getParameter("message_id"));
                String updatedMessageText = request.getParameter("message_text");
                String updatedPriority = request.getParameter("priority");
                LocalDateTime updatedStart = LocalDateTime.parse(request.getParameter("start_datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                LocalDateTime updatedEnd = null;
                if (request.getParameter("end_datetime") != null && !request.getParameter("end_datetime").isEmpty()) {
                    updatedEnd = LocalDateTime.parse(request.getParameter("end_datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                }
                Message updatedMessage = new Message(messageId, updatedMessageText, updatedPriority, updatedStart, updatedEnd);
                messageDAO.updateMessage(updatedMessage);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_messages");
                break;
            case "add_role": // 追加：ロール追加
                String rolename = request.getParameter("rolename");
                String rolecategory = request.getParameter("rolecategory");
                Role newRole = new Role(rolename, rolecategory);
                try {
                    roleDAO.insertRole(newRole);
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "ロールの追加に失敗しました。");
                }
                response.sendRedirect(request.getContextPath() + "/manager?action=view_roles");
                break;
            case "delete_role": // 追加：ロール削除
                try {
                    int deleteRoleId = Integer.parseInt(request.getParameter("role_id"));
                    roleDAO.deleteRole(deleteRoleId);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid role ID format: " + request.getParameter("role_id"));
                    request.setAttribute("errorMessage", "無効なロールIDです。");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "ロールの削除に失敗しました。");
                }
                response.sendRedirect(request.getContextPath() + "/manager?action=view_roles");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/manager");
                break;
        }
    }
    
}