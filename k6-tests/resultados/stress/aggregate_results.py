import csv
import sys
from collections import defaultdict

raw_file = 'resultados/stress/stress_raw.csv'
out_file = 'resultados/stress/stress_final_chart.csv'

# Aggregates by second: key = second (int)
# value = { 'vus': 0, 'total_reqs': 0, 'errors': 0 }
data_by_second = defaultdict(lambda: {'vus': 0, 'total_reqs': 0, 'errors': 0})

try:
    with open(raw_file, mode='r') as f:
        # Check if the file is empty or missing headers
        first_line = f.readline()
        if not first_line:
            print("Error: stress_raw.csv is empty.")
            sys.exit(1)
except FileNotFoundError:
    print(f"Error: {raw_file} not found. Make sure to run the k6 test first.")
    sys.exit(1)

min_timestamp = None

with open(raw_file, mode='r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        if not row.get('timestamp'):
            continue
        ts = float(row['timestamp'])
        if min_timestamp is None or ts < min_timestamp:
            min_timestamp = ts

with open(raw_file, mode='r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        if not row.get('timestamp') or not row.get('metric_name'):
            continue
        ts = float(row['timestamp'])
        sec = int(ts - min_timestamp)
        metric = row['metric_name']
        val = float(row['metric_value'])
        
        if metric == 'vus':
            data_by_second[sec]['vus'] = max(data_by_second[sec]['vus'], int(val))
        elif metric == 'erros_5xx':
            data_by_second[sec]['total_reqs'] += 1
            if val > 0:
                data_by_second[sec]['errors'] += 1

# Write to the final aggregated CSV
with open(out_file, mode='w', newline='') as f:
    f.write('Segundo;VUs;Taxa_Erro_Percentual\n')
    
    last_vus = 0
    # Gather all seconds from 0 to the maximum recorded second
    max_sec = max(data_by_second.keys()) if data_by_second else 0
    
    for sec in range(max_sec + 1):
        vus = data_by_second[sec]['vus']
        if vus == 0:
            vus = last_vus
        else:
            last_vus = vus
            
        reqs = data_by_second[sec]['total_reqs']
        errors = data_by_second[sec]['errors']
        err_rate = (errors / reqs * 100) if reqs > 0 else 0.0
        
        # Converte o ponto decimal para vírgula para compatibilidade com o Excel/Sheets em Português
        err_str = str(round(err_rate, 2)).replace('.', ',')
        
        f.write(f"{sec};{vus};{err_str}\n")

print(f"Aggregated CSV successfully generated at: {out_file}")
