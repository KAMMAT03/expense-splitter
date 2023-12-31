package pl.edu.pw.api.friendship;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.DBConnector;
import pl.edu.pw.api.friendship.dto.FriendsDTO;
import pl.edu.pw.api.friendship.dto.FriendshipRequestDTO;
import pl.edu.pw.api.security.JwtService;
import pl.edu.pw.models.Friendship;
import pl.edu.pw.models.User;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends")
public class FriendshipController {
	@Autowired
	private JwtService jwtService;
	private DBConnector dbc = new DBConnector("1");

	/**
	 * Sends a friendship request to the user with the given id (or accepts the friendship if the other side requested it)
	 * @param id id of the second user
	 */
	@GetMapping("/user/{id}/requestoracceptfriendship/{withid}")
	public void requestOrAcceptFriendship(@PathVariable("id") Long id, HttpServletRequest request,@PathVariable("withid") Long withId, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			User target = dbc.findUserById(withId);
			List<Friendship> friends = user.getFriendsWith();
			for (Friendship f :
					friends) {
				if (f.getStatus() == Friendship.Status.ACCEPTED && (Objects.equals(f.getReceiver().getName(), target.getName()) || Objects.equals(f.getSender().getName(), target.getName()))) {
					response.getWriter().print("Already friends with " + target.getName());
					return;
				} else if (f.getStatus() == Friendship.Status.DECLINED && Objects.equals(f.getReceiver().getName(), target.getName())) {
					response.getWriter().print("Request has been declined by " + target.getName());
					return;
				} else if (f.getStatus() == Friendship.Status.PENDING && Objects.equals(f.getReceiver().getName(), target.getName())) {
					response.getWriter().print("Request was already sent to " + target.getName());
					return;
				}
			}
			user.sendOrAcceptFriendship(target);
			dbc.updateUser(user);
			response.getWriter().print("Request was sent to " + target.getName() + " successfully");
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
	}


	/**
	 * Rejects the friendship request from the user with the given id (or cancels the friendship if it was already accepted)
	 * @param id id of the second user
	 */
	@GetMapping("/user/{id}/rejectfriendship/{withid}")
	public void rejectFriendship(@PathVariable("id") Long id, HttpServletRequest request,@PathVariable("withid") Long withId, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			User target = dbc.findUserById(withId);
			List<Friendship> friends = user.getFriendsWith();
			for (Friendship f :
					friends) {
				if (f.getStatus() == Friendship.Status.ACCEPTED && (Objects.equals(f.getReceiver().getName(), target.getName()) || Objects.equals(f.getSender().getName(), target.getName()))) {
					response.getWriter().print("Already friends with " + target.getName());
					return;
				} else if (f.getStatus() == Friendship.Status.DECLINED && Objects.equals(f.getSender().getName(), target.getName())) {
					response.getWriter().print("Request  from " + target.getName() + " has been already declined");
					return;
				}
			}
			user.rejectFriendship(target);
			dbc.updateUser(user);
			response.getWriter().print("Friendship with " + target.getName() + " was declined successfully");
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
	}


	/**
	 * Makes it so that the current user will automatically accept obligations from the user with the given id
	 * @param id id of the second user
	 */
	@GetMapping("/user/{id}/auto/{withid}")
	public void markAsAutoAccept(@PathVariable("id") Long id, HttpServletRequest request, @PathVariable("withid") Long withId, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			user.markAsAutoAccept(dbc.findUserById(withId));
			dbc.updateUser(user);
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
	}


	/**
	 * Returns a list of all users that are friends with the current user
	 * @return list of friends
	 */
	@GetMapping("/user/{id}/friends")
	public List<FriendsDTO> getFriends(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			List<Friendship> friends = user.getAllFriends();
			return friends.stream()
					.map(friend -> {
						FriendsDTO friendsDTO = new FriendsDTO();
						if(friend.getSender().getId() != user.getId()){
							friendsDTO.setId(friend.getSender().getId());
							friendsDTO.setUsername(friend.getSender().getName());
						} else{
							friendsDTO.setId(friend.getReceiver().getId());
							friendsDTO.setUsername(friend.getReceiver().getName());
						}


						return friendsDTO;
					})
					.collect(Collectors.toList());
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}


	/**
	 * Returns a list of all users that have sent a friendship request to the current user
	 */
	@GetMapping("/user/{id}/requests")
	public List<FriendshipRequestDTO> getFriendshipRequests(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if(jwtService.checkUserToken(id, request)) {
			User user = dbc.findUserById(id);
			List<Friendship> friendshipRequests = user.getAllFriendshipRequests();
			return friendshipRequests.stream()
					.map(friend -> {
						FriendshipRequestDTO friendsDTO = new FriendshipRequestDTO();
						friendsDTO.setId(friend.getSender().getId());
						friendsDTO.setUsername(friend.getSender().getName());
						return friendsDTO;
					})
					.collect(Collectors.toList());
		}else {
			response.getWriter().print("Access Denied");
			response.setStatus(401);
		}
		return null;
	}

}
