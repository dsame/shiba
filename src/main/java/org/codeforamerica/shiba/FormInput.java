package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

@Data
public class FormInput {
    FormInputType type;
    String name;
    String promptMessageKey;
    String helpMessageKey;
    String validationErrorMessageKey;
    List<Option> options;
    List<FormInput> followUps = Collections.emptyList();
    String followUpsValue;
    Validator validator = new Validator();

    public Validation getValidationFor(MultiValueMap<String, String> model) {
        return this.validator.validationFor(model);
    }

    public String fragment() {
        return switch (type) {
            case TEXT, NUMBER, SELECT -> "single-input";
            case DATE -> "date-input";
            case RADIO -> "radio-input";
            case CHECKBOX -> "checkbox-input";
        };
    }

    @SuppressWarnings("unused")
    public boolean hasFollowUps() {
        return !this.followUps.isEmpty();
    }
}