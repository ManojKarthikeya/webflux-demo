import React, { useState, useEffect, useRef } from 'react';
import {
    Box,
    TextField,
    Button,
    Paper,
    Typography,
    List,
    ListItem,
    ListItemText,
    Chip,
    Drawer,
    Divider,
    IconButton,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import PersonIcon from '@mui/icons-material/Person';
import GroupIcon from '@mui/icons-material/Group';
import { useChatWebSocket } from '../hooks/useChatWebSocket';
import { useQuery } from '@tanstack/react-query';
import { fetchChatHistory } from '../api/chat';

const ChatRoom: React.FC = () => {
    const roomId = 'general';
    const [inputMessage, setInputMessage] = useState('');
    const [userName, setUserName] = useState('');
    const [isJoined, setIsJoined] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const {
        isConnected,
        messages: liveMessages,
        activeUsers,
        userCount,
        sendMessage,
        joinRoom,
    } = useChatWebSocket(roomId);

    const { data: historyMessages = [] } = useQuery({
        queryKey: ['chatHistory', roomId],
        queryFn: () => fetchChatHistory(roomId),
        enabled: isJoined,
    });

    // Combine history and live messages, removing duplicates based on ID if necessary
    // For simplicity, we'll just concatenate, but in a real app we'd merge carefully
    // Actually, liveMessages start empty. History loads once.
    // We should probably initialize messages with history, but the hook manages messages state.
    // A better approach: The hook manages live messages. We display history + live.
    // But wait, if we fetch history, we might get messages that are also coming in live if we are not careful.
    // For this demo, let's just display history then live messages.
    
    const allMessages = [...historyMessages, ...liveMessages];

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [allMessages]);

    const handleJoin = (e: React.FormEvent) => {
        e.preventDefault();
        if (userName.trim()) {
            joinRoom(userName);
            setIsJoined(true);
        }
    };

    const handleSend = (e: React.FormEvent) => {
        e.preventDefault();
        if (inputMessage.trim()) {
            sendMessage(inputMessage);
            setInputMessage('');
        }
    };

    if (!isJoined) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: '80vh',
                }}
            >
                <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400 }}>
                    <Typography variant="h5" gutterBottom align="center">
                        Join Chat Room
                    </Typography>
                    <form onSubmit={handleJoin}>
                        <TextField
                            fullWidth
                            label="Username"
                            variant="outlined"
                            value={userName}
                            onChange={(e) => setUserName(e.target.value)}
                            margin="normal"
                            required
                        />
                        <Button
                            fullWidth
                            type="submit"
                            variant="contained"
                            color="primary"
                            size="large"
                            sx={{ mt: 2 }}
                        >
                            Join
                        </Button>
                    </form>
                </Paper>
            </Box>
        );
    }

    return (
        <Box sx={{ display: 'flex', height: 'calc(100vh - 100px)' }}>
            <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', mr: 2 }}>
                <Paper
                    elevation={3}
                    sx={{
                        flexGrow: 1,
                        mb: 2,
                        p: 2,
                        overflowY: 'auto',
                        display: 'flex',
                        flexDirection: 'column',
                    }}
                >
                    <List>
                        {allMessages.map((msg, index) => (
                            <ListItem
                                key={index}
                                sx={{
                                    flexDirection: 'column',
                                    alignItems: msg.userName === userName ? 'flex-end' : 'flex-start',
                                }}
                            >
                                <Box
                                    sx={{
                                        maxWidth: '70%',
                                        bgcolor: msg.userName === userName ? 'primary.light' : 'grey.200',
                                        color: msg.userName === userName ? 'primary.contrastText' : 'text.primary',
                                        borderRadius: 2,
                                        p: 1.5,
                                    }}
                                >
                                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold', fontSize: '0.75rem' }}>
                                        {msg.userName}
                                    </Typography>
                                    <Typography variant="body1">{msg.messageText}</Typography>
                                    <Typography variant="caption" sx={{ display: 'block', textAlign: 'right', mt: 0.5, opacity: 0.7 }}>
                                        {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString() : ''}
                                    </Typography>
                                </Box>
                            </ListItem>
                        ))}
                        <div ref={messagesEndRef} />
                    </List>
                </Paper>
                <Paper
                    component="form"
                    onSubmit={handleSend}
                    sx={{ p: '2px 4px', display: 'flex', alignItems: 'center' }}
                >
                    <TextField
                        sx={{ ml: 1, flex: 1 }}
                        placeholder="Type a message..."
                        variant="standard"
                        value={inputMessage}
                        onChange={(e) => setInputMessage(e.target.value)}
                        disabled={!isConnected}
                        InputProps={{
                            disableUnderline: true,
                        }}
                    />
                    <Divider sx={{ height: 28, m: 0.5 }} orientation="vertical" />
                    <IconButton color="primary" sx={{ p: '10px' }} type="submit" disabled={!isConnected || !inputMessage.trim()}>
                        <SendIcon />
                    </IconButton>
                </Paper>
                {!isConnected && (
                    <Typography variant="caption" color="error" align="center" sx={{ mt: 1 }}>
                        Disconnected. Reconnecting...
                    </Typography>
                )}
            </Box>
            <Paper elevation={3} sx={{ width: 250, display: { xs: 'none', md: 'block' } }}>
                <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider', display: 'flex', alignItems: 'center' }}>
                    <GroupIcon sx={{ mr: 1 }} />
                    <Typography variant="h6">
                        Online ({userCount})
                    </Typography>
                </Box>
                <List>
                    {activeUsers.map((user, index) => (
                        <ListItem key={index}>
                            <PersonIcon color="action" sx={{ mr: 1 }} />
                            <ListItemText primary={user} />
                            <Box
                                sx={{
                                    width: 8,
                                    height: 8,
                                    borderRadius: '50%',
                                    bgcolor: 'success.main',
                                }}
                            />
                        </ListItem>
                    ))}
                </List>
            </Paper>
        </Box>
    );
};

export default ChatRoom;
