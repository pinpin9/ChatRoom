package org.example.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class RegisterResponseMessage extends AbstractResponseMessage{
    public RegisterResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    @Override
    public int getMessageType() {
        return RegisterResponseMessage;
    }
}
