SELECT
    to_timestamp(lower(duration) AS "Start time",
    to_timestamp(upper(duration)) AS "End time",
    (SELECT value FROM intervalsv2 WHERE attribute = 'Threads/' || intervals.value || '/Exec_name' LIMIT 1) AS "CPU 1"
FROM
    intervalsv2 intervals -- <table name> <db name>
WHERE
    attribute = 'CPUs/1/Current_thread'
LIMIT 50
