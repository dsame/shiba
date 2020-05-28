package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class ProgramSelectionPresenter {
    private final ProgramSelection programSelection;

    private final Map<BenefitProgram, String> benefitProgramNameMap;

    public ProgramSelectionPresenter(ProgramSelection programSelection, MessageSource messageSource, Locale locale) {
        Set<ConstraintViolation<ProgramSelection>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(programSelection);
        if (!constraintViolations.isEmpty()) {
            throw new IllegalArgumentException();
        }

        benefitProgramNameMap = Map.of(
                BenefitProgram.EMERGENCY, messageSource.getMessage("how-it-works.emergency", new Object[]{}, locale),
                BenefitProgram.CHILD_CARE, messageSource.getMessage("how-it-works.child-care", new Object[]{}, locale),
                BenefitProgram.FOOD, messageSource.getMessage("how-it-works.food", new Object[]{}, locale),
                BenefitProgram.CASH, messageSource.getMessage("how-it-works.cash", new Object[]{}, locale)
        );

        this.programSelection = programSelection;
    }

    public String getTitleString() {
        SortedSet<BenefitProgram> programs = programSelection.getPrograms();
        if (programs.size() == 1) {
            return this.benefitProgramNameMap.get(programs.first());
        } else {
            BenefitProgram lastBenefitProgram = programs.last();
            String commaSeparatedList = programs.headSet(lastBenefitProgram).stream()
                    .map(this.benefitProgramNameMap::get)
                    .collect(Collectors.joining(", "));
            return commaSeparatedList.concat(String.format(" and %s", this.benefitProgramNameMap.get(lastBenefitProgram)));
        }
    }
}
