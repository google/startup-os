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

package com.google.startupos.common.firestore;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import java.util.List;
import java.util.Map;

/**
 * Utility classes to convert protobuf messages to/from Firestore JSON format. The Firestore JSON
 * format annotates each value with its type (e.g stringValue). This results in a larger JSON but
 * with more type information.
 */
public class FirestoreJsonFormat {

  public static Printer printer() {
    return new Printer();
  }

  public static Parser parser() {
    return new Parser();
  }

  /** A Printer converts protobuf message to Firestore JSON format. */
  public static class Printer {

    private Printer() {}

    /** Converts a protobuf message to Firestore JSON format. */
    public String print(MessageOrBuilder message) throws InvalidProtocolBufferException {
      JsonParser parser = new JsonParser();

      // we need .preservingProtoFieldNames() because
      // by default JsonFormat.Printer converts field identifiers to lowerCamelCase
      String messageAsJson = JsonFormat.printer().preservingProtoFieldNames().print(message);
      JsonObject firestoreJson = new JsonObject();
      firestoreJson.add(
          "fields",
          regularJsonAsFirestoreJson(parser.parse(messageAsJson).getAsJsonObject(), message));
      return firestoreJson.toString();
    }

    private JsonObject regularJsonAsFirestoreJson(JsonObject json, MessageOrBuilder proto) {
      for (Map.Entry<String, JsonElement> item : json.entrySet()) {
        String key = item.getKey();
        JsonElement value = item.getValue();

        FieldDescriptor protoField = proto.getDescriptorForType().findFieldByName(key);
        Object protoFieldValue = proto.getField(protoField);

        if (value.isJsonObject()) {
          json.add(key, wrapJsonObject(value));
          regularJsonAsFirestoreJson(value.getAsJsonObject(), (Message) protoFieldValue);
        } else if (value.isJsonArray()) {
          json.add(
              key, formatJsonArray(protoField, value.getAsJsonArray(), (List) protoFieldValue));
        } else if (value.isJsonPrimitive()) {
          json.add(key, wrapJsonPrimitive(value, protoField.getJavaType()));
        } else {
          throw new UnsupportedOperationException("No support yet for JsonNull");
        }
      }
      return json;
    }

    private JsonElement formatJsonArray(
        FieldDescriptor protoField, JsonArray jsonArray, List protoList) {

      JsonElement arrayWrapper = wrapJsonArray(jsonArray);

      for (int i = 0; i < jsonArray.size(); i++) {
        JsonElement item = jsonArray.get(i);
        if (item.isJsonObject()) {
          regularJsonAsFirestoreJson(item.getAsJsonObject(), (Message) protoList.get(i));
          jsonArray.set(i, wrapJsonObject(item));
        } else if (item.isJsonPrimitive()) {
          jsonArray.set(i, wrapJsonPrimitive(item, protoField.getJavaType()));
        } else if (item.isJsonNull()) {
          throw new UnsupportedOperationException("No support yet for JsonNull");
        } else {
          throw new UnsupportedOperationException(
              "A JsonArray inside a JsonArray should not happen in protos");
        }
      }

      return arrayWrapper;
    }

    private JsonObject wrapJsonPrimitive(JsonElement json, JavaType type) {
      JsonObject wrapper = new JsonObject();

      if (type == JavaType.STRING || type == JavaType.BYTE_STRING) {
        wrapper.addProperty("stringValue", json.getAsString());
      } else if (type == JavaType.ENUM || type == JavaType.INT || type == JavaType.LONG) {
        wrapper.addProperty("integerValue", json.getAsLong());
      } else if (type == JavaType.FLOAT || type == JavaType.DOUBLE) {
        wrapper.addProperty("doubleValue", json.getAsDouble());
      } else if (type == JavaType.BOOLEAN) {
        wrapper.addProperty("booleanValue", json.getAsBoolean());
      } else if (type == JavaType.MESSAGE) {
        throw new UnsupportedOperationException("A message isn't a primitive");
      } else {
        throw new UnsupportedOperationException("Unknown JavaType: " + type);
      }
      return wrapper;
    }

