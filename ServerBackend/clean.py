import os

path = './static/upload'
print(os.system(f'rm -f {path}/*'))
