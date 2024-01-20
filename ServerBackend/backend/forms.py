from django import forms


class UploadForm(forms.Form):
    img = forms.FileField()
    username = forms.CharField(max_length=20)


class RequestForm(forms.Form):
    filename = forms.CharField()
    username = forms.CharField(max_length=20)


class UserForm(forms.Form):
    username = forms.CharField()
    action = forms.CharField()
