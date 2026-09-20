package com.opspilot.service;

import com.opspilot.entity.Device;
import com.opspilot.common.security.DeviceTokenHasher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DeviceAuthService {
    public boolean canWrite(Device device, String deviceToken) {
        return isAuthenticatedManager() || hasValidDeviceToken(device, deviceToken);
    }

    public boolean hasValidDeviceToken(Device device, String deviceToken) {
        return device != null
                && device.getDeviceToken() != null
                && !device.getDeviceToken().isBlank()
                && DeviceTokenHasher.matches(deviceToken, device.getDeviceToken());
    }

    public boolean isAuthenticatedManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
