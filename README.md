## Introduction
Mysql Tracker can automatically generate mysql code which can create some triggers on target database. With these triggers, database would check every **update&delete** action. If condition meets, it would record these changes to history table.
## When track
* Every Delete Action can be recorded. It would push all the deleted rows to history table.
* Update Action would be recorded Only in stituation where at least one column changes from **non-empty** to any status.

## Addition
It can filter some columns(Changes will be ignored)
## Notice
Each table should contain an **'id'** column which sets as a primary key.
