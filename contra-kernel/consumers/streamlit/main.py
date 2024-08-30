from sqlalchemy import create_engine
from os import environ
import streamlit as st
import pandas as pd
import psycopg2 as p2
import plotly.express as px
import plotly.figure_factory as ff
import random

# Initialize connection.
# conn = st.connection("postgresql", type="sql")
# conn = st.connection(
#     "local_db",
#     type="sql",
#     url="mysql://postgres:postgrespw@localhost:5488/intervals"
# )
# conn = create_engine("mysql://postgres:postgrespw@localhost:5488/intervals")

# Try to get params from envvar
host = environ.get('STREAMLIT_DB_HOST')
if host is None:
    host = "localhost"
port = environ.get('STREAMLIT_DB_PORT')
if port is None:
    port = 5488
user = environ.get('STREAMLIT_DB_USER')
if user is None:
    user = "postgres"
password = environ.get('STREAMLIT_DB_PWD')
if password is None:
    password = "postgrespw"
query_path = environ.get('STREAMLIT_QUERY_PATH')
if query_path is None:
    query_path = "../queries/resources-view-CPU1-threads.sql"

params = {
    # # "host": "localhost",
    # # "port": 5488,
    # "host": "pg_data_wh",
    # "port": 5432,
    # "user": "postgres",
    # "password": "postgrespw"
    "host": host,
    "port": port,
    "user": user,
    "password": password
}
conn = p2.connect(**params, dbname= "intervals")

st.write("My First App")

# Dummy test query + chart using the charting features of st:
#
# SQL_Query = pd.read_sql('SELECT quark,quark FROM intervalsv2', conn)
# df = pd.DataFrame(SQL_Query, columns=['quark','quark'])
# st.line_chart(df)

# Below, an example of how to load a query "in the code" (i.e. not from file)
#
# SQL_Query = pd.read_sql(
# """SELECT
#     (lower(duration)) AS "Start time",
#     (upper(duration)) AS "End time",
#     (SELECT value FROM intervalsv2 WHERE attribute = \'Threads/\' || intervals.value || \'/Exec_name\' LIMIT 1) AS "Thread"
# FROM
#     intervalsv2 intervals -- <table name> <db name>
# WHERE
#     attribute = \'CPUs/1/Current_thread\'
# """, conn)
# # LIMIT 50""", conn)
#
# df = pd.DataFrame(SQL_Query)

# Read the sql file and execute the query (fill the df)
with open(query_path, 'r') as query:
    df = pd.read_sql_query(query.read(),conn)

st.write("Trying to create something similar to the Kernel Control flow view")

# Massage the dataframe to simplify creation of charts
#
# Remove 'None' intervals (a.k.a. null intervals/segments)
df.dropna(subset=['Thread'], inplace=True)
# NOTE: adding the 'delta' column to enable px.timeline to handle properly int/float x-axis instead of date
# See:
# https://community.plotly.com/t/gantt-chart-integer-float-x-axis-labels-instead-of-date/37233/2
# https://stackoverflow.com/questions/66078893/plotly-express-timeline-for-gantt-chart-with-integer-xaxis
df['delta'] = df['End time'] - df['Start time']

# Something similar to TC Kernel Control Flow View, using plotly express timeline
fig = px.timeline(df, x_start="Start time", x_end="End time", y="Thread", title="Control flow view")
fig.layout.xaxis.type = 'linear'
fig.data[0].x = df.delta.tolist()
fig = fig.full_figure_for_development(warn=False)
st.plotly_chart(fig, use_container_width=True)

st.write("Trying to create something similar to the Kernel Resources view")

# Massage the dataframe to simplify creation of charts
#
df['CPU nr'] = '1'

# The TC Kernel Resource View, using plotly express timeline (not fully working due to issue in px)
# The line below should work, but some issues are present in px.timeline.
# See: https://stackoverflow.com/questions/68500434/bars-of-plotly-timeline-disappear-when-adding-color
# fig = px.timeline(df, x_start="Start", x_end="Finish", y="Task", color="CPU Task")
fig = px.timeline(df, x_start="Start time", x_end="End time", y="CPU nr", title="Resources view")
fig.update_yaxes(autorange="reversed")
fig.layout.xaxis.type = 'linear'
fig.data[0].x = df.delta.tolist()
fig = fig.full_figure_for_development(warn=False)
st.plotly_chart(fig, use_container_width=True)

st.write("Trying to create something similar to the Kernel Resources view")

# NOTE:
# in theory we would not need to create the "delta" column above.
# See: https://stackoverflow.com/questions/73247210/how-to-plot-a-gantt-chart-using-timesteps-and-not-dates-using-plotly
#
# fig = px.timeline(df, x_start="Start", x_end="End", y="CPU 1")
# fig.update_layout(xaxis_type='linear', autosize=False, width=800, height=400)
# st.plotly_chart(fig, use_container_width=True)


# Massage the dataframe to simplify creation of charts
#
# For ff.create_gantt to work, the columns in the dataframe must include the following keys: Task, Start, Finish
df = df.rename(columns={"CPU nr": "Task", "Start time": "Start", "End time": "Finish"})

# For ff.create_gantt to work with many colors, we need to have a own colors method.
# See https://stackoverflow.com/questions/55965633/how-to-specify-additional-colors-in-plotly-gantt-chart
r = lambda: random.randint(0,255)
colors = ['#%02X%02X%02X' % (r(),r(),r())]
for i in range(1, df.Thread.nunique()+1):
    colors.append('#%02X%02X%02X' % (r(),r(),r()))

# The TC Kernel Resource View, using the deprecated create_gantt instead of plotly express timeline:
fig = ff.create_gantt(df, colors=colors, index_col = 'Thread',  bar_width = 0.4, show_colorbar=True, group_tasks=True, title="Resources view")
fig.update_layout(xaxis_type='linear', autosize=False, width=800, height=400)
st.plotly_chart(fig, use_container_width=True)

st.write("Trying to create something new, a treemap view")

# Massage the dataframe to simplify creation of charts
#
# NOTE: this could be done also using a separate SQL query, not sure what is best.
# FIXME: we do not want to sum all columns, only the "delta"
g = df.groupby('Thread').sum()

# A Treemap (similar to the bottom of a flame chart) to indicate which thread takes more time
fig = px.treemap(
    df,
    path=['Thread'],            # Define the hierarchy
    values='delta',             # The metric to size the areas
    color='delta',              # The color of the blocks
    hover_data={'delta': True}, # Additional data on hover
    title="Treemap Example"
)
st.plotly_chart(fig, use_container_width=True)
