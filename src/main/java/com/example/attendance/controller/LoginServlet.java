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

    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userDAO.getUserByUsername(username);

        if (user != null && user.getPassword().trim().equals(password.trim())) {
            // ログイン成功
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            if ("manager".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/manager.jsp");
            } else if ("employee".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/employee.jsp");
            } else {
                // 不正なロール
                request.setAttribute("error", "不正なロールです。");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } else {
            // ログイン失敗
            request.setAttribute("error", "ユーザー名またはパスワードが間違っています。");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}