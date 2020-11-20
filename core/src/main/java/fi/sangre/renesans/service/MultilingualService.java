package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.dto.Language;
import fi.sangre.renesans.graphql.input.PhraseInput;
import fi.sangre.renesans.model.Country;
import fi.sangre.renesans.model.MultilingualKey;
import fi.sangre.renesans.model.MultilingualPhrase;
import fi.sangre.renesans.repository.MultilingualKeyRepository;
import fi.sangre.renesans.repository.MultilingualPhraseRepository;
import lombok.extern.slf4j.Slf4j;
import net.time4j.PrettyTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.Duration;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.*;

@Slf4j

@Service
public class MultilingualService {
    private static final String DEFAULT_LANGUAGE_CODE = "en";

    private final  MultilingualPhraseRepository multilingualPhraseRepository;
    private final  MultilingualKeyRepository multilingualKeyRepository;

    @Autowired
    public MultilingualService(
            final MultilingualPhraseRepository multilingualPhraseRepository,
            final MultilingualKeyRepository multilingualKeyRepository
    ) {
        checkArgument(multilingualPhraseRepository != null, "MultilingualPhraseRepository is required");
        checkArgument(multilingualKeyRepository != null, "MultilingualKeyRepository is required");

        this.multilingualPhraseRepository = multilingualPhraseRepository;
        this.multilingualKeyRepository = multilingualKeyRepository;
    }

