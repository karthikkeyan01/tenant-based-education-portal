//package com.fts.tenantbasededuportal.socket.listener;
//
//import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
//import com.fts.tenantbasededuportal.socket.event.UserCreatedEvent;
//import com.fts.tenantbasededuportal.socket.service.SocketEventDeliveryService;
//import com.fts.tenantbasededuportal.socket.service.SocketEventService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class UserCreatedEventListener {
//
//    private final SocketEventService socketEventService;
//    private final SocketEventDeliveryService socketEventDeliveryService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleUserCreatedEvent(final UserCreatedEvent event) {
//
//        final List<SocketEvent> socketEvents = this.socketEventService.createUserCreatedSocketEvents(event);
//        this.socketEventDeliveryService.deliverEvents(socketEvents);
//    }
//}
