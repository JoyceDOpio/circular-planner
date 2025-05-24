

ACTIVE TIME SETUP

fun validateActiveTime(start: Time, end: Time): Boolean
When start and end values of active time are both equal to  0:00, the active time will be 24 hours (starting from midnight). Otherwise, the end value cannot be smaller than the start value.

Dragging along the task dial should designate the new task's start and end times. On drag end, a task creation/editing screen should open.

Single tap on a task (on the task dial) should open a screen with task details (with options to edit and delete task).
Double tap on a task (on the task dial) should open task editing screen.
Long press on the task dial within the bounds of a created task should activate task boundaries' edition.

fun calculateClockHoursBetween (start: Time, end: Time): Int
    Calculate the number of hour points between the start- and end time, e.g. between 6:20 AM and 11:12 AM there are 5 hour points: 7:00, 8:00, 9:00, 10:00 and 11:00.

