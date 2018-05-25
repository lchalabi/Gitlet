package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Lila Chalabi
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void badInit() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Commands commands = new Commands();
        commands.init();
        assertEquals(outContent.toString(),
                "A Gitlet version-control system already "
                        + "exists in the current directory.\n");
    }

    @Test
    public void logTest() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Commands commands = new Commands();
        String log = "===\n"
                +
                "commit e6f6d1a88e5673a818f767f86c36b6a8cb8b185a\n"
                +
                "Date: Wed Dec 6 15:35:40 2017 -0800\n"
                +
                "Merged master into other.\n"
                +
                "\n"
                +
                "===\n"
                +
                "commit 70350c36d94838905c1d67280d6c6b196a9a96e9\n"
                +
                "Date: Wed Dec 6 15:35:31 2017 -0800\n"
                +
                "2\n"
                +
                "\n"
                +
                "===\n"
                +
                "commit 7132c0bcc30d4d68f9e918afa832ff801f2f862a\n"
                +
                "Date: Wed Dec 31 16:00:00 1969 -0800\n"
                +
                "initial commit\n\n";
        commands.log();
        assertEquals(outContent.toString(), log);
    }

    @Test
    public void findTest() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Commands commands = new Commands();
        commands.find("Merged master into other.");
        String commitID = "e6f6d1a88e5673a818f767f86c36b6a8cb8b185a\n\n";
        assertEquals(outContent.toString(), commitID);
    }

    @Test
    public void splitpointTest() {
        Commands commands = new Commands();
        assertEquals(commands.findSplitPoint(
                "70350c36d94838905c1d67280d6c6b196a9a96e9",
                "e6f6d1a88e5673a818f767f86c36b6a8cb8b185a"),
                "70350c36d94838905c1d67280d6c6b196a9a96e9");
        assertEquals(commands.findSplitPoint(
                "0fb7b3d44472b661ed8dc54640e3fd6a66500378",
                "70350c36d94838905c1d67280d6c6b196a9a96e9"),
                "7132c0bcc30d4d68f9e918afa832ff801f2f862a");
    }

    @Test
    public void globallogTest() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Commands commands = new Commands();
        commands.globalLog();
        String globallog = "===\n"
                +
                "commit 7132c0bcc30d4d68f9e918afa832ff801f2f862a\n"
                +
                "Date: Wed Dec 31 16:00:00 1969 -0800\n"
                +
                "initial commit\n"
                +
                "\n"
                +
                "===\n"
                +
                "commit 0fb7b3d44472b661ed8dc54640e3fd6a66500378\n"
                +
                "Date: Wed Dec 6 15:34:51 2017 -0800\n"
                +
                "1\n"
                +
                "\n"
                +
                "===\n"
                +
                "commit 70350c36d94838905c1d67280d6c6b196a9a96e9\n"
                +
                "Date: Wed Dec 6 15:35:31 2017 -0800\n"
                +
                "2\n"
                +
                "\n"
                +
                "===\n"
                +
                "commit e6f6d1a88e5673a818f767f86c36b6a8cb8b185a\n"
                +
                "Date: Wed Dec 6 15:35:40 2017 -0800\n"
                +
                "Merged master into other.\n"
                +
                "\n";
        assertEquals(outContent.toString(), globallog);
    }



}


