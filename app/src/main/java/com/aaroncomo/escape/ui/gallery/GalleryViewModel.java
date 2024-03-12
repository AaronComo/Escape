package com.aaroncomo.escape.ui.gallery;


import static com.aaroncomo.escape.utils.HttpUtils.ip;
import static com.aaroncomo.escape.utils.HttpUtils.port;

import android.os.Handler;
import android.os.Message;

import androidx.lifecycle.ViewModel;

import com.aaroncomo.escape.utils.HttpUtils;
import com.aaroncomo.escape.ui.card.CardItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Response;


public class GalleryViewModel extends ViewModel {

    private static final int NORMAL = 0, VIP = 1;

    /**
     * @param allData 服务器返回的JSON数据
     * @param type    "normal": 普通   "vip": VIP
     * @return 解析出的所有url
     */
    public List<CardItem> getCardData(JSONObject allData, String type) {
        List<CardItem> cards = new ArrayList<>();
        try {
            JSONObject data = allData.getJSONObject(type).getJSONObject("images");
            int size = allData.getJSONObject(type).getInt("size");
            Iterator<String> iter = data.keys();
            while (iter.hasNext()) {
                cards.add(new CardItem(data.getString(iter.next()), "no hint"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cards;
    }

    public void getDataFromServer(Handler handler) throws Exception {
        new Thread(() -> {
            try {
                Response response = HttpUtils.POST("http://" + ip + ":" + port + "/backend/gallery/",
                        new FormBody.Builder().build());
                Message msg = new Message();
                msg.what = 0x10;
                msg.obj = new JSONObject(response.body().string());
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}