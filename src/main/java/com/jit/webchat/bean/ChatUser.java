package com.jit.webchat.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatUser {
	public String id; // HttpSession ID
	public String name; // User Name
}
