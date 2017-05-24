# dndi-android
A repository for MSIT-ESE studio project

## Policy of repository management

1. The master branch is a release branch. Any commit there should be releasable.
2. Everyone catches up with updates on the master branch everyday.
3. No `git reset` is allowed on the master branch.
4. Use valid commit messages and include name tag (e.g. [yulunt] fix uppercase and lowercase bug in login).
5. The granularity of commits on the master should be a “story”.
6. Pass integration test before any merges to the master/develop branch.
7. Anything in the published branch should be unitttested, master/develop/exp are published branches.
8. When you want to merge to public branch, clean commits on current branch first (e.g. merge local commits).
9. Check in only source codes.
10. No experiment on master, they should be kept on the exp branch.

