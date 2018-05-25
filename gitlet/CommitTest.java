package gitlet;

import org.junit.Test;

import static org.junit.Assert.*;


public class CommitTest {

    @Test
    public void testCommitDate() {
        gitlet.Commit com = new gitlet.Commit();
        java.lang.System.out.println(com.getTimeStamp());
    }

    @Test
    public void testUID() {
        gitlet.Commit com = new gitlet.Commit();
        gitlet.Commit com1 = new gitlet.Commit();
        assertEquals(com.getUid(), com1.getUid());
    }
}
