from django import forms


class UploadForm(forms.Form):
    img = forms.FileField()


class RequestForm(forms.Form):
    name = forms.CharField()
