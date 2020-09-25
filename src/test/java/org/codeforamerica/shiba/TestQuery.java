package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.Map;

public class TestQuery implements Query<Map<String, Address>>{
    @Override
    public Map<String, Address> run(ApplicationData applicationData) {
        Address address1 = new Address("street", "city", "CA", "02103");
        Address address2 = new Address("different street", "cool city", "NH", "66666");
        return Map.of(
                "validated", address1,
                "original", address2
        );
    }
}
