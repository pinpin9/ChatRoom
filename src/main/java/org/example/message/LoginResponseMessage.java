package org.example.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {


    private String userId;
    private String userHead;
    private String userNickName;
    public LoginResponseMessage(boolean success, String reason) {
        super(success, reason);

    }

    @Override
    public int getMessageType() {
        return LoginResponseMessage;
    }
}
