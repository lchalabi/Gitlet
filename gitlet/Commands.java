package gitlet;

import java.io.*;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.*;

/** This class represents the Commands possible for gitlet application.
 * @author Lila Chalabi*/

public class Commands {

    /** Initializes a .gitlet directory in the current directory if one does not already exist, throws an error message
     * otherwise.  This command must be executed exactly once and before any other commands may be called. */
    public void init() throws IOException {

        StagedArea stagedArea = new StagedArea();

        /* creates the initial commit and the master branch and sets the current branch to master.*/
        gitlet.Commit firstcommit = new gitlet.Commit();
        stagedArea._branches.put("master", firstcommit.getUid());
        stagedArea.current_branch_name = "master";

        /* creates the .gitlet directory if one does not exist */
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        gitlet.mkdir();

        /* creates the staging file and stores the current state of the StagedArea and creates the objects directory and stores
        * commits and the files (blobs) they reference. */
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        stagedFile.createNewFile();

        File objects =  new File(System.getProperty("user.dir")
                + "/.gitlet/objects");
        objects.mkdir();


        firstcommit.blobReference.putAll(stagedArea.stagedMap);
        stagedArea.stagedMap.clear();
        stagedArea.commitList.add(firstcommit.getUid());

        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(stagedFile));
        out.writeObject(stagedArea);
        out.close();

        ObjectOutputStream out1 =
                new ObjectOutputStream(new FileOutputStream(objects
                        + "/" + firstcommit.getUid()));
        out1.writeObject(firstcommit);
        out1.close();