    private List<MultilingualKey> getKeys(final List<String> keys, final String startsWith) {
        final List<MultilingualKey> multilingualKeys;
        if (keys != null && keys.size() > 0 && !StringUtils.isEmpty(startsWith)) {
            multilingualKeys =  multilingualKeyRepository.findAllByKeyInOrKeyStartsWith(keys, startsWith);
        } else if (keys != null && keys.size() > 0 && StringUtils.isEmpty(startsWith)) {
            multilingualKeys = multilingualKeyRepository.findAllByKeyIn(keys);
        } else if (!StringUtils.isEmpty(startsWith)) {
            multilingualKeys = getKeys(startsWith);
        } else {
            multilingualKeys = new ArrayList<>();
        }

        return multilingualKeys;
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<MultilingualKey> getKeys(@NonNull final String startsWith) {
        return multilingualKeyRepository.findAllByKeyStartsWith(startsWith);
    }

    /**
     * Gets {@link MultilingualPhrase} based on the key and language code or returns null if phrase is not found
     * @param key {@link MultilingualKey#getKey()} value
     * @param languageCode language code
     * @return {@link MultilingualPhrase} or null
     */
    public MultilingualPhrase getPhrase(final String key, final String languageCode) {
        final MultilingualKey multilingualKey = multilingualKeyRepository.findByKey(key);
        if (multilingualKey != null) {
            return multilingualKey.getPhrases().get(languageCode); // Do not fallback for english!!!
        }
        return null;
    }

    /**
     * Gets list of {@link MultilingualPhrase} based on the keys and language code or returns empty list if not found
     * @param keys list of {@link MultilingualKey#getKey()} values
     * @param languageCode language code
     * @return {@link MultilingualPhrase} or empty list
     */
    public List<MultilingualPhrase> getPhrases(final List<String> keys,
                                               final String startsWith,
                                               final String languageCode) {

        final List<MultilingualKey> multilingualKeys = getKeys(keys, startsWith);

        return multilingualKeys.stream().map(
                key -> key.getPhrases().get(languageCode)) //Do not fallback to english!!!
        .filter(Objects::nonNull)
        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public List<String> getValidLanguageCodes() {
        return multilingualPhraseRepository.findLocales();
    }

    public List<Language> getLanguages(final String languageTag) {
        Locale ctxLocale = Locale.forLanguageTag(languageTag);

        List<Language> languages = new ArrayList<>();
        MultilingualKey promptKey = multilingualKeyRepository.findByKey("language_prompt");

        for (String code : getValidLanguageCodes()) {
            Locale locale = Locale.forLanguageTag(code);
            MultilingualPhrase promptPhrase = promptKey.getPhrases().getOrDefault(code, promptKey.getPhrases().get(DEFAULT_LANGUAGE_CODE));
            languages.add(Language.builder()
                    .name(locale.getDisplayName(ctxLocale))
                    .nativeName(locale.getDisplayName())
                    .prompt(promptPhrase.getMessage())
                    .code(code)
                    .build());
        }
        return languages;
    }

    public List<Country> getCountries(final String languageTag) {

        String[] locales = Locale.getISOCountries();
        List<Country> countries = new ArrayList<>();
        Locale locale = Locale.forLanguageTag(languageTag);

        for (String countryCode : locales) {
            Locale obj = new Locale(languageTag, countryCode);
            countries.add(new Country(obj.getDisplayCountry(locale), countryCode));
        }

        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        countries.sort((Country o1, Country o2) -> collator.compare(o1.getName(), o2.getName()));

        return countries;
    }

    @Transactional
    public MultilingualPhrase savePhrase(final MultilingualKey key, final String text, final String languageCode) {
        checkArgument(key != null, "Key is required");

        final MultilingualPhrase multilingualPhrase = key.getPhrases().getOrDefault(languageCode, MultilingualPhrase.builder()
                .key(key)
                .locale(languageCode)
                .build());

        multilingualPhrase.setMessage(text);
        key.getPhrases().put(languageCode, multilingualPhrase);

        multilingualKeyRepository.save(key);
        return multilingualPhraseRepository.save(multilingualPhrase);

    }

    @Transactional
    public MultilingualPhrase saveNamedPhrase(PhraseInput phraseInput) {
        checkArgument(phraseInput.getName() != null || phraseInput.getId() != null, "Id or name is required");

        final MultilingualKey key;
        if (phraseInput.getName() != null) {
            log.debug("Saving phrase by key: '{}'", phraseInput.getName());
            key = getOrCreateByName(phraseInput.getName());
        } else {
            log.debug("Saving phrase by id: '{}'", phraseInput.getId());
            key = multilingualKeyRepository.findById(phraseInput.getId()).orElse(MultilingualKey.builder().build());
        }

        return savePhrase(key, phraseInput.getText(), phraseInput.getLanguageCode());
    }

    private MultilingualKey getOrCreateByName(final String keyName) {
        MultilingualKey multilingualKey = multilingualKeyRepository.findByKey(keyName);

        if (multilingualKey == null) {
            multilingualKey = MultilingualKey.builder()
                    .key(keyName)
                    .build();
        }

        return multilingualKey;
    }

    public Map<String, String> getPhrases(@NonNull final Long keyId) {
        return multilingualKeyRepository.findById(keyId)
                .map(MultilingualKey::getPhrases)
                .orElse(ImmutableMap.<String, MultilingualPhrase>of())
                .entrySet().stream()
                .collect(collectingAndThen(toMap(Map.Entry::getKey, e -> e.getValue().getMessage()), Collections::unmodifiableMap));
    }

    public MultilingualKey copyKeyWithPhrases(MultilingualKey key){
        checkArgument(key != null, "Key is required");

        MultilingualKey newKey = multilingualKeyRepository.save(MultilingualKey.builder().build());
        Collection<MultilingualPhrase> phrases =key.getPhrases().values();
        List<MultilingualPhrase> newPhrases = new ArrayList<>();
        for (MultilingualPhrase phrase : phrases) {
            newPhrases.add(MultilingualPhrase.builder()
                    .key(newKey)
                    .locale(phrase.getLocale())
                    .message(phrase.getMessage())
                    .build());
        }
        multilingualPhraseRepository.saveAll(newPhrases);
        return newKey;
    }
    /**
     * Gets map of < {@link MultilingualKey#getKey()}, {@link MultilingualPhrase#getMessage()} > based on the keys and language code or returns empty map if not found
     * <p>Fallbacks to english if the phrase is not found for provided language code</p>
     * @param keys list of {@link MultilingualKey#getKey()} values
     * @param languageCode language code
     * @return Map of found <key, message> or empty map
     */
    public Map<String, String> lookupPhrases(final List<String> keys,
                                             final String startsWith,
                                             final String languageCode) {
        final List<MultilingualKey> multilingualKeys = getKeys(keys, startsWith);

        return multilingualKeys.stream().map(
                key -> key.getPhrases().getOrDefault(languageCode, key.getPhrases().get(DEFAULT_LANGUAGE_CODE)))
                .filter(Objects::nonNull)
                .collect(collectingAndThen(toMap(key -> key.getKey().getKey(), MultilingualPhrase::getMessage), Collections::unmodifiableMap));
    }

    public String lookupPhrase(final Long keyId, final String languageCode) {
        if (keyId != null) {
            //TODO: use only multilingualKeyRepository to find the phrase
            final MultilingualKey key = multilingualKeyRepository.findById(keyId).orElse(null);
            if (key != null) {
                return lookupPhrase(key, languageCode);
            }
            return "Phrase key not found";
        }
        return null;
    }

    public String lookupPhrase(final String keyName, final String languageCode) {
        if (keyName != null) {
            final MultilingualKey key = multilingualKeyRepository.findByKey(keyName);
            return lookupPhrase(key, languageCode);
        }
        return null;
    }

    public String lookupPhrase(final String keyName, final String languageCode, final String defaultPhrase) {
        String phrase = defaultPhrase;
        if (keyName != null) {
            final MultilingualKey key = multilingualKeyRepository.findByKey(keyName);
            final String foundPhrase = lookupPhrase(key, languageCode);
            if (foundPhrase != null) {
                phrase = foundPhrase;
            }
        }
        return phrase;
    }

    public String lookupPhrase(final MultilingualKey key, final String languageCode) {
        if (key != null) {
            final MultilingualPhrase phrase = key.getPhrases().getOrDefault(languageCode, key.getPhrases().get(DEFAULT_LANGUAGE_CODE));
            if (phrase != null) {
                return phrase.getMessage();
            }
        }
        return null;
    }

    public String prettyTextOf(final Duration duration, final String languageCode) {
        final Locale locale;
        if (languageCode == null) {
            locale = Locale.forLanguageTag(DEFAULT_LANGUAGE_CODE);
        } else {
            locale = Locale.forLanguageTag(languageCode);
        }

        return PrettyTime.of(locale).print(duration);
    }
}

