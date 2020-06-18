package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.Utils.getFormInputName;

@Data
public class Condition {
    String input;
    String value;
    ValidationMatcher matcher = ValidationMatcher.CONTAINS;

    public Boolean appliesTo(MultiValueMap<String, String> model) {
        List<String> inputValue = Optional.ofNullable(model)
                .map(modelMap -> modelMap.get(getFormInputName(input)))
                .orElse(List.of());
        return this.matcher.matches(inputValue, value);
    }

    public Boolean appliesTo(FormData formData) {
        List<String> inputValue = Optional.ofNullable(formData)
                .map(nonNullFormData -> nonNullFormData.get(input).getValue())
                .orElse(List.of());
        return this.matcher.matches(inputValue, value);
    }

}
