package com.earlier.yma.utilities;

import io.realm.RealmObject;

public class RealmString extends RealmObject {

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
