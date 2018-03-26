/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.startupos.android;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

public class FirestoreGenRuleImpl {

  public static String TEMPLATE_TEXT =
      String.join(
          System.getProperty("line.separator"),
          "package com.google.bazel.example.android;",
          "",
          "import com.google.firebase.FirebaseOptions;",
          "",
          "public final class FirestoreConfig {",
          "   public static FirebaseOptions getFirebaseConfig() {",
          "       return new FirebaseOptions.Builder()",
          "           .setApplicationId(\"$appid\")",
          "           .setProjectId(\"$projectid\")",
          "           .setApiKey(\"$apikey\")",
          "           .setDatabaseUrl(\"$dburl\")",
          "           .setStorageBucket(\"$bucket\").build();",
          "       }",
          "   }",
          "}");

  public static void main(String[] args) throws IOException, SecurityException {
    JSONObject obj =
        new JSONObject(
            String.join(
                System.getProperty("line.separator"),
                Files.readAllLines(Paths.get("./android/google-services.json"))));

    JSONObject client = obj.getJSONArray("client").getJSONObject(0);
    JSONObject projectInfo = obj.getJSONObject("project_info");

    System.out.println(
        TEMPLATE_TEXT
            .replace("$appid", client.getJSONObject("client_info").getString("mobilesdk_app_id"))
            .replace("$projectid", projectInfo.getString("project_id"))
            .replace(
                "$apikey", client.getJSONArray("api_key").getJSONObject(0).getString("current_key"))
            .replace("$dburl", projectInfo.getString("firebase_url"))
            .replace("$bucket", projectInfo.getString("storage_bucket")));
  }
}
