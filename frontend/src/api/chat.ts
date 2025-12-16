import { apiClient } from './client';
import type { ChatMessage } from '../types';

export const fetchChatHistory = async (roomId: string): Promise<ChatMessage[]> => {
    const response = await apiClient.get<ChatMessage[]>(`/chat/${roomId}/history`);
    return response.data;
};
