package org.codeforamerica.shiba;

import lombok.Value;

@Value
public class Address {
    String street;
    String city;
    String state;
    String zipcode;

    @Override
    public String toString() { return ""; }
}
