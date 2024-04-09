package com.poly.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.poly.dao.AccountDAO;
import com.poly.dao.AuthorityDAO;
import com.poly.dao.RoleDAO;
import com.poly.entity.Account;
import com.poly.entity.Authority;
import com.poly.entity.Role;
import com.poly.service.MailerService;
import com.poly.service.SessionService;
import com.poly.service.UserService;

@Controller
public class LoginController2 {

	@Autowired
	AccountDAO accountDAO;
	@Autowired
	AuthorityDAO authorityDAO;
	@Autowired
	UserService userService;
	@Autowired
	RoleDAO roleDAO;
	@Autowired
	MailerService mailerService;
	private Map<String, String> otpMap = new HashMap<>();
	@Autowired
	SessionService sessionService;

	@RequestMapping("/login")
	public String loginForm(Model model) {
		return "/login";
	}

	@RequestMapping("/login/success")
	public String loginSuccess(Model model) {
		model.addAttribute("message", "Đăng nhập thành công!");
		return "forward:/login";
	}

	@RequestMapping("/oauth2/login/success")
	public String success(OAuth2AuthenticationToken oauthh2) {

		userService.loginFromOAuth2(oauthh2);
		return "forward:/login/success";
	}

	@RequestMapping("/login/error")
	public String error(Model model) {
		model.addAttribute("message", "Sai thông tin đăng nhập!");
		return "forward:/login";
	}

	@RequestMapping("/logout/success")
	public String logoff(Model model) {
		model.addAttribute("message", "Đăng xuất thành công!");

		return "forward:/login";
	}

	@RequestMapping("/access/denied")
	public String denied(Model model) {
		model.addAttribute("message", "Bạn không có quyền truy xuất!");
		return "/login";
	}

	@RequestMapping("/register.html")
	public String register() {
		return "register";
	}

	private String generateOtp() {
		Random random = new Random();
		int otpValue = 100000 + random.nextInt(900000);
		return String.valueOf(otpValue);
	}

	@RequestMapping("/register/success")
	public String register1(Model model, @RequestParam String username, @RequestParam String password,
			@RequestParam String fullname, @RequestParam String phone, @RequestParam String email,
			@RequestParam String confirmPassword,
			HttpServletRequest request, @RequestParam(required = false) String otp) throws MessagingException {
		model.addAttribute("username", username); // Thêm vào Controller
		model.addAttribute("fullname", fullname);
		model.addAttribute("email", email);
		Account userSession = sessionService.getAttribute("account");
		// Kiểm tra điều kiện đăng ký, như bạn đã thực hiện
		if (accountDAO.findById(username).isPresent()) {
			model.addAttribute("error", "Vui lòng đặt tên username khác!");
			return "register";
		} else {
			if (accountDAO.findByEmail(email).isPresent()) {
				model.addAttribute("error", "Địa chỉ email đã được sử dụng. Vui lòng chọn địa chỉ email khác!");
				return "register";
			} else if (!password.equals(confirmPassword)) {
				model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp. Vui lòng nhập lại.");
				return "register";
			} else {
				Account user = new Account();
				user.setUsername(userSession.getUsername());
				user.setPassword(userSession.getPassword());
				user.setFullname(userSession.getFullname());
				user.setEmail(userSession.getEmail());
				user.setPhoto("nv01.jpg");

				accountDAO.save(user);
				Authority authority = new Authority();
				authority.setAccount(user);
				Role role = roleDAO.findById("CUST").get();

				authority.setRole(role);
				authorityDAO.save(authority);
				model.addAttribute("message", "Đăng kí thành công!");
				return "/login";
				// String generatedOtp = generateOtp();
				// otpMap.put(username, generatedOtp);
				// // Lưu trữ otpMap trong phiên
				// sessionService.setAttribute("otpMap", otpMap);
				// sessionService.setAttribute("account", new Account(username, password,
				// fullname, phone, email));
				// mailerService.sendOtpEmail(email, "Xác nhận đăng ký", "Mã OTP của bạn là: " +
				// generatedOtp);
				// return "otp";
			}
		}

	}

	// @PostMapping("/verify-otp")
	// public String verifyOtp(Model model, @RequestParam String username,
	// @RequestParam String otp,
	// @RequestParam String password, @RequestParam String fullname, @RequestParam
	// String email,
	// @RequestParam String confirmPassword, HttpServletRequest request) {

	// // Kiểm tra mã OTP nhập vào có khớp với mã đã gửi hay không
	// Map<String, String> map = sessionService.getAttribute("otpMap");
	// Account userSession = sessionService.getAttribute("account");

	// if (map.containsKey(userSession.getUsername()) &&
	// map.get(userSession.getUsername()).equals(otp.trim())) {
	// // Nếu khớp, xóa mã OTP khỏi map và chuyển hướng đến trang đăng ký thành công
	// // hoặc trang chính
	// map.remove(username);
	// model.addAttribute("message", "Xác nhận OTP thành công!");
	// Account user = new Account();
	// user.setUsername(userSession.getUsername());
	// user.setPassword(userSession.getPassword());
	// user.setFullname(userSession.getFullname());
	// user.setEmail(userSession.getEmail());
	// user.setPhoto("nv01.jpg");

	// accountDAO.save(user);
	// Authority authority = new Authority();
	// authority.setAccount(user);
	// Role role = roleDAO.findById("CUST").get();

	// authority.setRole(role);
	// authorityDAO.save(authority);
	// model.addAttribute("message", "Đăng kí thành công!");
	// return "register";
	// } else {
	// // Nếu không khớp, hiển thị thông báo lỗi
	// model.addAttribute("error", "Mã OTP không hợp lệ!");
	// return "otp";
	// }
	// }

}