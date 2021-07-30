package com.github.veselroger.jwt.security;

import com.github.veselroger.jwt.model.User;
import com.github.veselroger.jwt.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Specific Service to obtain {@link UserDetailsImpl}.
 */
@Data
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name);
        // Fetch lazy collection while we have a transaction
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Transaction is not active!");
        }
        user.getRoles().size();
        return new UserDetailsImpl(user);
    }
}
