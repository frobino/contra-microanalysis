from sqlalchemy import create_engine
import streamlit as st
import pandas as pd
import psycopg2 as p2

# Initialize connection.
# conn = st.connection("postgresql", type="sql")
# conn = st.connection(
#     "local_db",
#     type="sql",
#     url="mysql://postgres:postgrespw@localhost:5488/intervals"
# )
# conn = create_engine("mysql://postgres:postgrespw@localhost:5488/intervals")

params = {
    "host": "localhost",
    "user": "postgres",
    "port": 5488,
    "password": "postgrespw" 
}
conn = p2.connect(**params, dbname= "intervals")

# df = conn.query('SELECT * FROM intervalsv2 LIMIT 50;', ttl="10m")

SQL_Query = pd.read_sql('SELECT quark,quark FROM intervalsv2', conn)

st.write("My First App")
df = pd.DataFrame(SQL_Query, columns=['quark','quark'])
st.line_chart(df)

SQL_Query = pd.read_sql(
"""SELECT
    lower(duration) AS "Start time",
    upper(duration) AS "End time",
    (SELECT value FROM intervalsv2 WHERE attribute = \'Threads/\' || intervals.value || \'/Exec_name\' LIMIT 1) AS "CPU 1"
FROM
    intervalsv2 intervals -- <table name> <db name>
WHERE
    attribute = \'CPUs/1/Current_thread\'
LIMIT 50""", conn)

df = pd.DataFrame(SQL_Query)
print(df)
