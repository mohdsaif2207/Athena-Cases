package com.athena.cases.security;

import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccessResolutionService accessResolutionService;

    public DatabaseUserDetailsService(
            UserRepository userRepository,
            AccessResolutionService accessResolutionService) {
        this.userRepository = userRepository;
        this.accessResolutionService = accessResolutionService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return accessResolutionService.toPrincipal(user);
    }

    /**
     * Builds a principal from an already-loaded user graph (roles/groups/teams eager).
     */
    public UserPrincipal toPrincipal(UserEntity user) {
        return accessResolutionService.toPrincipal(user);
    }
}
