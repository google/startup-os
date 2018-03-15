import json
from string import Template

google_services_json = json.load(open("android/google-services.json"))

values = {
    'appid':
    google_services_json["client"][0]["client_info"]["mobilesdk_app_id"],
    'projectid': google_services_json["project_info"]["project_id"],
    'apikey': google_services_json["client"][0]["api_key"][0]["current_key"],
    'dburl': google_services_json["project_info"]["firebase_url"],
    'bucket': google_services_json["project_info"]["storage_bucket"]
}

firestoreConfigTemplate = Template("""
package com.google.bazel.example.android;

import com.google.firebase.FirebaseOptions;


public final class FirestoreConfig {
    public static FirebaseOptions getFirebaseConfig() {
    	return new FirebaseOptions.Builder()
    		.setApplicationId("$appid")
    		.setProjectId("$projectid")
    		.setApiKey("$apikey")
    		.setDatabaseUrl("$dburl")
    		.setStorageBucket("$bucket").build();
    }
}
""")
print(firestoreConfigTemplate.substitute(values))
