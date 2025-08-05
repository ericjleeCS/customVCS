# Gitlet (Java)

A minimal, educational version-control system inspired by Git.  
Implements content-addressed objects, a staging area (index), commits with parent links, and a basic CLI.

## Requirements
- Java 17+
- Gradle 8+

## Build & Test
```bash
# run unit tests
./gradlew clean test

# build classes
./gradlew build
```

## Running
Run commands from the **working directory you want to version** (the directory that will contain `.gitlet/`).

```bash
# Using Gradle's run task (if configured):
./gradlew run --args="init"

# Or run the compiled classes directly:
java -cp build/classes/java/main gitlet.Main <command> [args]
```

## Commands

### `init`
Initialize a new repository in the current directory.

```bash
java -cp build/classes/java/main gitlet.Main init
```
Creates `.gitlet/` with:
```
.gitlet/
  HEAD                  # "ref: refs/heads/master"
  index                 # staging area (TSV lines)
  objects/              # content-addressed objects
  refs/
    heads/
      master            # current branch ref -> latest commit id
```
Prints: `Initialized empty repository in <abs path>`

### `add <path>`
Stage the current content of a file for the next commit.

```bash
java -cp build/classes/java/main gitlet.Main add README.md
```
Notes:
- If the file’s content matches what `HEAD` already tracks, it is **unstaged** (no-op).
- Paths must be inside the repo; `..` escapes are rejected.
- Paths and messages cannot contain tabs/newlines.

### `rm <path>`
Unstage or stage a removal.

```bash
java -cp build/classes/java/main gitlet.Main rm README.md
```
Behavior:
- If `<path>` is staged for addition → **unstages** it.
- Else if `<path>` is tracked by `HEAD` → **stages a removal**.
- Else prints: `No reason to remove the file.`

> By default this does **not** delete the working file (like `git rm --cached`).

### `commit <message>`
Create a new commit from the staged changes.

```bash
java -cp build/classes/java/main gitlet.Main commit "initial commit"
```
Rules:
- Message must be one line (no tabs/newlines).
- If nothing is staged, prints: `No changes added to commit.`

### `status`
Show branches and what’s currently staged.

```bash
java -cp build/classes/java/main gitlet.Main status
```
Output:
- `=== Branches ===` (current branch prefixed with `*`)
- `=== Staged Files ===`
- `=== Removed Files ===`

After a successful `commit`, the index is cleared, so these sections are empty unless you stage new changes.

### `reset <commitId>` *(optional — only if implemented)*
Move the current branch to `<commitId>`, update the working tree to match, and clear the index.

```bash
java -cp build/classes/java/main gitlet.Main reset <full-commit-id>
```
Includes a safety check to avoid overwriting untracked files.

## Example Session
```bash
# Initialize a repo
java -cp build/classes/java/main gitlet.Main init

# Create and stage a file
echo "hello" > hello.txt
java -cp build/classes/java/main gitlet.Main add hello.txt

# Commit
java -cp build/classes/java/main gitlet.Main commit "add hello"

# Modify and commit again
echo "hello world" > hello.txt
java -cp build/classes/java/main gitlet.Main add hello.txt
java -cp build/classes/java/main gitlet.Main commit "update hello"

# Stage a removal (doesn't delete working file)
java -cp build/classes/java/main gitlet.Main rm hello.txt
java -cp build/classes/java/main gitlet.Main status   # shows under Removed Files

# Finalize the removal
java -cp build/classes/java/main gitlet.Main commit "remove hello"
java -cp build/classes/java/main gitlet.Main status   # sections empty (index cleared)
```

## Repository Layout
```
.gitlet/
  HEAD                    # "ref: refs/heads/master"
  index                   # staging area (TSV lines)
  objects/                # content-addressed objects (via FileObjectStore)
  refs/
    heads/
      master              # current branch ref -> latest commit id
```

## Troubleshooting
- **“No reason to remove the file.”** — The file is neither staged for addition nor tracked in `HEAD`.
- **`status` shows empty sections after commit** — Correct; the index is cleared after a successful commit.
- **Start over** — Wipe history and re-init:
  ```bash
  rm -rf .gitlet
  java -cp build/classes/java/main gitlet.Main init
  ```

## Roadmap (future commands)
- `log`, `globalLog` — traverse commit graph
- `checkout` — file, commit+file, branch
- `branch`, `rm-branch`
- `merge` with conflict handling
- Abbrev resolution for short commit IDs

## License
MIT (or your choice)
