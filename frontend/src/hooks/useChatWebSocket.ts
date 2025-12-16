import { useEffect, useState, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ChatMessage, PresenceEvent } from '../types';

interface UseChatWebSocketReturn {
    isConnected: boolean;
    messages: ChatMessage[];
    activeUsers: string[];
    userCount: number;
    sendMessage: (text: string) => void;
    joinRoom: (userName: string) => void;
}

export const useChatWebSocket = (roomId: string): UseChatWebSocketReturn => {
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [activeUsers, setActiveUsers] = useState<string[]>([]);
    const [userCount, setUserCount] = useState(0);
    const clientRef = useRef<Client | null>(null);
    const [userName, setUserName] = useState<string | null>(null);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('Connected to WebSocket');
                setIsConnected(true);

                // Subscribe to chat messages
                client.subscribe(`/topic/chat/${roomId}`, (message: IMessage) => {
                    const chatMessage: ChatMessage = JSON.parse(message.body);
                    setMessages((prev) => [...prev, chatMessage]);
                });

                // Subscribe to presence updates
                client.subscribe(`/topic/presence/${roomId}`, (message: IMessage) => {
                    const presence: PresenceEvent = JSON.parse(message.body);
                    setActiveUsers(presence.activeUsers);
                    setUserCount(presence.userCount);
                });

                // If we have a username, join the room
                if (userName) {
                    client.publish({
                        destination: `/app/chat/join/${roomId}`,
                        body: JSON.stringify({ userName }),
                    });
                }
            },
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [roomId, userName]);

    const sendMessage = useCallback((text: string) => {
        if (clientRef.current && clientRef.current.connected && userName) {
            const message: ChatMessage = {
                roomId,
                userName,
                messageText: text,
            };
            clientRef.current.publish({
                destination: `/app/chat/${roomId}`,
                body: JSON.stringify(message),
            });
        }
    }, [roomId, userName]);

    const joinRoom = useCallback((name: string) => {
        setUserName(name);
    }, []);

    return {
        isConnected,
        messages,
        activeUsers,
        userCount,
        sendMessage,
        joinRoom,
    };
};
