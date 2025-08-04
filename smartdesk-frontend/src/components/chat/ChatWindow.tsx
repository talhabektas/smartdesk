import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuthStore } from '../../store/authStore';
import { webSocketService } from '../../services/websocket';
import { toast } from 'react-hot-toast';
import { ChatMessage, ChatMessageType, UserRole } from '../../types';
import ChatMessageList from './ChatMessageList';
import ChatInput from './ChatInput';
import ChatHeader from './ChatHeader';
import ChatTypingIndicator from './ChatTypingIndicator';

interface ChatWindowProps {
    ticketId: number;
    ticketNumber: string;
    ticketTitle: string;
    onClose?: () => void;
    isOpen?: boolean;
}

const ChatWindow: React.FC<ChatWindowProps> = ({
    ticketId,
    ticketNumber,
    ticketTitle,
    onClose,
    isOpen = true
}) => {
    const { user } = useAuthStore();
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isTyping, setIsTyping] = useState(false);
    const [typingUsers, setTypingUsers] = useState<string[]>([]);
    const [lastMessageId, setLastMessageId] = useState<number | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const typingTimeoutRef = useRef<NodeJS.Timeout>();

    // Scroll to bottom when new messages arrive
    const scrollToBottom = useCallback(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, []);

    // Load initial messages
    useEffect(() => {
        if (isOpen && ticketId) {
            loadMessages();
        }
    }, [ticketId, isOpen]);

    // Subscribe to WebSocket events
    useEffect(() => {
        if (!isOpen || !ticketId) return;

        // Subscribe to ticket chat messages
        const chatSubscription = webSocketService.subscribe(
            `/topic/ticket/${ticketId}/chat`,
            (message) => {
                const chatMessage = JSON.parse(message.body);
                handleNewMessage(chatMessage);
            }
        );

        // Subscribe to typing status
        const typingSubscription = webSocketService.subscribe(
            `/topic/ticket/${ticketId}/typing`,
            (message) => {
                const typingData = JSON.parse(message.body);
                handleTypingStatus(typingData);
            }
        );

        // Subscribe to read receipts
        const readReceiptSubscription = webSocketService.subscribe(
            `/topic/ticket/${ticketId}/read-receipt`,
            (message) => {
                const readData = JSON.parse(message.body);
                handleReadReceipt(readData);
            }
        );

        return () => {
            webSocketService.unsubscribe(chatSubscription);
            webSocketService.unsubscribe(typingSubscription);
            webSocketService.unsubscribe(readReceiptSubscription);
        };
    }, [ticketId, isOpen]);

    // Auto-scroll to bottom when messages change
    useEffect(() => {
        scrollToBottom();
    }, [messages, scrollToBottom]);

    const loadMessages = async () => {
        setIsLoading(true);
        try {
            const response = await fetch(`http://localhost:8067/api/v1/chat/tickets/${ticketId}/messages?page=0&size=50`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                setMessages(data.messages || []);

                // Mark messages as read
                if (data.messages && data.messages.length > 0) {
                    markAllMessagesAsRead();
                }
            } else {
                toast.error('Mesajlar yüklenirken hata oluştu');
            }
        } catch (error) {
            console.error('Failed to load messages:', error);
            toast.error('Mesajlar yüklenirken hata oluştu');
        } finally {
            setIsLoading(false);
        }
    };

    const handleNewMessage = (message: ChatMessage) => {
        console.log('💬 New message received:', message);
        console.log('💬 Current user ID:', user?.id);
        console.log('💬 Message sender ID:', message.senderId);
        console.log('💬 IDs equal?:', message.senderId === user?.id);

        setMessages(prev => {
            // Check if message already exists
            const exists = prev.some(m => m.id === message.id);
            if (exists) {
                console.log('💬 Message already exists, skipping:', message.id);
                return prev;
            }

            console.log('💬 Adding new message to state:', message.id);
            return [...prev, message];
        });

        // Mark as read if it's not from current user
        if (message.senderId !== user?.id) {
            markMessageAsRead(message.id);
        }

        // Update typing status
        if (message.senderId !== user?.id) {
            setIsTyping(false);
        }
    };

    const handleTypingStatus = (data: { userId: number; userEmail: string; isTyping: boolean }) => {
        if (data.userId === user?.id) return; // Don't show own typing

        if (data.isTyping) {
            setTypingUsers(prev => {
                if (!prev.includes(data.userEmail)) {
                    return [...prev, data.userEmail];
                }
                return prev;
            });
        } else {
            setTypingUsers(prev => prev.filter(email => email !== data.userEmail));
        }
    };

    const handleReadReceipt = (data: { messageId: number; readAt: string }) => {
        // Update message read status if needed
        setMessages(prev => prev.map(msg =>
            msg.id === data.messageId
                ? { ...msg, isRead: true, readAt: data.readAt }
                : msg
        ));
    };

    const sendMessage = async (content: string, messageType: ChatMessageType = ChatMessageType.TEXT) => {
        if (!content.trim()) return;

        try {
            const messageData = {
                ticketId: Number(ticketId), // Ensure it's a number
                content: content.trim(),
                messageType,
                isInternal: false,
            };

            console.log('💬 Sending message data:', messageData);

            // Send via WebSocket for real-time
            webSocketService.sendMessage(`/app/ticket/chat`, messageData);

            // Also send via REST API for persistence
            const response = await fetch(`http://localhost:8067/api/v1/chat/tickets/${ticketId}/messages`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(messageData),
            });

            console.log('💬 Message send response:', response.status);

            if (!response.ok) {
                toast.error('Mesaj gönderilirken hata oluştu');
            }
        } catch (error) {
            console.error('Failed to send message:', error);
            toast.error('Mesaj gönderilirken hata oluştu');
        }
    };

    const markMessageAsRead = async (messageId: number) => {
        try {
            await fetch(`http://localhost:8067/api/v1/chat/messages/${messageId}/read`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
            });
        } catch (error) {
            console.error('Failed to mark message as read:', error);
        }
    };

    const markAllMessagesAsRead = async () => {
        try {
            await fetch(`http://localhost:8067/api/v1/chat/tickets/${ticketId}/messages/read-all`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
            });
        } catch (error) {
            console.error('Failed to mark all messages as read:', error);
        }
    };

    const handleTyping = (isTyping: boolean) => {
        setIsTyping(isTyping);

        // Clear previous timeout
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }

        // Send typing status
        webSocketService.sendMessage(`/app/ticket/typing`, { ticketId: Number(ticketId), isTyping });

        // Auto-stop typing after 3 seconds
        if (isTyping) {
            typingTimeoutRef.current = setTimeout(() => {
                setIsTyping(false);
                webSocketService.sendMessage(`/app/ticket/typing`, { ticketId: Number(ticketId), isTyping: false });
            }, 3000);
        }
    };

    const handleFileUpload = async (file: File) => {
        try {
            // TODO: Implement file upload to server
            const formData = new FormData();
            formData.append('file', file);
            formData.append('ticketId', ticketId.toString());

            const response = await fetch('/api/v1/files/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
                body: formData,
            });

            if (response.ok) {
                const data = await response.json();

                // Send file message
                const messageData = {
                    ticketId,
                    content: `Dosya: ${file.name}`,
                    messageType: ChatMessageType.FILE,
                    fileUrl: data.fileUrl,
                    fileName: file.name,
                    fileSize: file.size,
                    fileType: file.type,
                };

                webSocketService.sendMessage(`/app/ticket/${ticketId}/chat`, messageData);
            } else {
                toast.error('Dosya yüklenirken hata oluştu');
            }
        } catch (error) {
            console.error('Failed to upload file:', error);
            toast.error('Dosya yüklenirken hata oluştu');
        }
    };

    if (!isOpen) return null;

    return (
        <div className="flex flex-col h-full bg-white border border-gray-200 rounded-lg shadow-sm">
            {/* Chat Header */}
            <ChatHeader
                title={`#${ticketNumber} - ${ticketTitle}`}
                subtitle={`${messages.length} mesaj`}
                onClose={onClose}
            />

            {/* Messages Area */}
            <div className="flex-1 overflow-hidden">
                <ChatMessageList
                    messages={messages}
                    currentUserId={user?.id || 0}
                />

                {/* Typing Indicator */}
                {typingUsers.length > 0 && (
                    <ChatTypingIndicator
                        isTyping={typingUsers.length > 0}
                        userName={typingUsers[0]}
                    />
                )}

                {/* Scroll to bottom anchor */}
                <div ref={messagesEndRef} />
            </div>

            {/* Chat Input */}
            <ChatInput
                onSendMessage={(message: string) => sendMessage(message, ChatMessageType.TEXT)}
                onTyping={handleTyping}
                disabled={!user}
                placeholder="Mesajınızı yazın..."
            />
        </div>
    );
};

export default ChatWindow; 