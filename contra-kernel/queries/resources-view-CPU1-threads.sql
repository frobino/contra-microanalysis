-- NOTE: the query below is "equivalent" to the following Trace Compass logc:
-- ResourcesStatusDataProvider
-- Integer currentThreadQuark = ss.optQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
-- see
--   - getTree
--   - getRowModel
SELECT
    (lower(duration)) AS "Start time",
    (upper(duration)) AS "End time",
    (SELECT value FROM intervalsv2 WHERE attribute = 'Threads/' || O.value || '/Exec_name' LIMIT 1) AS "Thread"
    -- If we would use "WHERE value AS CPU1" here, we would get the thread id, which is not what we want. We want the thread name.
    -- To be able to resolve the thread id into a thread name, we need a "nested query / subquery", i.e. re-query the table.
    -- An "inner query" is done using the (SELECT value FROM ...) as above.
    -- The "inner query" gets as "input" what the "outermost query" returns (i.e. values in form of thread ids, see O.value),
    -- and uses it to make a follow up query that uses the value.
    -- In this case it select and return 1 value from a row where the attribute is something like 'Threads/5387/Exec_name'
    -- The value returned is the 1st thread name that matches the thread id 5387.
    -- WARNING: this inner query returns the 1st 1st thread name that matches the thread id 5387, so if the thread id is reused in time,
    -- it could return the wrong name. It is probably needed to add some "time range" in the inner query, so that it picks the correct name
    -- for the specific thread id at a specific time.
    --
    -- See also correlated subquery vs basic subquery (https://www.red-gate.com/simple-talk/databases/sql-server/t-sql-programming-sql-server/using-a-subquery-in-a-select-statement/)
    -- "the correlate subquery references columns from the outer query"
    -- "The correlated subquery is run once for every row returned from the outer query"
    -- "When a subquery or correlated subquery is used in the column list the embedded code can only bring back a single column and a single column value. If you try to return multiple columns, or multiple values an error will occur."
FROM
    intervalsv2 O -- <table name> <table name alias - to be used in subquery>
    -- see: https://www.w3resource.com/sql/subqueries/understanding-sql-subqueries.php
WHERE
    attribute = 'CPUs/1/Current_thread'
    -- consider only rows where the column "attribute" matches "CPUs/1/Current_thread"
    -- Note that for these columns, the value will be a thread id, e.g. 5387
LIMIT 50
