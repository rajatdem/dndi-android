# dndi-android
An Android data normalization and inference framework using the bezirk
middleware for inter-process communication

## Policies of repository management

1. The master branch is a release branch. Any commit there should be releasable.
2. Everyone catches up with updates on the master/develop branch everyday.
3. No `git reset` is allowed on the master branch.
4. Use valid commit messages and include your name tag (e.g. [yulunt] fix uppercase and lowercase bug in login).
5. The granularity of commits on the master should be an “user story”.
6. Include and pass all unit/integration tests before merging to the master/develop branch.
7. Anything in the published branch should be unit tested, master/develop/exp are published branches.
8. When you want to merge to public branch, clean commits on current branch first (e.g. merge local commits).
9. Check in only source codes.
10. No experiment on master, they should be kept on the exp branch.
11. Remove branches once they are successfully merged to the master/develop branch.


## Coding style and code review checkboxes


- Exception handling
  1. No generic exception. Handle exceptions one by one and restructure them if too many duplicated codes.
  2. Always handle exception (logging does not count). Throw exception to the caller is a preferred approach.
  3. If you are positive that "crash" is the best way, then throw `RuntimeException`
- No finalizer, handle cleanup using a explicit `close()` function. Always check whether the resource is released properly.
- Use full qualifier for imported packages
- Every class and nontrivial public method you write must contain a Javadoc comment with at least one sentence 
describing what the class or method does. This sentence should start with a third person descriptive verb. 
(No need for trivial cases e.g. setters and getters)
- No methods longer than 40 lines
- Limit Variable Scope
- Naming convetion
  1. Non-public, non-static field names start with m.
  2. Static field names start with s.
  3. Other fields start with a lower case letter.
  4. Public static final fields (constants) are ALL\_CAPS\_WITH\_UNDERSCORES.
- Braces do not go on their own line; they go on the same line as the code before them
- Put `// TODO: <explanation>` for any temporary codes
- Use `StringBuilder` over operator `+`
- More details on [Android Coding Style](https://source.android.com/source/code-style)

