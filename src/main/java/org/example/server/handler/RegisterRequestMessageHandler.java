package org.example.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.LoginRequestMessage;
import org.example.message.LoginResponseMessage;
import org.example.message.RegisterRequestMessage;
import org.example.message.RegisterResponseMessage;
import org.example.server.service.UserServiceFactory;

public class RegisterRequestMessageHandler extends SimpleChannelInboundHandler<RegisterRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RegisterRequestMessage msg) throws Exception {
        String userId = msg.getUserId();
        String password = msg.getPassword();
        boolean register = UserServiceFactory.getUserService().register(userId, password);
        RegisterResponseMessage message;
        if (register) {
            message = new RegisterResponseMessage(true, "注册成功");
        } else {
            message = new RegisterResponseMessage(false, "注册失败");
        }
        ctx.writeAndFlush(message);
    }
}
