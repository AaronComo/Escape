from django.urls import path
from . import views

app_name = 'backend'
urlpatterns = [
  # path("", views.index),
  path("gallery/", views.gallery),
  path("upload/", views.upload, name="upload"),
  path("model/", views.model, name="model"),
  path("test/", views.test, name="test"),
  path("test_error/", views.test_error, name="test_error"),
]
