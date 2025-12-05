package com.eventmanager.service;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {

    User registerUser(String nom, String prenom, String email,
                      String password, String telephone, UserRole role);

    Optional<User> authenticate(String email, String password);

    User updateProfile(Long userId, String nom, String prenom, String telephone);

    void changePassword(Long userId, String oldPassword, String newPassword);

    User toggleAccountStatus(Long userId, boolean active);

    Map<String, Object> getUserStatistics(Long userId);

    List<User> getUsersWithFilters(String nom, String prenom,
                                   UserRole role, Boolean actif);

    List<User> searchUsers(String keyword);

    User getUserById(Long userId);

    Optional<User> getUserByEmail(String email);

    boolean isOrganizerOrAdmin(User user);

    Map<UserRole, Long> countUsersByRole();

    User updateUserRole(Long userId, UserRole newRole);
}