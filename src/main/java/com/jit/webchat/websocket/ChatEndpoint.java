package com.jit.webchat.websocket;

import com.alibaba.fastjson.JSON;
import com.jit.webchat.bean.ChatMsg;
import com.jit.webchat.bean.ChatUser;
import com.jit.webchat.securityutils.RSAUtils;
import com.jit.webchat.securityutils.SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
// 下面这个注解用来标记一个类是 WebSocket 的处理器
@ServerEndpoint(value = "/chat", configurator = ChatEndpointConfig.class)
public class ChatEndpoint {
    static Logger log = LoggerFactory.getLogger(ChatEndpoint.class);

    Session session; // 一个WebSocket连接,不是 HttpSession
    String user = "游客"; // 当前用户身份，考虑从 HttpSession中获取
    String id = "";

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        ChatRoom.i.addClient(this);

        // 从 EndpointConfig 获取当前 HttpSession对象
        HttpSession httpSession = (HttpSession)config.getUserProperties().get("httpSession");
        if(httpSession != null) {
            // 取得当前用户昵称
            String str = (String) httpSession.getAttribute("username");
            if (str != null) {
                this.user = str;
            }
            this.id = httpSession.getId();
        }

        // 刚进入聊天室时，显示最近30条缓存记录
        List<ChatMsg> msgList = ChatRoom.i.getMsgList();
        for(ChatMsg msg : msgList) {
            this.sendMessage(msg);
        }

        // 发送聊天室内的用户列表
        ChatMsg msg2 = new ChatMsg();
        msg2.type = "userList";
        msg2.content = ChatRoom.i.getUserList();
        this.sendMessage(msg2);

        // 发送'用户进入'消息
        ChatMsg msgEnter = new ChatMsg();
        msgEnter.type = "enter";
        msgEnter.content = new ChatUser(this.id, this.user);
        ChatRoom.i.sendAll(msgEnter);
    }

    // 接收来自前台的json字符串 send()
    @OnMessage
    public void onMessage(String message) throws Exception {

        ChatMsg msg = JSON.parseObject(message, ChatMsg.class);
        System.out.println("前端传来的消息 : "+ msg);
        msg.time = getCurrentTime();
        msg.from = this.user; // 发送者：当前用户

        // RSA算法加密聊天内容
        msg.content = RSAUtils.encryptRSA(RSAUtils.getPublicKey("chat.pub"), (String) msg.content);
        // 直接对加密后的内容签名
        String signature = SignatureUtils.getSignature((String) msg.content, RSAUtils.getPrivateKey("chat.pri"));
        msg.signature = signature;
        log.info("onMessage:返回前端的聊天对象-->" + msg);
        if (msg.to == null || msg.to.equals("")) {
            ChatRoom.i.sendAll(msg);
        } else {
            sendMessage(msg);   // 给自己回显发送的信息
            sendMessageById(msg);   // 给私聊的那个人发送一条信息
        }
    }

    @OnClose
    public void onClose() {
        ChatRoom.i.removeClient(this);

        // 发送'用户退出'消息
        ChatMsg msg = new ChatMsg();
        msg.type = "leave";
        msg.content = new ChatUser(this.id, this.user);
        ChatRoom.i.sendAll(msg);
    }

    @OnError
    public void onError(Throwable error) {
        ChatRoom.i.removeClient(this);
        System.out.println("**用户连接出错**");
        error.printStackTrace();

        // 发送'用户退出'消息
        ChatMsg msg = new ChatMsg();
        msg.type = "leave";
        msg.content = new ChatUser(this.id, this.user);
        ChatRoom.i.sendAll(msg);
    }


    public void sendMessage(ChatMsg msg) {
        try {
            session.getBasicRemote().sendText(JSON.toJSONString(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageById(ChatMsg msg) {
        try {
            Session selected = ChatRoom.i.getSession(msg);
            selected.getBasicRemote().sendText(JSON.toJSONString(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date());
    }
}
