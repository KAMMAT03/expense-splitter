package pl.edu.pw.api.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.DBConnector;
import pl.edu.pw.api.security.JwtService;
import pl.edu.pw.api.users.dto.UserDTO;
import pl.edu.pw.models.Obligation;
import pl.edu.pw.models.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
	@Autowired
	private JwtService jwtService;
	private DBConnector dbc = new DBConnector("1");

	@GetMapping("/user/{id}/getall")
	public List<UserDTO> getUsers(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (jwtService.checkUserToken(id, request)) {
			List<User> users = dbc.getAllUsers();
			return users.stream()
					.map(user -> {
						UserDTO userDTO = new UserDTO();
						userDTO.setId(user.getId());
						userDTO.setName(user.getName());
						return userDTO;
					})
					.collect(Collectors.toList());
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}

	@GetMapping("/user/{id}/total/{toid}")
	public String getTotalObligationsToTo(@PathVariable("id") Long id, HttpServletRequest request, @PathVariable("toid") Long toId, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			if( user!=null) {
				List<Obligation> obligations = user.getOwes();
				Double total = 0.0;
				for (Obligation ob :
						obligations) {
					if (ob.getCreditor().getId() == toId) {
						total += ob.getAmount();
					}
				}
				return total.toString();
			}else{
				response.getWriter().print("Access Denied");
				response.setStatus(401);
			}
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}

	@GetMapping("user/{id}/findid/{userid}")
	public UserDTO getUser(@PathVariable("id") Long id, HttpServletRequest request,@PathVariable("userid") Long userId, HttpServletResponse response) throws IOException {
		if (jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(userId);
			if(user!=null) {
				UserDTO u = new UserDTO();
				u.setName(user.getName());
				u.setId(user.getId());
				return u;
			}
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}

	/**
	 * Search users by name (ideally full text search, or at least prefix match)
	 * @param name - name of user
	 * @return list of users that match the search term
	 */
	@GetMapping("user/{id}/find/{name}")
	public List<UserDTO> findUsers(@PathVariable("id") Long id, HttpServletRequest request, @PathVariable String name, HttpServletResponse response) throws IOException {
		if (jwtService.checkUserToken(id, request)) {
			List<User> users = dbc.findUsersByPrefix(name);
			return users.stream()
					.map(user -> {
						UserDTO userDTO = new UserDTO();
						userDTO.setId(user.getId());
						userDTO.setName(user.getName());
						return userDTO;
					})
					.collect(Collectors.toList());
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}
}
