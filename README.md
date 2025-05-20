

ACTIVE TIME SETUP

fun validateActiveTime(start: Time, end: Time): Boolean
When start and end values of active time are both equal to  0:00, the active time will be 24 hours (starting from midnight). Otherwise, the end value cannot be smaller than the start value.

A long-press on the task dial should initiate creation of a new task. A long-press followed by dragging along the task dial should designate the new task's start and end times.


fun calculateClockHoursBetween (start: Time, end: Time): Int
    Calculate the number of hour points between the start- and end time, e.g. between 6:20 AM and 11:12 AM there are 5 hour points: 7:00, 8:00, 9:00, 10:00 and 11:00.

