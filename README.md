# `@atomist/git-content-sync-skill`

<!---atomist-skill-readme:start--->

Keep track of files that should have the same content across different GitHub repositories.

-   Raise Pull Requests when files have old content that should be updated.
-   Raise Issues when two files in different Repos have exactly the content and ask the maintainers whether this
    skill should start keeping them in sync.

# What it's useful for

Some files start out with the same content and then diverge. Other files get copied into Repositories but should
stay synchronized with some dynamic "target" content. This skill helps teams manage these updates.

# Before you get started

Connect and configure these integrations:

1. **GitHub**
2. **Slack** (optional)

The **GitHub** integration must be configured in order to use this skill.
At least one repository must be selected. We recommend connecting the **Slack** integration.

When the optional Slack integration is enabled, users can interact with this skill directly from Slack.

# How to configure

1.  **Select some glob patterns**

    These will represent candidate files that could have identical content across all Repositories. Selecting a
    glob pattern will start any synchronization process. However, the skill will being looking for files,
    matching this pattern, that have the same name and the same content.

2.  **Select some Repos**

    Either select all, if all your Repositories should participate. Or select a subset of your Repositories for content
    tracking.

# How to Use

The skill will raise Pull Requests whenever we detect a file that is out of a sync with a target version. If your
version should become the target for other users, make a Comment `/setTarget` and then close the PR. Make a Comment
`/setTarget --broadcast` if you want to immediately raise PRs on other Repos. You can also respond with with
`/unsetTarget` to remove stop synchronizing these files. All Pull Requests for this target will be immediately closed.

This skill will raise a GitHub Issue if it detects two files with the same name, and the same content. Close the
Issue to do nothing about this. The skill will not resend it. If you would like the content to stay the same, respond
in a comment with `/setTarget`.

If you have the Slack integration installed, you can use Slack to set targets and request information about files
that are out of sync.

1.  Set a Target

    ```
    @atomist content target set --file 'license.txt'
    ```

2.  Check a File across all Repos

    ```
    @atomist content target list --file 'license.txt'
    ```

<!---atomist-skill-readme:end--->

---

Created by [Atomist][atomist].
Need Help? [Join our Slack workspace][slack].

[atomist]: https://atomist.com/ "Atomist - How Teams Deliver Software"
[slack]: https://join.atomist.com/ "Atomist Community Slack"
