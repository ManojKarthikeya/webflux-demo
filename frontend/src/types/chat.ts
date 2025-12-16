export interface ChatMessage {
    id?: number;
    roomId: string;
    userName: string;
    messageText: string;
    createdAt?: string;
}

export interface PresenceEvent {
    activeUsers: string[];
    userCount: number;
}
