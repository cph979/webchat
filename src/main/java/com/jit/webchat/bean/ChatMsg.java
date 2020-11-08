package com.jit.webchat.bean;

import lombok.Data;

@Data
public class ChatMsg {
	public String type;
	public String from;
	public String to;
	public Object content;
	public String time;
	public String signature;
}
