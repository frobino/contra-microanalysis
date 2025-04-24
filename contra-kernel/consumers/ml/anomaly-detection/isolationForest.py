import psycopg2
import pandas as pd
from sklearn.ensemble import IsolationForest
import matplotlib.pyplot as plt

# Connect to PostgreSQL
try:
    conn = psycopg2.connect(
        dbname="intervals",
        user="postgres",
        password="postgrespw",
        host="172.17.0.2",
        port="5432"
    )
except psycopg2.Error as e:
    print(f"Error connecting to database: {e}")
    exit(1)

# Query to fetch thread execution times
query = """
SELECT attribute, (upper(duration) - lower(duration)) AS execution_time
FROM intervalsv2
WHERE value = '1' AND attribute LIKE 'Threads/____';
"""

# Load data into a DataFrame
try:
    df = pd.read_sql(query, conn)
except Exception as e:
    print(f"Error executing query: {e}")
    conn.close()
    exit(1)

# Close the database connection
conn.close()

# Fit Isolation Forest to detect anomalies
clf = IsolationForest(contamination=0.05, random_state=42)
df['anomaly'] = clf.fit_predict(df[['execution_time']])

# Identify anomalies
anomalies = df[df['anomaly'] == -1]

# Display anomalies
print("Detected Anomalies:")
print(anomalies)

# Plot execution times with anomalies highlighted
plt.figure(figsize=(12, 6))

# Plot all data points
plt.scatter(df.index, df['execution_time'], label='Normal', color='blue', s=10)

# Highlight anomalies
plt.scatter(anomalies.index, anomalies['execution_time'], label='Anomalies', color='red', s=10)

# Customize plot
plt.title('Execution Times with Anomalies Highlighted')
plt.xlabel('Index')
plt.ylabel('Execution Time')
plt.legend()

# Show plot
plt.savefig('anomalies_plot.png')
print("Plot saved as 'anomalies_plot.png'.")