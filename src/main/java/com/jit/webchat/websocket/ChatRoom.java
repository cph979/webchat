package com.jit.webchat.websocket;

import com.jit.webchat.bean.ChatMsg;
import com.jit.webchat.bean.ChatUser;
import com.jit.webchat.securityutils.RSAUtils;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 负责维护所有的客户端连接
 * 
 * 每一个客户端对应一个 ChatEndpointPoint
 *
 */
public class ChatRoom {
	// 创建全局实例  ChatRoom.i 
	public static ChatRoom i = new ChatRoom();
	
	// 客户端的列表
	private List<ChatEndpoint> clientList = new LinkedList<>();
		
	private List<ChatEndpoint> clientListCopy = new LinkedList<>();

	// 最近的30条消息
	private List<ChatMsg> msgList = new LinkedList<>();
	
	// 当有客户端连接时，将此客户端添加到 clientList
	public void addClient(ChatEndpoint client) {
		synchronized (clientList) {
			clientList.add(client);

			// 生成一份新的拷贝
			clientListCopy = new LinkedList<>(clientList);
		}
	}
	
	// 当有客户端断开时，将此客户端从 clientList 移除
	public void removeClient(ChatEndpoint client) {
		synchronized (clientList) {
			clientList.remove(client);

			// 生成一份新的拷贝
			clientListCopy = new LinkedList<>(clientList);
		}
	}
	
	// 发给所有人
	public void sendAll(ChatMsg msg) {
		// 缓存最近30条消息
		if ("sendAll".equals(msg.type)) {
			synchronized (msgList) {
				msgList.add(msg);
				if (msgList.size() > 30) {
					msgList.remove(0);
				}
			}
		}

		// 此处对并发问题进行了优化
		Iterator iter = clientListCopy.iterator();
		while (iter.hasNext()) {
			ChatEndpoint client = (ChatEndpoint) iter.next();
			client.sendMessage(msg);
		}
	}

	// 取得最近30条消息列表
	public List<ChatMsg> getMsgList() {
		synchronized (msgList) {
			// 创建一个拷贝
			return new LinkedList<>(msgList);
		}
	}

	// 取得用户列表
	public List<ChatUser> getUserList() {
		List<ChatUser> result = new ArrayList<>();

		Iterator iter = clientListCopy.iterator();
		while (iter.hasNext()) {
			ChatEndpoint client = (ChatEndpoint) iter.next();

			ChatUser u = new ChatUser();
			u.id = client.id;
			u.name = client.user;
			result.add(u);
		}
		return result;
	}

	// 通过httpsessionId拿到websocketSession
	public Session getSession(ChatMsg msg) {
		String toId = msg.to;
		Session selectedSession = null;

		Iterator iter = clientListCopy.iterator();
		while (iter.hasNext()) {
			ChatEndpoint client = (ChatEndpoint) iter.next();
			if (client.id.equals(toId)) {
				selectedSession = client.session;
			}
		}
		return selectedSession;
	}
}
