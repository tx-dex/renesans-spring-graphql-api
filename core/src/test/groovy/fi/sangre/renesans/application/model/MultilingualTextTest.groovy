package fi.sangre.renesans.application.model

import spock.lang.Specification

class MultilingualTextTest extends Specification {

    def "should not return null phrases"() {
        expect:
        new MultilingualText(null).getPhrases() == [:]
        new MultilingualText([:]).getPhrases() == [:]
        new MultilingualText(["fi":"1"]).getPhrases() == ["fi":"1"]
    }

    def "should check if empty"() {
        expect:
        new MultilingualText(null).isEmpty()
        new MultilingualText([:]).isEmpty()
        new MultilingualText(["fi":null]).isEmpty()
        !new MultilingualText(["fi":"1"]).isEmpty()
    }
}
