package com.livebus.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DriverController {

    @MessageMapping("/driver/update")
    @SendTo("/topic/route/101-A")
    public LocationUpdate updateLocation(LocationUpdate locationUpdate) {
        // Broadcasts the incoming LocationUpdate to all subscribers of /topic/route/101-A
        return locationUpdate;
    }
}
