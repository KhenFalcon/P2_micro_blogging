# How to use Github and Git

There's a lot of text here, but it is really just the basics of how to use git and github in a group project. If you have questions, or there are problems, please message Josh or the others (on groupme) and we'll figure it out.

## Prerequisites
1) You have [git](https://git-scm.com/install/) installed on your local computer
2) You have an empty directory (folder) on your local machine that you want to save the (github repository) project to.

## Connecting to a Remote Repository
**Note:** This is NOT the same thing as "fork"-ing. Forking creates a copy, this is how to connect to a pre-existing repository and continue making contributions to it instead of working off of a copy.

**Note (Part 2):** Depending on your IDE of choice (I use vscode) you can do this all there instead of on a terminal, but do be careful because naming conventions can stuff can make this all very convoluted. 

1) Navigate to your empty directory and open a PowerShell terminal
    - make sure your terminal is in the correct working directory. You might need to use the `$ cd <location>` command.
2) Create a local git repository: `$ git init`
3) Create local clone of the remote repository: `$ git clone <url>`
    - For this project, the `<url>` is: https://github.com/KhenFalcon/P1_relational_algebra.git
    - **Note:** unlike forking, the `clone` command still retains its connection to the remote repository. This means (theoretically) you can work on your local "clone" repository, then pull (read others' changes) and push (write your own changes) to the remote repository. 
4) You may have to ask the repository owner ( Josh >:D ) for collaborator status, in order to push (write) to the remote repository
5) Create your own working branch: `$ git branch <name>-working-branch`
6) Checkout to your working branch: `$ git checkout <name>-working-branch`

Steps 5 and 6 are necessary in order to avoid working collisions. Especially because most of us will be working on the same file. Please make sure to not work on the main branch and only your own working branch.

## Editing and Syncing to the Remote Repository
Essentially, there is every contributor's local copy of the project that they all work on individually. Then there is the remote repository, which is the main copy that everybody pushes their changes to.

Additionally, edits/changes are specific to each branch, and for each branch there is your local copy and the remote copy. For this reason, it is best for everyone to work on their local copies of their own unique working-branch, then push their copies to the combined remote (origin) main branch.

#### Pull Requests
Are not something you generally need to worry about. It is simply how you copy edits/changes from one remote branch to another. i.e. you "ask" the other branch to accept your changes. This can all be done on github and we will primarily use this to combine all our changes to the (remote) main branch

### Pulling
```
(REMOTE) main 
    ->(copies to)-> (REMOTE) working-branch     // pull request from main branch
        ->(copies to)-> (LOCAL) working-branch  // fetch and pull to local repos
```
Adds the changes that have been pushed to the remote repository to your local copy. Note that this is only the changes that have been recorded in your own local cache (i.e. since the last time your local machine has checked). To make sure you get the most recent changes **fetch**, THEN **pull**.
- `$ git fetch --all` - pulls and caches changes from all the remote branches
- `$ git pull` or `$ git pull origin main`- applies the cached remote changes (of your specific branch) to your local branch

### Pushing
```
(REMOTE) main
    <-(copies from)<- (REMOTE) working-branch       // pull request to main branch
        <-(copies from)<- (LOCAL) working-branch    // push to remote branch
```
Adds the changes you made on your local branch to the remote copy of the same branch. It is ALWAYS recommended that you **pull** THEN **push** to avoid any "collissions" in the remote repository. After you push to your remote working-branch, you can then go to the repository on github to then create a pull request so your changes can be added to the main branch.

Prior to pushing your changes, you have to make a "commit". Commits are essentially just a list of changes that you have made with an attached message with them to summarize what you did. When you push your "changes" you really are pushing these commits.
- `$ git add -A` - "stages" all your edits/changes to your next commit
- `$ git commit -m "<message>"` combines all your "staged" changes into a commit with the attached message
- `$ git push --all` pushes all your local commits to the remote copy of the same branch