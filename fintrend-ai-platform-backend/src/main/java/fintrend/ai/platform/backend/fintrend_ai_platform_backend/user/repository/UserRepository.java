package fintrend.ai.platform.backend.fintrend_ai_platform_backend.user.repository;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
