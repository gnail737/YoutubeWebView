package com.gnail737.youtubewebview;

public class NameValuePair {
    String mName;
    String mValue;

    /**
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return mValue;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        mValue = value;
    }

    public NameValuePair(String name, String value) {
        mName = name;
        mValue = value;
    }
}
