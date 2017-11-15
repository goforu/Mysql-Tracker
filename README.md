## Introduction
Mysql Tracker can automatically generate mysql code which can create some triggers on target database. With these triggers, database would check every **update&delete** action. If condition meets, it would capture these changes and put them into history table.
## When track
* Every Delete Action can be captured. It would push all the deleted rows to history table.
* Update Action would be captured Only in stituation where at least one column changes from **non-empty** to any status.

## Addition
You can also filter some columns(Changes can be ignored)
## Notice
Each table should contain an **'id'** column which sets as a primary key.
