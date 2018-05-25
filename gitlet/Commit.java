package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.lang.System;

/** The Commit class represents a commit object.  A commit contains a parentReference (id of its parent), a logMessage, a
 * blobReference hashmap (a map between UIDS of the files in the commit and respective filenames), timeStamp, and the commit's
 * own UID.
 * @author Lila Chalabi
 * */
public class Commit implements Serializable {

    /** initializes commit object with a log message.
     *
     * @param logMessage
     */
    Commit(String logMessage) {
        File headFile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headFile, Commit.class);

        String parentCommitUID = head.uid;

        this.logMessage = logMessage;
        this.blobReference = getBlobReference();

        this.parentReference = parentCommitUID;
        this.timeStamp = new Date();
        this.uid = Utils.sha1(logMessage, timeStamp.toString(),
                parentReference, blobReference.toString());
    }

    /** Initializes the first commit in a gitlet directory once the init command is called. */
    Commit() {
        this.logMessage = "initial commit";
        this.parentReference = "";

        this.timeStamp = new Date(0);

        this.uid = gitlet.Utils.sha1(logMessage, timeStamp.toString(),
                parentReference, blobReference.toString());
    }

    /** Various methods to access contents of commit instance variables.
     * @return  logmessage */
    String getLogMessage() {
        return this.logMessage;
    }
    /** access parentRef sha-1 UID. */
    String getParentReference() {
        return this.parentReference;
    }
    /** access timestamp.
     * @return date */
    Date getTimeStamp() {
        return this.timeStamp;
    }
    /** access UID. */
    String getUid() {
        return this.uid;
    }

    /**@return the blobReference which is a hashmap of all the filenames to their sha-1 UIDs, referring to blobs (versions of
     * files in the given commit). In constructing the blobReference map, we also see if there are files staged for removal in
     * the StagingArea. */
    HashMap<String, String> getBlobReference() {
        File headFile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headFile, Commit.class);

        HashMap<String, String> bR = head.blobReference;
        File file = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(file, StagedArea.class);
        bR.putAll(sA.stagedMap);

        for (String filename : sA.toDelete) {
            bR.remove(filename);
        }
        return bR;
    }

    /** log message. */
    private final String logMessage;
    /** date. */
    private final Date timeStamp;
    /** pref. */
    private final String parentReference;
    /** blob ref. */
    HashMap<String, String> blobReference = new HashMap<String, String>();
    /** uid. */
    private final String uid;
    /** head. */
    static Commit _head = null;

    /** Protects from issues with string array lists. */
    private class SArrayList extends ArrayList<String> {
    }


}
