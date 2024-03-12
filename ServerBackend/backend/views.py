import re
import datetime
import subprocess
import time

from django.shortcuts import render, redirect
from django.http import HttpResponse, HttpResponseForbidden
import json
import os
from backend.forms import UploadForm, RequestForm, UserForm

# is_server=False将使用get_inet_address动态获取本机ip, 并设置端口8000
is_server = False
ip = '119.3.185.140'
port = '8001'


def get_inet_address():
    """只在本地测试时有用"""
    global ip, port

    port = '8000'
    result = subprocess.run('ifconfig | grep broadcast', shell=True, capture_output=True, text=True)
    if result.returncode == 0:
        # 匹配第一个ip地址
        match = re.search(r'\b(?:\d{1,3}\.){3}\d{1,3}\b', result.stdout)
        if match:
            ip = match.group()
            # print(ip)
        else:
            print('没有找到ip')
    else:
        print('shell命令执行出错, 报错信息如下:')
        print(result.stderr)
    return ip, port


def upload(request):
    if request.method == 'POST':
        global ip, port
        if not is_server:
            ip, port = get_inet_address()

        # 处理上传
        af = UploadForm(request.POST, request.FILES)
        if af.is_valid():
            img = af.cleaned_data['img']
            username = af.cleaned_data['username']
            # name = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
            filename = f"{username}_{img.name}"

            print(f'Handling upload image: {filename}')

            # 保存图像并重命名为 用户名-文件名
            handle_uploaded_file(img, filename)
            response = {
                "filename": filename,
                "url": f'http://{ip}:{port}/static/upload/{filename}'
            }
            return HttpResponse(json.dumps(response))
        return HttpResponseForbidden('请求无效, 检查请求中的图像是否正确被加载')
    return HttpResponseForbidden('请使用POST请求.\n'
                                 '请求格式: Form类型, 包含一张png格式的图片.\n'
                                 '返回json格式: {"name": 已上传到服务器的图片名称, "url": 指向图像的链接}')


def handle_uploaded_file(file, filename):
    with open(f"./static/upload/{filename}", "wb+") as destination:
        for chunk in file.chunks():
            destination.write(chunk)


def test_error(request):
    if request.method == 'POST':
        response = {
            "status": "error",
            "method": request.method,
        }
        return HttpResponseForbidden(json.dumps(response))
    note = '使用POST方法请求此地址'
    return HttpResponseForbidden(note)


def model(request):
    """"
    运行模型, request需要包含名称字段
    """
    if request.method == 'POST':
        af = RequestForm(request.POST)
        if af.is_valid():
            filename = af.cleaned_data['filename']
            username = af.cleaned_data['username']
            action = af.cleaned_data['action']
            style = af.cleaned_data['style']

            # 更新用户数据
            data = json_read()
            if not data or not data.get(username):
                create_new_user(username, data)
            data[username]["available_time"] -= 1
            json_write(data)

            url, hit = None, False
            if action == "0":  # 修复
                # 调用模型执行, 把结果保存在static/generate/{filename}
                img_path = f'static/upload/{filename}'
                url, hit = get_return_url(filename)
            else:  # 合成
                url = f'http://{ip}:{port}/static/synthesis/{filename[:-4]}_style_{style}.jpg'
            time.sleep(2)
            pass

            return HttpResponse(url)
        return HttpResponseForbidden('生成失败')
    return HttpResponseForbidden('请使用POST请求')


def get_return_url(filename):
    global ip, port
    if not is_server:
        ip, port = get_inet_address()
    cache = set(os.listdir("./static/generate"))
    cache_name = filename[filename.find("_") + 1:]  # 去掉用户名
    if cache_name in cache:
        url = f"http://{ip}:{port}/static/generate/{cache_name}"
        hit = True
    else:
        url = f"http://{ip}:{port}/static/return1.png"
        hit = False
    return url, hit


def wrap_all_file(path):
    r"""
    列出给定路径下的所有文件, 并将其转为链接
    """
    global ip, port
    if not is_server:
        ip, port = get_inet_address()
    files = os.listdir(path)
    prefixed_dict = {name: f'http://{ip}:{port}/{path}/{name}' for name in files if not name.endswith('.DS_Store')}
    return prefixed_dict


def gallery(request):
    r"""
    返回所有图库图片
    """
    if request.method == 'POST':
        normal_list = wrap_all_file('static/gallery/normal')
        vip_list = wrap_all_file('static/gallery/vip')
        response = {
            "normal": {
                "size": len(normal_list),
                "images": normal_list,
            },
            "vip": {
                "size": len(vip_list),
                "images": vip_list,
            }
        }
        return HttpResponse(json.dumps(response))
    return HttpResponseForbidden('请使用POST请求.')


def get_vip_info(request):
    if request.method == 'POST':
        af = UserForm(request.POST)
        if af.is_valid():
            username = af.cleaned_data["username"]
            action = af.cleaned_data["action"]
            data = json_read()
            if not data.get(username):
                create_new_user(username, data)
                json_write(data)
            if action.find("update") != -1:  # 更新VIP
                idx = action.find("_")
                action = action[idx + 1:]
                data[username][action] += 1
                if action == "vip_ttl":
                    data[username]["available_time"] += 100
                json_write(data)
            return HttpResponse(json.dumps(data[username]))
        return HttpResponseForbidden()


def create_new_user(username, data):
    data.update({
        username: {
            "vip_ttl": 0,
            "uploaded": 0,
            "available_time": 10,
        }
    })


def json_read() -> dict:
    """读取json数据"""
    data = dict()
    with open("static/user_data.json", "r") as f:
        data = json.load(f)
    return data


def json_write(data):
    """写入json数据"""
    with open("static/user_data.json", "w") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)
