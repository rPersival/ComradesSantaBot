package org.rpersival.bot.interaction;

import discord4j.core.object.component.Button;

public class Buttons {

    public static final String registerId = "register";
    public static final String acceptId = "accept-rules";
    public static final String doNotAcceptId = "do-not-accept-rules";

    public static final Button registerButton = Button.success(registerId, "Register");
    public static final Button acceptRulesButton = Button.success(acceptId, "Yes");
    public static final Button doNotAcceptRulesButton = Button.danger(doNotAcceptId, "No");
}
