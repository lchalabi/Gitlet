In this project I implemented a version-control system that mimics some of the basic features of the popular system Git.

The main functionality that Gitlet supports is:
1) Saving the contents of entire directories of files. In Gitlet, this is called committing, and the saved contents themselves are called commits.
2) Restoring a version of one or more files or entire commits. In Gitlet, this is called checking out those files or that commit.
3) Viewing the history of backups. In Gitlet, you view this history in something called the log.
4) Maintaining related sequences of commits, called branches.
5) Merging changes made in one branch into another.

Every object, blob (file) or commit, has a unique integer id that serves as a reference to the object.  An interesting feature of Gitlet is that these ids
are universal: two objects with exactly the same content will have the same id on all systems. In the case of blobs, "same content"
means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the
same parent reference. The objects in a repository are thus said to be content addressable.

Similar to Git, I used a SHA-1 (secure hash 1) ID to identify objects which produces a 160-bit integer hash from any sequence of bytes.
Crypto hash functions have the useful property that it is difficult to find two different byte streams with the same hash value so the
likelihood of collision is very unlikely, (10^(-48)) to be exact!  One can think of it as a fundamental bug in the code that never occurs.

Notable classes:
Commands (implementation of all commands)
Commit (implementation of commit object)
Staged Area (location to store information for next commit)
Main (implementation of the interpreter)
Utils (utility methods mostly having to do with reading/writing files, serialization, and SHA-1 uids)

Testing:
All files that end in .in are integration tests passed through the python3 file tester.py (I did not write tester.py, only the
integration tests themselves). To run any given integration test within the gitlet/testing directory: python3 tester.py --verbose FILE.in.


