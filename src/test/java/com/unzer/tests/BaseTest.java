package com.unzer.tests;

import com.unzer.util.DatabaseHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;

public class BaseTest {

    @AfterAll
    public static void afterTest() {
        DatabaseHelper.closeConnection();
    }
}
