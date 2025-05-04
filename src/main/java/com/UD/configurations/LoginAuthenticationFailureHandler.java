package com.UD.configurations;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.UD.Entity.User;
import com.UD.helpers.AppConstant;
import com.UD.repositories.UserRepository;
import com.UD.services.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String email = request.getParameter("username");
		User user = userRepository.findByEmail(email);

		if (user != null) {

			if (user.getIsEnable()) {

				if (user.getIsAccountNonLocked()) {

					if (user.getFailedAttempt() < AppConstant.ATTEMPT_COUNT) {

						userService.increaseFailedAttempt(user);
					} else {
						userService.userAccountLock(user);
						exception = new LockedException("Account is Locked !! Failed 3 Attempts");
					}
				} else {

					if (userService.unlockAccountTimeExpired(user)) {
						exception = new LockedException("Account is Unlocked !! Please Try To Login ");
					} else {
						exception = new LockedException("Account is Locked !! Try Later");
					}
				}

			} else {
				exception = new LockedException("Account is Inactive");
			}
		}
		else {
			exception = new LockedException("Email or Password is Wrong, Please Check Again !!");
		}

		super.setDefaultFailureUrl("/login?error");
		super.onAuthenticationFailure(request, response, exception);

	}

}
