package com.example.attendance.controller; 
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.MessageDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.Message;
import com.example.attendance.dto.User;

@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            case "view_users":
                page = "users";
                List<User> allUsers = userDAO.getAllUsers();
                request.setAttribute("users", allUsers);
                break;
            case "view_messages":
                page = "messages";
                List<Message> messages = messageDAO.getAllMessages();
                request.setAttribute("messages", messages);
                break;
            default:
                // If action is unknown, default to dashboard
                page = "dashboard";
                break;
        }
        request.setAttribute("page", page);
        System.out.println("ManagerServlet: Final page attribute set to: " + page); // Debugging log
        request.getRequestDispatcher("/jsp/manager.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                response.sendRedirect(request.getContextPath() + "manager?action=view_attendance");
                break;
            case "add_user":
                // ユーザー新規作成ロジック
                // パスワードは実際の環境ではハッシュ化が必要です
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String role = request.getParameter("role");
                boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
                User user = new User(username, password, role, enabled);
                userDAO.insertUser(user);
                response.sendRedirect(request.getContextPath() + "/manager?action=view_users");
                break;
            case "delete_user":
                // ユーザー削除ロジック
                int deleteUserId = Integer.parseInt(request.getParameter("user_id"));
                userDAO.deleteUser(deleteUserId);
                response.sendRedirect(request.getContextPath() + "manager?action=view_users");
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
                response.sendRedirect(request.getContextPath() + "manager?action=view_messages");
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
            default:
                response.sendRedirect(request.getContextPath() + "manager");
                break;
        }
    }
}