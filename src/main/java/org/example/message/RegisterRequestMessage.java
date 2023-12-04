package org.example.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class RegisterRequestMessage extends Message{
    private String userId;
    private String password;


    public RegisterRequestMessage() {
    }

    public RegisterRequestMessage(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    @Override
    public int getMessageType() {
        return RegisterRequestMessage;
    }
}
