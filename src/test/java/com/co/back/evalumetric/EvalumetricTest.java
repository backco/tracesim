package com.co.back.evalumetric;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class EvalumetricTest {

    @Test
    void localDBUnsertTest () {

	Evalumetric.dbInsert("TEST", "TEST", "TEST", "TEST", -1.0, -1.0);
    }
}
