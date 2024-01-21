# 后端

## 接口

1. 相册

    1. /backend/gallery

    2. 返回图库所有图片

    3. 请求参数

        ~~~java
        RequestBody requestBody = new FormBody.Builder().build();
        ~~~

    4. 返回

        ~~~python
        {
            "normal": {
                "size": 普通会员图片总数,
                "images": 图片url集合,
            },
            "vip": {
                "size": 会员图片总数,
                "images": 图片url集合,
            }
        }
        ~~~

2. 上传

    1. /backend/upload

    2. 将用户上传的图片保存在本地并返回文件名和网络地址

    3. 请求参数

        ~~~python
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("img", name, RequestBody.create(file, MediaType.get("image/png")))
            .build();
        ~~~

    4. 返回

        ~~~python
        {
            "filename": 后端保存的文件名,
            "url": 图片url
        }
        ~~~

3. 启动模型

    1. /backend/model

    2. 调用修复模型修复给定图片

    3. 请求参数

        ~~~java
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("filename", filename) // 上传接口返回的信息
            .addFormDataPart("username", username)
            .build();
        ~~~

    4. 返回

        ~~~python
        修复图像的url
        ~~~

4. 会员信息

    1. /backend/vip

    2. 获取 / 更新用户信息

    3. 获取用户信息

        ~~~java
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("action", "get")
            .build();
        ~~~

    4. 更新上传图像数

        ~~~java
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("action", "update_uploaded")
            .build();
        ~~~

    5. 更新 VIP 时间

        ~~~java
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("action", "update_vip_ttl")
            .build();
        ~~~

    6. 返回

        ~~~python
        {
            "<username>": {
                "vip_ttl": VIP剩余时间,
                "uploaded": 已经上传图像数,
                "available_time": 剩余修复次数
            }
        }
        ~~~

        