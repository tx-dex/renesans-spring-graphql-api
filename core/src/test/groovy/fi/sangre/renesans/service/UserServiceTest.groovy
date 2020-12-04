package fi.sangre.renesans.service

import fi.sangre.renesans.aaa.JwtTokenService
import fi.sangre.renesans.persistence.repository.CustomerRepository
import fi.sangre.renesans.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class UserServiceTest extends Specification {
    final userRepository = Mock(UserRepository)
    final passwordEncoder = Mock(PasswordEncoder)
    final jwtTokenService = Mock(JwtTokenService)
    final tokenService = Mock(TokenService)
    final roleService = Mock(RoleService)
    final customerRepository = Mock(CustomerRepository)
    final applicationEventPublisher = Mock(ApplicationEventPublisher)

    def instance = new UserService(userRepository,
            passwordEncoder,
            jwtTokenService,
            tokenService,
            roleService,
            customerRepository,
            applicationEventPublisher
    )

    def "should not throw on requesting password reset when user not found"() {
        given:
        def email = "test@email.com"

        when:
        instance.requestPasswordReset(email, "en")

        then:
        notThrown Exception
        1 * userRepository.findByEmail(email) >> Optional.empty()
        0 * applicationEventPublisher.publishEvent(_)
    }
}




