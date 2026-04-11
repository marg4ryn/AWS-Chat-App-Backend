@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreate(Jwt jwt) {
        String username = jwt.getClaimAsString("cognito:username");
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    return userRepository.save(user);
                });
    }
}