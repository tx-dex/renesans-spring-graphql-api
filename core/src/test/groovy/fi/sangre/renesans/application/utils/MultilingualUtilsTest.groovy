package fi.sangre.renesans.application.utils

import spock.lang.Specification

class MultilingualUtilsTest extends Specification {
    final instance = new MultilingualUtils()

    def "should combine multilingual texts"() {
        expect:
        ["fi": "1", "en": "2"] == instance.combineMaps(["fi": "1"], ["en": "2"]).getPhrases()
        ["en": "2"] == instance.combineMaps(null, ["en": "2"]).getPhrases()
        ["fi": "1"] == instance.combineMaps(["fi": "1"], null).getPhrases()
        ["fi": "2", "en": "2"] == instance.combineMaps(["fi": "1", "en": "2"] , ["fi": "2"]).getPhrases()
        ["en": "2"] == instance.combineMaps(["fi": "1", "en": "2"] , ["fi": null]).getPhrases()
    }

    def "should create multilingual text"() {
        expect:
        instance.create(["fi":"1"]).getPhrases() == ["fi":"1"]
        instance.create("1", "fi").getPhrases() == ["fi":"1"]
        instance.create(null, "fi").getPhrases() == [:]
        instance.create("   ", "fi").getPhrases() == ["fi":null]
    }
}
