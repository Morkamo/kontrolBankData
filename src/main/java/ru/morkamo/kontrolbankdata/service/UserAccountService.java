package ru.morkamo.kontrolbankdata.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.repository.UserAccountRepository;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.security.DepartmentIds;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new AppUserPrincipal(
                user.getId(),
                user.getUsername(),
                "{noop}" + user.getPassword(),
                user.getDepartmentId());
    }

    public Optional<UserAccount> findById(Short id) {
        return userAccountRepository.findById(id);
    }

    public List<UserAccount> findAllManagedUsers() {
        return userAccountRepository.findAllByDepartmentIdNotOrderByIdAsc(DepartmentIds.ADMINISTRATOR);
    }

    public boolean existsById(Short id) {
        return userAccountRepository.existsById(id);
    }

    @Transactional
    public void updateUser(Short oldId, UserAccount values) {
        if (!oldId.equals(values.getId())) {
            userAccountRepository.changeId(oldId, values.getId());
        }

        UserAccount user = userAccountRepository.findById(values.getId()).orElseThrow();
        copyFields(user, values);
        userAccountRepository.save(user);
    }

    public void createUser(UserAccount values) {
        UserAccount user = new UserAccount();
        user.setId(values.getId());
        copyFields(user, values);
        userAccountRepository.save(user);
    }

    private void copyFields(UserAccount user, UserAccount values) {
        user.setUsername(values.getUsername().trim());
        user.setFullName(values.getFullName());
        user.setPassword(values.getPassword());
        user.setUserGroup(values.getUserGroup());
        user.setComputerName(values.getComputerName());
        user.setSendMessage(values.getSendMessage());
        user.setUserGroupForInteraction(values.getUserGroupForInteraction());
        user.setActiveOfRealTime(values.getActiveOfRealTime());
        user.setUserGroupForOnvp(values.getUserGroupForOnvp());
        user.setUserGroupForStatic(values.getUserGroupForStatic());
        user.setRank(values.getRank());
        user.setStamp(values.getStamp());
        user.setDepartmentId(values.getDepartmentId());
    }

    public void delete(UserAccount user) {
        userAccountRepository.delete(user);
    }
}
