# How to Contribute

First of all, thank you for contributing (in advance)! Here are some guidelines of how to contribute to this repository.
You may adopt it to start your PR, or modify it to fit the case - but **PLEASE READ THEM** before starting!

## PR Workflow & Conflicts Resolving

Whether you're a collaborator or not, a PR is required for modifications to be merged into the main branch. Please
follow these steps to open a PR:

1. **Issues before pulls.** Raise an issue and discuss before modifying. This avoids wasted effort.

2. **Mirror**. Fork this repository, or make sure it's up-to-date.

3. **Sync**. Clone or pull.

    - Make sure to use **rebase** when **syncing** `origin/x` and `x` where `x` is the branch
      name (`git pull origin main --rebase`). This prevents unnecessary merge commits when pulling.

4. **New branch for modifications.** Checkout a new branch for editing.

    - Except minor changes, we highly discourage editing directly on the `main` branch of a forked repository. This
      makes syncing forks harder and brings extra uncertainty (e.g. conflicts when syncing). Always keep the `main`
      branch **a mirror** of the source will make everyone's life easier.

5. **Code.** Make modifications desired, e.g. bug fixes, features additions, etc.

6. **Test.** Test locally. This is important, as our CI only run basic checks. To verify your modifications, tests are
   necessary.

7. **Update.** (Optional but recommended) After your modification, our repository might have updated. Make sure your
   branch is up-to-date by syncing forks and pull again. Then resolve conflicts, if any.

    - By updating the main branch, you'll be able to test your changes upon the latest version. You can still create a
      PR without updating or resolving any possible conflicts.

      However, for branches not up-to-date, especially with conflicts and a non-linear commit tree, the merge process
      can be really complex and time-consuming.

    - **DO NOT** merge `main` into your branch if `main` is ahead of your commits (Never ~~`git merge main feature`~~ or
      ~~`git rebase main feature`~~). Instead, **create a branch** with `feature` rebased on **top** of `main`. i.e.:

      ```shell
      # Sync forks before start
      git pull origin main --rebase # Update mirror
      git checkout -b temp main
      git rebase feature temp # Never modify main branch locally!
      # git mergetool # If any conflicts, resolve them (might be a lot!)
      # git rebase --continue # After resolving all conflicts
      git checkout temp
      # Make sure the rebase is successful by testing
      git branch -D feature # Double check before deleting!
      git branch -m temp feature # You'll have an updated branch!
      ```

      **The merge doesn't always go so smoothly.** When using rebase, some conflicts are so stubborn that a
      conflict-resolving is required for almost **each** new commit. Under such cases, you'll need to **test with merge,
      pull as-is**. i.e. Test your changes on the latest version using merge commit, then open a PR with your original
      branch.

      ```shell
      # Sync forks before start
      git pull origin main --rebase
      git checkout -b temp main
      git merge feature temp # Make sure feature remains untouched for PR
      # Resolve any conflicts. This only needs to be done once.
      git checkout temp
      # Test your changes under this merged environment
      git branch -D temp
      # Then, open a pull request: yours/feature -> us/main 
      ```

8. **PR!** Compare branches and open a pull request in our repository. Make sure to follow the PR template on the form
   page.

9. **Keep in touch.** We'll review your changes and (optionally) propose changes. You need to resolve such conversations
   so we can continue.

10. **Done!** If everything went fine, your pull request will be merged.

    - Without particular reason, we'll **rebase** your changes onto the `main` branch.

    - If the rebase contains conflicts which can't be easily resolved, we'll **squash merge** your commits. Please
      accept our apologies if this happens. We **won't consider merge commits** unless under extremely rare cases, since
      this brings extra trouble for possible reverts in the future.

## Code Style

There are no strict code style for the repository, as all code will be formatted when being merged. However, please at
least make sure the code is well-formatted without confusions, mangling or compressing.

## Code of Conduct

Collaboratos will review your code and (optionally) propose changes or open conversations. When negotiating, please
always follow our **code of conduct** (`CODE_OF_CONDUCT.md`). Do not engage in meaningless venting of emotions or blame
others harshly for their mistakes. Instead, prioritize problem solving, communicate more, and be nice.

## License Considerations

This repository uses GPL-3.0 or any later version. Your contributed code, whether in forms of PRs, code snippets in
issues or gists, once being merged or included, will be licensed under the exact same license. We do not accept an
alternative license, regardless or the compatibility. If you don't agree with this, **do not open PRs** so that we don't
get into pointless copyright disputes.

If external libraries are introduced in your modifications, please confirm that their license is at least compatible
with GPL-3.0 or a re-licensing is available. Otherwise we can't include it.

## My PR Is Not Getting Merged! Why?

For several reasons, one PR might be closed rather than merged (We won't leave a PR open without any updates).

- The author required.

  You can close a PR at any time before being merged, without any reason.

- Terrible code quality.

  We'll try our best to make the code reliable, but we can't always achieve this. If the code quality is too low to be
  fixed, then we have no choice but to reject it.

- Functionality tests failed and can't be fixed easily.

  A PR should at least work well before being merged, right?

- The feature branch is far behind the main branch and the test has failed.

  Both updating and testing will be hard under such cases. Consider start a new fork and apply the changes.

- The feature is irrelevant or not worthing its cost.

  Not all PRs are considered cost-effective. Let alone irrelevant commits!

- Code contains malicious code.

  This will harm the community and the codebase. Relevant personnel will be dealt with **seriously**.

- Unable to reach consensus on discussions.

  We can't just leave problems unresolved and, if neither of us can convince the other, we will have to reject the
  request.

- Other special circumstances.

Please understand that your PR might not ended up being merged out of these reasons. Whether this happens or not, please
don't let it discourage you from raising possible suggestions or starting a new one. Each issue or PR helps us to find
our faults and improve the project. Even if your code does not ultimately appear in the repository, your contribution
will still be recorded and mentioned.


