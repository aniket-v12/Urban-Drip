package com.UD.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.User;

public interface UserService {
	
	public User saveUser(User user);
	
	public User getUserByEmail(String email);
	
	public List<User> getAllUsers(String role);

	public boolean updateAccountStatus(int id, Boolean status);
	
	public void increaseFailedAttempt(User user);
	
	public void userAccountLock(User user);
	
	public boolean unlockAccountTimeExpired(User user);
	
	public void resetAttempt(int id);

	public void updateUserResetToken(String email, String resetToken);
	
	public User getUserByToken(String token);
	
	public User updateUser(User user);
	
	public User updateUserProfile(User user, MultipartFile file);
	
	public User saveAdmin(User user);

	public List<User> getAllAdmins(String role);
	
	public boolean existsEmail(String email);

}
