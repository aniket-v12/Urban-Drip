package com.UD.implementation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.User;
import com.UD.helpers.AppConstant;
import com.UD.repositories.UserRepository;
import com.UD.services.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public User saveUser(User user) {

		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setIsAccountNonLocked(true);
		user.setFailedAttempt(0);

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		User saveUser = userRepository.save(user);

		return saveUser;
	}

	@Override
	public User getUserByEmail(String email) {

		User user = userRepository.findByEmail(email);

		return user;
	}

	@Override
	public List<User> getAllUsers(String role) {
		List<User> allUsers = userRepository.findByRole(role);
		return allUsers;
	}

	@Override
	public boolean updateAccountStatus(int id, Boolean status) {

		Optional<User> findByUser = userRepository.findById(id);

		if (findByUser.isPresent()) {
			User user = findByUser.get();
			user.setIsEnable(status);

			userRepository.save(user);

			return true;
		}

		return false;
	}

	@Override
	public void increaseFailedAttempt(User user) {

		int attempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(User user) {

		user.setIsAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(User user) {

		long lockTime = user.getLockTime().getTime();
		long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

		long currentTime = System.currentTimeMillis();

		if (unLockTime < currentTime) {
			user.setIsAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}

		return false;
	}

	@Override
	public void resetAttempt(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {

		User findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);

		userRepository.save(findByEmail);
	}

	@Override
	public User getUserByToken(String token) {

		return userRepository.findByResetToken(token);
	}

	@Override
	public User updateUser(User user) {

		return userRepository.save(user);
	}

	@Override
	public User updateUserProfile(User user, MultipartFile file) {

		User oldUserDetails = userRepository.findById(user.getId()).get();

		if (!file.isEmpty()) {
			oldUserDetails.setProfileImage(file.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(oldUserDetails)) {

			oldUserDetails.setName(user.getName());
			oldUserDetails.setAddress(user.getAddress());
			oldUserDetails.setMobileNumber(user.getMobileNumber());
			oldUserDetails.setState(user.getState());
			oldUserDetails.setCity(user.getCity());
			oldUserDetails.setPincode(user.getPincode());

			oldUserDetails = userRepository.save(oldUserDetails);

		}

		try {
			if (!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("User Image Stored Path: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oldUserDetails;
	}

	@Override
	public User saveAdmin(User user) {

	    User existingUser = userRepository.findByEmail(user.getEmail());
	    
	    if (existingUser != null) {
	        // Optional: throw a custom exception or return null to indicate duplicate
	        throw new IllegalArgumentException("An admin with this email already exists!");
	    }

		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setIsAccountNonLocked(true);
		user.setFailedAttempt(0);

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		User saveAdmin = userRepository.save(user);

		return saveAdmin;
		
	}

	@Override
	public List<User> getAllAdmins(String role) {
		
		List<User> allAdmins = userRepository.findByRole(role);
		
		return allAdmins;
	}

	@Override
	public boolean existsEmail(String email) {
		
		return userRepository.existsByEmail(email);
	}

}
