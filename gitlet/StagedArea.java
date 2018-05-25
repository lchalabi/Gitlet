package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

/** add array list for to be removed files. */
class StagedArea implements Serializable {

    /** constructor for staging area. */
    StagedArea() {
        stagedMap = new HashMap<String, String>();
        toDelete = new ArrayList<String>();
        commitList = new ArrayList<String>();
        _branches = new HashMap<String, String>();

    }

    /** map of staged files, between file name and SHA-1 name. */
    public HashMap<String, String> stagedMap;

    /** array list of filenames to  be deleted. */
    public ArrayList<String> toDelete;

    /** array list of all commit sha-1 UIDs to-date. */
    public ArrayList<String> commitList;

    /** maps the branch name to the head sha-1 commit UID of a branch. */
    public HashMap<String, String> _branches = new HashMap<String, String>();

    /** current branch. */
    public String current_branch_name;
}
