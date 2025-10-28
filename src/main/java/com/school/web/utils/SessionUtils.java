package com.school.web.utils;

import com.school.feature.users.entity.User;
import com.school.feature.users.entity.UserSession;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SessionUtils {

    public static User getUser(HttpSession session) {
        final UserSession userSession = (UserSession) session.getAttribute("userSession");
        return userSession.getUser();
    }
}
