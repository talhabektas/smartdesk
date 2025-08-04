import React from 'react';
import ChatMessageItem from './ChatMessageItem';
import { ChatMessage } from '../../types';

interface ChatMessageListProps {
    messages: ChatMessage[];
    currentUserId: number;
}

const ChatMessageList: React.FC<ChatMessageListProps> = ({ messages, currentUserId }) => {
    return (
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 ? (
                <div className="text-center text-gray-500 py-8">
                    <p>Henüz mesaj yok</p>
                    <p className="text-sm">İlk mesajı göndererek sohbeti başlatın</p>
                </div>
            ) : (
                messages.map((message) => (
                    <ChatMessageItem
                        key={message.id}
                        message={message}
                        isOwnMessage={message.senderId === currentUserId}
                    />
                ))
            )}
        </div>
    );
};

export default ChatMessageList; 