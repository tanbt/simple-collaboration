package com.tanbt;

import org.junit.Assert;
import org.junit.Test;

public class ChangeTest {

    @Test
    public void convertToJson_backAndForth() {
        Change change = new Change("old value", "new value");
        String changeJson = change.toJson();
        Change changeObj = Change.fromJson(changeJson);
        Assert.assertEquals("old value", changeObj.getOldValue());
        Assert.assertEquals("new value", changeObj.getValue());
    }
}
