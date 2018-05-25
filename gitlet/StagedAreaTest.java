package gitlet;

import static org.junit.Assert.*;

import javafx.stage.Stage;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.System;

public class StagedAreaTest {

    @Test
    public void stagedMapTest() {
        StagedArea sA = new StagedArea();
        System.out.println(sA.stagedMap);
    }
}
