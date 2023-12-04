package org.example.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.LoginRequestMessage;
import org.example.message.LoginResponseMessage;
import org.example.server.service.UserServiceFactory;
import org.example.server.session.SessionFactory;
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String userId = msg.getUserId();
        String password = msg.getPassword();
        boolean login = UserServiceFactory.getUserService().login(userId, password);
        LoginResponseMessage message;
        if (login) {
            SessionFactory.getSession().bind(ctx.channel(), userId);
            message = new LoginResponseMessage(true, "登陆成功");
        } else {
            message = new LoginResponseMessage(false, "用户名或密码错误");
        }
        ctx.writeAndFlush(message);
    }
}
