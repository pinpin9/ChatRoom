package org.example.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.message.*;
import org.example.protocol.MessageCodecSharable;
import org.example.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        AtomicBoolean REGISTER = new AtomicBoolean(false);
        Object o1 = new Object();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                        // 接受服务器的响应消息
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg:{}", msg);
                            System.out.println(msg);
                            if ((msg instanceof LoginResponseMessage)) {
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if (response.isSuccess()) {
                                    //如果登陆成功
                                    LOGIN.set(true);
                                }
                                //唤醒system in线程
                                synchronized (o1) {
                                    o1.notify();
                                }
                            }
                            if ((msg instanceof RegisterResponseMessage)) {
                                RegisterResponseMessage response = (RegisterResponseMessage) msg;
                                if (response.isSuccess()) {
                                    REGISTER.set(true);
                                }
                                synchronized (o1) {
                                    o1.notify();
                                }
                            }
                        }
                        //在连接建立后触发active事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //负责接受用户在控制台的输入，负责向服务器发送各种消息
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                String userId = null;
                                String password = null;
                                String op = null;
                                do {
                                    synchronized (o1) {
                                        System.out.println("你需要登录还是注册？");
                                        op = scanner.nextLine();
                                        if (op.equals("register")) {
                                            System.out.println("请输入用户名:");
                                            userId = scanner.nextLine();
                                            System.out.println("请输入密码:");
                                            password = scanner.nextLine();
                                            RegisterRequestMessage message = new RegisterRequestMessage(userId, password);
                                            ctx.writeAndFlush(message);
                                            try {
                                                o1.wait();
                                                if (!REGISTER.get()) System.out.println("注册失败");
                                                REGISTER.set(false);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        if (op.equals("login")) {
                                            System.out.println("请输入用户名:");
                                            userId = scanner.nextLine();
                                            System.out.println("请输入密码:");
                                            password = scanner.nextLine();
                                            LoginRequestMessage message = new LoginRequestMessage(userId, password);
                                            ctx.writeAndFlush(message);
                                            try {
                                                o1.wait();
                                                if (!LOGIN.get()) System.out.println("登陆失败");
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                }while(!LOGIN.get());
                                while (true) {
                                    System.out.println("请输入您接下来要进行的操作");
                                    System.out.println("==================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    String command = scanner.nextLine();
                                    String s[] = command.split(" ");
                                    switch (s[0]) {
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(userId, s[1], s[2]));
                                            break;
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(userId, s[1], s[2]));
                                            break;
                                        case "gcreate":
                                            Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            set.add(userId); // 加入自己
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                            break;
                                        case "gmembers":
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(userId, s[1]));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(userId, s[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            }, "System in").start();
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
