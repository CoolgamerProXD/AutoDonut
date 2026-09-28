package com.google.gson;
public abstract class JsonElement {
  public JsonObject getAsJsonObject() { return null; }
  public JsonArray getAsJsonArray() { return null; }
  public String getAsString() { return null; }
  public int getAsInt() { return 0; }
  public long getAsLong() { return 0L; }
  public double getAsDouble() { return 0; }
  public boolean getAsBoolean() { return false; }
  public boolean isJsonNull() { return false; }
  public boolean isJsonObject() { return false; }
  public boolean isJsonArray() { return false; }
}
