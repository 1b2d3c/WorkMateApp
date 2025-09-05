package com.example.attendance.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // データベースからユーザーを検索
        // 実際のアプリケーションでは、パスワードはハッシュ化して比較する必要があります。
        User user = userDAO.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            // ログイン成功
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            // ロールに基づいてリダイレクト
            if (user.getRole().equals("manager")) {
                response.sendRedirect("manager/dashboard.jsp");
            } else if (user.getRole().equals("employee")) {
                response.sendRedirect("employee/dashboard.jsp");
            } else {
                // 不明なロールの場合
                request.setAttribute("error", "不明なユーザー権限です。");
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
        } else {
            // ログイン失敗
            request.setAttribute("error", "ユーザー名またはパスワードが正しくありません。");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}
