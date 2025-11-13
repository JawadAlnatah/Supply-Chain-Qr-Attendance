package com.team.supplychain.dao;

import com.team.supplychain.models.User;
import com.team.supplychain.utils.DatabaseConnection;
import java.sql.*;
   
   public class UserDAO {
       public User login(String username, String password) {
           String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
           try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
               
               pstmt.setString(1, username);
               pstmt.setString(2, password); // In production, use proper hashing
               
               ResultSet rs = pstmt.executeQuery();
               if (rs.next()) {
                   User user = new User();
                   user.setUserId(rs.getInt("user_id"));
                   user.setUsername(rs.getString("username"));
                   user.setEmail(rs.getString("email"));
                   user.setRole(rs.getString("role"));
                   return user;
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return null;
       }
   }
