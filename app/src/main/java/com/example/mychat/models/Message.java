/**
 * A class describing the message model
 * @autor Danilova Elizaveta
 * @version 2.0
 */
package com.example.mychat.models;

public class Message {
    /** Message text field */
    private String text;
    /** Sender name field */
    private String name;
    /** Image URL field */
    private String imageUrl;
    /** Sender id field */
    private String sender;
    /** Recipient id field  */
    private String recipient;
    /** Message sent time field */
    private String time;
    /** Field check whether the message was read by the recipient or not */
    private boolean isSeen;

    /**Default constructor without parameters - creating a new object
     * @see #Message(String, String, String, String, String, String, boolean) */
    public Message(){}

    /** Constructor for creating an object with given values
     * @param text - text in message
     * @param name - sender name
     * @param imageUrl - image in message
     * @param sender - sender id
     * @param recipient - recipient id
     * @param time - message sent time
     * @param isSeen - message was read or not
     * @see #Message() */
    public Message(String text, String name, String imageUrl, String sender, String recipient, String time, boolean isSeen) {
        this.text = text;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sender = sender;
        this.recipient = recipient;
        this.time = time;
        this.isSeen = isSeen;
    }
    /**
     * Function to get the value of {@link #isSeen}
     * @return message was read(true) or not(false)
     */
    public boolean isSeen() {
        return isSeen;
    }
    /**
     * Function to define the value of {@link #isSeen}
     * @param seen - message was read(true) or not(false)
     */
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
    /**
     * Function to get the value of {@link #name}
     * @return sender name
     */
    public String getName() {
        return name;
    }
    /**
     * Function to get the value of {@link #imageUrl}
     * @return image URL in message
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * Function to get the value of {@link #text}
     * @return text in message
     */
    public String getText() {
        return text;
    }
    /**
     * Function to define the value of {@link #text}
     * @param text - text in message
     */
    public void setText(String text) {
        this.text = text;
    }
    /**
     * Function to define the value of {@link #name}
     * @param name - sender name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Function to define the value of {@link #imageUrl}
     * @param imageUrl - image URL in message
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    /**
     * Function to get the value of {@link #sender}
     * @return sender id
     */
    public String getSender() {
        return sender;
    }
    /**
     * Function to define the value of {@link #sender}
     * @param sender - sender id
     */
    public void setSender(String sender) {
        this.sender = sender;
    }
    /**
     * Function to get the value of {@link #recipient}
     * @return recipient id
     */
    public String getRecipient() {
        return recipient;
    }
    /**
     * Function to define the value of {@link #recipient}
     * @param recipient - recipient id
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    /**
     * Function to get the value of {@link #time}
     * @return message sent time
     */
    public String getTime() {
        return time;
    }
    /**
     * Function to define the value of {@link #time}
     * @param time - message sent time
     */
    public void setTime(String time) {
        this.time = time;
    }
}
