package fi.sangre.renesans.aaa

import fi.sangre.renesans.model.User
import spock.lang.Specification

class UserPrincipalTest extends Specification {
    final USER = new User(firstName: "first", lastName: "last")

    def "should capitalize name"() {
        when:
        def principal = UserPrincipal.create(USER)
        then:
        principal.getName() == "First Last"


    }
}
