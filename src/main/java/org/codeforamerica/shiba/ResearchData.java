package org.codeforamerica.shiba;

import lombok.Builder;
import lombok.Value;

import java.sql.Date;

@Value
@Builder
public class ResearchData {
    String spokenLanguage;
    String writtenLanguage;
    String sex;
    String firstName;
    String lastName;
    Date dateOfBirth;
    Boolean enteredSsn;
    String phoneNumber;
    String email;
    Boolean phoneOptIn;
    Boolean emailOptIn;
    String zipCode;
    Boolean snap;
    Boolean cash;
    Boolean housing;
    Boolean emergency;
    Boolean liveAlone;
    Double moneyMadeLast30Days;
    Double homeExpensesAmount;
    Boolean areYouWorking;

    Boolean payRentOrMortgage;
    Boolean selfEmployment;
    Double unearnedIncome;
    Integer householdSize;
}
