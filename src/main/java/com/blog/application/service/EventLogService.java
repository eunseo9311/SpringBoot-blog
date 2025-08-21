package com.blog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogService.class);
    
    public void logSignupEvent(String email, String nickname, Long userId) {
        logger.info("EVENT_SIGNUP: email={}, nickname={}, userId={}", 
                   email, nickname, userId);
    }
    
    public void logLoginEvent(String email, boolean success, String reason) {
        if (success) {
            logger.info("EVENT_LOGIN_SUCCESS: email={}", email);
        } else {
            logger.warn("EVENT_LOGIN_FAILED: email={}, reason={}", email, reason);
        }
    }
    
    public void logLogoutEvent(String email) {
        logger.info("EVENT_LOGOUT: email={}", email);
    }
    
    public void logTokenRefreshEvent(String email, boolean success) {
        if (success) {
            logger.info("EVENT_TOKEN_REFRESH_SUCCESS: email={}", email);
        } else {
            logger.warn("EVENT_TOKEN_REFRESH_FAILED: email={}", email);
        }
    }
}