package com.template.states;

import net.corda.core.identity.Party;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class SampleStateTest {
    @Test
    public void hasResidentNameFieldOfCorrectType() throws NoSuchFieldException {
        // Does the residentName field exist?
        Field residentNameField = SampleState.class.getDeclaredField("holder");
        // Is the residentName field of the correct type?
        assertTrue(residentNameField.getType().isAssignableFrom(Party.class));
    }

    /* TODO: Add an 'myNumber' property of type {@link String} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasMyNumberFieldOfCorrectType() throws NoSuchFieldException {
        // Does the myNumber field exist?
        Field myNumberField = SampleState.class.getDeclaredField("price");
        // Is the myNumber field of the correct type?
        assertTrue(myNumberField.getType().isAssignableFrom(int.class));
    }


}