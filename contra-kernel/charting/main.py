from sqlalchemy import create_engine
import streamlit as st
import pandas as pd
import psycopg2 as p2
import plotly.express as px
import plotly.figure_factory as ff

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
    (lower(duration)) AS "Start",
    (upper(duration)) AS "Finish",
    (SELECT value FROM intervalsv2 WHERE attribute = \'Threads/\' || intervals.value || \'/Exec_name\' LIMIT 1) AS "CPU Task"
FROM
    intervalsv2 intervals -- <table name> <db name>
WHERE
    attribute = \'CPUs/1/Current_thread\'
LIMIT 50""", conn)

df = pd.DataFrame(SQL_Query)
df.dropna(subset=['CPU Task'], inplace=True)
df['Task'] = '1'
print(df)


# NO: https://stackoverflow.com/questions/73247210/how-to-plot-a-gantt-chart-using-timesteps-and-not-dates-using-plotly
# fig = px.timeline(df, x_start="Start", x_end="End", y="CPU 1")
# fig.update_layout(xaxis_type='linear', autosize=False, width=800, height=400)
# st.plotly_chart(fig, use_container_width=True)

# The TC Kernel Resource View, using the deprecated create_gantt instead of plotly express timeline:
fig = ff.create_gantt(df, index_col = 'CPU Task',  bar_width = 0.4, show_colorbar=True, group_tasks=True)
fig.update_layout(xaxis_type='linear', autosize=False, width=800, height=400)
st.plotly_chart(fig, use_container_width=True)

# Something similar to TC Kernel Control Flow View, using plotly express timeline
df['delta'] = df['Finish'] - df['Start']
fig = px.timeline(df, x_start="Start", x_end="Finish", y="CPU Task")
fig.layout.xaxis.type = 'linear'
fig.data[0].x = df.delta.tolist()
fig = fig.full_figure_for_development(warn=False)
st.plotly_chart(fig, use_container_width=True)

# The TC Kernel Resource View, using plotly express timeline (not fully working due to issue in px)
df['delta'] = df['Finish'] - df['Start']
# The line below should work, but some issues are present in px.timeline.
# See: https://stackoverflow.com/questions/68500434/bars-of-plotly-timeline-disappear-when-adding-color
# fig = px.timeline(df, x_start="Start", x_end="Finish", y="Task", color="CPU Task")
fig = px.timeline(df, x_start="Start", x_end="Finish", y="Task")
fig.update_yaxes(autorange="reversed")
fig.layout.xaxis.type = 'linear'
fig.data[0].x = df.delta.tolist()
fig = fig.full_figure_for_development(warn=False)
st.plotly_chart(fig, use_container_width=True)
