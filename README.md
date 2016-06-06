## Introduction
Mysql Tracker can automatically generate mysql code which can create some triggers on target database. With these triggers, database will check every **update&delete** action. If condition meets, it will record these changes to history table.
## When will record
* Every Delete Action will be recorded. It will push all the deleted rows to history table.
* Update Action will be recorded Only in stituation where at least one column changes from **non-empty** to any status.

## Addition
It can filter some columns(Changes will be ignored)
## Notice
Each table should contain an **'id'** column which sets as a primary key.
