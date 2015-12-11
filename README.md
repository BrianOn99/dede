dede, a tool for file deduplication
===================================

This is an assignment of cs course, but it works quite good, I think.
If you upload 2 files with similar content, a lot of space can be saved
(deduplicated).

## Usage
```
java program_name upload <min_chunk> <avg_chunk> <max_chunk> <prime d> <local_file_name> <local|remote>
java program_name download <remote_file_name>
java program_name delete <remote_file_name>
```

## Build
maven is used for project management. Make sure have maven installed.
mapdb (pure java database) is included as dependency.  It will be
downloaded automatically.

compile: `mvn compile`

run junit tests: `mvn test`

make a jar: sorry please add it to pom.xml yourself or jar it directly

There are some unit test, but the coverage is not very good.  Better
than nothing.