    private JsonObject wrapJsonArray(JsonArray jsonArray) {
      JsonObject externalWrapper = new JsonObject();
      JsonObject internalWrapper = new JsonObject();
      externalWrapper.add("arrayValue", internalWrapper);
      internalWrapper.add("values", jsonArray);
      return externalWrapper;
    }

    private JsonObject wrapJsonObject(JsonElement jsonObject) {
      JsonObject externalWrapper = new JsonObject();
      JsonObject internalWrapper = new JsonObject();
      externalWrapper.add("mapValue", internalWrapper);
      internalWrapper.add("fields", jsonObject);
      return externalWrapper;
    }
  }

  /** A Parser parses Firestore JSON to protobuf message. */
  public static class Parser {

    private static String[] PRIMITIVE_KEYS =
        new String[] {"doubleValue", "integerValue", "booleanValue", "stringValue"};
    private static String MAP_KEY = "mapValue";
    private static String MAP_FIELDS_KEY = "fields";
    private static String ARRAY_KEY = "arrayValue";
    private static String ARRAY_VALUES_KEY = "values";

    private Parser() {}

    /**
     * Parses from Firestore JSON into a protobuf message.
     *
     * @throws InvalidProtocolBufferException if the input is not valid Firestore JSON format or
     *     there are unknown fields in the input.
     */
    public void merge(String json, Message.Builder builder) throws InvalidProtocolBufferException {

      JsonParser parser = new JsonParser();
      JsonObject jsonObject = parser.parse(json).getAsJsonObject().getAsJsonObject(MAP_FIELDS_KEY);
      jsonObject = firestoreJsonToRegularJson(jsonObject);
      JsonFormat.parser().merge(jsonObject.toString(), builder);
    }

    /**
     * From JSON object {@code element} of the form {key: value}, extract value if key is one of
     * PRIMITIVE_KEYS, otherwise return null
     *
     * @param element object to extract value from
     * @return extracted value or null
     */
    private JsonPrimitive pluckPrimitive(JsonElement element) {
      JsonObject object = element.getAsJsonObject();
      for (String key : PRIMITIVE_KEYS) {
        if (object.has(key)) {
          return object.getAsJsonPrimitive(key);
        }
      }
      return null;
    }

    /**
     * Takes {@code json} in predefined known format and flattens it in a way that protobuf's
     * JsonFormat could later make it a protobuf message
     *
     * @param json object in firestore format
     * @return regular json object
     */
    private JsonObject firestoreJsonToRegularJson(JsonObject json) {

      JsonObject result = new JsonObject();
      JsonPrimitive primitive;

      for (Map.Entry<String, JsonElement> item : json.entrySet()) {

        String key = item.getKey();
        JsonObject value = item.getValue().getAsJsonObject();

        if ((primitive = pluckPrimitive(value)) != null) {
          result.add(key, primitive);
        } else if (value.has(MAP_KEY)) {
          result.add(
              key,
              firestoreJsonToRegularJson(
                  value.getAsJsonObject(MAP_KEY).getAsJsonObject(MAP_FIELDS_KEY)));
        } else if (value.has(ARRAY_KEY)) {
          JsonArray array = value.getAsJsonObject(ARRAY_KEY).getAsJsonArray(ARRAY_VALUES_KEY);
          JsonArray newArray = new JsonArray();
          for (JsonElement element : array) {
            if ((primitive = pluckPrimitive(element)) != null) {
              newArray.add(primitive);
            } else {
              JsonObject object = element.getAsJsonObject();
              if (object.has(MAP_KEY)) {
                newArray.add(
                    firestoreJsonToRegularJson(
                        object.getAsJsonObject(MAP_KEY).getAsJsonObject(MAP_FIELDS_KEY)));
              }
            }
          }
          result.add(key, newArray);
        }
      }
      return result;
    }
  }
}