        /* sets the head commit of the current branch (master) to be the first commit */
        File head = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Utils.writeObject(head, firstcommit);

    }


    /** Add allows us to add the file with the given filename in our current directory to the StagedArea in preparation f
     * or the next commit.  Technically, I read the staged file (deserialize it) and add the new filename, sha-1 UID
     * pair to the stagedMap in the staging area.  I take the file off the toDelete list if it is already on it.
     * @param filename
     */
    public void add(String filename)
            throws ClassNotFoundException, IOException {
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        File wdFile = new File(filename);
        if (!wdFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        /* creates a sha1 UID for the given file using the files contents */
        byte[] wdFileContents = Utils.readContents(wdFile);
        String sha1wdFile = Utils.sha1(wdFileContents);

        File file = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(file, StagedArea.class);

        if (sA.toDelete.contains(filename)) {
            sA.toDelete.remove(filename);
        }

        /* If the file remains unchanged since the last commit of the given branch (head) then we don't need to add it
        to the staging area. Head files are automatically added to the next commit unless they have been specified for
        removal by the rm command.  Else, we add the file, sha1 UID pair to the stagedMap and write the updated staged
        file. We also serialize the file itself and store it to the objects directory. */
        if (head.blobReference.containsKey(filename)
                && head.blobReference.get(filename).equals(sha1wdFile)) {
            sA.stagedMap.remove(filename);
            Utils.writeObject(file, sA);
        } else {
            sA.stagedMap.put(filename, sha1wdFile);

            File blob = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + sha1wdFile);
            blob.createNewFile();
            byte[] blobContents = Utils.readContents(wdFile);

            Utils.writeContents(blob, blobContents);
            Utils.writeObject(file, sA);
        }
    }


    /** The commit command creates a new commit, sets the new commit's blobReference to be equal to its parents.
     * Technically, I read from the staged file and put all added files into the new commit's blobReference map.
     * @param message is the log message.  If the user has not made any changes to the commit (ie they have not added
     * any files to the stagedMap in the staging area or to the toDelete list in the staging area) or if they have not
     * included a commit message, then I throw an error message.
     */
    public void commit(String message) {
        File file = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(file, StagedArea.class);
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        if (sA.stagedMap.isEmpty() && sA.toDelete.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }

        /* when we initialize a Commit we add all the files in the stagedMap to the blobReference which is why we
        are able to clear the stagedMap in the next line. */
        Commit newCommit = new Commit(message);

        sA.stagedMap.clear();
        sA.commitList.add(newCommit.getUid());

        /* sets the head to this new commit */
        sA._branches.put(sA.current_branch_name, newCommit.getUid());
        sA.toDelete.removeAll(head.blobReference.keySet());

        /* writing the information we have just created and updated to file */
        File commit = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + newCommit.getUid());
        Utils.writeObject(commit, newCommit);

        Utils.writeObject(file, sA);

        Utils.writeObject(headfile, newCommit);

    }

    /** Removes the specified file if the head file is tracking or the file has been staged for the next commit.  If the
     * the head commit is tracking the file then I delete the file from the working directory as well as unstaging it from the
     * next commit.
     * @param filename the name of the file specified for removal
     */
    public void rm(String filename) {
        boolean staged;
        boolean headIsTracking = false;

        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        staged = sA.stagedMap.containsKey(filename);

        if (staged) {
            sA.stagedMap.remove(filename);
        }

        if (head.blobReference.containsKey(filename)) {
            Utils.restrictedDelete(filename);
            headIsTracking = true;
            if (!sA.toDelete.contains(filename)) {
                sA.toDelete.add(filename);
            }
        }
        if (!headIsTracking && !staged) {
            System.out.println("No reason to remove the file.");
            return;
        }

        Utils.writeObject(stagedFile, sA);
    }

    /** Reveals a formatted list or log of all commits on the current branch, going back along the chain of parent references
     * until reaching the initial commit (where parent reference equals a blank string).  The log shows the commit UID, the
     * timestamp, and the log message. */
    public void log() {
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        Commit pointer = head;
        Formatter out = new Formatter();

        out.format("===\n");
        out.format("commit %s\n", pointer.getUid());

        SimpleDateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        out.format("Date: %s\n", dateformat.format(pointer.getTimeStamp()));

        out.format("%s\n", pointer.getLogMessage());

        while (true) {
            File parentcommitFILE = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/"
                    + pointer.getParentReference());
            pointer = Utils.readObject(parentcommitFILE, Commit.class);

            out.format("\n===\n");
            out.format("commit %s\n", pointer.getUid());
            out.format("Date: %s\n", dateformat.format
                    (pointer.getTimeStamp()));
            out.format("%s\n", pointer.getLogMessage());

            if (pointer.getParentReference().equals("")) {
                break;
            }

        }
        System.out.println(out.toString());
    }

    /** Like log, except displays information about all commits ever made. The order of the commits does not matter. */
    public void globalLog() {
        Formatter out = new Formatter();
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);
        Commit commit;
        File commitFile;
        SimpleDateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");

        /* sA.commitList is an array list of sha1 IDs pertaining to every commit ever made, added at the creation of
        the commit.
         */
        for (String sha1 : sA.commitList) {
            commitFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + sha1);
            commit = Utils.readObject(commitFile, Commit.class);

            out.format("===\n");
            out.format("commit %s\n", commit.getUid());
            out.format("Date: %s\n", dateformat.format(commit.getTimeStamp()));
            out.format("%s\n\n", commit.getLogMessage());
        }

        String text = out.toString();
        System.out.println(text.substring(0, text.lastIndexOf('\n')));
    }

    /** Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such
     * commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword
     * message, I put the operand in quotation marks, as for the commit command.
     * @param logMessage given commit message
     */
    public void find(String logMessage) {
        Formatter out = new Formatter();
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);
        int count = 0;
        String message;

        /* sA.commitList is an array list of sha1 IDs pertaining to every commit ever made, added at the creation of
        the commit.
         */
        for (String sha1 : sA.commitList) {
            File commitfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + sha1);
            Commit commit = Utils.readObject(commitfile, Commit.class);

            message = commit.getLogMessage();

            if (message.equals(logMessage)) {
                out.format("%s\n", commit.getUid());
                count += 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
        System.out.println(out.toString());

    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged or marked for untracking. I pull branches from an instance
     * variable of the StagedArea object, _branches, a hashmap of branch names with their head commit sha-1 ids.
     * I pull staged files from an instance variable of the StagedArea object, stagedMap, another hashmap that maps
     * filenames to their sha-1 ids.  Lastly for deleted files, I pull their names from an array list instance variable
     * of the StagedArea object, toDelete (a list of filenames that have been marked for removal). */
    public void status() {
        Formatter out = new Formatter();

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);


        SArrayList sortedbranchnames = new SArrayList();
        sortedbranchnames.addAll(sA._branches.keySet());
        Collections.sort(sortedbranchnames);

        out.format("=== Branches ===\n");
        for (String branchName : sortedbranchnames) {
            if (sA._branches.get(branchName).equals(head.getUid())) {
                branchName = '*' + branchName;
            }
            out.format(branchName + "\n");
        }

        out.format("\n=== Staged Files ===\n");
        SArrayList sortedstagednames = new SArrayList();
        sortedstagednames.addAll(sA.stagedMap.keySet());

        Collections.sort(sortedstagednames);
        for (String stagedName : sortedstagednames) {
            out.format(stagedName + "\n");
        }

        out.format("\n=== Removed Files ===\n");
        ArrayList<String> sorteddeletes = sA.toDelete;
        Collections.sort(sorteddeletes);
        for (String delete : sorteddeletes) {
            out.format(delete + "\n");
        }

        out.format("\n=== Modifications Not Staged For Commit ===\n");

        out.format("\n=== Untracked Files ===\n");

        System.out.println(out.toString());
    }

    /** Takes the version of the file as it exists in the head commit, the front of the current branch,
     * and puts it in the working directory, overwriting the version of the file that's already there if there is one.
     * The new version of the file is not staged.
     * @param filename name of file to checkout */
    public void checkout(String filename) throws IOException {
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);
        checkout(head.getUid(), filename);
    }

    /** Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
     * @param commitID UID of desired commit
     * @param filename name of file to checkout */
    public void checkout(String commitID, String filename) throws IOException {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        commitID = commitSearch(commitID);

        /* Error cases if the commit doesn't exist or if the commit doesnt contain the desired file */
        if (commitID.equals("null")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        File commitFile = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + commitID);
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (!commit.blobReference.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        /* find the sha1 of the desired version of the file to checkout, create the file in the current directory if it
        does not exist, write the contents of the desired version to the file in the current directory.  */
        String filesha1 = commit.blobReference.get(filename);
        File targetfile = new File(filename);
        File blob = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + filesha1);

        if (!targetfile.exists()) {
            targetfile.createNewFile();
        }

        byte[] blobcontents = Utils.readContents(blob);
        Utils.writeContents(targetfile, blobcontents);

    }

    /** Creates a new branch with the given name, and points it at the current head commit node. A branch is nothing more
     * than a name for a reference (a SHA-1 identifier) to a commit node. This command does not immediately switch to
     * the newly created branch (just as in real Git). Before the user ever calls branch, the code runs
     * with a default branch called "master".
     *
     * @param branchName the name of the new branch
     */
    public void branch(String branchName) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        if (sA._branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            File headfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/HEAD");
            Commit head = Utils.readObject(headfile, Commit.class);
            sA._branches.put(branchName, head.getUid());
            Utils.writeObject(stagedFile, sA);
        }
    }

    /** Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current
     * branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the
     * checked-out branch is the current branch.
     *
     * Errors: 1) if the file does not exist in the previous commit, print "file does not exist in that commit."
     * 2) if no commit with the given id exists, print "no commit with that id exists." Else, if the file does not exist in the given commit,
     * print the same message as for failure case 1, or if 3) no branch with that name exists, print "no such branch exists."
     * If that branch is the current branch, print "No need to checkout the current branch." If a working file is untracked
     * in the current branch and would be overwritten by the checkout, print "There is an untracked file in the way;
     * delete it or add it first." and exit; I perform this check before doing anything else.
     * @param branchName name of branch to checkout */
    public void branchCheckout(String branchName) throws IOException {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        if (!sA._branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (sA.current_branch_name.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            File bheadfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/"
                    + sA._branches.get(branchName));
            Commit bhead = Utils.readObject(bheadfile, Commit.class);

            Set<String> headfiles = head.blobReference.keySet();
            Set<String> bheadfiles = bhead.blobReference.keySet();

            List<String> wdfiles = Utils.plainFilenamesIn(
                    System.getProperty("user.dir"));
            for (String file : wdfiles) {
                if ((!headfiles.contains(file))
                        && bheadfiles.contains(file)) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it or add it first.");
                    return;
                }
            }

            for (String file : headfiles) {
                if (!bheadfiles.contains(file)) {
                    Utils.restrictedDelete(file);
                }
            }
            for (String bfile : bheadfiles) {
                checkout(bhead.getUid(), bfile);
            }

            if (!sA.current_branch_name.equals(branchName)) {
                sA.stagedMap.clear();
            }

            sA.current_branch_name = branchName;
            Utils.writeObject(headfile, bhead);
            Utils.writeObject(stagedFile, sA);
        }
    }

    /** Deletes the branch with the given name. This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     *
     * @param branchName the name of the branch whose pointer will be deleted
     */
    public void rmbranch(String branchName) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        if (!sA._branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (sA.current_branch_name.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            sA._branches.remove(branchName);
            Utils.writeObject(stagedFile, sA);
        }

    }

    /** Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node.  The commit id may be abbreviated as for checkout.
     * The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the
     * current branch head.
     *
     * @param commitID string
     * @throws IOException yes
     */
    public void reset(String commitID) throws IOException {

        commitID = commitSearch(commitID);

        if (commitID.equals("null")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        List<String> wdfiles = Utils.plainFilenamesIn(
                System.getProperty("user.dir"));

        File commitFile = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/"
                + commitID);
        Commit commit = Utils.readObject(commitFile, Commit.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        Set<String> committrackedfiles = commit.blobReference.keySet();
        Set<String> headtrackedfiles = head.blobReference.keySet();

        /* checks for untracked files that would be overwritten by reset, ie files that are in both the working directory and desired
        commit but have not been added/staged.
         */
        for (String file : wdfiles) {
            if ((!headtrackedfiles.contains(file))
                    && committrackedfiles.contains(file)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                return;
            }
        }

        /* deletes files from working directory that are present only in the head commit */
        for (String file : headtrackedfiles) {
            if (!committrackedfiles.contains(file)) {
                Utils.restrictedDelete(file);
            }
        }

        /* checks out all other files */
        for (String file : committrackedfiles) {
            checkout(commitID, file);
        }

        sA.stagedMap.clear();
        sA._branches.put(sA.current_branch_name, commitID);
        Utils.writeObject(headfile, commit);
        Utils.writeObject(stagedFile, sA);

    }

    /** Performs the infamous merge!!! Essentially, although the explanation is rather long and complicated given the various possible
     * circumstances, the command uses the split point of a given branch and the current branch to merge files from the given branch
     * into the current branch.  The split point is the commit ID before the creation of the new branch.
     *
     * @param branchName the name of the given branch to merge with the current branch
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void merge(String branchName) throws IOException,
            ClassNotFoundException {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit curr = Utils.readObject(headfile, Commit.class);

        Set<String> currfiles = curr.blobReference.keySet();

        if (!sA.stagedMap.isEmpty()
                || !Collections.disjoint(currfiles, sA.toDelete)) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!sA._branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (branchName.equals(sA.current_branch_name)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        File branchFile = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + sA._branches.get(branchName));
        Commit given = Utils.readObject(branchFile, Commit.class);

        Set<String> givenfiles = given.blobReference.keySet();

        List<String> wdfiles = Utils.plainFilenamesIn(System.getProperty
                ("user.dir"));
        for (String file : wdfiles) {
            if ((!currfiles.contains(file))
                    && givenfiles.contains(file)) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it or add it first.");
                return;
            }
        }

        String givenhead = sA._branches.get(branchName);
        String currhead = curr.getUid();
        String splitID = findSplitPoint(givenhead, currhead);

        if (splitID.equals(givenhead)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        } else if (splitID.equals(currhead)) {
            String currname = sA.current_branch_name;
            branchCheckout(branchName);
            sA.current_branch_name = currname;
            sA._branches.put(currname, givenhead);
            System.out.println("Current branch fast-forwarded.");
            Utils.writeObject(stagedFile, sA);
        } else {
            File spfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + splitID);
            Commit split = Utils.readObject(spfile, Commit.class);
            Set<String> splitfiles = split.blobReference.keySet();

            Set<String> allfiles = new HashSet<String>();
            allfiles.addAll(splitfiles);
            allfiles.addAll(currfiles);
            allfiles.addAll(givenfiles);
            boolean conflict = false;

            for (String file : allfiles) {
                String sID = split.blobReference.get(file);
                String cID = curr.blobReference.get(file);
                String gID = given.blobReference.get(file);

                if (sID != null) {
                    if (cID != null && gID != null) {
                        if ((!gID.equals(sID)) && cID.equals(sID)) {
                            checkout(givenhead, file);
                            add(file);
                        }
                    } else if (gID == null && cID != null) {
                        if (cID.equals(sID)) {
                            rm(file);
                        }
                    }
                } else if (cID == null && gID != null) {
                    checkout(givenhead, file);
                    add(file);
                }
                if ((sID != null && cID != null && gID != null
                        && !gID.equals(sID) && !gID.equals(sID)
                        && !cID.equals(sID))
                        || (sID == null && cID != null && gID != null
                        && !cID.equals(gID))
                        || (sID != null && gID == null && !sID.equals(cID))
                        || (sID != null && cID == null && !sID.equals(gID))) {
                    Formatter out = new Formatter();
                    File currfile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + cID);
                    File givenfile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + gID);
                    String currcontents;
                    String givencontents;
                    if (!currfile.exists()) {
                        givencontents = Utils.readContentsAsString(givenfile);
                        currcontents = "";
                    } else if (!givenfile.exists()) {
                        givencontents = "";
                        currcontents = Utils.readContentsAsString(currfile);
                    } else {
                        givencontents = Utils.readContentsAsString(givenfile);
                        currcontents = Utils.readContentsAsString(currfile);
                    }
                    out.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                            currcontents, givencontents);
                    String newcontents = out.toString();
                    File confl = new File(file);
                    Utils.writeContents(confl, newcontents);
                    add(file);
                    conflict = true;
                }
            }
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
            String logmessage = "Merged " + branchName + " into "
                    + sA.current_branch_name + ".";
            commit(logmessage);
        }

    }

    /** Locates the split point of two branches.  Creates a list of commit IDs starting from the
     * given branch head ID, and then goes backwards in parent references from the current branch head commitID and returns the
     * first commitID that is also found in the list of commit IDs starting from the given branch head ID.
     *
     * @param givenhead head of given branch
     * @param currhead head of current branch
     * @return most recent overlapping commitID, the split point of the branches
     */
    public String findSplitPoint(String givenhead, String currhead) {
        SArrayList givenhistory = new SArrayList();
        SArrayList currhistory = new SArrayList();
        while (!givenhead.equals("")) {
            File pgivenfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + givenhead);
            Commit pgiven = Utils.readObject(pgivenfile, Commit.class);
            givenhistory.add(givenhead);
            givenhead = pgiven.getParentReference();
        }
        while (!currhead.equals("")) {
            File pcurrfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + currhead);
            Commit pcurr = Utils.readObject(pcurrfile, Commit.class);
            if (givenhistory.contains(currhead)) {
                return currhead;
            }
            currhistory.add(currhead);
            currhead = pcurr.getParentReference();
        }
        System.out.println(currhistory);

        return "";
    }



    /** commitSearch allows for abbreviations of the hexadecimal sha-1 unique identifier when checking out, etc.
     * @param id is the potentially abbreviated string of the full 40 char hexadecimal uid
     * @return full 40 char hexadecimal id */
    public String commitSearch(String id) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        StagedArea sA = Utils.readObject(stagedFile, StagedArea.class);
        for (String commitID : sA.commitList) {
            if (commitID.startsWith(id)) {
                return commitID;
            }
        }
        return "null";
    }

    /** avoids issues with array lists of strings. */
    private class SArrayList extends ArrayList<String> {
    }





}

