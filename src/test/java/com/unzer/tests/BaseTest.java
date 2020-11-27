package com.unzer.tests;

import com.unzer.helpers.DatabaseHelper;
import org.junit.jupiter.api.AfterAll;

public class BaseTest {

    @AfterAll
    public static void afterTest() {
        DatabaseHelper.closeConnection();
    }
}
