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
    Boolean snap;
    Boolean cash;
    Boolean housing;
    Boolean emergency;
    String firstName;
    String lastName;
    Date dateOfBirth;
    String phoneNumber;
    String email;
    Boolean phoneOptIn;
    Boolean emailOptIn;
    String zipCode;
    Boolean liveAlone;
    Double thirtyDayIncome;
    Double moneyMadeLast30Days;
    Boolean payRentOrMortgage;
    Double homeExpensesAmount;
    Boolean areYouWorking;
    Boolean selfEmployment;
    Double unearnedIncome;
    Integer householdSize;
}
