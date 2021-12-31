package cc.aoeiuv020.open;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class IntentUtils {
    private static final String TAG = "IntentUtils";
    private static Uri parseFilePath(Uri data) {
        if (!"content".equalsIgnoreCase(data.getScheme()) || !"com.tencent.mobileqq.fileprovider".equals(data.getHost())) {
            return data;
        }
        try {
            return Uri.fromFile(new File(URLDecoder.decode(data.getPath().substring("/external_files".length()), "utf-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static Intent parseFilePath(Intent intent) {
        Log.d(TAG, "parseFilePath() called with: intent = [" + intentToString(intent) + "]");
        Uri data = intent.getData();
        if (data == null) {
            return intent;
        }
        intent.setData(parseFilePath(data));
        return intent;
    }

    public static String intentToString(Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("intent = ");
        if (intent == null) {
            sb.append("null").append('\n');
        } else {
            sb.append(intent.toString()).append('\n');
            sb.append("action = ").append(intent.getAction()).append('\n');
            sb.append("data = ").append(intent.getDataString()).append('\n');
            sb.append("extra = ");
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                sb.append("null").append('\n');
            } else {
                Map<String, Object> map = new HashMap<>();
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    map.put(key, value);
                }
                sb.append(map.toString());
            }
        }
        return sb.toString();
    }

}
