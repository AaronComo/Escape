import datetime
import subprocess
import re

result = subprocess.run('ifconfig | grep broadcast', shell=True, capture_output=True, text=True)
if result.returncode == 0:
    # 查询第一个ip地址
    match = re.search(r'\b(?:\d{1,3}\.){3}\d{1,3}\b', result.stdout)
    if match:
        ip = match.group()
        print(ip)
    else:
        print('没有找到ip')
else:
    print('shell命令执行出错, 报错信息如下:')
    print(result.stderr)
name = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
print(name)



