from django import forms


class UploadForm(forms.Form):
    img = forms.FileField()
    username = forms.CharField(max_length=20)


class RequestForm(forms.Form):
    """修复/合成模型的请求参数"""
    filename = forms.CharField()
    username = forms.CharField(max_length=20)
    action = forms.CharField(max_length=1)  # 0修复,1合成
    style = forms.CharField(max_length=2)   # 迁移风格


class UserForm(forms.Form):
    username = forms.CharField()
    action = forms.CharField()  # 查询或更新
