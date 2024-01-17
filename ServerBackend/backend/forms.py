from django import forms


class UploadForm(forms.Form):
    img = forms.FileField()
    username = forms.CharField(max_length=20)


class RequestForm(forms.Form):
    name = forms.CharField()


class UserForm(forms.Form):
    username = forms.CharField()
